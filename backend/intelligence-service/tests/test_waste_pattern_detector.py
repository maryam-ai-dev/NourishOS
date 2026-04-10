"""Tests for waste pattern detector."""

import pytest
from decimal import Decimal
from uuid import uuid4

from app.services.foodflow.waste_pattern_detector import (
    WasteEvent,
    detect_recurring_waste,
    analyze_waste_pattern,
)


def _waste(ingredient_id, quantity=100, reason="EXPIRED"):
    return WasteEvent(
        id=uuid4(),
        ingredient_id=ingredient_id,
        quantity=Decimal(str(quantity)),
        unit="g",
        reason=reason,
    )


class TestRecurringWaste:
    def test_three_events_flagged(self):
        ing = uuid4()
        events = [_waste(ing), _waste(ing), _waste(ing)]
        flagged = detect_recurring_waste(events)
        assert ing in flagged

    def test_exactly_two_events_flagged(self):
        ing = uuid4()
        events = [_waste(ing), _waste(ing)]
        flagged = detect_recurring_waste(events)
        assert ing in flagged

    def test_one_event_not_flagged(self):
        ing = uuid4()
        events = [_waste(ing)]
        flagged = detect_recurring_waste(events)
        assert ing not in flagged

    def test_boundary_at_two(self):
        """≥2 is the threshold — 2 is in, 1 is out."""
        ing_a = uuid4()  # 2 events
        ing_b = uuid4()  # 1 event
        events = [_waste(ing_a), _waste(ing_a), _waste(ing_b)]
        flagged = detect_recurring_waste(events)
        assert ing_a in flagged
        assert ing_b not in flagged

    def test_empty_events(self):
        flagged = detect_recurring_waste([])
        assert len(flagged) == 0

    def test_pure_computation(self):
        """RECURRING_WASTE is a runtime flag, not a DB column."""
        ing = uuid4()
        events = [_waste(ing), _waste(ing)]
        flagged = detect_recurring_waste(events)
        assert isinstance(flagged, set)


class TestWastePatternSummary:
    def test_waste_ratio_matches_manual_calculation(self):
        """200g wasted + 800g consumed → wasteRatio = 0.2"""
        ing = uuid4()
        events = [_waste(ing, 100), _waste(ing, 100)]
        pattern = analyze_waste_pattern(events, total_consumed=Decimal("800"))
        assert pattern.waste_ratio == Decimal("0.2")

    def test_waste_ratio_zero_waste(self):
        """0g wasted → wasteRatio = 0.0 (not NaN, not error)"""
        pattern = analyze_waste_pattern([], total_consumed=Decimal("500"))
        assert pattern.waste_ratio == Decimal("0")

    def test_waste_ratio_zero_denominator(self):
        """0 waste + 0 consumed → wasteRatio = 0.0 (not NaN)"""
        pattern = analyze_waste_pattern([], total_consumed=Decimal("0"))
        assert pattern.waste_ratio == Decimal("0")

    def test_waste_ratio_4_decimal_precision(self):
        ing = uuid4()
        events = [_waste(ing, 150)]
        pattern = analyze_waste_pattern(events, total_consumed=Decimal("850"))
        # 150 / (150 + 850) = 0.15
        assert round(pattern.waste_ratio, 4) == Decimal("0.15")

    def test_frequently_wasted_same_as_recurring(self):
        ing_a = uuid4()
        ing_b = uuid4()
        events = [_waste(ing_a), _waste(ing_a), _waste(ing_b)]
        pattern = analyze_waste_pattern(events, total_consumed=Decimal("500"))
        assert ing_a in pattern.frequently_wasted_ingredients
        assert ing_b not in pattern.frequently_wasted_ingredients

    def test_avg_weekly_waste_grams(self):
        ing = uuid4()
        events = [_waste(ing, 200), _waste(ing, 200)]
        pattern = analyze_waste_pattern(events, weeks=4)
        # 400g / 4 weeks = 100g
        assert pattern.avg_weekly_waste_grams == Decimal("100")

    def test_top_waste_reasons(self):
        ing = uuid4()
        events = [
            _waste(ing, reason="EXPIRED"),
            _waste(ing, reason="EXPIRED"),
            _waste(ing, reason="OVERCOOKED"),
        ]
        pattern = analyze_waste_pattern(events)
        assert pattern.top_waste_reasons["EXPIRED"] == 2
        assert pattern.top_waste_reasons["OVERCOOKED"] == 1
