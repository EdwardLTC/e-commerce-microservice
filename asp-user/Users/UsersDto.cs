using System.ComponentModel.DataAnnotations;
using asp_user.Attributes;

namespace asp_user.Users;

public class UpdateUserDto
{
    [MaxLength(50)] public string? Name { get; set; }
    [EmailAddress] public string? Email { get; set; }

    [ValidateIf("Password")]
    [StringLength(20, MinimumLength = 8)]
    public string? OldPassword { get; set; }

    [StringLength(20, MinimumLength = 8)] public string? Password { get; set; } = null!;
}

public class UserProfileDto
{
    public int Id { get; set; }
    public string Name { get; set; } = null!;
    public string Email { get; set; } = null!;
}

public class GetUserDto
{
    public int Id { get; set; }
}