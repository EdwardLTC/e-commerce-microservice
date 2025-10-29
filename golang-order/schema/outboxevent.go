package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
	"github.com/google/uuid"
)

type OutboxEvent struct {
	ent.Schema
}

func (OutboxEvent) Fields() []ent.Field {
	return []ent.Field{
		field.UUID("id", uuid.UUID{}).Default(uuid.New).Unique().Immutable(),
		field.String("aggregate_type"),          // e.g. "order"
		field.UUID("aggregate_id", uuid.UUID{}), // e.g. Order ID
		field.String("event_type"),              // e.g. "order.created"
		field.Bytes("payload"),
		field.Bool("processed").Default(false),
	}
}

func (OutboxEvent) Mixin() []ent.Mixin {
	return []ent.Mixin{
		TimestampMixin{},
	}
}
