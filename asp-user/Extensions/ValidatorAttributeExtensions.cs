using System.Reflection;
using asp_user.Attributes;
using FluentValidation;

namespace asp_user.Extensions;

public static class ValidatorAttributeExtensions
{
    public static IServiceCollection AddAttributedValidators(this IServiceCollection services,
        Assembly? assembly = null)
    {
        assembly ??= Assembly.GetCallingAssembly();

        var typesWithAttribute = assembly
            .GetTypes()
            .Where(t => t.GetCustomAttribute<RegisterValidatorAttribute>() != null)
            .Where(t => !t.IsAbstract && !t.IsInterface)
            .Where(t => t.GetInterfaces().Any(i =>
                i.IsGenericType &&
                i.GetGenericTypeDefinition() == typeof(IValidator<>)))
            .ToList();

        foreach (var validatorType in typesWithAttribute)
        {
            var interfaceType = validatorType
                .GetInterfaces()
                .First(i => i.IsGenericType && i.GetGenericTypeDefinition() == typeof(IValidator<>));

            services.AddScoped(interfaceType, validatorType);
        }

        return services;
    }
}