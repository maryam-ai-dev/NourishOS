"""Sprint 20.4: Food flow regression test with 4-week fixture data."""

from datetime import datetime, timedelta
from decimal import Decimal
from uuid import uuid4

from app.services.foodflow.consumption_analyzer import ConsumptionEvent, analyze_consumption
from app.services.foodflow.waste_pattern_detector import WasteEvent, analyze_waste_pattern, detect_recurring_waste
from app.services.foodflow.stock_flow_model import InventoryAdjustment, analyze_stock_flow
from app.services.foodflow.meal_reliability_analyzer import MealOutcomeEvent, analyze_meal_reliability


# Fixture IDs
SPINACH = uuid4()
CHICKEN = uuid4()
RICE = uuid4()
PASTA_MEAL = uuid4()
SOUFFLE_MEAL = uuid4()

BASE = datetime(2026, 3, 16)  # 4 weeks ago


def _consumption_fixtures():
    events = []
    for week in range(4):
        d = BASE + timedelta(weeks=week)
        events.append(ConsumptionEvent(id=uuid4(), ingredient_id=CHICKEN, quantity=Decimal("300"), unit="g", source="PLANNED_MEAL", calories=Decimal("400"), created_at=d))
        events.append(ConsumptionEvent(id=uuid4(), ingredient_id=RICE, quantity=Decimal("200"), unit="g", source="PLANNED_MEAL", calories=Decimal("250"), created_at=d + timedelta(days=1)))
        events.append(ConsumptionEvent(id=uuid4(), ingredient_id=SPINACH, quantity=Decimal("100"), unit="g", source="PLANNED_MEAL", calories=Decimal("30"), created_at=d + timedelta(days=2)))
    return events


def _waste_fixtures():
    events = []
    for week in range(3):
        d = BASE + timedelta(weeks=week)
        events.append(WasteEvent(id=uuid4(), ingredient_id=SPINACH, quantity=Decimal("80"), unit="g", reason="EXPIRED", created_at=d + timedelta(days=5)))
    return events


def _adjustment_fixtures():
    adjs = []
    for week in range(4):
        adjs.append(InventoryAdjustment(id=uuid4(), ingredient_id=RICE, adjustment_type="PURCHASE", quantity=Decimal("1000"), unit="g"))
        adjs.append(InventoryAdjustment(id=uuid4(), ingredient_id=RICE, adjustment_type="CONSUMPTION", quantity=Decimal("200"), unit="g"))
    return adjs


def _meal_outcome_fixtures():
    return [
        MealOutcomeEvent(id=uuid4(), meal_option_id=PASTA_MEAL, outcome="COMPLETED"),
        MealOutcomeEvent(id=uuid4(), meal_option_id=PASTA_MEAL, outcome="COMPLETED"),
        MealOutcomeEvent(id=uuid4(), meal_option_id=PASTA_MEAL, outcome="COMPLETED"),
        MealOutcomeEvent(id=uuid4(), meal_option_id=SOUFFLE_MEAL, outcome="ABANDONED"),
        MealOutcomeEvent(id=uuid4(), meal_option_id=SOUFFLE_MEAL, outcome="ABANDONED"),
        MealOutcomeEvent(id=uuid4(), meal_option_id=SOUFFLE_MEAL, outcome="COMPLETED"),
    ]


class TestFoodFlowRegression:
    def test_recurring_waste_flagged(self):
        waste_events = _waste_fixtures()
        recurring = detect_recurring_waste(waste_events)
        assert SPINACH in recurring, "Spinach wasted 3x should be RECURRING_WASTE"

    def test_over_bought_flagged(self):
        adjs = _adjustment_fixtures()
        summary = analyze_stock_flow(adjs, avg_weekly_usage={RICE: Decimal("50")})
        assert RICE in summary.over_bought_ingredients, "Rice purchased 4000g with 50g/week usage = over-bought"

    def test_reliable_meal_identified(self):
        outcomes = _meal_outcome_fixtures()
        reliability = analyze_meal_reliability(outcomes)
        assert PASTA_MEAL in reliability
        assert reliability[PASTA_MEAL].completion_rate == 1.0
        assert reliability[PASTA_MEAL].is_low_reliability is False
        assert reliability[SOUFFLE_MEAL].is_low_reliability is True

    def test_all_three_conditions_in_single_run(self):
        """Full analysis: RECURRING_WASTE + over-bought + reliable meal all present."""
        consumption = _consumption_fixtures()
        waste = _waste_fixtures()
        adjs = _adjustment_fixtures()
        outcomes = _meal_outcome_fixtures()

        profile = analyze_consumption(consumption, weeks=4)
        total_consumed = sum(e.quantity for e in consumption)
        waste_pattern = analyze_waste_pattern(waste, total_consumed=total_consumed)
        stock_flow = analyze_stock_flow(adjs, avg_weekly_usage={RICE: Decimal("50")})
        reliability = analyze_meal_reliability(outcomes)

        assert len(waste_pattern.frequently_wasted_ingredients) > 0
        assert len(stock_flow.over_bought_ingredients) > 0
        assert any(r.completion_rate == 1.0 for r in reliability.values())

    def test_consistent_across_repeated_runs(self):
        """Same fixture data produces same results."""
        waste1 = analyze_waste_pattern(_waste_fixtures(), total_consumed=Decimal("1200"))
        waste2 = analyze_waste_pattern(_waste_fixtures(), total_consumed=Decimal("1200"))
        assert waste1.waste_ratio == waste2.waste_ratio
        assert len(waste1.frequently_wasted_ingredients) == len(waste2.frequently_wasted_ingredients)
