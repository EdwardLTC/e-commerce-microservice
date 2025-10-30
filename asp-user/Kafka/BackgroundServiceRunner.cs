namespace asp_user.Kafka;

public class BackgroundServiceRunner : BackgroundService
{
	readonly KafkaAttributeConsumer _consumer;

	public BackgroundServiceRunner(KafkaAttributeConsumer consumer)
	{
		_consumer = consumer;
	}

	protected override Task ExecuteAsync(CancellationToken stoppingToken)
	{
		return Task.Run(() => _consumer.StartConsuming(stoppingToken), stoppingToken);
	}
}
