using JetBrains.Annotations;

namespace asp_user.Attributes;

[AttributeUsage(AttributeTargets.Method, AllowMultiple = true), MeansImplicitUse(ImplicitUseKindFlags.Access)]
public class KafkaTopicAttribute : Attribute
{

	public KafkaTopicAttribute(string topic)
	{
		Topic = topic;
	}

	public KafkaTopicAttribute(string topic, Type messageType)
	{
		Topic = topic;
		MessageType = messageType;
	}
	public string Topic { get; }
	public Type? MessageType { get; }
}
