defmodule ChatRealtime.Avro.ChatMessageSent do
  @moduledoc false

  import Bitwise

  @fields [:message_id, :room_id, :sender_id, :body, :sent_at]

  def encode(message) when is_map(message) do
    @fields
    |> Enum.map(fn field -> encode_string(Map.fetch!(message, field)) end)
    |> IO.iodata_to_binary()
  end

  defp encode_string(value) when is_binary(value) do
    [encode_long(byte_size(value)), value]
  end

  defp encode_long(value) when is_integer(value) and value >= 0 do
    value
    |> Kernel.*(2)
    |> encode_varint()
  end

  defp encode_varint(value) when value < 0x80, do: <<value>>

  defp encode_varint(value) do
    <<bor(band(value, 0x7F), 0x80)>> <> encode_varint(value >>> 7)
  end
end
