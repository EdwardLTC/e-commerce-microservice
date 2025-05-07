using JetBrains.Annotations;

namespace asp_user.Attributes;

[AttributeUsage(AttributeTargets.Method)]
[MeansImplicitUse(ImplicitUseTargetFlags.WithMembers)]
public class KafkaMessageHandlerAttribute(string topic, Type messageType) : Attribute
{
    public string Topic => topic;
    public Type MessageType => messageType;
}