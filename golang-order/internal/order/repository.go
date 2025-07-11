package order

import (
	"context"
	"github.com/google/uuid"
	"golang-order/ent"
	"golang-order/ent/order"
	"log"
)

type Repository interface {
	CreateDraftOrder(ctx context.Context, input CreateOrderInput) (*ent.Order, error)
	UpdateOrder(ctx context.Context, id uuid.UUID, updateBody UpdateOrder) (*ent.Order, error)
}

type orderRepository struct {
	client *ent.Client
}

func NewOrderRepository(client *ent.Client) Repository {
	return &orderRepository{client: client}
}

func (r *orderRepository) CreateDraftOrder(ctx context.Context, input CreateOrderInput) (*ent.Order, error) {
	tx, err := r.client.Tx(ctx)
	if err != nil {
		return nil, err
	}
	defer func(tx *ent.Tx) {
		err := tx.Rollback()
		if err != nil {
			log.Printf("Failed to rollback transaction: %v", err)
		}
	}(tx)

	// Create main order
	orderCreated, err := tx.Order.Create().
		SetCustomerID(input.CustomerID).
		SetStatus(order.StatusDraft).
		SetSubtotal(input.Subtotal).
		SetTax(input.Tax).
		SetShippingCost(input.ShippingCost).
		SetTotal(input.Total).
		SetShippingAddress(input.ShippingAddress).
		SetBillingAddress(input.BillingAddress).
		Save(ctx)

	if err != nil {
		return nil, err
	}

	// Create order items
	for _, item := range input.Items {
		_, err := tx.OrderItem.Create().
			SetOrderID(orderCreated.ID).
			SetProductID(item.ProductID).
			SetVariantID(item.VariantID).
			SetProductName(item.ProductName).
			SetVariantDescription(item.VariantDescription).
			SetUnitPrice(item.UnitPrice).
			SetQuantity(item.Quantity).
			SetTotalPrice(item.TotalPrice).
			SetImageURL(item.ImageURL).
			Save(ctx)
		if err != nil {
			return nil, err
		}
	}

	if err := tx.Commit(); err != nil {
		return nil, err
	}

	return orderCreated, nil
}

func (r *orderRepository) UpdateOrder(ctx context.Context, id uuid.UUID, updateBody UpdateOrder) (*ent.Order, error) {
	orderUpdated, err := r.client.Order.UpdateOneID(id).
		SetNillableStatus(updateBody.status).
		SetNillablePaymentIntentID(updateBody.paymentIntentId).
		Save(ctx)

	if err != nil {
		return nil, err
	}

	return orderUpdated, nil
}
