package main

import (
	"context"
	"log"
	"os/signal"
	"syscall"

	"chat-message-writer/internal/config"
	"chat-message-writer/internal/db"
	chatkafka "chat-message-writer/internal/kafka"
	"chat-message-writer/internal/api"
)

func main() {
	ctx, stop := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer stop()

	cfg := config.Load()

	store, err := db.New(ctx, cfg.CassandraContactPoints, cfg.CassandraKeyspace)
	if err != nil {
		log.Fatalf("connect Cassandra: %v", err)
	}
	defer store.Close()

	if err := store.Migrate(ctx); err != nil {
		log.Fatalf("migrate database: %v", err)
	}

	consumer, err := chatkafka.NewConsumer(cfg, store)
	if err != nil {
		log.Fatalf("create kafka consumer: %v", err)
	}
	defer consumer.Close()

	// start validator http server
	go func() {
		if err := api.Start(ctx, cfg.ValidatorAddr, store); err != nil {
			log.Fatalf("validator server: %v", err)
		}
	}()

	log.Printf("chat message writer started topic=%s group=%s", cfg.Topic, cfg.GroupID)

	if err := consumer.Run(ctx); err != nil {
		log.Fatalf("run kafka consumer: %v", err)
	}
}
