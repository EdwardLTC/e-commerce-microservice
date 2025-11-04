using asp_user.Attributes;
using asp_user.Contexts;
using asp_user.Exceptions;
using asp_user.Models;
using Com.Ecommerce.Aspnet.User;
using Grpc.Core;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.ChangeTracking;

namespace asp_user.Services;

[GrpcService]
public class UsersService(AppDbContext dbContext) : UserService.UserServiceBase
{
	public async Task<UserProfile?> GetUserById(Guid id)
	{
		return await dbContext.Users.Where(u => u.Id.Equals(id)).Select(u => new UserProfile
		{
			Id = u.Id.ToString(),
			Email = u.Email,
			Name = u.Name
		}).FirstOrDefaultAsync();
	}


	public override async Task<UserProfile> GetUserById(GetUserByIdRequest request, ServerCallContext context)
	{
		return await GetUserById(Guid.Parse(request.Id)) ??
		       throw new RpcException(new Status(StatusCode.NotFound, "User not found"));
	}

	public override async Task<UserProfile> GetUserByEmailAndPassword(GetUserByEmailAndPasswordRequest request,
		ServerCallContext context)
	{
		var user = await dbContext.Users.Where(u => u.Email == request.Email).Select(u => new
		{
			u.Password,
			u.Id,
			u.Email,
			u.Name
		}).FirstOrDefaultAsync();

		if (user == null) throw new RpcException(new Status(StatusCode.NotFound, "User not found"));

		if (!BCrypt.Net.BCrypt.Verify(request.Password, user.Password))
			throw new RpcException(new Status(StatusCode.Unauthenticated, "Invalid password"));

		return new UserProfile
		{
			Id = user.Id.ToString(),
			Email = user.Email,
			Name = user.Name
		};
	}

	public override async Task<UserProfile> CreateUser(CreateUserRequest request, ServerCallContext context)
	{
		var existingUser = await dbContext.Users.Where(u => u.Email == request.Email).Select(u => new
		{
			u.Id
		}).FirstOrDefaultAsync();

		if (existingUser != null) throw new RpcException(new Status(StatusCode.AlreadyExists, "User already exists"));

		EntityEntry<User> CreatedUser = dbContext.Users.Add(new User
		{
			Email = request.Email,
			Password = BCrypt.Net.BCrypt.HashPassword(request.Password),
			Name = request.Name
		});

		await dbContext.SaveChangesAsync();

		return new UserProfile
		{
			Id = CreatedUser.Entity.Id.ToString(),
			Email = CreatedUser.Entity.Email,
			Name = CreatedUser.Entity.Name
		};
	}

	public override async Task<UserProfile> ChangePassword(ChangePasswordRequest request, ServerCallContext context)
	{
		var user = await dbContext.Users.Where(u => u.Id.Equals(Guid.Parse(request.Id))).Select(u => new
		{
			u.Password,
			u.Id,
			u.Email,
			u.Name
		}).FirstOrDefaultAsync();

		if (user == null) throw new RpcException(new Status(StatusCode.NotFound, "User not found"));

		if (!BCrypt.Net.BCrypt.Verify(request.OldPassword, user.Password))
			throw new RpcException(new Status(StatusCode.Unauthenticated, "Invalid password"));

		EntityEntry<User> updatedUser = dbContext.Users.Update(new User
		{
			Id = user.Id,
			Email = user.Email,
			Password = BCrypt.Net.BCrypt.HashPassword(request.NewPassword),
			Name = user.Name
		});

		await dbContext.SaveChangesAsync();

		return new UserProfile
		{
			Id = updatedUser.Entity.Id.ToString(),
			Email = updatedUser.Entity.Email,
			Name = updatedUser.Entity.Name
		};
	}

	public async Task<User> DecreaseWalletAmountAsync(Guid userId, decimal amount)
	{
		User? user = await dbContext.Users.FirstOrDefaultAsync(u => u.Id == userId);
		if (user == null)
		{
			throw new InsufficientBalanceException(0, amount);
		}

		if (amount <= 0 || user.Wallet <= amount)
		{
			throw new InsufficientBalanceException(user.Wallet, amount);
		}

		user.Wallet -= amount;
		dbContext.Users.Update(user);
		await dbContext.SaveChangesAsync();
		return user;
	}
}
