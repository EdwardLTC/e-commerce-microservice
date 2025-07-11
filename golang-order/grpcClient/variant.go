package grpcClient

import (
	"context"
	"fmt"
	pb "golang-order/gen"
	"google.golang.org/grpc"
	"time"
)

func VariantClient() (pb.VariantServiceClient, error) {
	_, cancel := context.WithTimeout(context.Background(), 5*time.Second)

	defer cancel()

	conn, err := grpc.NewClient("localhost:50051")

	if err != nil {
		return nil, fmt.Errorf("could not connect to OrderService: %w", err)
	}

	client := pb.NewVariantServiceClient(conn)

	return client, nil
}
