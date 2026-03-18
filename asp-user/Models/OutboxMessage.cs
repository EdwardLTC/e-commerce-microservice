using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace asp_user.Models;

[Table("outbox_messages")]
public class OutboxMessage : BaseModel<Guid>
{
    [Required, MaxLength(100), Column("aggregateId")]
    public string AggregateId { get; set; } = string.Empty;

    [Required, MaxLength(200), Column("topic")]
    public string Topic { get; set; } = string.Empty;

    [Required, MaxLength(200), Column("key")]
    public string Key { get; set; } = string.Empty;

    [Required, Column("payload")]
    public byte[] Payload { get; set; } = Array.Empty<byte>();

    [Column("status")]
    public OutboxStatus Status { get; set; } = OutboxStatus.Pending;

    [Column("attempts")]
    public int Attempts { get; set; }

    [Column("lastError")]
    public string? LastError { get; set; }

    public static OutboxMessage Create(string topic, string key, string aggregateId, byte[] payload)
    {
        return new OutboxMessage
        {
            Topic = topic,
            Key = key,
            AggregateId = aggregateId,
            Payload = payload,
            Status = OutboxStatus.Pending,
            Attempts = 0
        };
    }
}

public enum OutboxStatus
{
    Pending = 0,
    Sent = 1,
    Failed = 2
}
