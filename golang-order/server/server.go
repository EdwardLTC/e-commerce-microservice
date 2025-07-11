package server

import (
	_ "github.com/jackc/pgx/v5/stdlib"
	"github.com/joho/godotenv"
	"golang-order/api/order"
	"golang-order/ent"
	pb "golang-order/gen"
	"golang-order/interceptor"
	"golang-order/pkg/db"
	"google.golang.org/grpc"
	"log"
	"net"
)

func StartGRPCServer() {
	if err := godotenv.Load(); err != nil {
		log.Println("No .env file found, using system env")
	}

	lis, err := net.Listen("tcp", ":50051")
	if err != nil {
		log.Fatalf("Failed to listen: %v", err)
	}

	entClient := db.NewEntClient()

	defer func(entClient *ent.Client) {
		err := entClient.Close()
		if err != nil {
			log.Fatalf("Failed to close ent client: %v", err)
		} else {
			log.Println("Ent client closed successfully")
		}
	}(entClient)

	s := grpc.NewServer(grpc.UnaryInterceptor(interceptor.ErrorWrappingInterceptor))

	pb.RegisterOrderServiceServer(s, order.NewHandler(entClient))

	log.Println("gRPC server running on :50051")
	if err := s.Serve(lis); err != nil {
		log.Fatalf("Failed to serve: %v", err)
	}
}
