FROM golang:1.24-alpine AS builder

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN go build -o myapp .

FROM alpine:latest

RUN apk --no-cache add ca-certificates

WORKDIR /app

ENV DATABASE_URL="postgres://postgres:postgres@host.docker.internal:5432/e_commerce_microservice?search_path=orders&sslmode=disable"

COPY --from=builder /app/myapp .
COPY --from=builder /app/myapp .
COPY --from=builder /app/.env .env

EXPOSE 4040

CMD ["./myapp"]
