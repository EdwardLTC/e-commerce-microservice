defmodule ChatRealtime.Message do
  import Bitwise

  @required_fields ~w(room_id sender_id body)a

  def build(attrs) when is_map(attrs) do
    normalized = normalize(attrs)

    with :ok <- require_fields(normalized),
         :ok <- validate_body(normalized.body) do
      now = DateTime.utc_now() |> DateTime.to_iso8601()

      {:ok,
       %{
         message_id: Map.get(normalized, :message_id) || uuid(),
         room_id: normalized.room_id,
         sender_id: normalized.sender_id,
         body: normalized.body,
         sent_at: now
       }}
    end
  end

  def build(_attrs), do: {:error, :invalid_payload}

  defp normalize(attrs) do
    %{
      message_id: get(attrs, :message_id),
      room_id: get(attrs, :conversation_id) || get(attrs, :room_id),
      sender_id: get(attrs, :sender_id),
      body: get(attrs, :body)
    }
  end

  defp get(attrs, key), do: Map.get(attrs, key) || Map.get(attrs, Atom.to_string(key))

  defp require_fields(attrs) do
    missing =
      @required_fields
      |> Enum.reject(fn field -> present?(Map.get(attrs, field)) end)

    case missing do
      [] -> :ok
      fields -> {:error, {:missing_fields, fields}}
    end
  end

  defp validate_body(body) when is_binary(body) do
    if String.length(String.trim(body)) <= 4_000 do
      :ok
    else
      {:error, :body_too_long}
    end
  end

  defp validate_body(_body), do: {:error, :invalid_body}

  defp present?(value) when is_binary(value), do: String.trim(value) != ""
  defp present?(nil), do: false
  defp present?(_value), do: true

  defp uuid do
    <<a::32, b::16, c::16, d::16, e::48>> = :crypto.strong_rand_bytes(16)

    c = bor(band(c, 0x0FFF), 0x4000)
    d = bor(band(d, 0x3FFF), 0x8000)

    [
      Base.encode16(<<a::32>>, case: :lower),
      Base.encode16(<<b::16>>, case: :lower),
      Base.encode16(<<c::16>>, case: :lower),
      Base.encode16(<<d::16>>, case: :lower),
      Base.encode16(<<e::48>>, case: :lower)
    ]
    |> Enum.join("-")
  end
end
