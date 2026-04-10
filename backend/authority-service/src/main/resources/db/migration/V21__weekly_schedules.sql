CREATE TABLE weekly_meal_schedules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id    UUID NOT NULL REFERENCES households(id),
    week_start_date DATE NOT NULL
);

CREATE TABLE scheduled_meal_slots (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule_id     UUID NOT NULL REFERENCES weekly_meal_schedules(id),
    day_of_week     INTEGER NOT NULL,
    meal_type       VARCHAR(20) NOT NULL,
    meal_plan_id    UUID REFERENCES meal_plans(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    UNIQUE (schedule_id, day_of_week, meal_type)
);
