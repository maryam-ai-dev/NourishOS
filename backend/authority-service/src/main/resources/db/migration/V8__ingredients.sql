CREATE TABLE ingredients (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL UNIQUE,
    category            VARCHAR(50) NOT NULL,
    default_unit        VARCHAR(10) NOT NULL,
    perishability_class VARCHAR(30) NOT NULL
);
