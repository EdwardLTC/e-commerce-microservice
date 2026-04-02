package grpcClient

import (
	"fmt"
	pb "golang-order/gen/proto"
	"os"
	"strings"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/credentials/insecure"
)

func VariantClient() (pb.VariantServiceClient, error) {
	target := os.Getenv("PRODUCT_SERVICE_URL")
	var transport grpc.DialOption
	if usesInsecureTransport(target) {
		transport = grpc.WithTransportCredentials(insecure.NewCredentials())
	} else {
		transport = grpc.WithTransportCredentials(credentials.NewTLS(nil))
	}

	conn, err := grpc.NewClient(target, transport)

	if err != nil {
		return nil, fmt.Errorf("could not connect to VariantService: %w", err)
	}

	client := pb.NewVariantServiceClient(conn)
	return client, nil
}

func usesInsecureTransport(target string) bool {
	return strings.Contains(target, "localhost") || strings.Contains(target, "127.0.0.1") || strings.Contains(target, "host.docker.internal")
}
