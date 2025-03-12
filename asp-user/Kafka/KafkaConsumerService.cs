using System.Reflection;
using System.Text.Json;
using asp_user.Attributes;
using Confluent.Kafka;

namespace asp_user.Kafka;

public class KafkaConsumerService(
    IConfiguration configuration,
    ILogger<KafkaConsumerService> logger,
    IServiceScopeFactory serviceScopeFactory)
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
        using var scope = serviceScopeFactory.CreateScope();

        var handlerMethods = DiscoverHandlerMethods(scope.ServiceProvider);

        if (handlerMethods.Count == 0)
        {
            logger.LogWarning("No Kafka handler found. Consumer will not subscribe.");
            return;
        }

        var topics = handlerMethods.Select(m => m.Topic).Distinct().ToList();

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
                var consumeResult = consumer.Consume(stoppingToken);
                if (consumeResult == null) continue;

                logger.LogInformation($"Received message from {consumeResult.Topic}: {consumeResult.Message.Value}");
                await HandleMessage(consumeResult.Topic, consumeResult.Message.Value, handlerMethods);
            }
            catch (OperationCanceledException)
            {
                logger.LogInformation("Kafka consumer is shutting down...");
            }
            catch (Exception ex)
            {
                logger.LogError($"Error consuming message: {ex.Message}");
            }
    }

    private List<(Type HandlerType, MethodInfo Method, string Topic)> DiscoverHandlerMethods(
        IServiceProvider serviceProvider)
    {
        var handlers = serviceProvider.GetServices<object>()
            .Where(h => h.GetType().GetCustomAttribute<KafkaHandlerAttribute>() != null)
            .ToList();

        var handlerMethods = new List<(Type, MethodInfo, string)>();

        foreach (var handler in handlers)
        {
            var methods = handler.GetType().GetMethods()
                .Select(m => (Method: m, Attribute: m.GetCustomAttribute<KafkaMessageHandlerAttribute>()))
                .Where(m => m.Attribute != null);

            foreach (var (method, attribute) in methods)
                handlerMethods.Add((handler.GetType(), method, attribute!.Topic));
        }

        return handlerMethods;
    }

    private async Task HandleMessage(string topic, string message,
        List<(Type HandlerType, MethodInfo Method, string Topic)> handlerMethods)
    {
        using var scope = serviceScopeFactory.CreateScope();

        foreach (var (handlerType, method, handlerTopic) in handlerMethods)
        {
            if (handlerTopic != topic) continue;

            var handler = scope.ServiceProvider.GetRequiredService(handlerType);

            var parameters = method.GetParameters();
            if (parameters.Length != 1)
            {
                logger.LogWarning($"Handler method {method.Name} must have exactly one parameter.");
                continue;
            }

            try
            {
                var paramType = parameters[0].ParameterType;
                var deserializedMessage = JsonSerializer.Deserialize(message, paramType);

                if (deserializedMessage == null)
                {
                    logger.LogWarning($"Failed to deserialize message for handler {method.Name}.");
                    continue;
                }

                var result = method.Invoke(handler, [deserializedMessage]);

                if (result is Task task) await task;
            }
            catch (Exception ex)
            {
                logger.LogError($"Error invoking handler {method.Name}: {ex.Message}");
            }
        }
    }

    public override void Dispose()
    {
        consumer.Close();
        consumer.Dispose();
        base.Dispose();
    }
}