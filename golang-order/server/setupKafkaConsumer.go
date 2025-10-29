package server

import (
	"golang-order/internal/kafka"
	"log"
	"os"
)

func setupKafkaConsumer(handler kafka.Handler) {
	events := []string{"user-events", "order-events", "payment-events"}
	consumer, err := kafka.NewKafkaConsumer(os.Getenv("KAFKA_BROKER"), "order-service-group", events, handler)

	if err != nil {
		log.Fatal(err)
	}

	defer consumer.Stop()

	consumer.Start()
}
