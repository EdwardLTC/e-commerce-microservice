package data

import (
	"context"
	"fmt"
	"time"

	"golang-order-kratos/gen/ent"

	_ "github.com/lib/pq"
)

func NewEntClient(databaseURL string) (*ent.Client, error) {
	if databaseURL == "" {
		return nil, fmt.Errorf("DATABASE_URL is required")
	}

	client, err := ent.Open("postgres", databaseURL)
	if err != nil {
		return nil, err
	}

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := client.Schema.Create(ctx); err != nil {
		_ = client.Close()
		return nil, err
	}

	return client, nil
}
