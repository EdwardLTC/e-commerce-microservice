defmodule ChatRealtimeWeb.UserChannel do
  use Phoenix.Channel

  alias ChatRealtime.KafkaProducer
  alias ChatRealtime.Message
  alias ChatRealtimeWeb.Endpoint

  @impl true
  def join("users:" <> user_id, _payload, socket) do
    if socket.assigns.user_id == user_id do
      {:ok, socket}
    else
      {:error, %{reason: "forbidden"}}
    end
  end

  @impl true
  def join("rooms:" <> room_id, _payload, socket) do
    # Validate room with message-writer service before allowing the join.
    # temporarily disabled for testing, but should be re-enabled in production.

    # case ChatRealtime.RoomValidator.validate_room(room_id, socket.assigns.user_id) do
    #   {:ok, validated_room_id} ->
    #     {:ok, assign(socket, :room_id, validated_room_id)}

    #   {:error, _reason} ->
    #     {:error, %{reason: "forbidden"}}
    # end

    {:ok, assign(socket, :room_id, room_id)}
  end

  @impl true
  def handle_in("room:open", %{"peer_user_id" => peer_user_id}, socket) do
    # Validate room with message-writer service before allowing the join.
    # temporarily disabled for testing, but should be re-enabled in production.
    # case ChatRealtime.RoomValidator.resolve_room(socket.assigns.user_id, peer_user_id) do
    #   {:ok, %{"room_id" => room_id}} ->
    #     {:reply, {:ok, %{room_id: room_id, topic: "rooms:#{room_id}"}}, socket}

    #   {:ok, %{"error" => reason}} ->
    #     {:reply, {:error, %{reason: reason}}, socket}

    #   {:error, reason} ->
    #     {:reply, {:error, %{reason: inspect(reason)}}, socket}
    # end

    {:reply, {:ok, %{room_id: "test-room-id", topic: "rooms:test-room-id"}}, socket}
  end

  @impl true
  def handle_in("message:send", payload, socket) do
    case socket.assigns[:room_id] do
      nil ->
        {:reply, {:error, %{reason: "room_not_joined"}}, socket}

      room_id ->
        payload = Map.put(payload, "sender_id", socket.assigns.user_id)
        # ensure the room_id is present on the payload
        payload = Map.put_new(payload, "room_id", room_id)

        with {:ok, message} <- Message.build(payload),
             :ok <- deliver(message),
             :ok <- KafkaProducer.publish_message(message) do
          {:reply, {:ok, %{message: message, persistence: "queued"}}, socket}
        else
          {:error, reason} ->
            {:reply, {:error, %{reason: inspect(reason)}}, socket}
        end
    end
  end

  defp deliver(message) do
    Endpoint.broadcast("rooms:#{message.room_id}", "message:new", %{message: message})
  end
end
