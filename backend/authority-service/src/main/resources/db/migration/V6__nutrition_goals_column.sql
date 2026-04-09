ALTER TABLE member_preference_profiles
    ADD COLUMN nutrition_goals JSONB NOT NULL DEFAULT '[]';
