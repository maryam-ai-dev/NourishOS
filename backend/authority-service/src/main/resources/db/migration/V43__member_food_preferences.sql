ALTER TABLE member_preference_profiles
    ADD COLUMN favourite_dishes JSONB NOT NULL DEFAULT '[]',
    ADD COLUMN disliked_dishes JSONB NOT NULL DEFAULT '[]';
