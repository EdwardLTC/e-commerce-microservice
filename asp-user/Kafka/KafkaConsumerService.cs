using System.Reflection;
using System.Text.Json;
using asp_user.Attributes;
using Confluent.Kafka;
using Microsoft.Extensions.Options;

namespace asp_user.Kafka;

public sealed class KafkaConsumerService(
    IOptions<ConsumerConfig> consumerConfig,
    ILogger<KafkaConsumerService> logger,
    IServiceProvider serviceProvider)
    : BackgroundService
{
    private static readonly JsonSerializerOptions jsonOptions = new()
    {
        PropertyNameCaseInsensitive = true,
        AllowTrailingCommas = true
    };

    private readonly IConsumer<Ignore, string> consumer =
        new ConsumerBuilder<Ignore, string>(new ConsumerConfig(consumerConfig.Value))
            .SetErrorHandler((_, e) => logger.LogError($"Kafka Consumer Error: {e.Reason}"))
            .SetLogHandler((_, _) => { })
            .Build();

    private readonly Dictionary<string, List<HandlerInfo>> topicHandlers = [];


    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        DiscoverHandlers();

        consumer.Subscribe(topicHandlers.Keys);

        logger.LogInformation("Kafka consumer service is running. Listening to topics: {Topics}",
            string.Join(", ", topicHandlers.Keys));

        _ = Task.Run(() => StartConsumptionLoop(stoppingToken), stoppingToken);

        while (!stoppingToken.IsCancellationRequested) await Task.Delay(Timeout.Infinite, stoppingToken);
    }

    private async Task StartConsumptionLoop(CancellationToken stoppingToken)
    {
        try
        {
            while (!stoppingToken.IsCancellationRequested)
                try
                {
                    var consumeResult = consumer.Consume(stoppingToken);
                    await ProcessMessageAsync(consumeResult);
                }
                catch (ConsumeException ex)
                {
                    logger.LogError(ex, "Kafka consume error");
                }
        }
        finally
        {
            consumer.Close();
        }
    }

    private async Task ProcessMessageAsync(ConsumeResult<Ignore, string> consumeResult)
    {
        var topic = consumeResult.Topic;

        if (!topicHandlers.TryGetValue(topic, out var handlers)) return;

        foreach (var handlerInfo in handlers)
        {
            using var scope = serviceProvider.CreateScope();
            var handler = scope.ServiceProvider.GetRequiredService(handlerInfo.Method.DeclaringType!);

            try
            {
                var message = JsonSerializer.Deserialize(
                    consumeResult.Message.Value,
                    handlerInfo.MessageType,
                    jsonOptions
                ) ?? throw new InvalidOperationException("Deserialization returned null");

                var result = handlerInfo.Method.Invoke(handler, [message]);
                if (result is Task task)
                    await task.ConfigureAwait(false);
            }
            catch (Exception ex)
            {
                logger.LogError(ex, "Error processing message for topic {Topic}", topic);
            }
        }
    }

    private void DiscoverHandlers()
    {
        var assembly = Assembly.GetEntryAssembly()!;
        foreach (var type in assembly.GetTypes())
        foreach (var method in type.GetMethods())
        {
            var attribute = method.GetCustomAttribute<KafkaMessageHandlerAttribute>();
            if (attribute == null) continue;

            ValidateMethodSignature(method, attribute.MessageType);

            if (!topicHandlers.TryGetValue(attribute.Topic, out var handlers))
            {
                handlers = [];
                topicHandlers[attribute.Topic] = handlers;
            }

            handlers.Add(new HandlerInfo(method, attribute.MessageType));
        }
    }

    private static void ValidateMethodSignature(MethodInfo method, Type messageType)
    {
        var parameters = method.GetParameters();

        if (parameters.Length != 1 || parameters[0].ParameterType != messageType)
            throw new InvalidOperationException(
                $"Method {method.Name} must have a single parameter of type {messageType.Name}.");
    }


    public override void Dispose()
    {
        consumer.Close();
        consumer.Dispose();
        base.Dispose();
    }

    private sealed record HandlerInfo(MethodInfo Method, Type MessageType);
}