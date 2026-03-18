using asp_user.Contexts;
using asp_user.Kafka;
using asp_user.Models;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Options;

namespace asp_user.Outbox;

public sealed class OutboxDispatchService(
	AppDbContext dbContext,
	KafkaProducerService producer,
	IOptions<OutboxOptions> options,
	ILogger<OutboxDispatchService> logger)
{
	public async Task<int> DispatchBatchAsync(CancellationToken stoppingToken)
	{
		DateTime retryCutoff = DateTime.UtcNow - options.Value.RetryBackoff;

		List<OutboxMessage> messages = await dbContext.OutboxMessages
			.Where(m => m.Status == OutboxStatus.Pending ||
			            m.Status == OutboxStatus.Failed && m.Attempts < options.Value.MaxAttempts &&
			            m.UpdatedAt <= retryCutoff)
			.OrderBy(m => m.CreatedAt)
			.Take(options.Value.BatchSize)
			.ToListAsync(stoppingToken);

		if (messages.Count == 0)
		{
			return 0;
		}

		foreach (OutboxMessage message in messages)
		{
			message.Attempts += 1;

			try
			{
				await producer.ProduceBytesAsync(message.Topic, message.Key, message.Payload);
				message.Status = OutboxStatus.Sent;
				message.LastError = null;
			}
			catch (Exception ex)
			{
				message.Status = OutboxStatus.Failed;
				message.LastError = ex.Message;
				logger.LogError(ex, "Outbox publish failed for {OutboxId} on {Topic}", message.Id, message.Topic);
			}
		}

		await dbContext.SaveChangesAsync(stoppingToken);
		return messages.Count;
	}
}
