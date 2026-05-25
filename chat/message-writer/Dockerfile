FROM golang:1.24-alpine AS build

WORKDIR /src

COPY go.mod ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 go build -o /out/chat-message-writer ./cmd/chat-message-writer

FROM alpine:3.22

WORKDIR /app

COPY --from=build /out/chat-message-writer /app/chat-message-writer

CMD ["/app/chat-message-writer"]
