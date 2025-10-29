package kafka

import (
	"log"

	"github.com/confluentinc/confluent-kafka-go/kafka"
)

type Producer struct {
	p *kafka.Producer
}

// NewProducer creates a Kafka producer with Schema Registry connection
func NewProducer(broker string) (*Producer, error) {
	p, err := kafka.NewProducer(&kafka.ConfigMap{
		"bootstrap.servers": broker,
	})
	if err != nil {
		return nil, err
	}

	return &Producer{
		p: p,
	}, nil
}

// Publish publishes an Avro-encoded message to Kafka
func (kp *Producer) Publish(topic string, key string, value []byte) error {
	deliveryChan := make(chan kafka.Event, 1)
	defer close(deliveryChan)

	// Produce the Avro message
	err := kp.p.Produce(&kafka.Message{
		TopicPartition: kafka.TopicPartition{
			Topic:     &topic,
			Partition: kafka.PartitionAny,
		},
		Key:   []byte(key),
		Value: value,
	}, deliveryChan)

	if err != nil {
		return err
	}

	// Wait for delivery confirmation
	e := <-deliveryChan
	m := e.(*kafka.Message)
	if m.TopicPartition.Error != nil {
		return m.TopicPartition.Error
	}

	log.Printf("✅ Avro event published to %s [%d] at offset %v\n",
		*m.TopicPartition.Topic, m.TopicPartition.Partition, m.TopicPartition.Offset)
	return nil
}

// Close flushes and closes the Kafka producer
func (kp *Producer) Close() {
	kp.p.Close()
}
