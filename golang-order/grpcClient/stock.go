package grpcClient

import (
	"context"
	"fmt"
	pb "golang-order/gen"
	"os"
	"time"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

func StockClient() (pb.StockServiceClient, error) {
	_, cancel := context.WithTimeout(context.Background(), 5*time.Second)

	defer cancel()

	conn, err := grpc.NewClient(os.Getenv("PRODUCT_SERVICE_URL"), grpc.WithTransportCredentials(insecure.NewCredentials()))

	if err != nil {
		return nil, fmt.Errorf("could not connect to VariantService: %w", err)
	}

	fmt.Println("Connected to VariantService at", os.Getenv("PRODUCT_SERVICE_URL"))

	client := pb.NewStockServiceClient(conn)

	return client, nil
}
