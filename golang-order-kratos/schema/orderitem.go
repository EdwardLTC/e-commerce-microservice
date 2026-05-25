package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"github.com/google/uuid"
)

type OrderItem struct {
	ent.Schema
}

func (OrderItem) Fields() []ent.Field {
	return []ent.Field{
		field.UUID("id", uuid.UUID{}).Default(uuid.New).Unique().Immutable(),
		field.UUID("product_id", uuid.UUID{}),          // Snapshot of product ID
		field.UUID("variant_id", uuid.UUID{}),          // Snapshot of variant ID
		field.String("product_name").NotEmpty(),        // Snapshot at purchase time
		field.String("variant_description").Optional(), // e.g. "Color: Red, Size: M"
		field.Float("unit_price").Min(0),               // Price per unit at purchase
		field.Int("quantity").Positive(),
		field.Float("total_price").Min(0),    // Calculated: unit_price * quantity
		field.String("image_url").Optional(), // Snapshot of primary image
	}
}

func (OrderItem) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("order", Order.Type).Ref("items").Unique().Required(),
	}
}

func (OrderItem) Mixin() []ent.Mixin {
	return []ent.Mixin{
		TimestampMixin{},
	}
}
