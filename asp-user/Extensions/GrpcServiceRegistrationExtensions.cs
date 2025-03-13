using System.Reflection;
using asp_user.Attributes;

namespace asp_user.Extensions;

public static class GrpcServiceRegistrationExtensions
{
    public static IServiceCollection AddGrpcServices(this IServiceCollection services)
    {
        var serviceTypes = Assembly.GetExecutingAssembly()
            .GetTypes()
            .Where(t => t.IsClass && !t.IsAbstract && t.GetCustomAttribute<GrpcServiceAttribute>() != null)
            .ToList();

        foreach (var type in serviceTypes) services.AddScoped(type); // Use Scoped instead of Transient

        return services;
    }

    public static IEndpointRouteBuilder MapGrpcServices(this IEndpointRouteBuilder endpoints)
    {
        var grpcServices = Assembly.GetExecutingAssembly()
            .GetTypes()
            .Where(t => t.IsClass && !t.IsAbstract && t.GetCustomAttribute<GrpcServiceAttribute>() != null)
            .ToList();

        foreach (var grpcService in grpcServices)
        {
            var mapGrpcServiceMethod = typeof(GrpcEndpointRouteBuilderExtensions)
                .GetMethod(nameof(GrpcEndpointRouteBuilderExtensions.MapGrpcService))
                ?.MakeGenericMethod(grpcService);

            mapGrpcServiceMethod?.Invoke(null, new object[] { endpoints });
        }

        return endpoints;
    }
}