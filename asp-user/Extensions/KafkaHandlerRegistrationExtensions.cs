using System.Reflection;
using asp_user.Attributes;

namespace asp_user.Extensions;

public static class KafkaHandlerRegistrationExtensions
{
    public static void AddKafkaHandlers(this IServiceCollection services, Assembly assembly)
    {
        var handlerTypes = assembly.GetTypes()
            .Where(t => t.GetCustomAttribute<KafkaHandlerAttribute>() is not null && !t.IsAbstract);

        foreach (var type in handlerTypes)
        {
            services.AddSingleton(type);
        }
    }
}