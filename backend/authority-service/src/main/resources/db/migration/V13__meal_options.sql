CREATE TABLE meal_options (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                     VARCHAR(255) NOT NULL UNIQUE,
    meal_type                VARCHAR(20) NOT NULL,
    estimated_protein_grams  DECIMAL(8, 2),
    estimated_calories       DECIMAL(8, 2),
    prep_time_minutes        INTEGER,
    sustainability_score     DECIMAL(3, 2)
);
