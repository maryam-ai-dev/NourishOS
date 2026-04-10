CREATE TABLE household_budgets (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id        UUID NOT NULL REFERENCES households(id) UNIQUE,
    weekly_limit_gbp    DECIMAL(10,2),
    groceries_limit_gbp DECIMAL(10,2),
    pantry_limit_gbp    DECIMAL(10,2),
    other_limit_gbp     DECIMAL(10,2)
);
