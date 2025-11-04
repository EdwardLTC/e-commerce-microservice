namespace asp_user.Kafka;

public class BackgroundServiceRunner(KafkaAttributeConsumer consumer) : BackgroundService
{
	protected override Task ExecuteAsync(CancellationToken stoppingToken)
	{
		return Task.Run(() => consumer.StartConsuming(stoppingToken), stoppingToken);
	}
}
