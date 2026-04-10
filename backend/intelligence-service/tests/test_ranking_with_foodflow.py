"""Tests for food flow signals wired into recommendation ranking."""

from decimal import Decimal
from uuid import uuid4
from types import SimpleNamespace

from app.services.recommendation.meal_ranker import rank_candidates


def _meal(name="Test", protein=30, sustainability=0.7, refs=None):
    return SimpleNamespace(
        id=uuid4(),
        name=name,
        meal_type="DINNER",
        estimated_protein_grams=Decimal(str(protein)),
        estimated_calories=Decimal("500"),
        sustainability_score=Decimal(str(sustainability)),
        ingredient_refs=refs or [],
    )


class TestFoodFlowInRanking:
    def test_abandoned_meal_ranks_lower(self):
        """Meal with ABANDONED history (low reliability) ranks lower."""
        good_meal = _meal("Reliable Pasta", protein=30, sustainability=0.7)
        bad_meal = _meal("Unreliable Soufflé", protein=30, sustainability=0.7)

        results = rank_candidates(
            [good_meal, bad_meal],
            protein_target=30.0,
            reliability_scores={
                str(good_meal.id): 0.9,
                str(bad_meal.id): 0.2,  # low reliability from ABANDONED history
            },
        )

        good_score = next(r for r in results if r["meal"].name == "Reliable Pasta")
        bad_score = next(r for r in results if r["meal"].name == "Unreliable Soufflé")
        assert good_score["composite_score"] > bad_score["composite_score"]
        assert good_score["score_breakdown"]["reliability"] > bad_score["score_breakdown"]["reliability"]

    def test_recurring_waste_reduces_sustainability(self):
        """Meal with lower sustainability_score (from RECURRING_WASTE) ranks lower on that dimension."""
        clean_meal = _meal("Clean Meal", sustainability=0.9)
        waste_meal = _meal("Waste Meal", sustainability=0.3)  # penalized by RECURRING_WASTE

        results = rank_candidates([clean_meal, waste_meal])

        clean = next(r for r in results if r["meal"].name == "Clean Meal")
        waste = next(r for r in results if r["meal"].name == "Waste Meal")
        assert clean["score_breakdown"]["sustainability"] > waste["score_breakdown"]["sustainability"]

    def test_score_breakdown_reflects_food_flow(self):
        meal = _meal("Test")
        results = rank_candidates(
            [meal],
            reliability_scores={str(meal.id): 0.3},
        )
        assert results[0]["score_breakdown"]["reliability"] == 0.3
