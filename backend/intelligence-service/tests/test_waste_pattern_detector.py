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
