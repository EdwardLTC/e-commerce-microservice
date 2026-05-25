defmodule ChatRealtime.Application do
  use Application

  @impl true
  def start(_type, _args) do
    children = [
      {Phoenix.PubSub, name: ChatRealtime.PubSub},
      {Finch, name: ChatRealtime.Finch},
      ChatRealtime.KafkaProducer,
      ChatRealtimeWeb.Endpoint
    ]

    opts = [strategy: :one_for_one, name: ChatRealtime.Supervisor]
    Supervisor.start_link(children, opts)
  end

  @impl true
  def config_change(changed, _new, removed) do
    ChatRealtimeWeb.Endpoint.config_change(changed, removed)
    :ok
  end
end
