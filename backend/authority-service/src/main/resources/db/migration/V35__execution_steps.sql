CREATE TABLE execution_steps (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id                     UUID NOT NULL REFERENCES execution_plans(id),
    step_order                  INTEGER NOT NULL,
    action_type                 VARCHAR(30) NOT NULL,
    assigned_to                 VARCHAR(10) NOT NULL CHECK (assigned_to IN ('MACHINE', 'USER')),
    status                      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    started_at                  TIMESTAMP,
    completed_at                TIMESTAMP,
    estimated_duration_seconds  INTEGER,
    ingredient_ref              JSONB
);
