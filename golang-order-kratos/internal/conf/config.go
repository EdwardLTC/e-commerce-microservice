package conf

import (
	"os"
	"strconv"
	"strings"
	"time"
)

type Bootstrap struct {
	Server  Server
	Data    Data
	Clients Clients
	Kafka   Kafka
	Worker  Worker
}

type Server struct {
	GRPCAddr string
}

type Data struct {
	DatabaseURL string
}

type Clients struct {
	ProductServiceURL string
}

type Kafka struct {
	Broker        string
	ConsumerGroup string
}

type Worker struct {
	OutboxInterval time.Duration
	OutboxBatch    int
	Topics         []string
}

func FromEnv() Bootstrap {
	port := getEnv("APP_PORT", "2002")

	return Bootstrap{
		Server: Server{
			GRPCAddr: ":" + port,
		},
		Data: Data{
			DatabaseURL: os.Getenv("DATABASE_URL"),
		},
		Clients: Clients{
			ProductServiceURL: os.Getenv("PRODUCT_SERVICE_URL"),
		},
		Kafka: Kafka{
			Broker:        os.Getenv("KAFKA_BROKER"),
			ConsumerGroup: getEnv("KAFKA_CONSUMER_GROUP", "order-service-group"),
		},
		Worker: Worker{
			OutboxInterval: getDuration("OUTBOX_INTERVAL", 5*time.Second),
			OutboxBatch:    getInt("OUTBOX_BATCH", 20),
			Topics:         getTopics(),
		},
	}
}

func getEnv(key, fallback string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return fallback
}

func getInt(key string, fallback int) int {
	raw := os.Getenv(key)
	if raw == "" {
		return fallback
	}

	value, err := strconv.Atoi(raw)
	if err != nil || value <= 0 {
		return fallback
	}
	return value
}

func getDuration(key string, fallback time.Duration) time.Duration {
	raw := os.Getenv(key)
	if raw == "" {
		return fallback
	}

	value, err := time.ParseDuration(raw)
	if err != nil {
		return fallback
	}
	return value
}

func getTopics() []string {
	raw := os.Getenv("KAFKA_TOPICS")
	if raw == "" {
		return []string{
			"stock.reduction.fail",
			"stock.reduction.success",
			"payment.fail",
			"payment.success",
		}
	}

	parts := strings.Split(raw, ",")
	topics := make([]string, 0, len(parts))
	for _, part := range parts {
		if value := strings.TrimSpace(part); value != "" {
			topics = append(topics, value)
		}
	}
	return topics
}
