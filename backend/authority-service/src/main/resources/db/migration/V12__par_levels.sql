CREATE TABLE par_levels (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id        UUID NOT NULL REFERENCES households(id),
    ingredient_id       UUID NOT NULL REFERENCES ingredients(id),
    preferred_quantity  DECIMAL(12, 4) NOT NULL,
    minimum_quantity    DECIMAL(12, 4) NOT NULL,
    unit                VARCHAR(10) NOT NULL,
    UNIQUE (household_id, ingredient_id)
);
