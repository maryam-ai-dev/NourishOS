"""Tests for nutrition scorer."""

from decimal import Decimal
from uuid import uuid4

from app.services.nutrition.nutrition_scorer import (
    MealNutrition,
    MemberGoal,
    score_nutrition,
)


class TestNutritionScorer:
    def test_returns_without_error(self):
        meal = MealNutrition(meal_id=uuid4(), protein_grams=Decimal("40"), calories=Decimal("500"))
        result = score_nutrition(meal, servings=3)
        assert result is not None

    def test_3_servings_40g_protein_meets_120g_goal(self):
        meal = MealNutrition(meal_id=uuid4(), protein_grams=Decimal("40"), calories=Decimal("500"))
        result = score_nutrition(meal, servings=3, household_protein_goal=Decimal("120"))
        assert result.protein_grams == Decimal("120")
        assert result.protein_goal_met is True

    def test_insufficient_protein_fails_goal(self):
        meal = MealNutrition(meal_id=uuid4(), protein_grams=Decimal("30"), calories=Decimal("400"))
        result = score_nutrition(meal, servings=2, household_protein_goal=Decimal("100"))
        assert result.protein_grams == Decimal("60")
        assert result.protein_goal_met is False

    def test_member_personal_goal_checked_separately(self):
        meal = MealNutrition(meal_id=uuid4(), protein_grams=Decimal("40"), calories=Decimal("500"))
        member = MemberGoal(
            member_id=uuid4(),
            display_name="Alice",
            protein_target=Decimal("60"),
        )
        result = score_nutrition(
            meal, servings=3,
            household_protein_goal=Decimal("120"),
            member_goals=[member],
        )
        assert result.protein_goal_met is True  # household 120g met
        assert len(result.member_fit_summary) == 1
        alice = result.member_fit_summary[0]
        assert alice.protein_goal_met is True  # 120g >= 60g personal goal

    def test_member_goal_not_met(self):
        meal = MealNutrition(meal_id=uuid4(), protein_grams=Decimal("20"), calories=Decimal("300"))
        member = MemberGoal(
            member_id=uuid4(),
            display_name="Bob",
            protein_target=Decimal("60"),
        )
        result = score_nutrition(meal, servings=2, member_goals=[member])
        bob = result.member_fit_summary[0]
        assert bob.protein_actual == Decimal("40")
        assert bob.protein_goal_met is False

    def test_calories_total(self):
        meal = MealNutrition(meal_id=uuid4(), protein_grams=Decimal("30"), calories=Decimal("450"))
        result = score_nutrition(meal, servings=4)
        assert result.calories_total == Decimal("1800")

    def test_no_goal_defaults_to_met(self):
        meal = MealNutrition(meal_id=uuid4(), protein_grams=Decimal("10"), calories=Decimal("200"))
        result = score_nutrition(meal, servings=1)
        assert result.protein_goal_met is True  # no goal → met by default

    def test_pure_computation(self):
        meal = MealNutrition(meal_id=uuid4(), protein_grams=Decimal("40"), calories=Decimal("500"))
        score_nutrition(meal, servings=1)
        # No side effects to check — pure computation
