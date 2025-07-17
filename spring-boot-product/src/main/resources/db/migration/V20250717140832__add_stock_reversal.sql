-- Step 1: Create the ENUM type (reuse if already created)
CREATE TYPE stock_reversal_status AS ENUM ('PENDING', 'COMPLETED', 'CANCELLED');

-- Step 2: Create the table
CREATE TABLE stock_reversal
(
    id         UUID PRIMARY KEY               DEFAULT gen_random_uuid(),
    variant_id UUID                  NOT NULL REFERENCES variants (id) ON DELETE CASCADE,
    quantity   INTEGER               NOT NULL,
    status     stock_reversal_status NOT NULL DEFAULT 'PENDING',
    checkin_at TIMESTAMP             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    release_at TIMESTAMP                      DEFAULT NULL,
    CONSTRAINT variant_checkin_unique UNIQUE (variant_id, checkin_at)
);
