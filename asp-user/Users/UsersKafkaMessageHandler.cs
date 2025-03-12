using asp_user.Attributes;

namespace asp_user.Users;

[KafkaHandler]
public class UsersKafkaMessageHandler
{
    [KafkaMessageHandler("user.get")]
    public Task<UserProfileDto> GetUserById(GetUserDto dto)
    {
        Console.WriteLine($"GetUserById: {dto.Id}");
        return Task.FromResult(new UserProfileDto
        {
            Id = dto.Id,
            Name = "John Doe",
            Email = ""
        });
    }
}