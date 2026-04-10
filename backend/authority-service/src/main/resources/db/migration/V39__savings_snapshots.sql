CREATE TABLE savings_snapshots (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id                UUID NOT NULL REFERENCES households(id),
    week_start_date             DATE NOT NULL,
    saved_from_waste_gbp        DECIMAL(10,2),
    previous_week_saved_gbp     DECIMAL(10,2),
    waste_items_this_week       INT NOT NULL DEFAULT 0,
    waste_items_previous_week   INT NOT NULL DEFAULT 0,
    meals_completed_rate        DECIMAL(5,4) NOT NULL DEFAULT 0.0,
    total_spent_gbp             DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_savings_household_week UNIQUE (household_id, week_start_date)
);

CREATE INDEX idx_savings_household ON savings_snapshots(household_id);
