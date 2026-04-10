CREATE TABLE intervention_requests (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id             UUID NOT NULL REFERENCES execution_plans(id),
    step_id             UUID NOT NULL REFERENCES execution_steps(id),
    intervention_type   VARCHAR(30) NOT NULL,
    message             VARCHAR(500),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    resolved_at         TIMESTAMP
);
