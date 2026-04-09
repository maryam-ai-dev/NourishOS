CREATE TABLE households (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    weekly_budget_limit DECIMAL(10, 2),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);
