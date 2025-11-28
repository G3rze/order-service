-- Enable extension for gen_random_uuid() (PostgreSQL 13+)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ==========================
-- TABLE: order_status
-- ==========================
CREATE TABLE order_status (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(50)  NOT NULL UNIQUE,  
    label       VARCHAR(100) NOT NULL,         
    description TEXT,
    is_final    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Seed some default statuses
INSERT INTO order_status (code, label, description, is_final)
VALUES
    ('PENDING_PAYMENT', 'Pending Payment', 'Order created and awaiting payment.', FALSE),
    ('PAID',            'Paid',            'Order has been fully paid.', TRUE),
    ('CANCELLED',       'Cancelled',       'Order has been cancelled.', TRUE)
ON CONFLICT (code) DO NOTHING;

-- ==========================
-- TABLE: product
-- ==========================
CREATE TABLE product (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    price       NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    description TEXT,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_name ON product (name);

-- ==========================
-- TABLE: orders
-- ==========================
CREATE TABLE orders (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id  UUID         NOT NULL,
    status_id    UUID         NOT NULL REFERENCES order_status (id),
    total_amount NUMERIC(12,2) NOT NULL CHECK (total_amount >= 0),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_status_id ON orders (status_id);
CREATE INDEX idx_orders_customer_created_at ON orders (customer_id, created_at DESC);

-- ==========================
-- TABLE: order_items
-- ==========================
CREATE TABLE order_items (
    id                            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id                      UUID         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id                    UUID         NOT NULL REFERENCES product (id) ON DELETE RESTRICT,
    product_name_snapshot         VARCHAR(255) NOT NULL,
    product_unit_price_snapshot   NUMERIC(12,2) NOT NULL CHECK (product_unit_price_snapshot >= 0),
    quantity                      INT          NOT NULL CHECK (quantity > 0),
    line_total                    NUMERIC(12,2) NOT NULL CHECK (line_total >= 0)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);