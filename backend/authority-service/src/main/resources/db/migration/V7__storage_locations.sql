CREATE TABLE storage_locations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id    UUID NOT NULL REFERENCES households(id),
    location_type   VARCHAR(20) NOT NULL,
    label           VARCHAR(255)
);
