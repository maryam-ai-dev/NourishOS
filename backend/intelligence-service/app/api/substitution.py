"""
POST /substitution/suggest — suggest ingredient substitutions.
No DB write — proposals returned for Spring Boot to persist on user approval.
"""

from fastapi import APIRouter
from pydantic import BaseModel, Field
from typing import Optional, List, Set
from uuid import UUID
from decimal import Decimal

from app.services.substitution.substitution_engine import (
    IngredientOption,
    suggest_substitutions,
)

router = APIRouter(prefix="/substitution", tags=["substitution"])


class IngredientOptionRequest(BaseModel):
    ingredient_id: UUID = Field(alias="ingredientId")
    name: str
    protein_per_100g: float = Field(alias="proteinPer100g")
    category: str
    is_available: bool = Field(False, alias="isAvailable")
    model_config = {"populate_by_name": True}


class SubstitutionRequest(BaseModel):
    household_id: UUID = Field(alias="householdId")
    missing_ingredients: List[IngredientOptionRequest] = Field(alias="missingIngredients")
    available_ingredients: List[IngredientOptionRequest] = Field(alias="availableIngredients")
    recurring_waste_ids: List[UUID] = Field(default_factory=list, alias="recurringWasteIds")
    model_config = {"populate_by_name": True}


class SubstitutionResponse(BaseModel):
    original_id: UUID = Field(alias="originalId")
    original_name: str = Field(alias="originalName")
    substitute_id: UUID = Field(alias="substituteId")
    substitute_name: str = Field(alias="substituteName")
    requires_approval: bool = Field(alias="requiresApproval")
    reason: str
    model_config = {"populate_by_name": True}


@router.post("/suggest", response_model=List[SubstitutionResponse])
async def suggest(request: SubstitutionRequest):
    missing = [
        IngredientOption(
            ingredient_id=m.ingredient_id,
            name=m.name,
            protein_per_100g=Decimal(str(m.protein_per_100g)),
            category=m.category,
            is_available=False,
        )
        for m in request.missing_ingredients
    ]

    available = [
        IngredientOption(
            ingredient_id=a.ingredient_id,
            name=a.name,
            protein_per_100g=Decimal(str(a.protein_per_100g)),
            category=a.category,
            is_available=a.is_available,
        )
        for a in request.available_ingredients
    ]

    recurring_ids = set(request.recurring_waste_ids)

    results = suggest_substitutions(missing, available, recurring_ids)

    return [
        SubstitutionResponse(
            original_id=r.original_id,
            original_name=r.original_name,
            substitute_id=r.substitute_id,
            substitute_name=r.substitute_name,
            requires_approval=r.requires_approval,
            reason=r.reason,
        )
        for r in results
    ]
