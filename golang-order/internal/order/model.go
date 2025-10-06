package order

import (
	"golang-order/ent/order"

	"github.com/google/uuid"
)

type UpdateOrder struct {
	status          *order.Status
	paymentIntentId *uuid.UUID
}

type CreateOrderInput struct {
	CustomerID      uuid.UUID
	Subtotal        float64
	Tax             float64
	ShippingCost    float64
	Total           float64
	ShippingAddress string
	BillingAddress  string
	Items           []ItemInput
}

type ItemInput struct {
	ProductID          uuid.UUID
	VariantID          uuid.UUID
	ProductName        string
	VariantDescription string
	UnitPrice          float64
	Quantity           int
	TotalPrice         float64
	ImageURL           string
}
