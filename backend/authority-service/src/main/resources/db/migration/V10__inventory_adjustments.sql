CREATE TABLE inventory_adjustments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lot_id           UUID NOT NULL REFERENCES ingredient_lots(id),
    adjustment_type  VARCHAR(20) NOT NULL,
    quantity_delta   DECIMAL(12, 4) NOT NULL,
    unit             VARCHAR(10) NOT NULL,
    reason           VARCHAR(500),
    adjusted_at      TIMESTAMP NOT NULL DEFAULT now()
);
