package order

import (
	"context"
	"github.com/google/uuid"
	"golang-order/ent"
	"golang-order/ent/order"
	pb "golang-order/gen"
	"golang-order/utility/array"
	"golang-order/utility/ptr"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/wrapperspb"
)

type Orchestrator struct {
	db            *ent.Client
	repository    Repository
	variantClient pb.VariantServiceClient
	paymentClient pb.PaymentServiceClient
}

func NewOrderOrchestrator(
	db *ent.Client,
	variantClient pb.VariantServiceClient,
	paymentClient pb.PaymentServiceClient,
) *Orchestrator {
	return &Orchestrator{
		db:            db,
		repository:    NewOrderRepository(db),
		variantClient: variantClient,
		paymentClient: paymentClient,
	}
}

func (o *Orchestrator) CreateOrder(ctx context.Context, req *pb.CreateOrderRequest) (*pb.CreateOrderResponse, error) {
	reservation, err := o.reserveStock(ctx, req.Items) // Step 1: Reserve stock to avoid over selling

	if err != nil {
		return nil, err
	}

	var subTotal float64
	var tax float64
	var shippingCost float64

	items := make([]ItemInput, len(reservation.Variants))

	for _, item := range reservation.Variants {
		var itemRequest = array.Find(req.Items, func(element *pb.OrderItemRequest) bool {
			return item.Id == element.VariantId
		})

		if itemRequest == nil {
			return nil, status.Error(codes.InvalidArgument, "Item not found in request")
		}

		items = append(items, ItemInput{
			ProductID:          uuid.MustParse(item.Product.Id),
			VariantID:          uuid.MustParse(item.Id),
			ProductName:        item.Product.Name,
			VariantDescription: item.Sku,
			UnitPrice:          item.SalePrice,
			Quantity:           int((*itemRequest).Quantity),
			TotalPrice:         float64((*itemRequest).Quantity) * item.SalePrice,
			ImageURL:           item.MediaUrl,
		})

		totalPrice := float64((*itemRequest).Quantity) * item.SalePrice
		subTotal += totalPrice
	}

	total := subTotal + tax + shippingCost

	orderReturned, err := o.repository.CreateDraftOrder(ctx, CreateOrderInput{
		CustomerID:      uuid.MustParse(req.CustomerId),
		Subtotal:        subTotal,
		Tax:             tax,
		ShippingCost:    shippingCost,
		Total:           total,
		ShippingAddress: req.ShippingAddress,
		BillingAddress:  req.BillingAddress,
		Items:           items,
	}) // Step 2: Create order in database

	if err != nil {
		o.releaseStock(ctx, reservation.ReservationId)
		return nil, err
	}

	paymentIntent, err := o.createPaymentIntent(ctx, orderReturned.Total, orderReturned.ID.String()) // Step 3: Create payment intent

	if err != nil {
		o.releaseStock(ctx, reservation.ReservationId)
		return nil, err
	}

	orderReturned, err = o.repository.UpdateOrder(ctx, orderReturned.ID, UpdateOrder{
		status:          ptr.Ptr(order.StatusPendingPayment),
		paymentIntentId: ptr.Ptr(uuid.MustParse(paymentIntent.IntentId)),
	}) // Step 4: Update order status to pending payment

	if err != nil {
		o.releaseStock(ctx, reservation.ReservationId)
		return nil, err
	}

	return &pb.CreateOrderResponse{
		Id:         orderReturned.ID.String(),
		Status:     entToProtoOrderStatus(orderReturned.Status),
		PaymentUrl: paymentIntent.PaymentUrl,
	}, nil
}

func (o *Orchestrator) reserveStock(ctx context.Context, items []*pb.OrderItemRequest) (*pb.ReserveStockResponse, error) {
	reservation, err := o.variantClient.ReserveStock(ctx, &pb.ReserveStockRequest{
		Items: array.Map(items, func(item *pb.OrderItemRequest) *pb.VariantItem {
			return &pb.VariantItem{
				VariantId: item.VariantId,
				Quantity:  item.Quantity,
			}
		}),
	})

	if err != nil {
		return nil, status.Errorf(codes.ResourceExhausted, "stock reservation failed: %v", err)
	}

	if len(items) != len(reservation.Variants) {
		return nil, status.Errorf(codes.InvalidArgument, "not all variants were reserved")
	}

	return reservation, nil
}

func (o *Orchestrator) releaseStock(ctx context.Context, reservationID string) {
	_, err := o.variantClient.ReleaseStock(ctx, &pb.ReleaseStockRequest{ReservationId: reservationID})
	if err != nil {
		print("failed to release stock: ", err)
	}
}

func (o *Orchestrator) createPaymentIntent(ctx context.Context, amount float64, orderId string) (*pb.CreateIntentResponse, error) {
	paymentIntent, err := o.paymentClient.CreateIntent(ctx, &pb.CreateIntentRequest{
		OrderId:       wrapperspb.String(orderId),
		Amount:        wrapperspb.Double(amount),
		Currency:      wrapperspb.String("VND"),
		PaymentMethod: wrapperspb.String("card"),
		Description:   wrapperspb.String("Order payment"),
	})

	if err != nil {
		return nil, status.Errorf(codes.FailedPrecondition, "payment intent failed: %v", err)
	}
	return paymentIntent, nil
}

func entToProtoOrderStatus(status order.Status) pb.OrderStatus {
	switch status {
	case order.StatusDraft:
		return pb.OrderStatus_ORDER_STATUS_DRAFT_UNSPECIFIED
	case order.StatusPendingPayment:
		return pb.OrderStatus_ORDER_STATUS_PENDING_PAYMENT
	case order.StatusPaymentReceived:
		return pb.OrderStatus_ORDER_STATUS_PAYMENT_RECEIVED
	case order.StatusShipped:
		return pb.OrderStatus_ORDER_STATUS_SHIPPED
	case order.StatusDelivered:
		return pb.OrderStatus_ORDER_STATUS_DELIVERED
	case order.StatusCancelled:
		return pb.OrderStatus_ORDER_STATUS_CANCELLED
	default:
		return pb.OrderStatus_ORDER_STATUS_DRAFT_UNSPECIFIED
	}
}
