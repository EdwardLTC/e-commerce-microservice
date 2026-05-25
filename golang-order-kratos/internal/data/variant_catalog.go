package data

import (
	"context"
	"fmt"
	"strings"

	pb "golang-order-kratos/gen/proto"
	"golang-order-kratos/internal/biz"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/credentials/insecure"
)

type VariantCatalog struct {
	conn   *grpc.ClientConn
	client pb.VariantServiceClient
}

func NewVariantCatalog(target string) (*VariantCatalog, error) {
	if target == "" {
		return nil, fmt.Errorf("PRODUCT_SERVICE_URL is required")
	}

	transport := grpc.WithTransportCredentials(credentials.NewTLS(nil))
	if usesInsecureTransport(target) {
		transport = grpc.WithTransportCredentials(insecure.NewCredentials())
	}

	conn, err := grpc.Dial(target, transport)
	if err != nil {
		return nil, err
	}

	return &VariantCatalog{
		conn:   conn,
		client: pb.NewVariantServiceClient(conn),
	}, nil
}

func (c *VariantCatalog) GetVariantsByIDs(ctx context.Context, ids []string) ([]biz.VariantInfo, error) {
	response, err := c.client.GetVariantsByIds(ctx, &pb.GetVariantByIdsRequest{Ids: ids})
	if err != nil {
		return nil, err
	}

	result := make([]biz.VariantInfo, 0, len(response.Variants))
	for _, variant := range response.Variants {
		result = append(result, biz.VariantInfo{
			ID:                 variant.Id,
			ProductID:          variant.ProductId,
			ProductName:        variant.ProductName,
			VariantDescription: variant.Sku,
			ImageURL:           variant.MediaUrl,
			SalePrice:          variant.SalePrice,
		})
	}

	return result, nil
}

func (c *VariantCatalog) Close() error {
	return c.conn.Close()
}

func usesInsecureTransport(target string) bool {
	return strings.Contains(target, "localhost") ||
		strings.Contains(target, "127.0.0.1") ||
		strings.Contains(target, "host.docker.internal")
}
