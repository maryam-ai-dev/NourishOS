CREATE TABLE meal_plans (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meal_request_id             UUID NOT NULL REFERENCES meal_requests(id),
    selected_meal_option_id     UUID REFERENCES meal_options(id),
    servings                    INTEGER NOT NULL,
    planned_time                TIMESTAMP,
    protein_score_snapshot      DECIMAL(5, 4),
    waste_score_snapshot        DECIMAL(5, 4),
    reliability_score_snapshot  DECIMAL(5, 4)
);
