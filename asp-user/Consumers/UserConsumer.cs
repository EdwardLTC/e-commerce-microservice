using asp_user.Attributes;
using asp_user.Exceptions;
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

		try
		{
			await userService.DecreaseWalletAndEnqueuePaymentSuccessAsync(Guid.Parse(order.CustomerId), (decimal)order.Total, evt.order_id);
		}
		catch (InsufficientBalanceException e)
		{
			Console.WriteLine(e);
			await userService.EnqueuePaymentFailedAsync(evt.order_id, e.Message);
		}

		consumer.Commit(cr);
	}
}
