using System.Net;
using asp_user.Attributes;
using asp_user.Contexts;
using asp_user.exceptions;
using asp_user.Models;
using Microsoft.EntityFrameworkCore;

namespace asp_user.Users;

[RegisterService]
public class UsersService(AppDbContext dbContext)
{
    public async Task<UserProfileDto> GetUserById(int id)
    {
        return await dbContext.Users
                   .Where(u => u.Id == id)
                   .Select(u => new UserProfileDto
                   {
                       Id = u.Id,
                       Name = u.Name,
                       Email = u.Email
                   })
                   .FirstOrDefaultAsync()
               ?? throw new HttpException(HttpStatusCode.BadRequest, "User not found");
    }

    public async Task<User> CreateUser(User user)
    {
        var existingUser = await dbContext.Users.Where(u => u.Email == user.Email).Select(u => new
        {
            u.Id
        }).FirstOrDefaultAsync();

        if (existingUser != null) throw new HttpException(HttpStatusCode.BadRequest, "Email already exists");

        var CreatedUser = dbContext.Users.Add(user);
        await dbContext.SaveChangesAsync();
        return CreatedUser.Entity;
    }

    public async Task<UserProfileDto> UpdateUser(int id, UpdateUserDto user)
    {
        var entity = await dbContext.Users.FirstOrDefaultAsync(u => u.Id == id);
        if (entity == null) throw new HttpException(HttpStatusCode.BadRequest, "Users not found");


        entity.Name = user.Name ?? entity.Name;
        entity.Email = user.Email ?? entity.Email;

        if (user.Password != null)
        {
            if (!BCrypt.Net.BCrypt.Verify(user.OldPassword, entity.Password))
                throw new HttpException(HttpStatusCode.BadRequest, "Invalid password");

            entity.Password = BCrypt.Net.BCrypt.HashPassword(user.Password);
        }

        await dbContext.SaveChangesAsync();

        return new UserProfileDto
        {
            Id = entity.Id,
            Name = entity.Name,
            Email = entity.Email
        };
    }
}