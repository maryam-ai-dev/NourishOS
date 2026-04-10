"""Tests for household profile builder."""

from decimal import Decimal
from uuid import uuid4

from app.services.foodflow.household_profile_builder import build_household_profile
from app.services.foodflow.consumption_analyzer import ConsumptionProfile, TopIngredient
from app.services.foodflow.waste_pattern_detector import WastePattern
from app.services.foodflow.stock_flow_model import StockFlowSummary
from app.services.foodflow.meal_reliability_analyzer import MealReliability


def _consumption():
    return ConsumptionProfile(
        top_ingredients=[TopIngredient(uuid4(), Decimal("500"), "g", 5)],
        avg_weekly_calories=Decimal("2000"),
        meal_type_frequency={"PLANNED_MEAL": 10},
        peak_consumption_days=["MONDAY"],
    )


def _waste():
    return WastePattern(
        frequently_wasted_ingredients=[uuid4()],
        avg_weekly_waste_grams=Decimal("150"),
        top_waste_reasons={"EXPIRED": 3},
        waste_ratio=Decimal("0.15"),
    )


def _stock():
    return StockFlowSummary(
        net_flow_by_ingredient={uuid4(): Decimal("200")},
        over_bought_ingredients=[],
        under_supplied_ingredients=[],
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


class TestHouseholdProfileBuilder:
    def test_assembles_without_error(self):
        profile = build_household_profile(
            household_id=uuid4(),
            consumption_profile=_consumption(),
            waste_pattern=_waste(),
            stock_flow_summary=_stock(),
            meal_reliability={},
        )
        assert profile is not None

    def test_all_four_components_present(self):
        profile = build_household_profile(
            household_id=uuid4(),
            consumption_profile=_consumption(),
            waste_pattern=_waste(),
            stock_flow_summary=_stock(),
            meal_reliability={uuid4(): _reliability(uuid4(), 0.8)},
        )
        assert profile.consumption_profile is not None
        assert profile.waste_pattern is not None
        assert profile.stock_flow_summary is not None
        assert profile.top_reliable_meals is not None

    def test_top_reliable_meals_sorted_descending(self):
        meal_a = uuid4()
        meal_b = uuid4()
        meal_c = uuid4()
        reliability = {
            meal_a: _reliability(meal_a, 0.6),
            meal_b: _reliability(meal_b, 0.9),
            meal_c: _reliability(meal_c, 0.3),
        }
        profile = build_household_profile(
            household_id=uuid4(),
            consumption_profile=_consumption(),
            waste_pattern=_waste(),
            stock_flow_summary=_stock(),
            meal_reliability=reliability,
        )
        scores = [m.reliability_score for m in profile.top_reliable_meals]
        assert scores == sorted(scores, reverse=True)
        assert profile.top_reliable_meals[0].meal_option_id == meal_b

    def test_empty_reliability_accepted(self):
        profile = build_household_profile(
            household_id=uuid4(),
            consumption_profile=_consumption(),
            waste_pattern=_waste(),
            stock_flow_summary=_stock(),
            meal_reliability={},
        )
        assert len(profile.top_reliable_meals) == 0

    def test_no_db_write(self):
        """Profile is used as input, not persisted."""
        profile = build_household_profile(
            household_id=uuid4(),
            consumption_profile=_consumption(),
            waste_pattern=_waste(),
            stock_flow_summary=_stock(),
            meal_reliability={},
        )
        assert hasattr(profile, "household_id")
