package grpcClient

import (
	"fmt"
	pb "golang-order/gen/proto"
	"os"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

func VariantClient() (pb.VariantServiceClient, error) {
	conn, err := grpc.NewClient(os.Getenv("PRODUCT_SERVICE_URL"), grpc.WithTransportCredentials(insecure.NewCredentials()))

	if err != nil {
		return nil, fmt.Errorf("could not connect to VariantService: %w", err)
	}

	client := pb.NewVariantServiceClient(conn)
	return client, nil
}
