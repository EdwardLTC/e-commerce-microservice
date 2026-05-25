CREATE TYPE kafka_outbox_status AS ENUM ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED');

CREATE TABLE kafka_outbox
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic           VARCHAR(255) NOT NULL,
    message_key     VARCHAR(255),
    payload_base64   TEXT          NOT NULL,
    status          kafka_outbox_status NOT NULL DEFAULT 'PENDING',
    attempts        INTEGER       NOT NULL DEFAULT 0,
    error_message   TEXT,
    published_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_kafka_outbox_status_created_at ON kafka_outbox (status, created_at);

