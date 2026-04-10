CREATE TABLE weekly_spend_records (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id        UUID NOT NULL REFERENCES households(id),
    week_start_date     DATE NOT NULL,
    groceries_spent_gbp DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    pantry_spent_gbp    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    other_spent_gbp     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_spent_gbp     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT uq_spend_household_week UNIQUE (household_id, week_start_date)
);
