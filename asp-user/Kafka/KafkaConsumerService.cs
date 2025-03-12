using System.Reflection;
using System.Text.Json;
using asp_user.Attributes;
using Confluent.Kafka;

namespace asp_user.Kafka;

public class KafkaConsumerService(
    IConfiguration configuration,
    ILogger<KafkaConsumerService> logger,
    IServiceProvider serviceProvider)
    : BackgroundService
{
    private readonly IConsumer<Ignore, string> consumer = new ConsumerBuilder<Ignore, string>(
        new ConsumerConfig
        {
            BootstrapServers = configuration["Kafka:BootstrapServers"],
            GroupId = "asp-user-group",
            AutoOffsetReset = AutoOffsetReset.Earliest,
            EnableAutoCommit = false,
            SessionTimeoutMs = 30000,
            MaxPollIntervalMs = 60000,
            HeartbeatIntervalMs = 10000,
            SocketTimeoutMs = 10000,
            MetadataMaxAgeMs = 300000
        }).SetErrorHandler((_, error) => logger.LogError($"Kafka Consumer Error: {error.Reason}")).Build();

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        using var scope = serviceProvider.CreateScope();

        var handlers = scope.ServiceProvider.GetServices<object>()
            .Where(h => h.GetType().GetCustomAttribute<KafkaHandlerAttribute>() != null)
            .ToList();


        var topics = handlers
            .SelectMany(h => h.GetType().GetMethods())
            .Select(m => m.GetCustomAttribute<KafkaMessageHandlerAttribute>())
            .Where(a => a is not null)
            .Select(a => a!.Topic)
            .Distinct()
            .ToList();

        if (topics.Count == 0)
        {
            logger.LogWarning("No Kafka topics found. Consumer will not subscribe.");
            return;
        }

        logger.LogInformation($"Subscribed to topics: {string.Join(", ", topics)}");
        consumer.Subscribe(topics);

        while (!stoppingToken.IsCancellationRequested)
            try
            {
                var consumeResult = consumer.Consume(TimeSpan.FromSeconds(1));
                if (consumeResult == null) continue;
                var message = consumeResult.Message.Value;
                var topic = consumeResult.Topic;

                logger.LogInformation($"Received message from {topic}: {message}");

                await HandleMessage(topic, message);
            }
            catch (Exception ex)
            {
                logger.LogError($"Error consuming message: {ex.Message}");
            }
    }

    private async Task HandleMessage(string topic, string message)
    {
        using var scope = serviceProvider.CreateScope();

        var handlers = scope.ServiceProvider.GetServices<object>()
            .Where(h => h.GetType().GetCustomAttribute<KafkaHandlerAttribute>() != null)
            .ToList();

        foreach (var handler in handlers)
        {
            var methods = handler.GetType().GetMethods()
                .Where(m => m.GetCustomAttribute<KafkaMessageHandlerAttribute>()?.Topic == topic);

            foreach (var method in methods)
            {
                var parameters = method.GetParameters();
                if (parameters.Length != 1) continue;

                var paramType = parameters[0].ParameterType;

                var deserializedMessage = JsonSerializer.Deserialize(message, paramType);

                if (deserializedMessage == null) continue;

                var result = method.Invoke(handler, [deserializedMessage]);

                if (result is Task task) await task;
            }
        }
    }
}