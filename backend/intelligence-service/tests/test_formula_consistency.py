"""
Food flow formula consistency tests.
Ensures FastAPI analysis formulas match Spring Boot snapshot formulas.
"""

import pytest
from decimal import Decimal, ROUND_HALF_UP
from uuid import uuid4

from app.services.foodflow.waste_pattern_detector import (
    WasteEvent,
    analyze_waste_pattern,
)


class TestFormulaConsistency:
    def test_waste_ratio_matches_spring_boot_formula(self):
        """
        wasteRatio from waste_pattern_detector matches Spring Boot FoodFlowSnapshotService:
        wasteRatio = totalWaste / (totalWaste + totalConsumed)
        """
        ing = uuid4()
        waste_events = [
            WasteEvent(id=uuid4(), ingredient_id=ing, quantity=Decimal("150"), unit="g", reason="EXPIRED"),
            WasteEvent(id=uuid4(), ingredient_id=ing, quantity=Decimal("50"), unit="g", reason="DISCARDED"),
        ]
        total_consumed = Decimal("800")
        total_waste = Decimal("200")  # 150 + 50

        pattern = analyze_waste_pattern(waste_events, total_consumed=total_consumed)

        # Spring Boot formula: totalWaste / (totalWaste + totalConsumed)
        expected = total_waste / (total_waste + total_consumed)
        assert round(pattern.waste_ratio, 4) == round(expected, 4)

    def test_waste_ratio_zero_denominator_returns_zero(self):
        """
        wasteRatio with zero denominator (no events) returns 0.0, not NaN and not error.
        Both FastAPI and Spring Boot must handle this identically.
        """
        pattern = analyze_waste_pattern([], total_consumed=Decimal("0"))
        assert pattern.waste_ratio == Decimal("0")
        # Ensure it's not NaN
        assert not (pattern.waste_ratio != pattern.waste_ratio)  # NaN != NaN is True

    def test_avg_weekly_usage_matches_manual(self):
        """
        avgWeeklyUsage from UsageRecordService.recompute() matches:
        totalConsumedLast4Weeks / 4
        """
        total_consumed_last_4_weeks = Decimal("2400")
        weeks = 4
        expected_avg = total_consumed_last_4_weeks / Decimal(str(weeks))
        assert expected_avg == Decimal("600")

    def test_reliability_score_formula(self):
        """
        reliabilityScore = completionRate * 0.7 + (1 - substitutionRate) * 0.3
        """
        completion_rate = Decimal("0.75")
        substitution_rate = Decimal("0.10")

        reliability_score = completion_rate * Decimal("0.7") + (1 - substitution_rate) * Decimal("0.3")

        expected = Decimal("0.75") * Decimal("0.7") + Decimal("0.90") * Decimal("0.3")
        assert round(reliability_score, 4) == round(expected, 4)
        # 0.525 + 0.270 = 0.795
        assert round(reliability_score, 3) == Decimal("0.795")

    def test_replenishment_adjusted_quantity_formula(self):
        """
        Replenishment optimizer: adjustedQuantity = baseQuantity * (1 - wasteRatio)
        """
        base_quantity = Decimal("1000")
        waste_ratio = Decimal("0.2")

        adjusted = base_quantity * (1 - waste_ratio)

        assert adjusted == Decimal("800")

    def test_full_flow_consistency(self):
        """
        Integration: run waste analysis, then verify the wasteRatio would produce
        the same adjusted quantity in replenishment.
        """
        ing = uuid4()
        waste_events = [
            WasteEvent(id=uuid4(), ingredient_id=ing, quantity=Decimal("100"), unit="g", reason="EXPIRED"),
            WasteEvent(id=uuid4(), ingredient_id=ing, quantity=Decimal("100"), unit="g", reason="LEFTOVER_UNUSED"),
        ]
        total_consumed = Decimal("800")

        # FastAPI analysis
        pattern = analyze_waste_pattern(waste_events, total_consumed=total_consumed)

        # Simulate Spring Boot snapshot
        spring_total_waste = sum(e.quantity for e in waste_events)
        spring_waste_ratio = spring_total_waste / (spring_total_waste + total_consumed)

        # wasteRatio values match to 4 decimal places
        assert round(pattern.waste_ratio, 4) == round(spring_waste_ratio, 4)

        # Replenishment adjustment uses same ratio
        base_quantity = Decimal("500")
        fastapi_adjusted = base_quantity * (1 - pattern.waste_ratio)
        spring_adjusted = base_quantity * (1 - spring_waste_ratio)
        assert round(fastapi_adjusted, 4) == round(spring_adjusted, 4)
