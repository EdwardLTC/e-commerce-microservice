using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace asp_user.Models;

public interface IHasTimestamps
{
    DateTime CreatedAt { get; set; }
    DateTime UpdatedAt { get; set; }
}

public class BaseModel<T> : IHasTimestamps
{
    [Key] [Column("id")] public T Id { get; set; } = default!;

    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    [Column("createdAt")]
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    [Column("updatedAt")] public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
}