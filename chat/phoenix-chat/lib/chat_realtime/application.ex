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
      ChatRealtime.MembershipCache,
      ChatRealtime.Presence,
														Avrora,
      ChatRealtimeWeb.Endpoint
    ]

    opts = [strategy: :one_for_one, name: ChatRealtime.Supervisor]

    with {:ok, pid} <- Supervisor.start_link(children, opts) do
      print_test_client_url()
      {:ok, pid}
    end
  end

  defp print_test_client_url do
    if Application.get_env(:chat_realtime, :env) == :dev or Mix.env() == :dev do
      config = Application.get_env(:chat_realtime, ChatRealtimeWeb.Endpoint)
      port = get_in(config, [:http, :port]) || 4000

      IO.puts([
        IO.ANSI.green(),
        "\n🔗 Test client: http://localhost:#{port}/test_client.html\n",
        IO.ANSI.reset()
      ])
    end
  end

  @impl true
  def config_change(changed, _new, removed) do
    ChatRealtimeWeb.Endpoint.config_change(changed, removed)
    :ok
  end
end
