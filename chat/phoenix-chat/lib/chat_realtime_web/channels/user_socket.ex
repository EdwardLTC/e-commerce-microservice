defmodule ChatRealtimeWeb.UserSocket do
  use Phoenix.Socket

  channel("users:*", ChatRealtimeWeb.UserChannel)
  channel("rooms:*", ChatRealtimeWeb.UserChannel)

  @impl true
  def connect(%{"user_id" => user_id}, socket, _connect_info) when is_binary(user_id) do
    {:ok, assign(socket, :user_id, user_id)}
  end

  def connect(_params, _socket, _connect_info), do: :error

  @impl true
  def id(socket), do: "users_socket:#{socket.assigns.user_id}"
end
