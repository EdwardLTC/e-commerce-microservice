package order

import (
	"golang-order/gen/ent"
)

type ConsumerHandler struct {
	db *ent.Client
}

func NewConsumerHandler(db *ent.Client) *ConsumerHandler {
	return &ConsumerHandler{
		db: db,
	}
}
