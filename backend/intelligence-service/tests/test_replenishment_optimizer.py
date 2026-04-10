"""Tests for replenishment optimizer."""

from decimal import Decimal
from uuid import uuid4

from app.services.forecasting.replenishment_optimizer import (
    StockoutInput,
    optimize_replenishment,
)


def _input(usage=100, urgency="WARNING", unit="g", recurring=False, waste_ratio=0):
    return StockoutInput(
        ingredient_id=uuid4(),
        weekly_usage_rate=Decimal(str(usage)),
        urgency=urgency,
        unit=unit,
        is_recurring_waste=recurring,
        waste_ratio=Decimal(str(waste_ratio)),
    )


class TestBaseQuantity:
    def test_returns_without_error(self):
        result = optimize_replenishment([_input()])
        assert len(result) == 1

    def test_covers_at_least_one_week(self):
        inp = _input(usage=100, urgency="WARNING")
        result = optimize_replenishment([inp])
        assert result[0].base_quantity >= Decimal("100")

    def test_critical_covers_two_weeks(self):
        inp = _input(usage=100, urgency="CRITICAL")
        result = optimize_replenishment([inp])
        assert result[0].base_quantity == Decimal("200")

    def test_quantity_in_canonical_unit(self):
        inp = _input(usage=100, unit="g")
        result = optimize_replenishment([inp])
        assert result[0].unit == "g"

    def test_zero_usage_excluded(self):
        inp = _input(usage=0)
        result = optimize_replenishment([inp])
        assert len(result) == 0


class TestWasteAdjustment:
    def test_recurring_waste_gets_lower_quantity(self):
        inp = _input(usage=100, urgency="WARNING", recurring=True, waste_ratio=0.2)
        result = optimize_replenishment([inp], recurring_waste_ids={inp.ingredient_id})
        assert result[0].adjusted_quantity < result[0].base_quantity

    def test_adjusted_quantity_formula(self):
        """adjustedQuantity = baseQuantity * (1 - wasteRatio)"""
        inp = _input(usage=100, urgency="WARNING", recurring=True, waste_ratio=0.2)
        result = optimize_replenishment([inp])
        expected = Decimal("100") * (1 - Decimal("0.2"))
        assert result[0].adjusted_quantity == expected

    def test_adjusted_for_waste_flag(self):
        inp_waste = _input(usage=100, recurring=True, waste_ratio=0.3)
        inp_normal = _input(usage=100)
        results = optimize_replenishment([inp_waste, inp_normal])
        assert results[0].adjusted_for_waste is True
        assert results[1].adjusted_for_waste is False

    def test_non_waste_unchanged(self):
        inp = _input(usage=100)
        result = optimize_replenishment([inp])
        assert result[0].adjusted_quantity == result[0].base_quantity
        assert result[0].adjusted_for_waste is False

    def test_reason_non_empty(self):
        inp = _input(usage=100, recurring=True, waste_ratio=0.15)
        result = optimize_replenishment([inp])
        assert result[0].reason != ""
