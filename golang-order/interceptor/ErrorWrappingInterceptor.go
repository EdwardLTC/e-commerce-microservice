package interceptor

import (
	"context"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"log"
	"runtime/debug"
)

func ErrorWrappingInterceptor(
	ctx context.Context,
	req interface{},
	info *grpc.UnaryServerInfo,
	handler grpc.UnaryHandler,
) (resp interface{}, err error) {
	resp, err = handler(ctx, req)

	if err != nil {
		if s, ok := status.FromError(err); ok {
			log.Printf("[gRPC ERROR] method=%s code=%s message=%s",
				info.FullMethod, s.Code(), s.Message(),
			)
			return resp, err
		}

		log.Printf("[INTERNAL ERROR] method=%s err=%v\nStackTrace:\n%s",
			info.FullMethod, err, debug.Stack(),
		)

		err = status.Error(codes.Internal, "Something went wrong")
	}

	return resp, err
}
