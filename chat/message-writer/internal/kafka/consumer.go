package kafka

import (
	"context"
	"errors"
	"fmt"
	"log"
	"time"

	"chat-message-writer/internal/config"
	"chat-message-writer/internal/db"

	"github.com/linkedin/goavro/v2"
	"github.com/twmb/franz-go/pkg/kgo"
)

const chatMessageSentSchema = `{
  "type": "record",
  "name": "ChatMessageSent",
  "namespace": "com.example.chat",
  "fields": [
	{ "name": "message_id", "type": "string" },
	{ "name": "room_id", "type": "string" },
	{ "name": "sender_id", "type": "string" },
	{ "name": "body", "type": "string" },
	{ "name": "sent_at", "type": "string" }
  ]
}`

type Store interface {
	InsertMessage(ctx context.Context, message db.Message) error
}

type Consumer struct {
	client *kgo.Client
	store  Store
	codec  *goavro.Codec
}

func NewConsumer(cfg config.Config, store Store) (*Consumer, error) {
	codec, err := goavro.NewCodec(chatMessageSentSchema)
	if err != nil {
		return nil, err
	}

	client, err := kgo.NewClient(
		kgo.SeedBrokers(cfg.Brokers...),
		kgo.ConsumerGroup(cfg.GroupID),
		kgo.ConsumeTopics(cfg.Topic),
		kgo.DisableAutoCommit(),
	)
	if err != nil {
		return nil, err
	}

	return &Consumer{client: client, store: store, codec: codec}, nil
}

func (c *Consumer) Close() {
	c.client.Close()
}

func (c *Consumer) Run(ctx context.Context) error {
	for {
		fetches := c.client.PollFetches(ctx)
		if errors.Is(fetches.Err(), context.Canceled) {
			return nil
		}
		if fetches.Err() != nil {
			log.Printf("kafka fetch error: %v", fetches.Err())
			continue
		}

		if err := c.handleFetches(ctx, fetches); err != nil {
			return err
		}
	}
}

func (c *Consumer) handleFetches(ctx context.Context, fetches kgo.Fetches) error {
	var failed error

	fetches.EachRecord(func(record *kgo.Record) {
		if failed != nil {
			return
		}

		message, err := c.decodeMessage(record.Value)
		if err != nil {
			failed = fmt.Errorf("decode message at offset %d: %w", record.Offset, err)
			return
		}

		if err := c.store.InsertMessage(ctx, message); err != nil {
			failed = fmt.Errorf("insert message %s: %w", message.MessageID, err)
			return
		}

		if err := c.client.CommitRecords(ctx, record); err != nil {
			failed = fmt.Errorf("commit message %s: %w", message.MessageID, err)
			return
		}

		log.Printf("persisted chat message id=%s room=%s", message.MessageID, message.RoomID)
	})

	return failed
}

func (c *Consumer) decodeMessage(value []byte) (db.Message, error) {
	native, _, err := c.codec.NativeFromBinary(value)
	if err != nil {
		return db.Message{}, err
	}

	raw, ok := native.(map[string]interface{})
	if !ok {
		return db.Message{}, errors.New("avro payload is not a record")
	}

	messageID, _ := raw["message_id"].(string)
	roomID, _ := raw["room_id"].(string)
	senderID, _ := raw["sender_id"].(string)
	body, _ := raw["body"].(string)
	sentAtValue, _ := raw["sent_at"].(string)

	if messageID == "" || roomID == "" || senderID == "" || body == "" || sentAtValue == "" {
		return db.Message{}, errors.New("missing required message fields")
	}

	sentAt, err := time.Parse(time.RFC3339Nano, sentAtValue)
	if err != nil {
		return db.Message{}, err
	}

	return db.Message{
		MessageID: messageID,
		RoomID:    roomID,
		SenderID:  senderID,
		Body:      body,
		SentAt:    sentAt,
	}, nil
}
