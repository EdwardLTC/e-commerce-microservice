using System.Reflection;
using asp_user.Attributes;

namespace asp_user.Extensions;

public static class KafkaHandlerRegistrationExtensions
{
    public static IServiceCollection AddKafkaHandlers(this IServiceCollection services, Assembly assembly)
    {
        var handlerTypes = assembly.GetTypes()
            .Where(t => t.GetCustomAttribute<KafkaHandlerAttribute>() != null);

        foreach (var type in handlerTypes) services.AddTransient(type);

        return services;
    }
}