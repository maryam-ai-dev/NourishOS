ALTER TABLE member_preference_profiles
    ADD COLUMN favourite_cuisines JSONB NOT NULL DEFAULT '[]';
