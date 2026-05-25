package worker

import (
	"context"
	"time"

	"golang-order-kratos/internal/biz"
	"golang-order-kratos/internal/data"

	"github.com/go-kratos/kratos/v2/log"
)

type OutboxDispatcher struct {
	repo     biz.OrderRepo
	producer *data.KafkaProducer
	interval time.Duration
	batch    int
	log      *log.Helper
}

func NewOutboxDispatcher(repo biz.OrderRepo, producer *data.KafkaProducer, interval time.Duration, batch int, logger log.Logger) *OutboxDispatcher {
	return &OutboxDispatcher{
		repo:     repo,
		producer: producer,
		interval: interval,
		batch:    batch,
		log:      log.NewHelper(logger),
	}
}

func (d *OutboxDispatcher) Start(ctx context.Context) {
	ticker := time.NewTicker(d.interval)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			d.flush(ctx)
		}
	}
}

func (d *OutboxDispatcher) flush(ctx context.Context) {
	events, err := d.repo.ListPendingOutbox(ctx, d.batch)
	if err != nil {
		d.log.Errorf("failed to query outbox events: %v", err)
		return
	}

	for _, event := range events {
		if err := d.producer.Publish(event.EventType, event.AggregateID.String(), event.Payload); err != nil {
			d.log.Errorf("failed to publish event %s: %v", event.ID, err)
			continue
		}

		if err := d.repo.MarkOutboxProcessed(ctx, event.ID); err != nil {
			d.log.Errorf("failed to mark outbox event %s as processed: %v", event.ID, err)
		}
	}
}
