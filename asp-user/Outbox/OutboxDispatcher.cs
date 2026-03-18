using Microsoft.Extensions.Options;

namespace asp_user.Outbox;

public sealed class OutboxDispatcher(IServiceScopeFactory scopeFactory, IOptions<OutboxOptions> options) : BackgroundService
{
	protected override async Task ExecuteAsync(CancellationToken stoppingToken)
	{
		while (!stoppingToken.IsCancellationRequested)
		{
			using IServiceScope scope = scopeFactory.CreateScope();
			OutboxDispatchService dispatchService = scope.ServiceProvider.GetRequiredService<OutboxDispatchService>();
			int dispatched = await dispatchService.DispatchBatchAsync(stoppingToken);
			if (dispatched == 0)
			{
				await Task.Delay(options.Value.PollInterval, stoppingToken);
			}
		}
	}
}
