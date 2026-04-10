CREATE TABLE replenishment_requests (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id          UUID NOT NULL REFERENCES households(id),
    total_estimated_cost  DECIMAL(10, 2),
    status                VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at            TIMESTAMP NOT NULL DEFAULT now()
);
