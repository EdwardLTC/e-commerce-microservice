defmodule ChatRealtime.Application do
  @moduledoc """
	The ChatRealtime application module is responsible for starting and supervising the application's processes. It defines the
	supervision tree, which includes the PubSub system, Finch HTTP client, Kafka producer, and the Phoenix endpoint. This module ensures that all necessary components are started and monitored for fault tolerance.
	"""
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
