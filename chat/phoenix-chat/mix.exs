defmodule ChatRealtime.MixProject do
  use Mix.Project

  def project do
    [
      app: :chat_realtime,
      version: "0.1.0",
      elixir: "~> 1.17",
      elixirc_paths: elixirc_paths(Mix.env()),
      start_permanent: Mix.env() == :prod,
      deps: deps()
    ]
  end

  def application do
    [
      mod: {ChatRealtime.Application, []},
      extra_applications: [:logger, :runtime_tools]
    ]
  end

  defp elixirc_paths(:test), do: ["lib", "test/support"]
  defp elixirc_paths(_), do: ["lib"]

  defp deps do
    [
      {:phoenix, "~> 1.8.0"},
      {:bandit, "~> 1.8"},
      {:finch, "~> 0.16"},
      {:jason, "~> 1.4"},
      {:brod, "~> 4.6"},
      {:uniq, "~> 0.6"},
			{:avrora, "~> 0.29"},
      {:credo, "~> 1.7", only: [:dev, :test], runtime: false},
      {:dialyxir, "~> 1.4", only: [:dev], runtime: false}
    ]
  end
end
