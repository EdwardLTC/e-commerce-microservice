defmodule ChatRealtime.KafkaProducer do
  @moduledoc """
  A GenServer that acts as a Kafka producer for publishing chat messages to a Kafka topic.
  """
  use GenServer

  require Logger

  alias ChatRealtime.Avro.ChatMessageSent

  @client_id :chat_realtime_kafka_client

  @spec start_link(any()) :: :ignore | {:error, any()} | {:ok, pid()}
  def start_link(_opts) do
    GenServer.start_link(__MODULE__, [], name: __MODULE__)
  end

  @spec publish_message(ChatRealtime.Message.message()) :: :ok | {:error, term()}
  def publish_message(message) do
    GenServer.call(__MODULE__, {:publish_message, message}, 5_000)
  end

  @impl true
  @spec init(any()) :: {:ok, %{topic: any()}} | {:stop, any()}
  def init(_opts) do
    kafka_config = Application.fetch_env!(:chat_realtime, :kafka)

    brokers =
      kafka_config
      |> Keyword.fetch!(:brokers)
      |> parse_brokers()

    topic = Keyword.fetch!(kafka_config, :topic)

    case :brod.start_client(
           brokers,
           @client_id,
           auto_start_producers: true
         ) do
      :ok ->
        Logger.info("Kafka producer started. brokers=#{inspect(brokers)}, topic=#{topic}")
        {:ok, %{topic: topic}}

      {:error, {:already_started, _pid}} ->
        {:ok, %{topic: topic}}

      {:error, reason} ->
        Logger.error("Failed to start Kafka client: #{inspect(reason)}")
        {:stop, reason}
    end
  end

  @impl true
  @spec handle_call({:publish_message, ChatRealtime.Message.message()}, GenServer.from(), %{
          topic: String.t()
        }) :: {:reply, :ok | {:error, term()}, %{topic: String.t()}}
  def handle_call({:publish_message, message}, _from, %{topic: topic} = state) do
    payload = ChatMessageSent.encode(message)
    key = message.room_id

    case :brod.produce_sync(@client_id, topic, :hash, key, payload) do
      :ok ->
        Logger.debug("Kafka message published topic=#{topic} room_id=#{key}")
        {:reply, :ok, state}

      {:error, reason} ->
        Logger.error(
          "Failed to publish Kafka message " <>
            "topic=#{topic} room_id=#{key} reason=#{inspect(reason)}"
        )

        {:reply, {:error, reason}, state}
    end
  end

  @spec parse_brokers(String.t()) :: [{charlist(), integer()}]
  defp parse_brokers(brokers) do
    brokers
    |> String.split(",", trim: true)
    |> Enum.map(fn broker ->
      [host, port] = String.split(broker, ":", parts: 2)

      {
        String.to_charlist(host),
        String.to_integer(port)
      }
    end)
  end
end
