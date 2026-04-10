CREATE TABLE waste_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id    UUID NOT NULL REFERENCES households(id),
    ingredient_id   UUID NOT NULL REFERENCES ingredients(id),
    lot_id          UUID REFERENCES ingredient_lots(id),
    quantity        DECIMAL(12, 4) NOT NULL,
    unit            VARCHAR(10) NOT NULL,
    waste_reason    VARCHAR(30) NOT NULL CHECK (waste_reason IN ('EXPIRED', 'OVERCOOKED', 'DISCARDED', 'LEFTOVER_UNUSED')),
    wasted_at       TIMESTAMP NOT NULL DEFAULT now()
);
