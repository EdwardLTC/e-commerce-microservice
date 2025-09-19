package db

import (
	"context"
	"golang-order/ent"
	"log"
	"os"
	"time"

	_ "github.com/lib/pq"
)

func NewEntClient() *ent.Client {
	client, err := ent.Open("postgres", os.Getenv("DATABASE_URL"))
	if err != nil {
		log.Fatalf("❌ Failed opening connection to database: %v", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := client.Schema.Create(ctx); err != nil {
		log.Fatalf("❌ Failed creating schema resources: %v", err)
	}

	log.Println("✅ Connected to PostgresSQL")
	return client
}
