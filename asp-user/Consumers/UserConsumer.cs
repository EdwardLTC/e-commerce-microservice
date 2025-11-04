using asp_user.Attributes;
using asp_user.Exceptions;
using asp_user.GrpcServiceClients;
using asp_user.Kafka;
using asp_user.Services;
using Com.Ecommerce.Golang.Order;
using com.example.payment;
using com.example.stock;
using Confluent.Kafka;

namespace asp_user.Consumers;

public class UserConsumer(UsersService userService, OrderServiceClient orderServiceClient, KafkaProducerService producer)
{

	[KafkaTopic("stock.reduction.success", typeof(StockReductionSuccessEvent))]
	public async Task OnStockReduceSuccess(StockReductionSuccessEvent evt, IConsumer<string, byte[]> consumer, ConsumeResult<string, byte[]> cr)
	{
		Order order = await orderServiceClient.GetOrderAsync(evt.order_id);

		try
		{
			await userService.DecreaseWalletAmountAsync(Guid.Parse(order.CustomerId), (decimal)order.Total);

			PaymentSuccessEvent paymentSuccess = new PaymentSuccessEvent
			{
				order_id = evt.order_id
			};

			await producer.ProduceAvroAsync("payment.success", evt.order_id, paymentSuccess);
		}
		catch (InsufficientBalanceException e)
		{
			Console.WriteLine(e);
			PaymentFailedEvent paymentFailed = new PaymentFailedEvent
			{
				order_id = evt.order_id,
				fail_reason = e.Message
			};
			await producer.ProduceAvroAsync("payment.fail", evt.order_id, paymentFailed);
			throw;
		}
		consumer.Commit(cr);
	}
}
