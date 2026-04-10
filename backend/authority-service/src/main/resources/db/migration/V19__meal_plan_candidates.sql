CREATE TABLE meal_plan_candidates (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meal_plan_id     UUID NOT NULL REFERENCES meal_plans(id),
    meal_option_id   UUID NOT NULL REFERENCES meal_options(id),
    composite_score  DECIMAL(5, 4),
    score_breakdown  JSONB NOT NULL DEFAULT '{}'
);
