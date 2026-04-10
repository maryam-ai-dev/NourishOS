CREATE TABLE meal_requests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id    UUID NOT NULL REFERENCES households(id),
    request_type    VARCHAR(20) NOT NULL,
    requested_at    TIMESTAMP NOT NULL DEFAULT now(),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);
