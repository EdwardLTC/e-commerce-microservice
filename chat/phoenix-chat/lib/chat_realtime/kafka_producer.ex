defmodule ChatRealtime.KafkaProducer do
  use GenServer

  require Logger

  alias ChatRealtime.Avro.ChatMessageSent

  @client_id :chat_realtime_kafka_client

  def start_link(_opts) do
    GenServer.start_link(__MODULE__, [], name: __MODULE__)
  end

  def publish_message(message) do
    GenServer.call(__MODULE__, {:publish_message, message}, 5_000)
  end

  @impl true
  def init(_opts) do
    kafka_config = Application.fetch_env!(:chat_realtime, :kafka)
    brokers = parse_brokers(Keyword.fetch!(kafka_config, :brokers))
    topic = Keyword.fetch!(kafka_config, :topic)

    case :brod.start_client(brokers, @client_id, auto_start_producers: true) do
      :ok ->
        {:ok, %{topic: topic}}

      {:error, {:already_started, _pid}} ->
        {:ok, %{topic: topic}}

      {:error, reason} ->
        {:stop, reason}
    end
  end

  @impl true
  def handle_call({:publish_message, message}, _from, %{topic: topic} = state) do
    payload = ChatMessageSent.encode(message)

    # use room_id as the partitioning key so Kafka partitions by room
    partition = -1
    key = Map.get(message, :room_id) || Map.get(message, "room_id")
    case :brod.produce_sync(@client_id, topic, partition, key, payload) do
      :ok ->
        {:reply, :ok, state}

      {:error, reason} ->
        Logger.error("failed to publish chat message: #{inspect(reason)}")
        {:reply, {:error, reason}, state}
    end
  end

  defp parse_brokers(brokers) do
    brokers
    |> String.split(",", trim: true)
    |> Enum.map(fn broker ->
      [host, port] = String.split(broker, ":", parts: 2)
      {String.to_charlist(host), String.to_integer(port)}
    end)
  end
end
