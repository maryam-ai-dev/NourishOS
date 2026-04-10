"""Tests for flow insight builder."""

from datetime import date
from decimal import Decimal
from uuid import uuid4

from app.services.foodflow.flow_insight_builder import build_insights, CATEGORIES
from app.services.foodflow.waste_pattern_detector import WastePattern
from app.services.foodflow.stock_flow_model import StockFlowSummary
from app.services.foodflow.meal_reliability_analyzer import MealReliability
from app.services.foodflow.consumption_analyzer import ConsumptionProfile, TopIngredient


def _waste_pattern(recurring_ids=None, waste_ratio=Decimal("0.15")):
    return WastePattern(
        frequently_wasted_ingredients=recurring_ids or [],
        avg_weekly_waste_grams=Decimal("200"),
        top_waste_reasons={"EXPIRED": 3},
        waste_ratio=waste_ratio,
    )


def _stock_flow(under=None, over=None):
    return StockFlowSummary(
        net_flow_by_ingredient={},
        over_bought_ingredients=over or [],
        under_supplied_ingredients=under or [],
    )


def _reliability(meal_id, score):
    return MealReliability(
        meal_option_id=meal_id,
        completion_rate=score,
        abandonment_rate=1 - score,
        substitution_rate=0.0,
        reliability_score=score,
        is_low_reliability=score < 0.5,
    )


def _consumption():
    return ConsumptionProfile(
        top_ingredients=[TopIngredient(uuid4(), Decimal("500"), "g", 5)],
        avg_weekly_calories=Decimal("2000"),
        meal_type_frequency={"PLANNED_MEAL": 10},
        peak_consumption_days=["MONDAY", "FRIDAY"],
    )


class TestFlowInsightBuilder:
    def test_at_least_one_insight_per_category(self):
        recurring_id = uuid4()
        under_id = uuid4()
        low_meal = uuid4()

        result = build_insights(
            household_id=uuid4(),
            snapshot_week=date(2026, 4, 13),
            waste_pattern=_waste_pattern(recurring_ids=[recurring_id]),
            stock_flow=_stock_flow(under=[under_id]),
            meal_reliability={low_meal: _reliability(low_meal, 0.3)},
            consumption_profile=_consumption(),
        )

        categories_present = {i.category for i in result.insights}
        for cat in CATEGORIES:
            assert cat in categories_present, f"Missing category: {cat}"

    def test_waste_insight_names_recurring_ingredient(self):
        recurring_id = uuid4()
        result = build_insights(
            household_id=uuid4(),
            snapshot_week=date(2026, 4, 13),
            waste_pattern=_waste_pattern(recurring_ids=[recurring_id]),
        )
        waste_insight = next(i for i in result.insights if i.category == "WASTE")
        assert "Recurring waste" in waste_insight.insight_text

    def test_replenishment_insight_names_under_supplied(self):
        under_id = uuid4()
        result = build_insights(
            household_id=uuid4(),
            snapshot_week=date(2026, 4, 13),
            stock_flow=_stock_flow(under=[under_id]),
        )
        rep_insight = next(i for i in result.insights if i.category == "REPLENISHMENT")
        assert "ran out of stock" in rep_insight.insight_text

    def test_reliability_insight_for_low_reliability(self):
        meal = uuid4()
        result = build_insights(
            household_id=uuid4(),
            snapshot_week=date(2026, 4, 13),
            meal_reliability={meal: _reliability(meal, 0.3)},
        )
        rel_insight = next(i for i in result.insights if i.category == "RELIABILITY")
        assert "low reliability" in rel_insight.insight_text

    def test_nutrition_insight_includes_calories(self):
        result = build_insights(
            household_id=uuid4(),
            snapshot_week=date(2026, 4, 13),
            consumption_profile=_consumption(),
        )
        nut_insight = next(i for i in result.insights if i.category == "NUTRITION")
        assert "2000" in nut_insight.insight_text

    def test_empty_data_still_produces_insights(self):
        result = build_insights(
            household_id=uuid4(),
            snapshot_week=date(2026, 4, 13),
        )
        assert len(result.insights) >= 3  # REPLENISHMENT, RELIABILITY, NUTRITION always present
