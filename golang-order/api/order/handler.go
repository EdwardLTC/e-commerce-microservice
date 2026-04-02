package order

import (
	"context"
	"fmt"
	"golang-order/gen/ent"
	"golang-order/gen/ent/order"
	pb "golang-order/gen/proto"
	"golang-order/grpcClient"
	orderinternal "golang-order/internal/order"

	"github.com/google/uuid"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type Handler struct {
	pb.UnimplementedOrderServiceServer
	db  *ent.Client
	svc orderinternal.Choreography
}

func NewHandler(db *ent.Client) *Handler {
	variantClient, err := grpcClient.VariantClient()
	if err != nil {
		fmt.Println(err)
	}

	return &Handler{db: db, svc: *orderinternal.NewOrderChoreography(db, variantClient)}
}

func (h *Handler) CreateOrder(ctx context.Context, req *pb.CreateOrderRequest) (*pb.CreateOrderResponse, error) {
	return h.svc.CreateOrder(ctx, req)
}

func (h *Handler) GetOrder(ctx context.Context, req *pb.GetOrderRequest) (*pb.Order, error) {
	orderID, err := uuid.Parse(req.Id)
	if err != nil {
		return nil, status.Errorf(codes.InvalidArgument, "invalid order ID")
	}

	entity, err := h.db.Order.Query().Where(order.IDEQ(orderID)).WithItems().Only(ctx)
	if err != nil {
		return nil, status.Errorf(codes.NotFound, "order not found")
	}

	return &pb.Order{
		Id:              entity.ID.String(),
		CustomerId:      entity.CustomerID.String(),
		Status:          entToProtoOrderStatus(entity.Status),
		Subtotal:        entity.Subtotal,
		Tax:             entity.Tax,
		ShippingCost:    entity.ShippingCost,
		Total:           entity.Total,
		ShippingAddress: entity.ShippingAddress,
		BillingAddress:  entity.BillingAddress,
		CreatedAt:       timestamppb.New(entity.CreatedAt),
		UpdatedAt:       timestamppb.New(entity.UpdatedAt),
		Items: func() []*pb.OrderItem {
			items := make([]*pb.OrderItem, 0, len(entity.Edges.Items))
			for _, item := range entity.Edges.Items {
				items = append(items, &pb.OrderItem{
					Id:                 item.ID.String(),
					ProductId:          item.ProductID.String(),
					VariantId:          item.VariantID.String(),
					ProductName:        item.ProductName,
					VariantDescription: item.VariantDescription,
					UnitPrice:          item.UnitPrice,
					SalePrice:          item.UnitPrice,
					Quantity:           int32(item.Quantity),
					TotalPrice:         item.TotalPrice,
					ImageUrl:           item.ImageURL,
					CreatedAt:          timestamppb.New(item.CreatedAt),
					UpdatedAt:          timestamppb.New(item.UpdatedAt),
				})
			}
			return items
		}(),
	}, nil
}

func entToProtoOrderStatus(status order.Status) pb.OrderStatus {
	switch status {
	case order.StatusCreated:
		return pb.OrderStatus_CREATED
	case order.StatusPendingInventory:
		return pb.OrderStatus_PENDING_INVENTORY
	case order.StatusInventoryReserved:
		return pb.OrderStatus_INVENTORY_RESERVED
	case order.StatusInventoryReservedFailed:
		return pb.OrderStatus_INVENTORY_RESERVED_FAILED
	case order.StatusPaymentPending:
		return pb.OrderStatus_PAYMENT_PENDING
	case order.StatusPaymentCompleted:
		return pb.OrderStatus_PAYMENT_COMPLETED
	case order.StatusPaymentFailed:
		return pb.OrderStatus_PAYMENT_FAILED
	case order.StatusShipping:
		return pb.OrderStatus_SHIPPING
	case order.StatusCompleted:
		return pb.OrderStatus_COMPLETED
	default:
		return pb.OrderStatus_CREATED
	}
}
