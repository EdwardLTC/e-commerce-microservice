package server

import (
	"context"
	"golang-order/gen/ent"
	"golang-order/internal/kafka"
	"golang-order/internal/outbox"
	"time"
)

func setupOutboxDispatcher(entClient *ent.Client, kafkaProducer *kafka.Producer) (cancelFunc context.CancelFunc) {
	outboxDispatcher := outbox.NewDispatcher(
		entClient,
		kafkaProducer,
		5*time.Second,
		100,
	)
	ctx, cancel := context.WithCancel(context.Background())

	go outboxDispatcher.Start(ctx)

	return cancel
}
