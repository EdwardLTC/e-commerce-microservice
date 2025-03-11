using System.ComponentModel.DataAnnotations;
using System.Reflection;

namespace asp_user.Attributes;

[AttributeUsage(AttributeTargets.Property)]
public class ValidateIfAttribute(string conditionalProperty, object? expectedValue = null) : ValidationAttribute
{
    protected override ValidationResult? IsValid(object? value, ValidationContext validationContext)
    {
        var propertyInfo = validationContext.ObjectType.GetProperty(conditionalProperty,
            BindingFlags.Public | BindingFlags.Instance);

        if (propertyInfo == null) return new ValidationResult($"Property '{conditionalProperty}' not found.");

        var conditionalValue = propertyInfo.GetValue(validationContext.ObjectInstance);

        var conditionMet = expectedValue == null
            ? conditionalValue != null
            : conditionalValue?.Equals(expectedValue) == true;

        if (conditionMet && string.IsNullOrEmpty(value?.ToString()))
            return new ValidationResult(ErrorMessage ??
                                        $"{validationContext.DisplayName} is required when {conditionalProperty} is set to {expectedValue ?? "not null"}.");

        return ValidationResult.Success;
    }
}