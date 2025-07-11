package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"github.com/google/uuid"
)

// Refund handles returns/refunds
type Refund struct {
	ent.Schema
}

func (Refund) Fields() []ent.Field {
	return []ent.Field{
		field.UUID("id", uuid.UUID{}).Default(uuid.New).Unique().Immutable(),
		field.Enum("status").Values("requested", "approved", "rejected", "processed"),
		field.Float("amount").Min(0),
		field.Text("reason"),
		field.UUID("payment_refund_id", uuid.UUID{}).Optional(), // Payment service reference
	}
}

func (Refund) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("order", Order.Type).Ref("refunds").Unique(),
		edge.To("items", OrderItem.Type),
	}
}

func (Refund) Mixin() []ent.Mixin {
	return []ent.Mixin{
		TimestampMixin{},
	}
}
