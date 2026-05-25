# Chat Message Writer

Go Kafka consumer that persists realtime chat messages produced by `phoenix-chat` into Cassandra and manages chat rooms for two-user conversations.

Messages are decoded from Avro using the shared schema at repository root `.avro/chat_message_sent.avsc`.

## Environment

- `KAFKA_BROKERS`: Kafka bootstrap servers, default `localhost:9092`
- `CHAT_MESSAGES_TOPIC`: Kafka topic, default `chat.messages.v1`
- `CHAT_MESSAGES_GROUP_ID`: consumer group, default `chat-message-writer`
- `CASSANDRA_CONTACT_POINTS`: comma-separated Cassandra hosts, default `localhost:9042`
- `CASSANDRA_KEYSPACE`: Cassandra keyspace, default `chat`
- `VALIDATOR_ADDR`: HTTP address for the room resolver API, default `0.0.0.0:8080`

The configured keyspace must exist before startup. The consumer creates the `chat_messages`, `chat_rooms_by_pair`, and `chat_rooms_by_id` tables in that keyspace if they do not exist.

## Room management API

The writer exposes a small HTTP API used by `phoenix-chat`:

- `POST /v1/rooms/resolve` with `{ "user_id": "...", "peer_user_id": "..." }`
  - finds an existing room for the two users, or creates one if it does not exist
  - returns `{ "room_id": "...", "created": true|false }`
- `POST /v1/rooms/validate` with `{ "room_id": "...", "user_id": "..." }`
  - validates that the room exists and the user belongs to it

The room tables are stored in Cassandra so room ids are durable and can be reused later.
