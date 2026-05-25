defmodule ChatRealtimeWeb.Router do
  use Phoenix.Router, helpers: false

  pipeline :api do
    plug(:accepts, ["json"])
  end

  scope "/", ChatRealtimeWeb do
    pipe_through(:api)

    get("/health", HealthController, :show)
  end
end
