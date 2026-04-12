CREATE TABLE shopping_list_items (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    replenishment_request_id    UUID NOT NULL REFERENCES replenishment_requests(id),
    ingredient_id               UUID REFERENCES ingredients(id),
    ingredient_name             VARCHAR(200) NOT NULL,
    quantity                    DECIMAL(10,2) NOT NULL,
    unit                        VARCHAR(20) NOT NULL,
    category                    VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    is_checked                  BOOLEAN NOT NULL DEFAULT false,
    checked_at                  TIMESTAMPTZ
);

CREATE INDEX idx_shopping_list_request ON shopping_list_items(replenishment_request_id);
