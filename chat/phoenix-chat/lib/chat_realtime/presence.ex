defmodule ChatRealtime.Presence do
  @moduledoc """
  Presence tracking cho user online/offline theo từng room và theo user_id global.
  Dùng CRDT-based tracking của Phoenix.Presence - tự động sync giữa các node
  nếu sau này scale ra nhiều node (không cần Redis/external store).
  """
  use Phoenix.Presence,
    otp_app: :chat_realtime,
    pubsub_server: ChatRealtime.PubSub
end
