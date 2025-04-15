CREATE TYPE product_status AS ENUM ('DRAFT', 'ACTIVE', 'DISCONTINUED', 'ARCHIVED');
CREATE TYPE stock_status AS ENUM ('IN_STOCK', 'OUT_OF_STOCK', 'BACKORDER', 'LIMITED');

CREATE TABLE products
(
    id           UUID PRIMARY KEY                                            DEFAULT gen_random_uuid(),

    name         VARCHAR(255)   NOT NULL,
    description  TEXT           NOT NULL,
    price        NUMERIC(12, 2) NOT NULL CHECK (price >= 0),

    quantity     INT            NOT NULL CHECK (quantity >= 0)               DEFAULT 0,
    stock_status stock_status   NOT NULL                                     DEFAULT 'OUT_OF_STOCK',

    seller_id    UUID           NOT NULL,
    category_ids UUID[]                                                      DEFAULT '{}',

    status       product_status NOT NULL                                     DEFAULT 'DRAFT',

    total_sales  INT            NOT NULL CHECK (total_sales >= 0)            DEFAULT 0,
    rating       NUMERIC(3, 2)  NOT NULL CHECK (rating >= 0 AND rating <= 5) DEFAULT 0,

    created_at   TIMESTAMPTZ    NOT NULL                                     DEFAULT NOW(),
    updated_at   TIMESTAMPTZ    NOT NULL                                     DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ,

    version      INT            NOT NULL                                     DEFAULT 0
);

-- Indexes
CREATE INDEX idx_products_seller ON products (seller_id);
CREATE INDEX idx_products_status ON products (status);
CREATE INDEX idx_products_categories ON products USING GIN (category_ids);