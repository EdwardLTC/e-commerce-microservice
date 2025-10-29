package server

import (
	"golang-order/gen/ent"
	"golang-order/pkg/db"
	"log"
)

func setupDBClient() *ent.Client {
	entClient := db.NewEntClient()
	defer func() {
		if err := entClient.Close(); err != nil {
			log.Printf("Failed to close ent client: %v", err)
		}
	}()

	return entClient
}
