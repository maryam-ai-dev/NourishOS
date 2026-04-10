CREATE TABLE policy_decisions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_type   VARCHAR(50) NOT NULL,
    input       JSONB NOT NULL DEFAULT '{}',
    decision    VARCHAR(30) NOT NULL,
    reason      VARCHAR(500),
    decided_at  TIMESTAMP NOT NULL DEFAULT now()
);
