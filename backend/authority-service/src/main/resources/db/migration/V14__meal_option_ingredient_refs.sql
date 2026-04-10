ALTER TABLE meal_options
    ADD COLUMN ingredient_refs JSONB NOT NULL DEFAULT '[]';
