CREATE TABLE food_flow_snapshots (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id              UUID NOT NULL REFERENCES households(id),
    snapshot_date             DATE NOT NULL,
    total_waste_grams         DECIMAL(12, 4) NOT NULL DEFAULT 0,
    total_consumed_grams      DECIMAL(12, 4) NOT NULL DEFAULT 0,
    waste_ratio               DECIMAL(5, 4) NOT NULL DEFAULT 0,
    top_wasted_ingredients    JSONB NOT NULL DEFAULT '[]',
    top_consumed_ingredients  JSONB NOT NULL DEFAULT '[]'
);
