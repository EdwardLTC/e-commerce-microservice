using asp_user.Attributes;
using asp_user.Contexts;
using asp_user.Models;
using Grpc.Core;
using Microsoft.EntityFrameworkCore;

namespace asp_user.Users;

[GrpcService]
public class UsersService(AppDbContext dbContext) : IUserService.IUserServiceBase
{
    public override async Task<UserProfile> GetUserById(GetUserByIdRequest request, ServerCallContext context)
    {
        return await dbContext.Users.Where(u => u.Id == request.Id).Select(u => new UserProfile
               {
                   Id = u.Id,
                   Email = u.Email,
                   Name = u.Name
               }).FirstOrDefaultAsync() ??
               throw new RpcException(new Status(StatusCode.NotFound, "User not found"));
    }

    public override async Task<UserProfile> CreateUser(CreateUserRequest request, ServerCallContext context)
    {
        var user = new User
        {
            Email = request.Email,
            Name = request.Name,
            Password = BCrypt.Net.BCrypt.HashPassword(request.Password)
        };

        dbContext.Users.Add(user);
        await dbContext.SaveChangesAsync();

        return new UserProfile
        {
            Id = user.Id,
            Email = user.Email,
            Name = user.Name
        };
    }
}