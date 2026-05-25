package service

import (
	"context"

	pb "golang-order-kratos/gen/proto"
	"golang-order-kratos/internal/biz"
)

type OrderService struct {
	pb.UnimplementedOrderServiceServer

	uc *biz.OrderUsecase
}

func NewOrderService(uc *biz.OrderUsecase) *OrderService {
	return &OrderService{uc: uc}
}

func (s *OrderService) CreateOrder(ctx context.Context, req *pb.CreateOrderRequest) (*pb.CreateOrderResponse, error) {
	return s.uc.CreateOrder(ctx, req)
}

func (s *OrderService) GetOrder(ctx context.Context, req *pb.GetOrderRequest) (*pb.Order, error) {
	return s.uc.GetOrder(ctx, req)
}

func (s *OrderService) GetOrders(ctx context.Context, req *pb.ListOrdersRequest) (*pb.ListOrdersResponse, error) {
	return s.uc.ListOrders(ctx, req)
}
