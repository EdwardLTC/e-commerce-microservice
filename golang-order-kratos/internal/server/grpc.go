package server

import (
	pb "golang-order-kratos/gen/proto"
	"golang-order-kratos/internal/service"

	"github.com/go-kratos/kratos/v2/log"
	"github.com/go-kratos/kratos/v2/middleware/recovery"
	kggrpc "github.com/go-kratos/kratos/v2/transport/grpc"
)

func NewGRPCServer(addr string, orderService *service.OrderService, logger log.Logger) *kggrpc.Server {
	server := kggrpc.NewServer(
		kggrpc.Address(addr),
		kggrpc.Middleware(
			recovery.Recovery(),
		),
	)

	pb.RegisterOrderServiceServer(server, orderService)
	_ = logger
	return server
}
