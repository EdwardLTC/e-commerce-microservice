using System.Reflection;
using asp_user.Attributes;
using Avro.IO;
using Avro.Specific;
using Confluent.Kafka;
using Microsoft.Extensions.Options;

namespace asp_user.Kafka;

public class KafkaAttributeConsumer(
	IOptions<ConsumerConfig> config,
	ILogger<KafkaAttributeConsumer> logger,
	IServiceProvider serviceProvider)
{
	readonly IConsumer<string, byte[]> _consumer =
		new ConsumerBuilder<string, byte[]>(config.Value)
			.SetValueDeserializer(Deserializers.ByteArray)
			.Build();

	public void StartConsuming(CancellationToken cancellationToken)
	{
		IEnumerable<KafkaHandlerInfo> handlers = FindKafkaHandlers().ToList();

		List<string> topics = handlers.Select(h => h.Topic).Distinct().ToList();
		if (topics.Count == 0)
		{
			logger.LogWarning("⚠️ No KafkaTopic handlers found to subscribe.");
			return;
		}
		_consumer.Subscribe(topics);

		logger.LogInformation($"🚀 Subscribed to topics: {string.Join(", ", topics)}");

		while (!cancellationToken.IsCancellationRequested)
		{
			try
			{
				ConsumeResult<string, byte[]>? cr = _consumer.Consume(cancellationToken);
				KafkaHandlerInfo? handler = handlers.FirstOrDefault(h => h.Topic == cr.Topic);

				if (handler == null)
				{
					logger.LogWarning($"⚠️ No handler for topic {cr.Topic}");
					continue;
				}

				object record = DeserializeAvro(cr.Message.Value, handler.MessageType);
				handler.Invoke(serviceProvider, record, _consumer, cr);
			}
			catch (ConsumeException ex)
			{
				logger.LogError($"❌ Consume error: {ex.Error.Reason}");
			}
		}
	}

	static object DeserializeAvro(byte[] data, Type messageType)
	{
		using MemoryStream ms = new MemoryStream(data);
		BinaryDecoder decoder = new BinaryDecoder(ms);
		ISpecificRecord instance = (ISpecificRecord)Activator.CreateInstance(messageType)!;
		SpecificDatumReader<ISpecificRecord> reader = new SpecificDatumReader<ISpecificRecord>(instance.Schema, instance.Schema);
		return reader.Read(instance, decoder);
	}

	static IEnumerable<KafkaHandlerInfo> FindKafkaHandlers()
	{
		return from type in AppDomain.CurrentDomain.GetAssemblies().SelectMany(a => a.GetTypes())
			from method in type.GetMethods()
			let attr = method.GetCustomAttribute<KafkaTopicAttribute>()
			where attr != null
			select new KafkaHandlerInfo(type, method, attr.Topic, attr.MessageType!);
	}

	record KafkaHandlerInfo(Type DeclaringType, MethodInfo Method, string Topic, Type MessageType)
	{
		public void Invoke(IServiceProvider sp, object message, IConsumer<string, byte[]> consumer, ConsumeResult<string, byte[]> cr)
		{
			object instance = sp.GetService(DeclaringType) ?? Activator.CreateInstance(DeclaringType)!;

			ParameterInfo[] parameters = Method.GetParameters();

			object?[] args = parameters.Length switch
			{
				1 => [message],
				2 => [message, consumer],
				3 => [message, consumer, cr],
				_ => throw new InvalidOperationException($"Invalid parameter count for {Method.Name}")
			};

			Method.Invoke(instance, args);
		}
	}
}
