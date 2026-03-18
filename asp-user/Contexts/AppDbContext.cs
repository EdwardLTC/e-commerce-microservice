using asp_user.Models;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.ChangeTracking;

namespace asp_user.Contexts;

public class AppDbContext(DbContextOptions<AppDbContext> options) : DbContext(options)
{
	public DbSet<User> Users { get; set; }
	public DbSet<OutboxMessage> OutboxMessages { get; set; }

	public override int SaveChanges()
	{
		ApplyChangeTrackerHook();
		return base.SaveChanges();
	}

	public override async Task<int> SaveChangesAsync(CancellationToken cancellationToken = default(CancellationToken))
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

		modelBuilder.Entity<OutboxMessage>()
			.Property(o => o.Id)
			.HasDefaultValueSql("gen_random_uuid()");

		modelBuilder.Entity<OutboxMessage>()
			.Property(o => o.Attempts)
			.HasDefaultValue(0);

		modelBuilder.Entity<OutboxMessage>()
			.Property(o => o.Status)
			.HasDefaultValue(OutboxStatus.Pending);

		modelBuilder.Entity<OutboxMessage>()
			.HasIndex(o => new
			{
				o.AggregateId,
				o.Topic
			})
			.IsUnique();

		modelBuilder.Entity<OutboxMessage>()
			.HasIndex(o => new
			{
				o.Status,
				o.CreatedAt
			});
	}

	void ApplyChangeTrackerHook()
	{
		foreach (EntityEntry<IHasTimestamps> entry in ChangeTracker.Entries<IHasTimestamps>())
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
