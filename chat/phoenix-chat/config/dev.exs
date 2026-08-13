import Config

config :chat_realtime, ChatRealtimeWeb.Endpoint,
  server: true,
  http: [ip: {0, 0, 0, 0}, port: String.to_integer(System.get_env("PORT") || "4001")],
  check_origin: false,
  code_reloader: false,
  debug_errors: true,
  secret_key_base: "development-secret-key-base-for-chat-realtime-service"

config :chat_realtime, :kafka,
  brokers: System.get_env("KAFKA_BROKERS", "192.168.64.3:9092"),
  topic: System.get_env("CHAT_MESSAGES_TOPIC", "chat.messages.v1")

config :phoenix, :plug_init_mode, :runtime

config :chat_realtime,
       :message_writer_validator_url,
       System.get_env("MESSAGE_WRITER_VALIDATOR_URL") || "http://localhost:8080"
