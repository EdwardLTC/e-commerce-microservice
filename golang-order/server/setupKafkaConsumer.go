package server

import (
	"golang-order/internal/kafka"
	"log"
	"os"
)

func setupKafkaConsumer(handler kafka.Handler) *kafka.Consumer {
	events := []string{"stock.reduction.fail", "stock.reduction.success"}
	consumer, err := kafka.NewKafkaConsumer(os.Getenv("KAFKA_BROKER"), "order-service-group", events, handler)

	if err != nil {
		log.Printf("Failed to initialize Kafka consumer: %v", err)
		return nil
	}

	if err := consumer.Start(); err != nil {
		log.Printf("Failed to start Kafka consumer: %v", err)
	}

	return consumer
}
