package data

import "github.com/confluentinc/confluent-kafka-go/kafka"

type KafkaProducer struct {
	producer *kafka.Producer
}

func NewKafkaProducer(broker string) (*KafkaProducer, error) {
	producer, err := kafka.NewProducer(&kafka.ConfigMap{
		"bootstrap.servers": broker,
	})
	if err != nil {
		return nil, err
	}

	return &KafkaProducer{producer: producer}, nil
}

func (p *KafkaProducer) Publish(topic, key string, value []byte) error {
	delivery := make(chan kafka.Event, 1)
	defer close(delivery)

	if err := p.producer.Produce(&kafka.Message{
		TopicPartition: kafka.TopicPartition{
			Topic:     &topic,
			Partition: kafka.PartitionAny,
		},
		Key:   []byte(key),
		Value: value,
	}, delivery); err != nil {
		return err
	}

	event := <-delivery
	message := event.(*kafka.Message)
	return message.TopicPartition.Error
}

func (p *KafkaProducer) Close() {
	p.producer.Close()
}
