using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace asp_user.Models;

[Table("users")]
[Index(nameof(Email), IsUnique = true)]
public class User : BaseModel<Guid>
{
    [Required]
    [MaxLength(50)]
    [EmailAddress]
    [Column("email")]
    public string Email { get; set; } = null!;

    [Required] [Column("password")] public string Password { get; set; } = null!;

    [MaxLength(50)] [Column("name")] public string Name { get; set; } = string.Empty;
}