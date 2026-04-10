ALTER TABLE replenishment_suggestions
    ADD COLUMN replenishment_request_id UUID REFERENCES replenishment_requests(id);
