defmodule ChatRealtime.MembershipCache do
  @moduledoc """
  Cache thành viên của room bằng ETS để tránh gọi Golang/DB mỗi lần join/gửi tin.
  Populate lazy khi cache miss (gọi RoomValidator), và nên có 1 consumer riêng
  lắng nghe Kafka topic "conversation.events" để invalidate/update real-time
  (member.added / member.removed) - phần đó chưa nằm trong file này.
  """
  use GenServer

  @table :membership_cache

  def start_link(_opts), do: GenServer.start_link(__MODULE__, nil, name: __MODULE__)

  @impl true
  def init(_),
    do:
      (
        :ets.new(@table, [:named_table, :public, :set, read_concurrency: true])
        {:ok, nil}
      )

  @spec member?(String.t(), String.t()) :: boolean()
  def member?(room_id, user_id) do
    case :ets.lookup(@table, room_id) do
      [{^room_id, member_ids}] -> user_id in member_ids
      [] -> false
    end
  end

  @spec members_of(String.t()) :: [String.t()]
  def members_of(room_id) do
    case :ets.lookup(@table, room_id) do
      [{^room_id, member_ids}] -> member_ids
      [] -> []
    end
  end

  @spec put_members(String.t(), [String.t()]) :: :ok
  def put_members(room_id, member_ids) do
    :ets.insert(@table, {room_id, member_ids})
    :ok
  end

  @spec add_member(String.t(), String.t()) :: :ok
  def add_member(room_id, user_id) do
    put_members(room_id, Enum.uniq([user_id | members_of(room_id)]))
  end
end
