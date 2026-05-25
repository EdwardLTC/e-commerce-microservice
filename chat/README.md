# Chat Microservices

This repository splits chat into two independent services:

- `phoenix-chat` — the realtime WebSocket service that handles socket connections and message delivery.
- `message-writer` — the background Kafka consumer that persists messages to Cassandra.

This separation is intentional. Chat has two very different workloads:

1. **Realtime socket traffic**: low-latency connection handling, fan-out, and presence-style concurrency.
2. **Persistence work**: reliable ingestion, validation, deduplication, and database writes.

Keeping those workloads in separate services makes the system easier to scale, deploy, and operate.

## Architecture Flow

```text
Client -> phoenix-chat -> Kafka -> message-writer -> Cassandra
```

### What `phoenix-chat` does

- Accepts WebSocket connections
- Authenticates and routes messages to the correct user topic
- Delivers `message:new` events immediately to the receiver
- Publishes the same message to Kafka as Avro for asynchronous persistence

### What `message-writer` does

- Consumes messages from Kafka
- Decodes the Avro payload
- Inserts messages into Cassandra
- Uses idempotent writes so retries do not create duplicates

## Why split the producer and consumer into two services?

### 1. Realtime delivery should not wait on database writes

Socket handling must stay fast. If the database is slow, temporarily unavailable, or under heavy load, users should still be able to send and receive messages in realtime.

By moving persistence to a separate consumer, the WebSocket service can acknowledge and deliver messages immediately while Kafka buffers the persistence work.

### 2. Each workload scales differently

The realtime service scales based on:

- number of connected sockets
- message fan-out
- concurrent channels and users

The persistence service scales based on:

- Kafka throughput
- database write capacity
- batch and retry pressure

These are not the same bottlenecks, so combining them would force both to scale together even when only one side needs it.

### 3. Better fault isolation

If persistence is degraded, realtime delivery still works.
If the realtime service is restarted, the consumer can continue processing already queued messages.
If Kafka briefly absorbs load, neither side needs to fail together.

This makes the system more resilient than a single monolith where a slow database can directly impact sockets.

### 4. Independent deployment and maintenance

You can deploy or roll back the WebSocket service without touching the persistence worker.
You can also tune consumer settings, database behavior, and retry logic without affecting the realtime stack.

That separation is especially useful when the team wants to evolve the message pipeline independently from the user-facing socket layer.

## Why Elixir is a strong fit for socket handling

`phoenix-chat` uses Phoenix/Elixir because it is very well suited to realtime concurrency.

### Reasons Elixir fits sockets well

- **BEAM concurrency model**: lightweight processes make it easy to manage many long-lived socket connections.
- **Fault tolerance**: the supervision tree model helps isolate failures and restart components cleanly.
- **Phoenix Channels**: a mature abstraction for websockets, pub/sub, and realtime fan-out.
- **Low overhead per connection**: Elixir is commonly chosen for systems with large numbers of concurrent clients.
- **Expressive realtime primitives**: presence, channels, and pub/sub are a natural fit for chat.

In short, Elixir is excellent for the part of the system that must keep many sockets alive and responsive at the same time.

## Why Go is a strong fit for persistence

`message-writer` uses Go because it is a good fit for a dedicated background consumer.

### Reasons Go fits persistence work well

- **Simple, efficient runtime**: good throughput with modest resource usage.
- **Easy to build a small standalone worker**: ideal for a service that just consumes Kafka and writes to a database.
- **Strong concurrency support**: goroutines make consumer processing and coordination straightforward.
- **Fast startup and easy deployment**: static or near-static binaries are convenient for containers and ops.
- **Good ecosystem for infrastructure services**: Kafka clients, database drivers, and observability tooling are mature.

For a persistence worker, simplicity matters. Go keeps the service focused on ingesting messages reliably rather than carrying the full realtime application stack.

## Why not use Elixir for persistence too?

Elixir **could** be used for persistence, but in this architecture it is not the best tradeoff.

### Reasons not to use Elixir for the writer here

- **No need to keep the Phoenix runtime in the persistence path**: the writer has a very narrow job and does not need websocket features.
- **Smaller operational footprint**: a dedicated Go worker is lightweight and easy to isolate.
- **Clearer service boundaries**: one service owns realtime delivery, the other owns storage.
- **Avoid coupling persistence to the realtime stack**: if the persistence logic lived inside the Phoenix app, database pressure could affect socket processing more directly.
- **Different optimization goals**: Elixir is best used where its strengths matter most; Go is a clean fit for a stateless consumer that decodes messages and writes them out.

This does **not** mean Elixir is bad for persistence. It is absolutely capable of writing to databases. The reason for using Go here is architectural: the persistence worker is intentionally kept minimal, independent, and operationally separate from the realtime service.

## Message format

Messages are published from `phoenix-chat` to Kafka as Avro using the shared schema at repository root:

```text
.avro/chat_message_sent.avsc
```

This keeps the producer and consumer aligned on the message contract.

## Project layout

- `phoenix-chat/` — Elixir Phoenix websocket service
- `message-writer/` — Go Kafka consumer and Cassandra writer

## Summary

This design uses the right tool for each job:

- **Elixir/Phoenix** for websocket concurrency and realtime delivery
- **Go** for a lean, reliable Kafka consumer and persistence worker
- **Cassandra** for durable, horizontally scalable message storage
- **Kafka** as the boundary between them so realtime delivery and storage remain decoupled

The result is a system that is easier to scale, easier to recover, and easier to evolve over time.

