using JetBrains.Annotations;

namespace asp_user.Attributes;

[AttributeUsage(AttributeTargets.Class, Inherited = false)]
[MeansImplicitUse(ImplicitUseTargetFlags.WithMembers)]
public class GrpcServiceAttribute : Attribute
{
}