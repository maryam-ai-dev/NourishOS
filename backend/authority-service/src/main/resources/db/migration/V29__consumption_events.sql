CREATE TABLE consumption_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id    UUID NOT NULL REFERENCES households(id),
    ingredient_id   UUID NOT NULL REFERENCES ingredients(id),
    lot_id          UUID REFERENCES ingredient_lots(id),
    quantity        DECIMAL(12, 4) NOT NULL,
    unit            VARCHAR(10) NOT NULL,
    consumed_at     TIMESTAMP NOT NULL DEFAULT now(),
    meal_plan_id    UUID REFERENCES meal_plans(id),
    source          VARCHAR(20) NOT NULL CHECK (source IN ('PLANNED_MEAL', 'UNPLANNED', 'SNACK'))
);
