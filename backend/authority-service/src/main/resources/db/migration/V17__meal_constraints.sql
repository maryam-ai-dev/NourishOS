CREATE TABLE meal_constraints (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meal_request_id           UUID NOT NULL UNIQUE REFERENCES meal_requests(id),
    servings                  INTEGER NOT NULL,
    protein_target_grams      DECIMAL(8, 2),
    preferred_time            VARCHAR(20),
    max_effort                VARCHAR(20),
    participating_member_ids  JSONB NOT NULL DEFAULT '[]'
);
