"""
POST /nutrition/score — score nutrition for a meal.
"""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import Optional, List
from uuid import UUID
from decimal import Decimal

from app.services.nutrition.nutrition_scorer import (
    MealNutrition,
    MemberGoal,
    score_nutrition,
)
from app.clients.authority_client import get_meal_catalog

router = APIRouter(prefix="/nutrition", tags=["nutrition"])


class MemberGoalRequest(BaseModel):
    member_id: UUID = Field(alias="memberId")
    display_name: str = Field(alias="displayName")
    protein_target: Optional[float] = Field(None, alias="proteinTarget")
    model_config = {"populate_by_name": True}


class NutritionScoreRequest(BaseModel):
    meal_id: UUID = Field(alias="mealId")
    servings: int = 1
    household_protein_goal: Optional[float] = Field(None, alias="householdProteinGoal")
    member_goals: List[MemberGoalRequest] = Field(default_factory=list, alias="memberGoals")
    model_config = {"populate_by_name": True}


class MemberFitResponse(BaseModel):
    member_id: UUID = Field(alias="memberId")
    display_name: str = Field(alias="displayName")
    protein_target: Optional[float] = Field(None, alias="proteinTarget")
    protein_actual: float = Field(alias="proteinActual")
    protein_goal_met: bool = Field(alias="proteinGoalMet")
    model_config = {"populate_by_name": True}


class NutritionScoreResponse(BaseModel):
    protein_grams: float = Field(alias="proteinGrams")
    calories_total: float = Field(alias="caloriesTotal")
    protein_goal_met: bool = Field(alias="proteinGoalMet")
    member_fit_summary: List[MemberFitResponse] = Field(alias="memberFitSummary")
    model_config = {"populate_by_name": True}


@router.post("/score", response_model=NutritionScoreResponse)
async def nutrition_score(request: NutritionScoreRequest):
    # Look up meal from catalog
    try:
        catalog = get_meal_catalog()
    except Exception:
        raise HTTPException(status_code=404, detail="Could not fetch meal catalog")

    meal_opt = next((m for m in catalog if m.id == request.meal_id), None)
    if meal_opt is None:
        raise HTTPException(status_code=404, detail=f"Meal {request.meal_id} not found")

    meal = MealNutrition(
        meal_id=meal_opt.id,
        protein_grams=meal_opt.estimated_protein_grams or Decimal("0"),
        calories=meal_opt.estimated_calories or Decimal("0"),
    )

    member_goals = [
        MemberGoal(
            member_id=mg.member_id,
            display_name=mg.display_name,
            protein_target=Decimal(str(mg.protein_target)) if mg.protein_target else None,
        )
        for mg in request.member_goals
    ]

    household_goal = Decimal(str(request.household_protein_goal)) if request.household_protein_goal else None

    result = score_nutrition(meal, request.servings, household_goal, member_goals)

    return NutritionScoreResponse(
        protein_grams=float(result.protein_grams),
        calories_total=float(result.calories_total),
        protein_goal_met=result.protein_goal_met,
        member_fit_summary=[
            MemberFitResponse(
                member_id=mf.member_id,
                display_name=mf.display_name,
                protein_target=float(mf.protein_target) if mf.protein_target else None,
                protein_actual=float(mf.protein_actual),
                protein_goal_met=mf.protein_goal_met,
            )
            for mf in result.member_fit_summary
        ],
    )
