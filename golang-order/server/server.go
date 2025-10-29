package server

import (
	"golang-order/api/order"
	pb "golang-order/gen/proto"
	"golang-order/interceptor"
	"log"
	"net"
	"os"
	"os/signal"
	"syscall"

	_ "github.com/jackc/pgx/v5/stdlib"
	"github.com/joho/godotenv"
	"google.golang.org/grpc"
)

func Start() {
	// Load env vars
	if err := godotenv.Load(); err != nil {
		log.Println("No .env file found, using system env")
	}

	port := os.Getenv("APP_PORT")
	if port == "" {
		log.Fatal("APP_PORT environment variable is not set")
	}

	// Init gRPC listener
	lis, err := net.Listen("tcp", ":"+port)
	if err != nil {
		log.Fatalf("Failed to listen: %v", err)
	}
	
	entClient := setupDBClient()
	kafkaProducer := setupKafkaProducer()
	outboxCancel := setupOutboxDispatcher(entClient, kafkaProducer)
	kafkaConsumerCancel := setupKafkaConsumer()

	// Setup gRPC server
	orderHandler := order.NewHandler(entClient)
	s := grpc.NewServer(grpc.UnaryInterceptor(interceptor.ErrorWrappingInterceptor))
	pb.RegisterOrderServiceServer(s, orderHandler)

	// Graceful shutdown on SIGINT/SIGTERM
	go func() {
		ch := make(chan os.Signal, 1)
		signal.Notify(ch, syscall.SIGINT, syscall.SIGTERM)
		<-ch
		log.Println("Shutting down gRPC server...")
		s.GracefulStop()
		outboxCancel()
		kafkaConsumerCancel()
	}()

	log.Printf("gRPC server running on port %s", port)

	if err := s.Serve(lis); err != nil {
		log.Fatalf("Failed to serve: %v", err)
	}
}
