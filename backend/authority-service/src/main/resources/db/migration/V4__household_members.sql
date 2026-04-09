CREATE TABLE household_members (
    id                            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id                  UUID NOT NULL REFERENCES households(id),
    display_name                  VARCHAR(255) NOT NULL,
    age_group                     VARCHAR(20) NOT NULL DEFAULT 'ADULT',
    effort_sensitivity            VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    participates_in_meal_planning BOOLEAN NOT NULL DEFAULT true
);
