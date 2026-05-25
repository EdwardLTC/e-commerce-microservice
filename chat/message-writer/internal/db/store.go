package db

import (
	"context"
	"errors"
	"fmt"
	"strings"
	"time"

	"github.com/gocql/gocql"
)

type Message struct {
	MessageID      string    `json:"message_id"`
	RoomID         string    `json:"room_id"`
	SenderID       string    `json:"sender_id"`
	Body           string    `json:"body"`
	SentAt         time.Time `json:"sent_at"`
}

type Room struct {
	RoomID    string    `json:"room_id"`
	PairKey   string    `json:"pair_key"`
	UserA     string    `json:"user_a"`
	UserB     string    `json:"user_b"`
	CreatedAt time.Time `json:"created_at"`
}

type Store struct {
	session *gocql.Session
}

const createChatMessagesTable = `
CREATE TABLE IF NOT EXISTS chat_messages (
  message_id text PRIMARY KEY,
  room_id text,
  sender_id text,
  body text,
  sent_at timestamp,
  persisted_at timestamp
)
`

const createChatRoomsByPairTable = `
CREATE TABLE IF NOT EXISTS chat_rooms_by_pair (
	pair_key text PRIMARY KEY,
	room_id text,
	user_a text,
	user_b text,
	created_at timestamp
)
`

const createChatRoomsByIDTable = `
CREATE TABLE IF NOT EXISTS chat_rooms_by_id (
	room_id text PRIMARY KEY,
	pair_key text,
	user_a text,
	user_b text,
	created_at timestamp
)
`

const insertChatMessage = `
INSERT INTO chat_messages (
  message_id,
  room_id,
  sender_id,
  body,
  sent_at,
  persisted_at
) VALUES (?, ?, ?, ?, ?, ?) IF NOT EXISTS
`

const insertRoomByPair = `
INSERT INTO chat_rooms_by_pair (
	pair_key,
	room_id,
	user_a,
	user_b,
	created_at
) VALUES (?, ?, ?, ?, ?) IF NOT EXISTS
`

const insertRoomByID = `
INSERT INTO chat_rooms_by_id (
	room_id,
	pair_key,
	user_a,
	user_b,
	created_at
) VALUES (?, ?, ?, ?, ?) IF NOT EXISTS
`

func New(ctx context.Context, contactPoints []string, keyspace string) (*Store, error) {
	if err := ctx.Err(); err != nil {
		return nil, err
	}

	normalized := normalizeContactPoints(contactPoints)
	if len(normalized) == 0 {
		return nil, errors.New("at least one Cassandra contact point is required")
	}
	if strings.TrimSpace(keyspace) == "" {
		return nil, errors.New("Cassandra keyspace is required")
	}

	cluster := gocql.NewCluster(normalized...)
	cluster.Keyspace = keyspace
	cluster.Consistency = gocql.Quorum
	cluster.Timeout = 10 * time.Second
	cluster.ConnectTimeout = 10 * time.Second

	session, err := cluster.CreateSession()
	if err != nil {
		return nil, fmt.Errorf("create Cassandra session: %w", err)
	}

	return &Store{session: session}, nil
}

func (s *Store) Close() {
	s.session.Close()
}

func (s *Store) Migrate(ctx context.Context) error {
	if err := s.session.Query(createChatMessagesTable).WithContext(ctx).Exec(); err != nil {
		return err
	}
	if err := s.session.Query(createChatRoomsByPairTable).WithContext(ctx).Exec(); err != nil {
		return err
	}
	return s.session.Query(createChatRoomsByIDTable).WithContext(ctx).Exec()
}

func (s *Store) InsertMessage(ctx context.Context, message Message) error {
	persistedAt := time.Now().UTC()
	return s.session.Query(insertChatMessage,
		message.MessageID,
		message.RoomID,
		message.SenderID,
		message.Body,
		message.SentAt,
		persistedAt,
	).WithContext(ctx).Exec()
}

func (s *Store) ResolveRoom(ctx context.Context, userID, peerUserID string) (Room, bool, error) {
	userA, userB, pairKey, err := normalizeRoomParticipants(userID, peerUserID)
	if err != nil {
		return Room{}, false, err
	}

	room, err := s.getRoomByPairKey(ctx, pairKey)
	if err == nil {
		if err := s.ensureRoomByID(ctx, room); err != nil {
			return Room{}, false, err
		}
		return room, false, nil
	}
	if err != gocql.ErrNotFound {
		return Room{}, false, err
	}

	roomID := gocql.TimeUUID().String()
	now := time.Now().UTC()
	if err := s.session.Query(insertRoomByPair,
		pairKey,
		roomID,
		userA,
		userB,
		now,
	).WithContext(ctx).Exec(); err != nil {
		return Room{}, false, err
	}

	room, err = s.getRoomByPairKey(ctx, pairKey)
	if err != nil {
		return Room{}, false, err
	}

	if err := s.ensureRoomByID(ctx, room); err != nil {
		return Room{}, false, err
	}

	return room, true, nil
}

func (s *Store) ValidateRoom(ctx context.Context, roomID, userID string) (bool, error) {
	room, err := s.getRoomByID(ctx, roomID)
	if err != nil {
		if err == gocql.ErrNotFound {
			return false, nil
		}
		return false, err
	}

	return room.UserA == userID || room.UserB == userID, nil
}

func (s *Store) getRoomByPairKey(ctx context.Context, pairKey string) (Room, error) {
	var room Room
	if err := s.session.Query(`SELECT pair_key, room_id, user_a, user_b, created_at FROM chat_rooms_by_pair WHERE pair_key = ? LIMIT 1`, pairKey).
		WithContext(ctx).
		Scan(&room.PairKey, &room.RoomID, &room.UserA, &room.UserB, &room.CreatedAt); err != nil {
		return Room{}, err
	}

	return room, nil
}

func (s *Store) getRoomByID(ctx context.Context, roomID string) (Room, error) {
	var room Room
	if err := s.session.Query(`SELECT room_id, pair_key, user_a, user_b, created_at FROM chat_rooms_by_id WHERE room_id = ? LIMIT 1`, roomID).
		WithContext(ctx).
		Scan(&room.RoomID, &room.PairKey, &room.UserA, &room.UserB, &room.CreatedAt); err != nil {
		return Room{}, err
	}

	return room, nil
}

func (s *Store) ensureRoomByID(ctx context.Context, room Room) error {
	return s.session.Query(insertRoomByID,
		room.RoomID,
		room.PairKey,
		room.UserA,
		room.UserB,
		room.CreatedAt,
	).WithContext(ctx).Exec()
}

func normalizeContactPoints(contactPoints []string) []string {
	normalized := make([]string, 0, len(contactPoints))
	for _, contactPoint := range contactPoints {
		contactPoint = strings.TrimSpace(contactPoint)
		if contactPoint != "" {
			normalized = append(normalized, contactPoint)
		}
	}

	return normalized
}

func normalizeRoomParticipants(userID, peerUserID string) (string, string, string, error) {
	userID = strings.TrimSpace(userID)
	peerUserID = strings.TrimSpace(peerUserID)
	if userID == "" || peerUserID == "" {
		return "", "", "", errors.New("both user ids are required")
	}
	if userID == peerUserID {
		return "", "", "", errors.New("cannot create a room with yourself")
	}

	userA, userB := userID, peerUserID
	if userA > userB {
		userA, userB = userB, userA
	}

	return userA, userB, userA + ":" + userB, nil
}

