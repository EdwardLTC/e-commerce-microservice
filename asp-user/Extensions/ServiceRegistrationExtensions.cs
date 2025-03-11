using System.Reflection;
using asp_user.Attributes;

namespace asp_user.Extensions;

public static class ServiceRegistrationExtensions
{
    public static void AddAutoRegisteredServices(this IServiceCollection services, Assembly assembly)
    {
        var typesWithAttribute = assembly.GetTypes()
            .Where(t => t.GetCustomAttribute<RegisterServiceAttribute>() != null);

        foreach (var type in typesWithAttribute)
        {
            var attribute = type.GetCustomAttribute<RegisterServiceAttribute>();

            if (attribute != null) services.Add(new ServiceDescriptor(type, type, attribute.Lifetime));
        }
    }
}