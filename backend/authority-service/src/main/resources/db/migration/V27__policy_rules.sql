CREATE TABLE policy_rules (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_set_id UUID NOT NULL REFERENCES policy_sets(id),
    rule_type     VARCHAR(50) NOT NULL,
    value         VARCHAR(255),
    is_active     BOOLEAN NOT NULL DEFAULT true,
    UNIQUE (policy_set_id, rule_type)
);
