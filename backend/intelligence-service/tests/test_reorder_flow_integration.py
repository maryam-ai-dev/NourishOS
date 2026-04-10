"""Integration test: reorder flow with waste adjustment (Sprint 19.10)."""

from decimal import Decimal
from uuid import uuid4

from app.services.forecasting.demand_model import IngredientStock, predict_stockouts
from app.services.forecasting.replenishment_optimizer import StockoutInput, optimize_replenishment
from app.services.foodflow.waste_pattern_detector import WasteEvent, detect_recurring_waste
from datetime import date


class TestReorderFlowIntegration:
    def test_full_reorder_flow_with_waste_adjustment(self):
        """
        End-to-end:
        1. Detect RECURRING_WASTE from waste events
        2. Predict stockouts from inventory
        3. Generate replenishment suggestions with waste adjustment
        4. Verify adjustedForWaste=true and reduced quantity
        """
        # Step 1: Detect recurring waste
        spinach_id = uuid4()
        waste_events = [
            WasteEvent(id=uuid4(), ingredient_id=spinach_id, quantity=Decimal("100"), unit="g", reason="EXPIRED"),
            WasteEvent(id=uuid4(), ingredient_id=spinach_id, quantity=Decimal("80"), unit="g", reason="LEFTOVER_UNUSED"),
            WasteEvent(id=uuid4(), ingredient_id=spinach_id, quantity=Decimal("60"), unit="g", reason="EXPIRED"),
        ]
        recurring = detect_recurring_waste(waste_events)
        assert spinach_id in recurring

        # Step 2: Predict stockouts
        stocks = [
            IngredientStock(
                ingredient_id=spinach_id,
                current_quantity=Decimal("80"),
                unit="g",
                avg_weekly_usage=Decimal("200"),
                par_level_minimum=Decimal("50"),
            ),
        ]
        predictions = predict_stockouts(stocks, date(2026, 4, 13))
        assert len(predictions) == 1
        assert predictions[0].urgency in ("CRITICAL", "WARNING")

        # Step 3: Generate replenishment with waste adjustment
        waste_ratio = Decimal("0.3")  # 30% waste rate for spinach
        inputs = [
            StockoutInput(
                ingredient_id=spinach_id,
                weekly_usage_rate=Decimal("200"),
                urgency=predictions[0].urgency,
                unit="g",
                is_recurring_waste=True,
                waste_ratio=waste_ratio,
            ),
        ]
        suggestions = optimize_replenishment(inputs, recurring_waste_ids=recurring)

        # Step 4: Verify
        assert len(suggestions) == 1
        s = suggestions[0]
        assert s.adjusted_for_waste is True
        assert s.adjusted_quantity < s.base_quantity
        # Formula: adjusted = base * (1 - 0.3)
        expected_adjusted = s.base_quantity * (1 - waste_ratio)
        assert s.adjusted_quantity == expected_adjusted
        assert "waste" in s.reason.lower()

    def test_non_waste_ingredient_not_adjusted(self):
        """Non-RECURRING_WASTE ingredient gets full quantity."""
        chicken_id = uuid4()
        inputs = [
            StockoutInput(
                ingredient_id=chicken_id,
                weekly_usage_rate=Decimal("500"),
                urgency="WARNING",
                unit="g",
            ),
        ]
        suggestions = optimize_replenishment(inputs)
        assert len(suggestions) == 1
        assert suggestions[0].adjusted_for_waste is False
        assert suggestions[0].adjusted_quantity == suggestions[0].base_quantity

    def test_waste_adjusted_suggestion_has_explanation(self):
        """adjustedForWaste=true suggestions have explanation referencing waste history."""
        ing_id = uuid4()
        inputs = [
            StockoutInput(
                ingredient_id=ing_id,
                weekly_usage_rate=Decimal("100"),
                urgency="CRITICAL",
                unit="g",
                is_recurring_waste=True,
                waste_ratio=Decimal("0.25"),
            ),
        ]
        suggestions = optimize_replenishment(inputs)
        assert suggestions[0].reason != ""
        assert "waste" in suggestions[0].reason.lower()
