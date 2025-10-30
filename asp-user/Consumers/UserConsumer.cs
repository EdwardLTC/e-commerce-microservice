using asp_user.Attributes;
using asp_user.GrpcServiceClients;
using asp_user.Services;
using Com.Ecommerce.Golang.Order;
using com.example.stock;
using Confluent.Kafka;

namespace asp_user.Consumers;

public class UserConsumer(UsersService userService, OrderServiceClient orderServiceClient)
{

	[KafkaTopic("stock.reduction.success", typeof(StockReductionSuccessEvent))]
	public async Task OnStockReduceSuccess(StockReductionSuccessEvent evt, IConsumer<string, byte[]> consumer, ConsumeResult<string, byte[]> cr)
	{
		Order order = await orderServiceClient.GetOrderAsync(evt.order_id);
		await userService.DecreaseWalletAmountAsync(Guid.Parse(order.CustomerId), (decimal)order.Total);
		Console.WriteLine($"Stock reduction successful for order {evt.order_id} with price {evt.price}");
		consumer.Commit(cr);
	}
}
