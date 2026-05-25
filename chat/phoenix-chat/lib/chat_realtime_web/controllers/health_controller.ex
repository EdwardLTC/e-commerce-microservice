defmodule ChatRealtimeWeb.HealthController do
  use Phoenix.Controller, formats: [:json]

  def show(conn, _params) do
    json(conn, %{status: "ok", service: "phoenix-chat"})
  end
end
