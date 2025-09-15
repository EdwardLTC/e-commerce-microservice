CREATE TABLE products
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id        TEXT         NOT NULL,
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    brand            VARCHAR(100),
    media_urls       TEXT[]           DEFAULT ARRAY []::TEXT[],
    is_active        BOOLEAN          DEFAULT TRUE,
    total_sale_count INT              DEFAULT 0,
    average_rating   NUMERIC(3, 2)    DEFAULT 0 CHECK (average_rating >= 0 AND average_rating <= 5),
    created_at       TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE option_types
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id    UUID         NOT NULL,
    name          VARCHAR(100) NOT NULL,
    display_order SMALLINT         DEFAULT 1,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT uq_option_types_product_name UNIQUE (product_id, name)
);

CREATE TABLE option_values
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    option_type_id UUID         NOT NULL,
    value          VARCHAR(100) NOT NULL,
    media_url      TEXT,
    display_order  SMALLINT         DEFAULT 1,
    FOREIGN KEY (option_type_id) REFERENCES option_types (id) ON DELETE CASCADE,
    CONSTRAINT uq_option_values_type_value UNIQUE (option_type_id, value)
);

CREATE TYPE VARIANT_STATUS AS ENUM ('ACTIVE', 'HIDDEN', 'OUT_OF_STOCK');

CREATE TABLE variants
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID           NOT NULL,
    sku        VARCHAR(50),
    price      NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    sale_price NUMERIC(12, 2) CHECK (sale_price >= 0),
    stock      INT              DEFAULT 0 CHECK (stock >= 0),
    weight     NUMERIC(8, 2),
    dimensions VARCHAR(50),
    status     VARIANT_STATUS   DEFAULT 'ACTIVE',
    media_url  VARCHAR(255),
    created_at TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

CREATE TABLE variant_option_values
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    variant_id      UUID NOT NULL,
    option_value_id UUID NOT NULL,
    UNIQUE (variant_id, option_value_id),
    FOREIGN KEY (variant_id) REFERENCES variants (id) ON DELETE CASCADE,
    FOREIGN KEY (option_value_id) REFERENCES option_values (id) ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION update_modified_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_products_modtime
    BEFORE UPDATE
    ON products
    FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_variants_modtime
    BEFORE UPDATE
    ON variants
    FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

CREATE OR REPLACE FUNCTION update_variant_status_to_out_of_stock()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.status = VARIANT_STATUS.OUT_OF_STOCK;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_variant_status
    AFTER UPDATE OF stock
    ON variants
    FOR EACH ROW
    WHEN (OLD.stock IS DISTINCT FROM NEW.stock AND NEW.stock = 0)
EXECUTE FUNCTION update_variant_status_to_out_of_stock();