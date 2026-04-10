CREATE TABLE replenishment_suggestions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id        UUID NOT NULL REFERENCES households(id),
    ingredient_id       UUID NOT NULL REFERENCES ingredients(id),
    suggested_quantity  DECIMAL(12, 4) NOT NULL,
    unit                VARCHAR(10) NOT NULL,
    reason              VARCHAR(500),
    urgency             VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    adjusted_for_waste  BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);
