package order

import (
	"bytes"
	"context"
	"golang-order/gen/avro"
	"golang-order/gen/ent"
	"golang-order/gen/ent/order"
	pb "golang-order/gen/proto"
	"golang-order/utility"

	"github.com/google/uuid"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type Choreography struct {
	db            *ent.Client
	variantClient pb.VariantServiceClient
}

func NewOrderChoreography(db *ent.Client, variantClient pb.VariantServiceClient) *Choreography {
	return &Choreography{
		db:            db,
		variantClient: variantClient,
	}
}

func (o *Choreography) CreateOrder(ctx context.Context, req *pb.CreateOrderRequest) (*pb.CreateOrderResponse, error) {

	response, err := o.variantClient.GetVariantsByIds(ctx, &pb.GetVariantByIdsRequest{
		Ids: utility.Map(req.Items, func(item *pb.OrderItemRequest) string {
			return item.VariantId
		}),
	})

	if err != nil {
		return nil, err
	}

	for _, it := range req.Items {
		var variant = utility.Find(response.Variants, func(variant *pb.GetVariantsResponse_Variant) bool {
			return variant.Id == it.VariantId
		})

		if variant == nil {
			return nil, status.Errorf(codes.InvalidArgument, "variant ID %s not found", it.VariantId)
		}

		if (*variant).Price != it.UnitPrice {
			return nil, status.Errorf(codes.InvalidArgument, "price mismatch for variant ID %s", it.VariantId)
		}
	}

	tx, err := o.db.Tx(ctx)
	if err != nil {
		return nil, err
	}

	defer func(tx *ent.Tx) {
		err := tx.Rollback()
		if err != nil {
			return
		}
	}(tx)

	subTotal := 0.0
	utility.ForEach(req.Items, func(item *pb.OrderItemRequest) {
		subTotal += float64(item.Quantity) * item.UnitPrice
	})
	tax := subTotal * 0.1
	total := tax + subTotal

	orderCreated, err := tx.Order.Create().
		SetCustomerID(uuid.MustParse(req.CustomerId)).
		SetStatus(order.StatusCreated).
		SetSubtotal(subTotal).
		SetTax(tax).
		SetShippingCost(0).
		SetTotal(total).
		SetShippingAddress(req.ShippingAddress).
		SetBillingAddress(req.BillingAddress).
		Save(ctx)

	if err != nil {
		return nil, err
	}

	var itemCreates = utility.Map(response.Variants, func(variant *pb.GetVariantsResponse_Variant) *ent.OrderItemCreate {
		itemReq := utility.Find(req.Items, func(element *pb.OrderItemRequest) bool {
			return variant.Id == element.VariantId
		})

		totalPrice := float64((*itemReq).Quantity) * variant.SalePrice

		return tx.OrderItem.Create().
			SetOrder(orderCreated).
			SetProductID(uuid.MustParse(variant.ProductId)).
			SetVariantID(uuid.MustParse(variant.Id)).
			SetProductName(variant.ProductName).
			SetVariantDescription(variant.Sku).
			SetUnitPrice(variant.SalePrice).
			SetQuantity(int((*itemReq).Quantity)).
			SetTotalPrice(totalPrice).
			SetImageURL(variant.MediaUrl)
	})

	_, err = tx.OrderItem.CreateBulk(itemCreates...).Save(ctx)

	if err != nil {
		return nil, err
	}

	event := avro.OrderCreatedEvent{
		Order_id:        orderCreated.ID.String(),
		Status:          orderCreated.Status.String(),
		Temporary_price: total,
		Customer_id:     orderCreated.CustomerID.String(),
		Items: utility.Map(req.Items, func(it *pb.OrderItemRequest) avro.Item {
			return avro.Item{
				Variant_id: it.VariantId,
				Quantity:   it.Quantity,
				Unit_price: it.UnitPrice,
			}
		}),
	}

	buf := new(bytes.Buffer)
	err = event.Serialize(buf)

	if err != nil {
		return nil, err
	}

	_, err = tx.OutboxEvent.Create().
		SetAggregateType("order").
		SetAggregateID(orderCreated.ID).
		SetEventType("order.created").
		SetPayload(buf.Bytes()).
		Save(ctx)

	if err != nil {
		return nil, err
	}

	if err := tx.Commit(); err != nil {
		return nil, err
	}

	return &pb.CreateOrderResponse{
		Id:     orderCreated.ID.String(),
		Status: entToProtoOrderStatus(orderCreated.Status),
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
