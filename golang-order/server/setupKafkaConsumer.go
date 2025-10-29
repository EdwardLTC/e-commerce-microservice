package server

import (
	"context"
	"golang-order/internal/kafka"
	"log"
	"os"
	"time"

	kafka2 "github.com/confluentinc/confluent-kafka-go/kafka"
)

func setupKafkaConsumer() context.CancelFunc {
	consumer, err := kafka.NewEnhancedConsumer(kafka.ConsumerConfig{
		Broker:          os.Getenv("KAFKA_BROKER"),
		GroupID:         "order-service-group",
		Topics:          []string{"user-events", "order-events", "payment-events"},
		AutoOffsetReset: "earliest",
		MaxRetries:      3,
		RetryDelay:      time.Second,
	})

	if err != nil {
		log.Fatal(err)
	}
	defer consumer.Stop()

	consumer.Use(kafka.LoggingMiddleware())
	decorator := consumer.Decorator()
	decorator.
		HandleFunc("user-events", func(ctx context.Context, message *kafka2.Message) error {
			return nil
		}).
		HandleFunc("order-events", func(ctx context.Context, message *kafka2.Message) error {
			return nil
		})

	ctx, cancel := context.WithCancel(context.Background())

	go consumer.Start(ctx)

	return cancel
}
