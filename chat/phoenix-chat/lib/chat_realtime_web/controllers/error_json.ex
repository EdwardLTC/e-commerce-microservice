defmodule ChatRealtimeWeb.ErrorJSON do
  def render(_template, _assigns) do
    %{error: "unexpected_error"}
  end
end
