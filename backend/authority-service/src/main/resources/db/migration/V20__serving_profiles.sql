CREATE TABLE serving_profiles (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meal_plan_id              UUID NOT NULL UNIQUE REFERENCES meal_plans(id),
    household_size            INTEGER NOT NULL,
    participating_member_ids  JSONB NOT NULL DEFAULT '[]',
    scaling_factor            DECIMAL(6, 4) NOT NULL DEFAULT 1.0
);
