CREATE TABLE execution_plans (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meal_plan_id                UUID NOT NULL REFERENCES meal_plans(id),
    status                      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    started_at                  TIMESTAMP,
    completed_at                TIMESTAMP,
    estimated_duration_seconds  INTEGER
);
