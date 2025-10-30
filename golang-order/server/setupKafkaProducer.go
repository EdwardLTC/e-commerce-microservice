package server

import (
	"golang-order/internal/kafka"
	"log"
	"os"
)

func setupKafkaProducer() *kafka.Producer {
	kafkaProducer, err := kafka.NewProducer(os.Getenv("KAFKA_BROKER"))
	if err != nil {
		log.Fatalf("Failed to initialize Kafka producer: %v", err)
	}

	return kafkaProducer
}
