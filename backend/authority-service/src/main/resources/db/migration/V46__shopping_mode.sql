ALTER TABLE replenishment_requests
    ADD COLUMN shopping_mode VARCHAR(20) NOT NULL DEFAULT 'IN_STORE';
