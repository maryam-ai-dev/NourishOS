"""Tests for ingredient lifecycle analyzer."""

from datetime import datetime, timedelta
from decimal import Decimal
from uuid import uuid4

from app.services.foodflow.ingredient_lifecycle_analyzer import (
    LifecycleEvent,
    analyze_lifecycle,
)


def _event(ingredient_id, event_type, quantity, days_offset=0):
    base = datetime(2026, 4, 1)
    return LifecycleEvent(
        ingredient_id=ingredient_id,
        event_type=event_type,
        quantity=Decimal(str(quantity)),
        created_at=base + timedelta(days=days_offset),
    )


class TestIngredientLifecycle:
    def test_returns_per_ingredient_stats(self):
        ing = uuid4()
        events = [
            _event(ing, "PURCHASE", 500, 0),
            _event(ing, "CONSUMPTION", 300, 3),
        ]
        result = analyze_lifecycle(events)
        assert ing in result
        lifecycle = result[ing]
        assert lifecycle.avg_days_to_use >= 0
        assert 0.0 <= lifecycle.waste_probability <= 1.0

    def test_avg_days_to_use(self):
        ing = uuid4()
        events = [
            _event(ing, "PURCHASE", 500, 0),
            _event(ing, "CONSUMPTION", 200, 5),
            _event(ing, "CONSUMPTION", 200, 7),
        ]
        result = analyze_lifecycle(events)
        # First consumption: 5 days after purchase
        # Second consumption: 7 days after purchase
        # Average: (5 + 7) / 2 = 6
        assert result[ing].avg_days_to_use == 6.0

    def test_waste_probability_zero_when_never_wasted(self):
        ing = uuid4()
        events = [
            _event(ing, "PURCHASE", 500, 0),
            _event(ing, "CONSUMPTION", 500, 3),
        ]
        result = analyze_lifecycle(events)
        assert result[ing].waste_probability == 0.0

    def test_waste_probability_in_range(self):
        ing = uuid4()
        events = [
            _event(ing, "PURCHASE", 500, 0),
            _event(ing, "CONSUMPTION", 200, 2),
            _event(ing, "WASTE", 100, 5),
        ]
        result = analyze_lifecycle(events)
        # 1 waste / (1 consumption + 1 waste) = 0.5
        assert result[ing].waste_probability == 0.5
        assert 0.0 <= result[ing].waste_probability <= 1.0

    def test_multiple_ingredients(self):
        ing_a = uuid4()
        ing_b = uuid4()
        events = [
            _event(ing_a, "PURCHASE", 500, 0),
            _event(ing_a, "CONSUMPTION", 500, 2),
            _event(ing_b, "PURCHASE", 200, 0),
            _event(ing_b, "WASTE", 200, 7),
        ]
        result = analyze_lifecycle(events)
        assert ing_a in result
        assert ing_b in result
        assert result[ing_a].waste_probability == 0.0
        assert result[ing_b].waste_probability == 1.0

    def test_empty_events(self):
        result = analyze_lifecycle([])
        assert len(result) == 0

    def test_totals_correct(self):
        ing = uuid4()
        events = [
            _event(ing, "PURCHASE", 300, 0),
            _event(ing, "PURCHASE", 200, 1),
            _event(ing, "CONSUMPTION", 400, 3),
            _event(ing, "WASTE", 50, 5),
        ]
        result = analyze_lifecycle(events)
        assert result[ing].total_purchased == Decimal("500")
        assert result[ing].total_consumed == Decimal("400")
        assert result[ing].total_wasted == Decimal("50")
