defmodule ChatRealtimeWeb.UserChannel do
  @moduledoc """
  Channel xử lý cả kênh cá nhân "users:<id>" (notification riêng) và
  kênh room "rooms:<id>" (chat 1-1 hoặc group).
  """
  use Phoenix.Channel

  alias ChatRealtime.{
    BlockCache,
    KafkaProducer,
    MembershipCache,
    Message,
    RoomValidator,
    Presence
  }

  alias ChatRealtimeWeb.{Endpoint}

  # ---------- JOIN ----------

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
		after_join(room_id, socket)
#    user_id = socket.assigns.user_id
#
#    if MembershipCache.member?(room_id, user_id) do
#      after_join(room_id, socket)
#    else
#      # cache miss -> fallback validate qua service, rồi populate lại cache
#      case RoomValidator.validate_room(room_id, user_id) do
#        {:ok, member_ids} ->
#          MembershipCache.put_members(room_id, member_ids)
#          after_join(room_id, socket)
#
#        {:error, _reason} ->
#          {:error, %{reason: "forbidden"}}
#      end
#    end
  end

  defp after_join(room_id, socket) do
    socket = assign(socket, :room_id, room_id)
    send(self(), :track_presence)
    {:ok, socket}
  end

  @impl true
  def handle_info(:track_presence, socket) do
    {:ok, _} =
      Presence.track(socket, socket.assigns.user_id, %{online_at: System.system_time(:second)})

    push(socket, "presence_state", Presence.list(socket))
    {:noreply, socket}
  end

  # ---------- SEND MESSAGE ----------

  @impl true
  def handle_in("message:send", payload, socket) do
    case socket.assigns[:room_id] do
      nil ->
        {:reply, {:error, %{reason: "room_not_joined"}}, socket}

      room_id ->
        sender_id = socket.assigns.user_id
        payload = payload |> Map.put("sender_id", sender_id) |> Map.put_new("room_id", room_id)

        with :ok <- check_not_blocked(room_id, sender_id),
             {:ok, message} <- Message.build(payload),
             :ok <- deliver(message, socket),
						 {:ok, encoded} <- ChatRealtime.AvroEncoder.encode(message),
             :ok <- KafkaProducer.publish_message(encoded, room_id) do
          notify_recipients(message, socket)
          {:reply, {:ok, %{message: message, persistence: "queued"}}, socket}
        else
          {:error, reason} ->
            {:reply, {:error, %{reason: inspect(reason)}}, socket}
        end
    end
  end

  @impl true
  def handle_in("typing:start", _payload, socket) do
    broadcast_from(socket, "typing:diff", %{user_id: socket.assigns.user_id, typing: true})
    {:noreply, socket}
  end

  @impl true
  def handle_in("typing:stop", _payload, socket) do
    broadcast_from(socket, "typing:diff", %{user_id: socket.assigns.user_id, typing: false})
    {:noreply, socket}
  end

  @impl true
  def handle_in("message:read", %{"message_id" => message_id}, socket) do
    broadcast_from(socket, "message:read", %{
      user_id: socket.assigns.user_id,
      message_id: message_id
    })

    {:noreply, socket}
  end

  # ---------- INTERNAL ----------

  @spec check_not_blocked(String.t(), String.t()) :: :ok | {:error, :blocked}
  defp check_not_blocked(room_id, sender_id) do
    room_id
    |> MembershipCache.members_of()
    |> Enum.reject(&(&1 == sender_id))
    |> Enum.any?(&BlockCache.blocked?(sender_id, &1))
    |> if(do: {:error, :blocked}, else: :ok)
  end

  @spec deliver(Message.message(), Phoenix.Socket.t()) :: :ok
  defp deliver(message, socket) do
    broadcast_from(socket, "message:new", %{message: message})
  end

  # Notify riêng qua kênh cá nhân "users:<id>" cho các thành viên khác trong room -
  # để client tự động join "rooms:<id>" nếu chưa join (case chat lần đầu, room mới).
  @spec notify_recipients(Message.message(), Phoenix.Socket.t()) :: :ok
  defp notify_recipients(message, _socket) do
    message.room_id
    |> MembershipCache.members_of()
    |> Enum.reject(&(&1 == message.sender_id))
    |> Enum.each(fn recipient_id ->
      Endpoint.broadcast("users:#{recipient_id}", "room:invite", %{
        room_id: message.room_id,
        from: message.sender_id,
        preview: message.body
      })
    end)

    :ok
  end
end
