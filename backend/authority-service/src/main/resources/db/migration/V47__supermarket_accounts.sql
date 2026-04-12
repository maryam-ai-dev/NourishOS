CREATE TABLE supermarket_accounts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id        UUID NOT NULL REFERENCES households(id),
    supermarket_name    VARCHAR(30) NOT NULL,
    account_email       VARCHAR(200),
    is_connected        BOOLEAN NOT NULL DEFAULT true,
    connected_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_synced_at      TIMESTAMPTZ,
    CONSTRAINT uq_supermarket_household_name UNIQUE (household_id, supermarket_name)
);
