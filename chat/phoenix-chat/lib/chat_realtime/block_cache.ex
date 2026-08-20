defmodule ChatRealtime.BlockCache do
  @moduledoc "Cache trạng thái block giữa 2 user, tương tự MembershipCache."
  use GenServer

  @table :block_cache

  def start_link(_opts), do: GenServer.start_link(__MODULE__, nil, name: __MODULE__)

  @impl true
  def init(_),
    do:
      (
        :ets.new(@table, [:named_table, :public, :set, read_concurrency: true])
        {:ok, nil}
      )

  @spec blocked?(String.t(), String.t()) :: boolean()
  def blocked?(user_a, user_b) do
    :ets.member(@table, {user_a, user_b}) or :ets.member(@table, {user_b, user_a})
  end

  @spec put_block(String.t(), String.t()) :: :ok
  def put_block(blocker_id, blocked_id) do
    :ets.insert(@table, {{blocker_id, blocked_id}, true})
    :ok
  end

  @spec remove_block(String.t(), String.t()) :: :ok
  def remove_block(blocker_id, blocked_id) do
    :ets.delete(@table, {blocker_id, blocked_id})
    :ok
  end
end
