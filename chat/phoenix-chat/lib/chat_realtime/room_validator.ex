defmodule ChatRealtime.RoomValidator do
  @moduledoc """
  Simple HTTP client to resolve and validate chat rooms via the message-writer API.
  """

  @timeout 5_000

  def resolve_room(user_id, peer_user_id) do
    request_json("/v1/rooms/resolve", %{user_id: user_id, peer_user_id: peer_user_id})
  end

  def validate_room(room_id, user_id) do
    case request_json("/v1/rooms/validate", %{room_id: room_id, user_id: user_id}) do
      {:ok, %{"valid" => true}} -> {:ok, room_id}
      {:error, reason} -> {:error, reason}
      {:ok, %{"error" => error}} -> {:error, error}
      {:ok, _} -> {:error, :invalid_response}
    end
  end

  defp request_json(path, payload) do
    base =
      Application.get_env(:chat_realtime, :message_writer_validator_url, "http://localhost:8080")

    url = base <> path
    body = Jason.encode!(payload)

    case Finch.build(:post, url, [{"content-type", "application/json"}], body)
         |> Finch.request(ChatRealtime.Finch, receive_timeout: @timeout) do
      {:ok, %Finch.Response{status: 200, body: resp}} ->
        Jason.decode(resp)

      {:ok, %Finch.Response{status: status, body: resp}} ->
        case Jason.decode(resp) do
          {:ok, %{"error" => err}} -> {:error, err}
          _ -> {:error, {:http_error, status}}
        end

      {:error, reason} ->
        {:error, reason}
    end
  end
end
