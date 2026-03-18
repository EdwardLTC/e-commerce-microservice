namespace asp_user.Outbox;

public class OutboxOptions
{
    public int BatchSize { get; set; } = 20;
    public int PollIntervalSeconds { get; set; } = 2;
    public int RetryBackoffSeconds { get; set; } = 10;
    public int MaxAttempts { get; set; } = 10;

    public TimeSpan PollInterval => TimeSpan.FromSeconds(PollIntervalSeconds);
    public TimeSpan RetryBackoff => TimeSpan.FromSeconds(RetryBackoffSeconds);
}
