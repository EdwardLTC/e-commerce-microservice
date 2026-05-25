package config

import (
	"os"
	"strings"
)

type Config struct {
	Brokers               []string
	Topic                 string
	GroupID               string
	CassandraContactPoints []string
	CassandraKeyspace     string
	ValidatorAddr         string
}

func Load() Config {
	return Config{
		Brokers:                []string{env("KAFKA_BROKERS", "localhost:9092")},
		Topic:                  env("CHAT_MESSAGES_TOPIC", "chat.messages.v1"),
		GroupID:                env("CHAT_MESSAGES_GROUP_ID", "chat-message-writer"),
		CassandraContactPoints: splitEnv("CASSANDRA_CONTACT_POINTS", "localhost:9042"),
		CassandraKeyspace:      env("CASSANDRA_KEYSPACE", "chat"),
		ValidatorAddr:          env("VALIDATOR_ADDR", "0.0.0.0:8080"),
	}
}

func splitEnv(key string, fallback string) []string {
	value := env(key, fallback)
	parts := strings.Split(value, ",")
	result := make([]string, 0, len(parts))
	for _, part := range parts {
		part = strings.TrimSpace(part)
		if part != "" {
			result = append(result, part)
		}
	}

	return result
}

func env(key string, fallback string) string {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}

	return value
}
