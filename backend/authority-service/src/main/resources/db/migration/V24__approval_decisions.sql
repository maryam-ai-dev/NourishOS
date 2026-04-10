CREATE TABLE approval_decisions (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    replenishment_request_id  UUID NOT NULL REFERENCES replenishment_requests(id),
    decided_by                VARCHAR(255),
    decided_at                TIMESTAMP NOT NULL DEFAULT now(),
    decision                  VARCHAR(20) NOT NULL,
    notes                     VARCHAR(500)
);
