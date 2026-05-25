import Config

if config_env() == :prod do
  port = String.to_integer(System.get_env("PORT") || "4001")

  config :chat_realtime, ChatRealtimeWeb.Endpoint,
    http: [ip: {0, 0, 0, 0}, port: port],
    check_origin: System.get_env("PHX_CHECK_ORIGIN", "false") == "true",
    secret_key_base: System.fetch_env!("SECRET_KEY_BASE")

  config :chat_realtime, :kafka,
    brokers: System.get_env("KAFKA_BROKERS", "kafka:9092"),
    topic: System.get_env("CHAT_MESSAGES_TOPIC", "chat.messages.v1")
end
