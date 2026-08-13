defmodule ChatRealtime.Message do
  import Bitwise

  @required_fields ~w(room_id sender_id body)a

  @type attrs :: %{
          optional(:message_id) => String.t() | nil,
          optional(:room_id) => String.t() | nil,
          optional(:conversation_id) => String.t() | nil,
          optional(:sender_id) => String.t() | nil,
          optional(:body) => String.t() | nil,
          optional(String.t()) => String.t() | nil
        }

  @type message :: %{
          message_id: String.t(),
          room_id: String.t(),
          sender_id: String.t(),
          body: String.t(),
          sent_at: String.t()
        }

  @type build_error ::
          :invalid_payload
          | :body_too_long
          | :invalid_body
          | {:missing_fields, [:room_id | :sender_id | :body, ...]}

  @type build_result :: {:ok, message()} | {:error, build_error()}

  @spec build(attrs()) :: build_result()
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

  @spec build(term()) :: {:error, :invalid_payload}
  def build(_attrs), do: {:error, :invalid_payload}

  @spec normalize(attrs()) :: %{
          message_id: String.t() | nil,
          room_id: String.t() | nil,
          sender_id: String.t() | nil,
          body: String.t() | nil
        }
  defp normalize(attrs) do
    %{
      message_id: get(attrs, :message_id),
      room_id: get(attrs, :conversation_id) || get(attrs, :room_id),
      sender_id: get(attrs, :sender_id),
      body: get(attrs, :body)
    }
  end

  @spec get(attrs(), atom()) :: term()
  defp get(attrs, key), do: Map.get(attrs, key) || Map.get(attrs, Atom.to_string(key))

  @spec require_fields(map()) ::
          :ok
          | {:error, {:missing_fields, [:room_id | :sender_id | :body, ...]}}
  defp require_fields(attrs) do
    missing =
      @required_fields
      |> Enum.reject(fn field -> present?(Map.get(attrs, field)) end)

    case missing do
      [] -> :ok
      fields -> {:error, {:missing_fields, fields}}
    end
  end

  @spec validate_body(String.t()) :: :ok | {:error, :body_too_long}
  defp validate_body(body) when is_binary(body) do
    if String.length(String.trim(body)) <= 4_000 do
      :ok
    else
      {:error, :body_too_long}
    end
  end

  @spec validate_body(term()) :: {:error, :invalid_body}
  defp validate_body(_body), do: {:error, :invalid_body}

  @spec present?(term()) :: boolean()
  defp present?(value) when is_binary(value), do: String.trim(value) != ""
  defp present?(nil), do: false
  defp present?(_value), do: true

  @spec uuid() :: String.t()
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
