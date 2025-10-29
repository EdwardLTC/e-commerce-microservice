-- 1. Create the ENUM type for stock_reversal_status
CREATE TYPE stock_reversal_status AS ENUM ('RELEASED', 'REVERSED');

-- 2. Create the stock_reversals table
CREATE TABLE stock_reversals (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 status stock_reversal_status NOT NULL DEFAULT 'RELEASED',
                                 order_id VARCHAR(36) UNIQUE NOT NULL,
                                 created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
                                 updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- 3. Create the stock_reversal_items table
CREATE TABLE stock_reversal_items (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      reversal_id UUID NOT NULL REFERENCES stock_reversals(id) ON DELETE CASCADE,
                                      variant_id UUID NOT NULL REFERENCES variants(id) ON DELETE CASCADE,
                                      quantity INTEGER NOT NULL,
                                      created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
                                      updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
                                      CONSTRAINT reversal_variant_unique UNIQUE (reversal_id, variant_id)
);
