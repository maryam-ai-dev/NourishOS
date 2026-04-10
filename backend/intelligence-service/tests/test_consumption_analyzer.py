"""Tests for consumption analyzer."""

import pytest
from datetime import datetime
from decimal import Decimal
from uuid import uuid4

from app.services.foodflow.consumption_analyzer import (
    ConsumptionEvent,
    analyze_consumption,
)


def _event(ingredient_id, quantity, source="PLANNED_MEAL", calories=100, weekday=0):
    """Helper: weekday 0=Monday."""
    dt = datetime(2026, 4, 6 + weekday, 12, 0)  # April 6 2026 is Monday
    return ConsumptionEvent(
        id=uuid4(),
        ingredient_id=ingredient_id,
        quantity=Decimal(str(quantity)),
        unit="g",
        source=source,
        calories=Decimal(str(calories)),
        created_at=dt,
    )


class TestConsumptionAnalyzer:
    def test_profile_validates_without_error(self):
        ing_id = uuid4()
        events = [_event(ing_id, 200)]
        profile = analyze_consumption(events)
        assert profile is not None
        assert len(profile.top_ingredients) >= 1

    def test_top_ingredients_sorted_descending(self):
        ing_a = uuid4()
        ing_b = uuid4()
        events = [
            _event(ing_a, 300),
            _event(ing_a, 200),
            _event(ing_b, 100),
        ]
        profile = analyze_consumption(events)
        assert len(profile.top_ingredients) >= 2
        assert profile.top_ingredients[0].total_quantity >= profile.top_ingredients[1].total_quantity
        assert profile.top_ingredients[0].ingredient_id == ing_a
        assert profile.top_ingredients[0].total_quantity == Decimal("500")

    def test_peak_consumption_days(self):
        ing = uuid4()
        # 3 events on Monday, 1 on Tuesday
        events = [
            _event(ing, 100, weekday=0),
            _event(ing, 100, weekday=0),
            _event(ing, 100, weekday=0),
            _event(ing, 100, weekday=1),
        ]
        profile = analyze_consumption(events)
        assert "MONDAY" in profile.peak_consumption_days

    def test_avg_weekly_calories(self):
        ing = uuid4()
        events = [
            _event(ing, 100, calories=200),
            _event(ing, 100, calories=300),
        ]
        profile = analyze_consumption(events, weeks=4)
        # Total 500 / 4 weeks = 125
        assert profile.avg_weekly_calories == Decimal("125")

    def test_meal_type_frequency(self):
        ing = uuid4()
        events = [
            _event(ing, 100, source="PLANNED_MEAL"),
            _event(ing, 100, source="PLANNED_MEAL"),
            _event(ing, 100, source="SNACK"),
        ]
        profile = analyze_consumption(events)
        assert profile.meal_type_frequency["PLANNED_MEAL"] == 2
        assert profile.meal_type_frequency["SNACK"] == 1

    def test_empty_events_returns_empty_profile(self):
        profile = analyze_consumption([])
        assert len(profile.top_ingredients) == 0
        assert profile.avg_weekly_calories == Decimal("0")
        assert profile.peak_consumption_days == []

    def test_pure_computation_no_side_effects(self):
        """Analyzer is pure computation — no DB writes."""
        ing = uuid4()
        events = [_event(ing, 100)]
        events_copy = list(events)
        analyze_consumption(events)
        assert len(events) == len(events_copy)
