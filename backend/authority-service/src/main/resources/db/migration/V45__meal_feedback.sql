CREATE TABLE meal_preference_feedback (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id                UUID NOT NULL REFERENCES households(id),
    member_id                   UUID REFERENCES household_members(id),
    meal_option_id              UUID NOT NULL REFERENCES meal_options(id),
    feedback_type               VARCHAR(30) NOT NULL,
    swapped_to_meal_option_id   UUID REFERENCES meal_options(id),
    recorded_at                 TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_meal_feedback_household ON meal_preference_feedback(household_id);
CREATE INDEX idx_meal_feedback_meal ON meal_preference_feedback(meal_option_id);
