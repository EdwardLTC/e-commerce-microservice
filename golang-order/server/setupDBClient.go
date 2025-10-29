package server

import (
	"golang-order/gen/ent"
	"golang-order/pkg/db"
)

func setupDBClient() *ent.Client {
	entClient := db.NewEntClient()
	return entClient
}
