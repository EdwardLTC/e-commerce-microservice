package biz

import (
	"context"
	"fmt"
	"golang-order-kratos/gen/ent"
	entorder "golang-order-kratos/gen/ent/order"
	pb "golang-order-kratos/gen/proto"
	"time"

	"golang-order-kratos/gen/avro"

	"github.com/go-kratos/kratos/v2/log"
	"github.com/google/uuid"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type VariantInfo struct {
	ID                 string
	ProductID          string
	ProductName        string
	VariantDescription string
	ImageURL           string
	SalePrice          float64
}

type VariantCatalog interface {
	GetVariantsByIDs(ctx context.Context, ids []string) ([]VariantInfo, error)
}

type OrderRepo interface {
	Create(ctx context.Context, command CreateOrderCommand) (*ent.Order, error)
	Get(ctx context.Context, id uuid.UUID) (*ent.Order, error)
	List(ctx context.Context, query ListOrdersQuery) ([]*ent.Order, int, error)
	UpdateStatus(ctx context.Context, orderID uuid.UUID, status entorder.Status, errorMessage *string, expectedCurrent entorder.Status) error
	ListPendingOutbox(ctx context.Context, batch int) ([]*ent.OutboxEvent, error)
	MarkOutboxProcessed(ctx context.Context, eventID uuid.UUID) error
}

type CreateOrderCommand struct {
	CustomerID       uuid.UUID
	ShippingAddress  string
	BillingAddress   string
	Items            []CreateOrderItem
	EventItems       []avro.Item
	ComputedSubtotal float64
	ComputedTax      float64
	ComputedTotal    float64
}

type CreateOrderItem struct {
	ProductID          uuid.UUID
	VariantID          uuid.UUID
	ProductName        string
	VariantDescription string
	UnitPrice          float64
	Quantity           int
	TotalPrice         float64
	ImageURL           string
}

type ListOrdersQuery struct {
	Page         int
	PageSize     int
	CustomerID   *uuid.UUID
	Status       *entorder.Status
	CreatedAfter *time.Time
}

type OrderUsecase struct {
	repo    OrderRepo
	catalog VariantCatalog
	log     *log.Helper
}

func NewOrderUsecase(repo OrderRepo, catalog VariantCatalog, logger log.Logger) *OrderUsecase {
	return &OrderUsecase{
		repo:    repo,
		catalog: catalog,
		log:     log.NewHelper(logger),
	}
}

func (uc *OrderUsecase) CreateOrder(ctx context.Context, req *pb.CreateOrderRequest) (*pb.CreateOrderResponse, error) {
	customerID, err := uuid.Parse(req.CustomerId)
	if err != nil {
		return nil, status.Error(codes.InvalidArgument, "invalid customer ID")
	}

	if len(req.Items) == 0 {
		return nil, status.Error(codes.InvalidArgument, "order must contain at least one item")
	}

	variantIDs := make([]string, 0, len(req.Items))
	for _, item := range req.Items {
		variantIDs = append(variantIDs, item.VariantId)
	}

	variants, err := uc.catalog.GetVariantsByIDs(ctx, variantIDs)
	if err != nil {
		return nil, err
	}

	variantMap := make(map[string]VariantInfo, len(variants))
	for _, variant := range variants {
		variantMap[variant.ID] = variant
	}

	command := CreateOrderCommand{
		CustomerID:      customerID,
		ShippingAddress: req.ShippingAddress,
		BillingAddress:  req.BillingAddress,
		Items:           make([]CreateOrderItem, 0, len(req.Items)),
	}

	eventItems := make([]avro.Item, 0, len(req.Items))
	for _, item := range req.Items {
		variant, ok := variantMap[item.VariantId]
		if !ok {
			return nil, status.Errorf(codes.InvalidArgument, "variant ID %s not found", item.VariantId)
		}

		if variant.SalePrice != item.UnitPrice {
			return nil, status.Errorf(codes.InvalidArgument, "price mismatch for variant ID %s", item.VariantId)
		}

		productID, err := uuid.Parse(variant.ProductID)
		if err != nil {
			return nil, status.Errorf(codes.Internal, "invalid product ID from product service for variant %s", item.VariantId)
		}

		variantID, err := uuid.Parse(variant.ID)
		if err != nil {
			return nil, status.Errorf(codes.Internal, "invalid variant ID from product service for variant %s", item.VariantId)
		}

		lineTotal := float64(item.Quantity) * variant.SalePrice
		command.ComputedSubtotal += lineTotal
		command.Items = append(command.Items, CreateOrderItem{
			ProductID:          productID,
			VariantID:          variantID,
			ProductName:        variant.ProductName,
			VariantDescription: variant.VariantDescription,
			UnitPrice:          variant.SalePrice,
			Quantity:           int(item.Quantity),
			TotalPrice:         lineTotal,
			ImageURL:           variant.ImageURL,
		})
		eventItems = append(eventItems, avro.Item{
			Variant_id: item.VariantId,
			Quantity:   item.Quantity,
			Unit_price: variant.SalePrice,
		})
	}

	command.ComputedTax = command.ComputedSubtotal * 0.1
	command.ComputedTotal = command.ComputedSubtotal + command.ComputedTax

	command.EventItems = eventItems

	entity, err := uc.repo.Create(ctx, command)
	if err != nil {
		return nil, status.Errorf(codes.Internal, "failed to create order: %v", err)
	}

	return &pb.CreateOrderResponse{
		Id:     entity.ID.String(),
		Status: toProtoOrderStatus(entity.Status),
	}, nil
}

func (uc *OrderUsecase) GetOrder(ctx context.Context, req *pb.GetOrderRequest) (*pb.Order, error) {
	orderID, err := uuid.Parse(req.Id)
	if err != nil {
		return nil, status.Error(codes.InvalidArgument, "invalid order ID")
	}

	entity, err := uc.repo.Get(ctx, orderID)
	if err != nil {
		return nil, status.Error(codes.NotFound, "order not found")
	}

	return ToProtoOrder(entity), nil
}

func (uc *OrderUsecase) ListOrders(ctx context.Context, req *pb.ListOrdersRequest) (*pb.ListOrdersResponse, error) {
	query := ListOrdersQuery{
		Page:     normalizedPage(req.Page),
		PageSize: normalizedPageSize(req.PageSize),
	}

	if req.CustomerId != "" {
		customerID, err := uuid.Parse(req.CustomerId)
		if err != nil {
			return nil, status.Error(codes.InvalidArgument, "invalid customer ID")
		}
		query.CustomerID = &customerID
	}

	if req.Status != pb.OrderStatus_CREATED {
		statusValue := fromProtoOrderStatus(req.Status)
		query.Status = &statusValue
	}

	if req.CreatedAfter != nil {
		createdAfter := req.CreatedAfter.AsTime()
		query.CreatedAfter = &createdAfter
	}

	orders, totalCount, err := uc.repo.List(ctx, query)
	if err != nil {
		return nil, status.Errorf(codes.Internal, "failed to list orders: %v", err)
	}

	response := &pb.ListOrdersResponse{
		Orders:      make([]*pb.Order, 0, len(orders)),
		TotalCount:  int32(totalCount),
		CurrentPage: int32(query.Page),
		TotalPages:  int32(totalPages(totalCount, query.PageSize)),
	}
	for _, entity := range orders {
		response.Orders = append(response.Orders, ToProtoOrder(entity))
	}

	return response, nil
}

func (uc *OrderUsecase) HandleStockEvent(ctx context.Context, orderID string, isSuccess bool, message string) error {
	parsedID, err := uuid.Parse(orderID)
	if err != nil {
		return fmt.Errorf("invalid order id in stock event: %w", err)
	}

	statusValue := entorder.StatusInventoryReservedFailed
	if isSuccess {
		statusValue = entorder.StatusInventoryReserved
	}

	var errorMessage *string
	if message != "" {
		errorMessage = &message
	}

	return uc.repo.UpdateStatus(ctx, parsedID, statusValue, errorMessage, entorder.StatusCreated)
}

func (uc *OrderUsecase) HandlePaymentEvent(ctx context.Context, orderID string, isSuccess bool, message string) error {
	parsedID, err := uuid.Parse(orderID)
	if err != nil {
		return fmt.Errorf("invalid order id in payment event: %w", err)
	}

	statusValue := entorder.StatusPaymentFailed
	if isSuccess {
		statusValue = entorder.StatusPaymentCompleted
	}

	var errorMessage *string
	if message != "" {
		errorMessage = &message
	}

	return uc.repo.UpdateStatus(ctx, parsedID, statusValue, errorMessage, entorder.StatusInventoryReserved)
}

func normalizedPage(page int32) int {
	if page <= 0 {
		return 1
	}
	return int(page)
}

func normalizedPageSize(pageSize int32) int {
	if pageSize <= 0 {
		return 20
	}
	if pageSize > 100 {
		return 100
	}
	return int(pageSize)
}

func totalPages(totalCount int, pageSize int) int {
	if totalCount == 0 {
		return 0
	}
	return (totalCount + pageSize - 1) / pageSize
}
