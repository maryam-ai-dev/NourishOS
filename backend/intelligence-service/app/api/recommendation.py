"""
POST /recommendation/rank — rank meals for a household.
FastAPI proposes; Spring Boot persists candidates.
"""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional, List
from uuid import UUID

from app.services.recommendation.candidate_builder import build_candidates
from app.services.recommendation.meal_ranker import rank_candidates

router = APIRouter(prefix="/recommendation", tags=["recommendation"])


class RankRequest(BaseModel):
    household_id: UUID
    protein_target: Optional[float] = None
    max_results: int = 10


class ScoreBreakdown(BaseModel):
    preference_fit: float
    protein_goal: float
    sustainability: float
    availability: float
    reliability: float


class RankedMeal(BaseModel):
    meal_id: UUID
    meal_name: str
    composite_score: float
    score_breakdown: ScoreBreakdown


class RankResponse(BaseModel):
    household_id: UUID
    ranked: List[RankedMeal]


@router.post("/rank", response_model=RankResponse)
async def rank_meals(request: RankRequest):
    if not request.household_id:
        raise HTTPException(status_code=422, detail="household_id is required")

    # In production, these would be fetched from Spring Boot via authority_client
    # For now, return a structured stub that proves the pipeline works
    try:
        from app.clients.authority_client import get_meal_catalog
        catalog = get_meal_catalog()
    except Exception:
        # If Spring Boot isn't running, use empty catalog
        catalog = []

    if not catalog:
        return RankResponse(household_id=request.household_id, ranked=[])

    # Build candidates (no restrictions for now)
    candidates = build_candidates(catalog, set(), set(), set(), max_missing_ingredients=999)

    # Rank
    ranked = rank_candidates(candidates, protein_target=request.protein_target)

    # Trim to max_results
    ranked = ranked[:request.max_results]

    return RankResponse(
        household_id=request.household_id,
        ranked=[
            RankedMeal(
                meal_id=r["meal"].id,
                meal_name=r["meal"].name,
                composite_score=r["composite_score"],
                score_breakdown=ScoreBreakdown(**r["score_breakdown"]),
            )
            for r in ranked
        ],
    )
