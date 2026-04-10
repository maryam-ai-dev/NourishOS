CREATE TABLE audit_records (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type  VARCHAR(50) NOT NULL,
    entity_id   UUID,
    entity_type VARCHAR(50),
    payload     JSONB NOT NULL DEFAULT '{}',
    actor_type  VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);
