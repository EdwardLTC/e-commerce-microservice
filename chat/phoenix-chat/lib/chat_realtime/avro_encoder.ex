defmodule ChatRealtime.AvroEncoder do
	@moduledoc """
	Encode ChatRealtime.Message thành Avro binary trước khi publish Kafka.
	Dùng schemas cục bộ ở priv/schemas/message.avsc (namespace io.chatrealtime).
	"""

	alias ChatRealtime.Message

	@schema_name "io.chatrealtime.Message"

	@spec encode(Message.message()) :: {:ok, binary()} | {:error, term()}
	def encode(%Message{} = message) do
		payload = %{
			"message_id" => message.message_id,
			"room_id" => message.room_id,
			"sender_id" => message.sender_id,
			"body" => message.body,
			"sent_at" => message.sent_at,
			"client_message_id" => message.client_message_id
		}

		Avrora.encode(payload, schema_name: @schema_name)
	end

	@spec decode(binary()) :: {:ok, map()} | {:error, term()}
	def decode(binary) do
		Avrora.decode(binary, schema_name: @schema_name)
	end
end