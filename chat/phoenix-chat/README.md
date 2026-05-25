# Phoenix Chat Realtime

Phoenix websocket service for realtime chat delivery.

## Socket API

Connect to:

```text
ws://localhost:4001/socket/websocket?user_id=<user-id>
```

Join your personal user topic first:

```text
users:<user-id>
```

Then ask Phoenix to resolve or create a room for a peer user.

Join a room topic (for group or private rooms):

```text
rooms:<room-id>
```

Send a message to a room:

```json
{
  "topic": "rooms:<room-id>",
  "event": "message:send",
  "payload": {
    "room_id": "room-1",
    "body": "hello"
  }
}
```

If two users have never chatted before, the client should first request a room id from the writer service by asking Phoenix to open a room. That flow is handled by the `room:open` socket event, which calls the writer's room resolver and returns a canonical `room_id`.

Example room-open request:

```json
{
  "topic": "users:<user-id>",
  "event": "room:open",
  "payload": {
    "peer_user_id": "other-user-id"
  }
}
```

Response:

```json
{
  "room_id": "room-uuid",
  "topic": "rooms:room-uuid"
}
```

After that, the client joins `rooms:<room-id>`. All subscribers to `rooms:<room-id>` get `message:new` immediately. The same message is published as Avro to Kafka topic `chat.messages.v1` for async persistence. The schema is defined at repository root in `.avro/chat_message_sent.avsc`.

## Environment

- `PORT`: HTTP/socket port, default `4001`
- `KAFKA_BROKERS`: Kafka bootstrap servers, default `kafka:9092` in Docker
- `CHAT_MESSAGES_TOPIC`: Kafka topic, default `chat.messages.v1`
- `SECRET_KEY_BASE`: required in production
