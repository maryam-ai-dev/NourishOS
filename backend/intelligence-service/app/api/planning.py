"""
POST /planning/weekly — propose a weekly meal plan for a household.
FastAPI proposes; Spring Boot persists on user confirm via POST /planning/schedules.
"""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import Optional, List, Dict
from uuid import UUID
from datetime import date
from decimal import Decimal

from app.services.planning.weekly_planner import (
    plan_weekly_slots,
    MealCandidate,
    MemberProfile,
    IngredientInfo,
    NearExpiryIngredient,
)
from app.clients.authority_client import get_members, get_meal_catalog

router = APIRouter(prefix="/planning", tags=["planning"])


class WeeklyPlanRequest(BaseModel):
    household_id: UUID = Field(alias="householdId")
    week_start_date: date = Field(alias="weekStartDate")
    preferences: Optional[Dict] = None

    model_config = {"populate_by_name": True}


class SlotResponse(BaseModel):
    day: int
    day_of_week: str = Field(alias="dayOfWeek")
    meal_type: str = Field(alias="mealType")
    meal_id: UUID = Field(alias="mealId")
    meal_name: str = Field(alias="mealName")
    estimated_protein_grams: float = Field(alias="estimatedProteinGrams")
    is_perishable_priority: bool = Field(False, alias="isPerishablePriority")

    model_config = {"populate_by_name": True}


class MemberFitSummary(BaseModel):
    display_name: str = Field(alias="displayName")
    compliant_slots: int = Field(alias="compliantSlots")
    total_slots: int = Field(alias="totalSlots")
    compliance_rate: float = Field(alias="complianceRate")

    model_config = {"populate_by_name": True}


class WeeklyPlanResponse(BaseModel):
    household_id: UUID = Field(alias="householdId")
    week_start_date: date = Field(alias="weekStartDate")
    slots: List[SlotResponse]
    missing_ingredients: List[Dict] = Field(default_factory=list, alias="missingIngredients")
    waste_reduction_score: float = Field(alias="wasteReductionScore")
    member_fit_summary: Dict[str, MemberFitSummary] = Field(
        default_factory=dict, alias="memberFitSummary"
    )

    model_config = {"populate_by_name": True}


@router.post("/weekly", response_model=WeeklyPlanResponse)
async def plan_weekly(request: WeeklyPlanRequest):
    if not request.household_id:
        raise HTTPException(status_code=422, detail="householdId is required")

    # Validate week_start_date format (already handled by Pydantic)

    # Fetch data from Spring Boot authority service
    try:
        members_data = get_members(str(request.household_id))
        catalog = get_meal_catalog()
    except Exception:
        members_data = []
        catalog = []

    # Convert authority data to planner types
    members = [
        MemberProfile(
            member_id=m.id,
            display_name=m.display_name,
        )
        for m in members_data
    ]

    candidates = [
        MealCandidate(
            meal_id=opt.id,
            meal_name=opt.name,
            meal_type=opt.meal_type,
            estimated_protein_grams=opt.estimated_protein_grams or Decimal("0"),
            ingredient_infos=[
                IngredientInfo(
                    ingredient_id=ref.ingredient_id,
                    base_quantity=ref.base_quantity,
                    unit=ref.unit,
                    optional=ref.optional,
                )
                for ref in opt.ingredient_refs
            ],
            composite_score=float(opt.sustainability_score or Decimal("0.5")),
        )
        for opt in catalog
    ]

    result = plan_weekly_slots(
        household_id=request.household_id,
        week_start_date=request.week_start_date,
        candidates=candidates,
        members=members,
    )

    # Build response
    slots = [
        SlotResponse(
            day=s.day,
            day_of_week=s.day_of_week,
            meal_type=s.meal_type,
            meal_id=s.meal_id,
            meal_name=s.meal_name,
            estimated_protein_grams=float(s.estimated_protein_grams),
            is_perishable_priority=s.is_perishable_priority,
        )
        for s in result.slots
    ]

    member_fit = {
        k: MemberFitSummary(
            display_name=v["display_name"],
            compliant_slots=v["compliant_slots"],
            total_slots=v["total_slots"],
            compliance_rate=v["compliance_rate"],
        )
        for k, v in result.member_fit_summary.items()
    }

    return WeeklyPlanResponse(
        household_id=result.household_id,
        week_start_date=result.week_start_date,
        slots=slots,
        missing_ingredients=result.missing_ingredients,
        waste_reduction_score=result.waste_reduction_score,
        member_fit_summary=member_fit,
    )
