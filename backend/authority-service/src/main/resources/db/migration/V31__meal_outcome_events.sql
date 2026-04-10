CREATE TABLE meal_outcome_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meal_plan_id    UUID NOT NULL UNIQUE REFERENCES meal_plans(id),
    household_id    UUID NOT NULL REFERENCES households(id),
    outcome         VARCHAR(30) NOT NULL CHECK (outcome IN ('COMPLETED', 'ABANDONED', 'SUBSTITUTED', 'PARTIALLY_COMPLETED')),
    completed_at    TIMESTAMP NOT NULL DEFAULT now(),
    notes           VARCHAR(500)
);
