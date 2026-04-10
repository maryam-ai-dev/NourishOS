CREATE TABLE household_insights (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id    UUID NOT NULL REFERENCES households(id),
    snapshot_week   DATE NOT NULL,
    category        VARCHAR(50) NOT NULL,
    insight_text    TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_insight_household_week_category UNIQUE (household_id, snapshot_week, category)
);

CREATE INDEX idx_household_insights_household ON household_insights(household_id);
