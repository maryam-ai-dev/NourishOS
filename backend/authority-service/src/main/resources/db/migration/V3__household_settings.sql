CREATE TABLE household_settings (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id            UUID NOT NULL UNIQUE REFERENCES households(id),
    effort_tolerance        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    sustainability_priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    weekly_budget_limit     DECIMAL(10, 2),
    default_servings        INTEGER NOT NULL DEFAULT 4
);
