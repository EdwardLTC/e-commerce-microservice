package worker

import (
	"bytes"
	"context"
	"time"

	"golang-order-kratos/gen/avro"
	"golang-order-kratos/internal/biz"

	"github.com/confluentinc/confluent-kafka-go/kafka"
	"github.com/go-kratos/kratos/v2/log"
)

type OrderEventConsumer struct {
	consumer *kafka.Consumer
	topics   []string
	usecase  *biz.OrderUsecase
	log      *log.Helper
}

func NewOrderEventConsumer(brokers, groupID string, topics []string, usecase *biz.OrderUsecase, logger log.Logger) (*OrderEventConsumer, error) {
	consumer, err := kafka.NewConsumer(&kafka.ConfigMap{
		"bootstrap.servers":  brokers,
		"group.id":           groupID,
		"enable.auto.commit": false,
		"auto.offset.reset":  "earliest",
		"session.timeout.ms": 10000,
	})
	if err != nil {
		return nil, err
	}

	return &OrderEventConsumer{
		consumer: consumer,
		topics:   topics,
		usecase:  usecase,
		log:      log.NewHelper(logger),
	}, nil
}

func (c *OrderEventConsumer) Start(ctx context.Context) {
	if err := c.consumer.SubscribeTopics(c.topics, nil); err != nil {
		c.log.Errorf("failed to subscribe topics: %v", err)
		return
	}

	for {
		select {
		case <-ctx.Done():
			return
		default:
			event := c.consumer.Poll(1000)
			if event == nil {
				continue
			}

			switch message := event.(type) {
			case *kafka.Message:
				c.handleMessage(ctx, message)
			case kafka.Error:
				c.log.Errorf("kafka error: %v", message)
				time.Sleep(2 * time.Second)
			}
		}
	}
}

func (c *OrderEventConsumer) Stop() error {
	return c.consumer.Close()
}

func (c *OrderEventConsumer) handleMessage(ctx context.Context, msg *kafka.Message) {
	if msg.TopicPartition.Topic == nil {
		return
	}

	var err error
	switch *msg.TopicPartition.Topic {
	case "stock.reduction.fail":
		event, decodeErr := avro.DeserializeStockReductionFailedEvent(bytes.NewReader(msg.Value))
		if decodeErr != nil {
			err = decodeErr
			break
		}
		err = c.usecase.HandleStockEvent(ctx, event.Order_id, false, event.Message)
	case "stock.reduction.success":
		event, decodeErr := avro.DeserializeStockReductionSuccessEvent(bytes.NewReader(msg.Value))
		if decodeErr != nil {
			err = decodeErr
			break
		}
		err = c.usecase.HandleStockEvent(ctx, event.Order_id, true, "")
	case "payment.fail":
		event, decodeErr := avro.DeserializePaymentFailedEvent(bytes.NewReader(msg.Value))
		if decodeErr != nil {
			err = decodeErr
			break
		}
		err = c.usecase.HandlePaymentEvent(ctx, event.Order_id, false, event.Fail_reason)
	case "payment.success":
		event, decodeErr := avro.DeserializePaymentSuccessEvent(bytes.NewReader(msg.Value))
		if decodeErr != nil {
			err = decodeErr
			break
		}
		err = c.usecase.HandlePaymentEvent(ctx, event.Order_id, true, "")
	default:
		if _, commitErr := c.consumer.CommitMessage(msg); commitErr != nil {
			c.log.Errorf("failed to commit unknown topic message: %v", commitErr)
		}
		return
	}

	if err != nil {
		c.log.Errorf("failed to handle kafka event on topic %s: %v", *msg.TopicPartition.Topic, err)
		return
	}

	if _, err := c.consumer.CommitMessage(msg); err != nil {
		c.log.Errorf("failed to commit kafka message: %v", err)
	}
}
