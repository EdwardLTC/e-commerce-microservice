defmodule ChatRealtime.Message do
  @moduledoc """
  Struct + validation cho message trước khi broadcast/persist.
  Dùng @enforce_keys để đảm bảo không có field nào bị nil sau khi build thành công
  -> đây chính là fix cho lỗi Dialyzer invalid_contract trước đó.
  """

  @derive {Jason.Encoder,
           only: [:message_id, :room_id, :sender_id, :body, :sent_at, :client_message_id]}
  @enforce_keys [:message_id, :room_id, :sender_id, :body, :sent_at]
  defstruct [:message_id, :room_id, :sender_id, :body, :sent_at, :client_message_id]

  @type message :: %__MODULE__{
          message_id: String.t(),
          room_id: String.t(),
          sender_id: String.t(),
          body: String.t(),
          sent_at: String.t(),
          client_message_id: String.t() | nil
        }

  @spec build(map()) :: {:ok, message()} | {:error, :invalid_payload}
  def build(%{"room_id" => room_id, "sender_id" => sender_id, "content" => body} = payload)
      when is_binary(room_id) and room_id != "" and
             is_binary(sender_id) and sender_id != "" and
             is_binary(body) and body != "" do
    {:ok,
     %__MODULE__{
       message_id: Uniq.UUID.uuid7(),
       room_id: room_id,
       sender_id: sender_id,
       body: body,
       sent_at: DateTime.utc_now() |> DateTime.to_iso8601(),
       client_message_id: Map.get(payload, "client_message_id")
     }}
  end

  def build(_invalid_payload), do: {:error, :invalid_payload}
end
