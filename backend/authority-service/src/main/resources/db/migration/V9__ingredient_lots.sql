CREATE TABLE ingredient_lots (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ingredient_id         UUID NOT NULL REFERENCES ingredients(id),
    storage_location_id   UUID REFERENCES storage_locations(id),
    quantity              DECIMAL(12, 4) NOT NULL DEFAULT 0,
    unit                  VARCHAR(10) NOT NULL,
    purchased_at          TIMESTAMP NOT NULL DEFAULT now(),
    expiry_date           TIMESTAMP,
    is_open               BOOLEAN NOT NULL DEFAULT false,
    is_managed            BOOLEAN NOT NULL DEFAULT true,
    correction_flag       BOOLEAN NOT NULL DEFAULT false,
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
