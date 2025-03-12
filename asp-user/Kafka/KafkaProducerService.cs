using System.Text.Json;
using asp_user.Attributes;
using Confluent.Kafka;

namespace asp_user.Kafka;

[RegisterService(ServiceLifetime.Singleton)]
public class KafkaProducerService(IConfiguration configuration, ILogger<KafkaProducerService> logger)
{
    private readonly IProducer<string, string> producer = new ProducerBuilder<string, string>(new ProducerConfig
    {
        BootstrapServers = configuration["Kafka:BootstrapServers"],
        Acks = Acks.All,
        MessageTimeoutMs = 10000,
        SocketTimeoutMs = 10000,
        RetryBackoffMs = 500,
        EnableIdempotence = true,
        MaxInFlight = 5
    }).SetErrorHandler((_, e) => logger.LogError($"kafka Producer Error: {e.Reason}")).Build();

    public async Task<DeliveryResult<string, string>> ProduceAsync<T>(string topic, string key, T message)
    {
        try
        {
            var kafkaMessage = new Message<string, string>
            {
                Key = key,
                Value = JsonSerializer.Serialize(message)
            };

            var deliveryResult = await producer.ProduceAsync(topic, kafkaMessage);

            logger.LogInformation(
                $"✅ Message sent to {deliveryResult.Topic} (Partition: {deliveryResult.Partition}, Offset: {deliveryResult.Offset})");

            return deliveryResult;
        }
        catch (ProduceException<string, string> ex)
        {
            logger.LogError($"❌ Kafka produce exception: {ex.Error.Reason}");

            return new DeliveryResult<string, string>
            {
                Status = PersistenceStatus.NotPersisted,
                TopicPartitionOffset = new TopicPartitionOffset(topic, Partition.Any, Offset.Unset),
                Message = new Message<string, string> { Key = key, Value = JsonSerializer.Serialize(message) }
            };
        }
        catch (Exception ex)
        {
            logger.LogError($"❌ Unexpected error: {ex.Message}");

            throw;
        }
    }
}