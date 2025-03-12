namespace asp_user.Attributes;

[AttributeUsage(AttributeTargets.Method)]
public class KafkaMessageHandlerAttribute(string topic) : Attribute
{
    public string Topic => topic;
}