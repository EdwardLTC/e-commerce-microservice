package server

import (
	"golang-order/api/order"
	"golang-order/gen/ent"
	pb "golang-order/gen/proto"
	"golang-order/interceptor"
	"log"
	"net"
	"os"

	"google.golang.org/grpc"
)

func initGrpcServer(entClient *ent.Client) *grpc.Server {
	port := os.Getenv("APP_PORT")
	if port == "" {
		log.Fatal("APP_PORT environment variable is not set")
	}

	lis, err := net.Listen("tcp", ":"+port)
	if err != nil {
		log.Fatalf("Failed to listen: %v", err)
	}

	orderHandler := order.NewHandler(entClient)
	s := grpc.NewServer(grpc.UnaryInterceptor(interceptor.ErrorWrappingInterceptor))
	pb.RegisterOrderServiceServer(s, orderHandler)

	log.Printf("gRPC server running on port %s", port)

	if err := s.Serve(lis); err != nil {
		log.Fatalf("Failed to serve: %v", err)
	}
	
	return s

}
