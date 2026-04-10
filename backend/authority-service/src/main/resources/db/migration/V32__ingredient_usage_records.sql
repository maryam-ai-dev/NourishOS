CREATE TABLE ingredient_usage_records (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id                UUID NOT NULL REFERENCES households(id),
    ingredient_id               UUID NOT NULL REFERENCES ingredients(id),
    total_consumed_last_4_weeks DECIMAL(12, 4) NOT NULL DEFAULT 0,
    total_wasted_last_4_weeks   DECIMAL(12, 4) NOT NULL DEFAULT 0,
    avg_weekly_usage            DECIMAL(12, 4) NOT NULL DEFAULT 0,
    last_recomputed_at          TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (household_id, ingredient_id)
);
