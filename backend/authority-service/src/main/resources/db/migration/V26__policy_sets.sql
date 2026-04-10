CREATE TABLE policy_sets (
    id                               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id                     UUID NOT NULL UNIQUE REFERENCES households(id),
    auto_reorder_limit               DECIMAL(10, 2) NOT NULL DEFAULT 50.00,
    substitution_approval_required   BOOLEAN NOT NULL DEFAULT true,
    night_mode_enabled               BOOLEAN NOT NULL DEFAULT false,
    max_autonomous_actions           INTEGER NOT NULL DEFAULT 5,
    waste_alert_threshold            DECIMAL(3, 2) NOT NULL DEFAULT 0.30
);
