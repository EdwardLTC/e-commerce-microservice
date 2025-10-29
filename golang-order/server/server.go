package server

import (
	"golang-order/internal/order"
	"log"
	"os"
	"os/signal"
	"syscall"

	_ "github.com/jackc/pgx/v5/stdlib"
	"github.com/joho/godotenv"
)

func Start() {
	// Load env vars
	if err := godotenv.Load(); err != nil {
		log.Println("No .env file found, using system env")
	}

	entClient := setupDBClient()
	kafkaProducer := setupKafkaProducer()
	outboxCancel := setupOutboxDispatcher(entClient, kafkaProducer)
	setupKafkaConsumer(order.NewConsumerHandler(entClient))
	grpcServer := initGrpcServer(entClient)

	// Graceful shutdown on SIGINT/SIGTERM
	go func() {
		ch := make(chan os.Signal, 1)
		signal.Notify(ch, syscall.SIGINT, syscall.SIGTERM)
		<-ch
		log.Println("Shutting down gRPC server...")
		grpcServer.GracefulStop()
		outboxCancel()
		if err := entClient.Close(); err != nil {
			log.Printf("Error closing ent client: %v", err)
		}
		log.Println("Shutdown complete.")
		os.Exit(0)
	}()
}
