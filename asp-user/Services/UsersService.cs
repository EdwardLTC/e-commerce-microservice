using asp_user.Attributes;
using asp_user.Contexts;
using asp_user.Models;
using Grpc.Core;
using Microsoft.EntityFrameworkCore;

namespace asp_user.Services;

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
        var existingUser = await dbContext.Users.Where(u => u.Email == request.Email).Select(u => new
        {
            u.Id
        }).FirstOrDefaultAsync();

        if (existingUser != null) throw new RpcException(new Status(StatusCode.AlreadyExists, "User already exists"));

        var CreatedUser = dbContext.Users.Add(new User
        {
            Email = request.Email,
            Name = request.Name
        });

        await dbContext.SaveChangesAsync();

        return new UserProfile
        {
            Id = CreatedUser.Entity.Id,
            Email = CreatedUser.Entity.Email,
            Name = CreatedUser.Entity.Name
        };
    }
}