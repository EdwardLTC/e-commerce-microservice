package outbox

import (
	"context"
	"golang-order/internal/kafka"
	"log"
	"time"

	"golang-order/gen/ent"
	"golang-order/gen/ent/outboxevent"
)

type Producer interface {
	Publish(topic, key string, value interface{}) error
}

type Dispatcher struct {
	db       *ent.Client
	producer *kafka.Producer
	interval time.Duration
	batch    int
}

func NewDispatcher(db *ent.Client, producer *kafka.Producer, interval time.Duration, batch int) *Dispatcher {
	return &Dispatcher{
		db:       db,
		producer: producer,
		interval: interval,
		batch:    batch,
	}
}

// Start runs the outbox worker periodically until ctx is cancelled
func (d *Dispatcher) Start(ctx context.Context) {
	ticker := time.NewTicker(d.interval)
	defer ticker.Stop()

	log.Printf("[Outbox] Dispatcher started (interval=%v)", d.interval)

	for {
		select {
		case <-ticker.C:
			if err := d.processPendingEvents(ctx); err != nil {
				log.Printf("[Outbox] Error: %v", err)
			}
		case <-ctx.Done():
			log.Println("[Outbox] Dispatcher stopped")
			return
		}
	}
}

func (d *Dispatcher) processPendingEvents(ctx context.Context) error {
	events, err := d.db.OutboxEvent.
		Query().
		Where(outboxevent.ProcessedEQ(false)).
		Order(ent.Asc(outboxevent.FieldCreatedAt)).
		Limit(d.batch).
		All(ctx)
	if err != nil {
		return err
	}

	if len(events) == 0 {
		return nil // nothing to process
	}

	for _, evt := range events {
		// publish to Kafka (topic = evt.EventType)
		err := d.producer.Publish(evt.EventType, evt.AggregateID.String(), evt.Payload)

		if err != nil {
			log.Printf("[Outbox] Failed to publish %s: %v", evt.ID, err)
			continue // leave as unprocessed to retry later
		}

		// mark as processed
		_, err = d.db.OutboxEvent.UpdateOneID(evt.ID).
			SetProcessed(true).
			Save(ctx)
		if err != nil {
			log.Printf("[Outbox] Failed to mark processed %s: %v", evt.ID, err)
		} else {
			log.Printf("[Outbox] Successfully processed event %s (%s)", evt.ID, evt.EventType)
		}
	}

	return nil
}
