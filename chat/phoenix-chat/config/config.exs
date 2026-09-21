import Config

config :chat_realtime, ChatRealtimeWeb.Endpoint,
  adapter: Bandit.PhoenixAdapter,
  url: [host: "localhost"],
  render_errors: [
    formats: [json: ChatRealtimeWeb.ErrorJSON],
    layout: false
  ],
  pubsub_server: ChatRealtime.PubSub,
  live_view: [signing_salt: "chat-realtime"]

config :logger, :console,
  format: "$time $metadata[$level] $message\n",
  metadata: [:request_id]

config :phoenix, :json_library, Jason

config :avrora,
  registry_url: nil,
  schemas_path: "priv/schemas"

if File.exists?("#{__DIR__}/#{config_env()}.exs") do
  import_config "#{config_env()}.exs"
end
