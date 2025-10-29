package order

import (
	"context"
	"fmt"
	"golang-order/gen/ent"
	pb "golang-order/gen/proto"
	"golang-order/grpcClient"
	"golang-order/internal/order"
)

type Handler struct {
	pb.UnimplementedOrderServiceServer
	svc order.Choreography
}

func NewHandler(db *ent.Client) *Handler {
	variantClient, err := grpcClient.VariantClient()
	if err != nil {
		fmt.Println(err)
	}

	return &Handler{svc: *order.NewOrderChoreography(db, variantClient)}
}

func (h *Handler) CreateOrder(ctx context.Context, req *pb.CreateOrderRequest) (*pb.CreateOrderResponse, error) {
	return h.svc.CreateOrder(ctx, req)
}
