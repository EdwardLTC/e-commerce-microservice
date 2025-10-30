using asp_user.Models;
using Microsoft.EntityFrameworkCore;

namespace asp_user.Contexts;

public class AppDbContext(DbContextOptions<AppDbContext> options) : DbContext(options)
{
    public DbSet<User> Users { get; set; }

    public override int SaveChanges()
    {
        ApplyChangeTrackerHook();
        return base.SaveChanges();
    }

    public override async Task<int> SaveChangesAsync(CancellationToken cancellationToken = default)
    {
        ApplyChangeTrackerHook();
        return await base.SaveChangesAsync(cancellationToken);
    }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.HasDefaultSchema("users");

        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<User>()
            .Property(u => u.Id)
            .HasDefaultValueSql("gen_random_uuid()");
    }

    void ApplyChangeTrackerHook()
    {
        foreach (var entry in ChangeTracker.Entries<IHasTimestamps>())
            switch (entry.State)
            {
            case EntityState.Added:
                entry.Entity.CreatedAt = DateTime.UtcNow;
                entry.Entity.UpdatedAt = DateTime.UtcNow;
                break;

            case EntityState.Modified:
                entry.Entity.UpdatedAt = DateTime.UtcNow;
                break;
            }
    }
}