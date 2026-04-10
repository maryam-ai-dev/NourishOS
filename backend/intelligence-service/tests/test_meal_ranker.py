"""Tests for meal ranker composite score formula integration."""

from decimal import Decimal
from uuid import uuid4
from types import SimpleNamespace

from app.services.recommendation.meal_ranker import rank_candidates


def _meal(name="Test Meal", protein=30, sustainability=0.7, refs=None):
    """Create a mock meal object matching MealOption interface."""
    meal_id = uuid4()
    return SimpleNamespace(
        id=meal_id,
        name=name,
        meal_type="DINNER",
        estimated_protein_grams=Decimal(str(protein)),
        estimated_calories=Decimal("500"),
        sustainability_score=Decimal(str(sustainability)),
        ingredient_refs=refs or [],
    )


class TestCompositeFormula:
    def test_formula_correct(self):
        """
        Composite = 0.25 * preference + 0.25 * protein + 0.25 * sustainability
                   + 0.15 * availability + 0.10 * reliability
        """
        meal = _meal(protein=30, sustainability=0.8)
        results = rank_candidates(
            [meal],
            protein_target=30.0,
            reliability_scores={str(meal.id): 0.9},
            available_ingredient_ids=set(),
        )

        r = results[0]
        bd = r["score_breakdown"]

        # Verify individual scores
        assert bd["preference_fit"] == 0.7  # default
        assert bd["protein_goal"] == 1.0    # 30/30 = 1.0
        assert bd["sustainability"] == 0.8
        assert bd["availability"] == 1.0    # no required refs
        assert bd["reliability"] == 0.9

        # Verify composite matches formula to 4 decimal places
        expected = (
            0.25 * bd["preference_fit"]
            + 0.25 * bd["protein_goal"]
            + 0.25 * bd["sustainability"]
            + 0.15 * bd["availability"]
            + 0.10 * bd["reliability"]
        )
        assert r["composite_score"] == round(expected, 4)

    def test_all_five_dimensions_in_breakdown(self):
        meal = _meal()
        results = rank_candidates([meal])
        bd = results[0]["score_breakdown"]
        assert "preference_fit" in bd
        assert "protein_goal" in bd
        assert "sustainability" in bd
        assert "availability" in bd
        assert "reliability" in bd

    def test_composite_rounded_to_4_decimals(self):
        meal = _meal(protein=17, sustainability=0.333)
        results = rank_candidates([meal], protein_target=50.0)
        composite = results[0]["composite_score"]
        # Ensure it's rounded to 4 decimal places
        assert composite == round(composite, 4)

    def test_ranking_order_by_composite_descending(self):
        high = _meal("High Score", protein=50, sustainability=0.9)
        low = _meal("Low Score", protein=5, sustainability=0.1)
        results = rank_candidates([low, high], protein_target=50.0)
        assert results[0]["meal"].name == "High Score"
        assert results[1]["meal"].name == "Low Score"

    def test_reliability_from_food_flow_analysis(self):
        """Reliability scores fed from meal_reliability_analyzer."""
        meal = _meal()
        results = rank_candidates(
            [meal],
            reliability_scores={str(meal.id): 0.3},
        )
        assert results[0]["score_breakdown"]["reliability"] == 0.3

    def test_default_reliability_when_no_history(self):
        meal = _meal()
        results = rank_candidates([meal])
        assert results[0]["score_breakdown"]["reliability"] == 0.5
