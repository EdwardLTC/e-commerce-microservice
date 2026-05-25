package api

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"time"

	"chat-message-writer/internal/db"
)

type resolveRequest struct {
	UserID     string `json:"user_id"`
	PeerUserID string `json:"peer_user_id"`
}

type resolveResponse struct {
	RoomID  string `json:"room_id,omitempty"`
	Created bool   `json:"created,omitempty"`
	Error   string `json:"error,omitempty"`
}

type validateRequest struct {
	RoomID string `json:"room_id"`
	UserID string `json:"user_id"`
}

type validateResponse struct {
	Valid bool   `json:"valid"`
	Error string `json:"error,omitempty"`
}

// Start starts a simple HTTP room management server on addr (e.g. "0.0.0.0:8080").
func Start(ctx context.Context, addr string, store *db.Store) error {
	mux := http.NewServeMux()
	mux.HandleFunc("/v1/rooms/resolve", resolveHandler(store))
	mux.HandleFunc("/v1/rooms/validate", validateHandler(store))

	srv := &http.Server{
		Addr:    addr,
		Handler: mux,
	}

	go func() {
		<-ctx.Done()
		ctxShut, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		srv.Shutdown(ctxShut)
	}()

	log.Printf("validator: starting HTTP server on %s", addr)
	if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
		return err
	}
	return nil
}

func resolveHandler(store *db.Store) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			w.WriteHeader(http.StatusMethodNotAllowed)
			return
		}

		var req resolveRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			writeJSONError(w, http.StatusBadRequest, "invalid json")
			return
		}

		room, created, err := store.ResolveRoom(r.Context(), req.UserID, req.PeerUserID)
		if err != nil {
			writeJSONError(w, http.StatusBadRequest, err.Error())
			return
		}

		w.Header().Set("Content-Type", "application/json")
		_ = json.NewEncoder(w).Encode(resolveResponse{RoomID: room.RoomID, Created: created})
	}
}

func validateHandler(store *db.Store) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		w.WriteHeader(http.StatusMethodNotAllowed)
		return
	}

	var req validateRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeJSONError(w, http.StatusBadRequest, "invalid json")
		return
	}

	valid, err := store.ValidateRoom(r.Context(), req.RoomID, req.UserID)
	if err != nil {
		writeJSONError(w, http.StatusInternalServerError, fmt.Sprintf("validate room: %v", err))
		return
	}
	if !valid {
		writeJSONError(w, http.StatusForbidden, "forbidden")
		return
	}

	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(validateResponse{Valid: true})
	}
}

func writeJSONError(w http.ResponseWriter, status int, msg string) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(map[string]string{"error": msg})
}

