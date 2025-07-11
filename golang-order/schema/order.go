package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
	"github.com/google/uuid"
)

type Order struct {
	ent.Schema
}

type OrderStatus string

func OrderStatusValues() []string {
	return []string{
		string(Draft),
		string(PendingPayment),
		string(PaymentReceived),
		string(Preparing),
		string(Shipped),
		string(Delivered),
		string(Cancelled),
	}
}

const (
	Draft           OrderStatus = "draft"
	PendingPayment  OrderStatus = "pending_payment"
	PaymentReceived OrderStatus = "payment_received"
	Preparing       OrderStatus = "preparing"
	Shipped         OrderStatus = "shipped"
	Delivered       OrderStatus = "delivered"
	Cancelled       OrderStatus = "cancelled"
)

func (Order) Fields() []ent.Field {
	return []ent.Field{
		field.UUID("id", uuid.UUID{}).Default(uuid.New).Unique().Immutable(),
		field.UUID("customer_id", uuid.UUID{}), // Reference to user service
		field.Enum("status").Values(OrderStatusValues()...).Default(string(Draft)),
		field.Float("subtotal").Default(0).Min(0),
		field.Float("tax").Default(0).Min(0),
		field.Float("shipping_cost").Default(0).Min(0),
		field.Float("total").Default(0).Min(0),
		field.Text("shipping_address"),
		field.Text("billing_address"),
		field.UUID("payment_intent_id", uuid.UUID{}).Optional(), // Reference to payment service
	}
}

func (Order) Edges() []ent.Edge {
	return []ent.Edge{
		edge.To("items", OrderItem.Type),
		edge.To("refunds", Refund.Type),
	}
}

func (Order) Mixin() []ent.Mixin {
	return []ent.Mixin{
		TimestampMixin{},
	}
}

func (Order) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("customer_id"),
		index.Fields("status"),
		index.Fields("payment_intent_id"),
	}
}
