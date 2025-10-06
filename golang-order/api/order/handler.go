package order

import (
	"context"
	"golang-order/ent"
	pb "golang-order/gen"
	"golang-order/grpcClient"
	"golang-order/internal/order"
)

type Handler struct {
	pb.UnimplementedOrderServiceServer
	svc order.Orchestrator
}

func NewHandler(db *ent.Client) *Handler {
	stockClient, err := grpcClient.StockClient()

	if err != nil {
		panic("failed to connect to VariantService: " + err.Error())
	}

	return &Handler{svc: *order.NewOrderOrchestrator(db, stockClient, nil)}
}

func (h *Handler) CreateOrder(ctx context.Context, req *pb.CreateOrderRequest) (*pb.CreateOrderResponse, error) {
	return h.svc.CreateOrder(ctx, req)
}
