CREATE TABLE member_preference_profiles (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id             UUID NOT NULL UNIQUE REFERENCES household_members(id),
    disliked_ingredients  JSONB NOT NULL DEFAULT '[]',
    dietary_restrictions  JSONB NOT NULL DEFAULT '[]',
    protein_goal_grams    INTEGER,
    preferred_meal_types  JSONB NOT NULL DEFAULT '[]'
);
