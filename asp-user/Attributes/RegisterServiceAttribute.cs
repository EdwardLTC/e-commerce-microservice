namespace asp_user.Attributes;

[AttributeUsage(AttributeTargets.Class)]
public class RegisterServiceAttribute(ServiceLifetime lifetime = ServiceLifetime.Scoped) : Attribute
{
    public ServiceLifetime Lifetime => lifetime;
}