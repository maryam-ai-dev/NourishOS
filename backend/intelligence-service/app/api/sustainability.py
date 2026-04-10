"""
POST /sustainability/score — score sustainability for a meal.
"""

from fastapi import APIRouter
from pydantic import BaseModel, Field
from typing import Optional, List, Set
from uuid import UUID

from app.services.sustainability.sustainability_scorer import (
    IngredientContext,
    MealContext,
    score_sustainability,
)

router = APIRouter(prefix="/sustainability", tags=["sustainability"])


class IngredientContextRequest(BaseModel):
    ingredient_id: UUID = Field(alias="ingredientId")
    perishability_class: str = Field(alias="perishabilityClass")
    is_opened: bool = Field(False, alias="isOpened")
    is_recurring_waste: bool = Field(False, alias="isRecurringWaste")
    model_config = {"populate_by_name": True}


class SustainabilityRequest(BaseModel):
    household_id: UUID = Field(alias="householdId")
    meal_id: UUID = Field(alias="mealId")
    ingredients: List[IngredientContextRequest] = Field(default_factory=list)
    tags: List[str] = Field(default_factory=list)
    high_heat_steps: int = Field(0, alias="highHeatSteps")
    total_steps: int = Field(1, alias="totalSteps")
    model_config = {"populate_by_name": True}


class SustainabilityResponse(BaseModel):
    waste_risk_score: float = Field(alias="wasteRiskScore")
    reuse_score: float = Field(alias="reuseScore")
    energy_score: float = Field(alias="energyScore")
    environmental_score: float = Field(alias="environmentalScore")
    overall_sustainability_score: float = Field(alias="overallSustainabilityScore")
    model_config = {"populate_by_name": True}


@router.post("/score", response_model=SustainabilityResponse)
async def sustainability_score(request: SustainabilityRequest):
    ingredients = [
        IngredientContext(
            ingredient_id=ing.ingredient_id,
            perishability_class=ing.perishability_class,
            is_opened=ing.is_opened,
            is_recurring_waste=ing.is_recurring_waste,
        )
        for ing in request.ingredients
    ]

    meal = MealContext(
        meal_id=request.meal_id,
        ingredients=ingredients,
        tags=set(request.tags),
        high_heat_steps=request.high_heat_steps,
        total_steps=request.total_steps,
    )

    result = score_sustainability(meal)

    return SustainabilityResponse(
        waste_risk_score=result.waste_risk_score,
        reuse_score=result.reuse_score,
        energy_score=result.energy_score,
        environmental_score=result.environmental_score,
        overall_sustainability_score=result.overall_sustainability_score,
    )
