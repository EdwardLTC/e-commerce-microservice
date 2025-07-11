package grpcClient

import (
	"context"
	"fmt"
	pb "golang-order/gen"
	"google.golang.org/grpc"
	"time"
)

func ProductClient() (pb.ProductServiceClient, *grpc.ClientConn, error) {
	_, cancel := context.WithTimeout(context.Background(), 5*time.Second)

	defer cancel()

	conn, err := grpc.NewClient("localhost:50051")

	if err != nil {
		return nil, nil, fmt.Errorf("could not connect to OrderService: %w", err)
	}

	client := pb.NewProductServiceClient(conn)

	return client, conn, nil
}
