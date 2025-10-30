package kafka

import (
	"context"
	"log"
	"sync"
	"time"

	"github.com/confluentinc/confluent-kafka-go/kafka"
)

type Handler interface {
	HandleMessage(msg *kafka.Message, ack *kafka.Consumer)
}

type Consumer struct {
	c       *kafka.Consumer
	topics  []string
	handler Handler

	ctx    context.Context
	cancel context.CancelFunc
	wg     sync.WaitGroup
}

// NewKafkaConsumer creates a consumer with manual commits (enable.auto.commit=false).
// brokers is a comma-separated list, e.g. "broker1:9092,broker2:9092".
func NewKafkaConsumer(brokers string, groupID string, topics []string, handler Handler) (*Consumer, error) {
	cfg := &kafka.ConfigMap{
		"bootstrap.servers":  brokers,
		"group.id":           groupID,
		"enable.auto.commit": false,      // disable auto commits
		"auto.offset.reset":  "earliest", // start from oldest if no offset
		"session.timeout.ms": 10000,
	}

	c, err := kafka.NewConsumer(cfg)
	if err != nil {
		return nil, err
	}

	ctx, cancel := context.WithCancel(context.Background())
	return &Consumer{
		c:       c,
		topics:  topics,
		handler: handler,
		ctx:     ctx,
		cancel:  cancel,
	}, nil
}

// Start begins background polling. It returns immediately.
func (kc *Consumer) Start() error {
	if err := kc.c.SubscribeTopics(kc.topics, nil); err != nil {
		return err
	}

	kc.wg.Add(1)
	go kc.run()
	return nil
}

func (kc *Consumer) run() {
	defer kc.wg.Done()
	for {
		select {
		case <-kc.ctx.Done():
			return
		default:
			ev := kc.c.Poll(1000)
			if ev == nil {
				continue
			}

			switch e := ev.(type) {
			case *kafka.Message:
				log.Printf("Handling message on topic %s, partition %d, offset %d\n", *e.TopicPartition.Topic, e.TopicPartition.Partition, e.TopicPartition.Offset)
				kc.handler.HandleMessage(e, kc.c)
			case kafka.Error:
				log.Printf("kafka error: %v", e)
				time.Sleep(2 * time.Second)
			default:
			}
		}
	}
}

// Stop stops the consumer gracefully and closes the underlying client.
func (kc *Consumer) Stop() error {
	kc.cancel()
	kc.wg.Wait()
	return kc.c.Close()
}
