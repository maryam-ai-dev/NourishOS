"""
Nutrition scorer — per-member nutrition goal fit.
Pure computation — no DB writes.
"""

from dataclasses import dataclass, field
from typing import List, Dict, Optional
from uuid import UUID
from decimal import Decimal


@dataclass
class MealNutrition:
    meal_id: UUID
    protein_grams: Decimal
    calories: Decimal


@dataclass
class MemberGoal:
    member_id: UUID
    display_name: str
    protein_target: Optional[Decimal] = None  # personal goal in grams
    calorie_target: Optional[Decimal] = None


@dataclass
class MemberFit:
    member_id: UUID
    display_name: str
    protein_target: Optional[Decimal]
    protein_actual: Decimal
    protein_goal_met: bool


@dataclass
class NutritionScore:
    protein_grams: Decimal
    calories_total: Decimal
    protein_goal_met: bool
    member_fit_summary: List[MemberFit]


def score_nutrition(
    meal: MealNutrition,
    servings: int,
    household_protein_goal: Optional[Decimal] = None,
    member_goals: Optional[List[MemberGoal]] = None,
) -> NutritionScore:
    """
    Score nutrition for a meal at given servings.
    - proteinGrams = meal.protein_grams * servings
    - caloriesTotal = meal.calories * servings
    - proteinGoalMet = total protein >= household goal
    - memberFitSummary = per-member protein goal check
    """
    if member_goals is None:
        member_goals = []

    total_protein = meal.protein_grams * Decimal(str(servings))
    total_calories = meal.calories * Decimal(str(servings))

    household_met = True
    if household_protein_goal is not None:
        household_met = total_protein >= household_protein_goal

    member_fits = []
    for mg in member_goals:
        actual = total_protein  # each member gets the full meal total (simplified)
        met = True
        if mg.protein_target is not None:
            met = actual >= mg.protein_target
        member_fits.append(MemberFit(
            member_id=mg.member_id,
            display_name=mg.display_name,
            protein_target=mg.protein_target,
            protein_actual=actual,
            protein_goal_met=met,
        ))

    return NutritionScore(
        protein_grams=total_protein,
        calories_total=total_calories,
        protein_goal_met=household_met,
        member_fit_summary=member_fits,
    )
