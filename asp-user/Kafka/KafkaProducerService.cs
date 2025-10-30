using Avro.IO;
using Avro.Specific;
using Confluent.Kafka;
using Microsoft.Extensions.Options;

namespace asp_user.Kafka;

public class KafkaProducerService(IOptions<ProducerConfig> producerConfig, ILogger<KafkaProducerService> logger)
{
    readonly IProducer<string, byte[]> producer =
        new ProducerBuilder<string, byte[]>(new ProducerConfig(producerConfig.Value))
            .SetKeySerializer(Serializers.Utf8)
            .SetValueSerializer(Serializers.ByteArray)
            .SetErrorHandler((_, e) => logger.LogError($"Kafka Producer Error: {e.Reason}"))
            .SetLogHandler((_, _) => { })
            .Build();

    public async Task<DeliveryResult<string, byte[]>> ProduceAvroAsync<T>(string topic, string key, T message)
        where T : ISpecificRecord
    {
        try
        {
            var valueBytes = SerializeAvro(message);

            var kafkaMessage = new Message<string, byte[]>
            {
                Key = key,
                Value = valueBytes
            };

            var deliveryResult = await producer.ProduceAsync(topic, kafkaMessage);

            logger.LogInformation(
                $"✅ Avro message sent to {deliveryResult.Topic} (Partition: {deliveryResult.Partition}, Offset: {deliveryResult.Offset})");

            return deliveryResult;
        }
        catch (ProduceException<string, byte[]> ex)
        {
            logger.LogError($"❌ Kafka produce exception: {ex.Error.Reason}");
            throw;
        }
        catch (Exception ex)
        {
            logger.LogError($"❌ Unexpected error: {ex.Message}");
            throw;
        }
    }

    static byte[] SerializeAvro<T>(T record) where T : ISpecificRecord
    {
        using var ms = new MemoryStream();
        var encoder = new BinaryEncoder(ms);
        var writer = new SpecificDatumWriter<T>(record.Schema);
        writer.Write(record, encoder);
        return ms.ToArray();
    }
}