CREATE TABLE expiry_records (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lot_id            UUID NOT NULL UNIQUE REFERENCES ingredient_lots(id),
    expiry_date       TIMESTAMP,
    freshness_status  VARCHAR(20) NOT NULL DEFAULT 'FRESH',
    notified_at       TIMESTAMP
);
