using Com.Ecommerce.Golang.Order;
using Grpc.Net.Client;

namespace asp_user.GrpcServiceClients;

public class OrderServiceClient(string grpcServerUrl)
{
	readonly OrderService.OrderServiceClient _client = new OrderService.OrderServiceClient(GrpcChannel.ForAddress(grpcServerUrl));

	public async Task<Order> GetOrderAsync(string orderId)
	{
		return await _client.GetOrderAsync(new GetOrderRequest
		{
			Id = orderId
		});
	}
}
