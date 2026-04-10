"""Tests for stock flow model."""

import pytest
from decimal import Decimal
from uuid import uuid4

from app.services.foodflow.stock_flow_model import (
    InventoryAdjustment,
    compute_net_flow,
    analyze_stock_flow,
)


def _adj(ingredient_id, adj_type, quantity):
    return InventoryAdjustment(
        id=uuid4(),
        ingredient_id=ingredient_id,
        adjustment_type=adj_type,
        quantity=Decimal(str(quantity)),
        unit="g",
    )


class TestNetFlow:
    def test_positive_net_flow(self):
        """4 purchases, 0 consumption → positive net flow."""
        ing = uuid4()
        adjustments = [
            _adj(ing, "PURCHASE", 100),
            _adj(ing, "PURCHASE", 100),
            _adj(ing, "PURCHASE", 100),
            _adj(ing, "PURCHASE", 100),
        ]
        flows = compute_net_flow(adjustments)
        assert flows[ing] == Decimal("400")
        assert flows[ing] > 0

    def test_negative_net_flow(self):
        """Consumed faster than purchased → negative net flow."""
        ing = uuid4()
        adjustments = [
            _adj(ing, "PURCHASE", 100),
            _adj(ing, "CONSUMPTION", 200),
            _adj(ing, "WASTE", 50),
        ]
        flows = compute_net_flow(adjustments)
        assert flows[ing] == Decimal("-150")
        assert flows[ing] < 0

    def test_net_flow_multiple_ingredients(self):
        ing_a = uuid4()
        ing_b = uuid4()
        adjustments = [
            _adj(ing_a, "PURCHASE", 500),
            _adj(ing_a, "CONSUMPTION", 300),
            _adj(ing_b, "PURCHASE", 200),
        ]
        flows = compute_net_flow(adjustments)
        assert flows[ing_a] == Decimal("200")
        assert flows[ing_b] == Decimal("200")

    def test_empty_adjustments(self):
        flows = compute_net_flow([])
        assert len(flows) == 0

    def test_pure_computation(self):
        ing = uuid4()
        adjustments = [_adj(ing, "PURCHASE", 100)]
        flows = compute_net_flow(adjustments)
        assert isinstance(flows, dict)
