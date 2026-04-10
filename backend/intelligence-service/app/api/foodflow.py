"""
Food flow intelligence endpoints.
POST /foodflow/analyze — full analysis pipeline
POST /foodflow/insights — generate and POST insights to Spring Boot
POST /foodflow/replenishment-score — score reorder against flow patterns
POST /foodflow/meal-reliability — reliability data for meal options
"""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import Optional, List, Dict
from uuid import UUID
from decimal import Decimal
from datetime import date

from app.services.foodflow.consumption_analyzer import (
    ConsumptionEvent as ConsumptionEventData,
    ConsumptionProfile,
    TopIngredient,
    analyze_consumption,
)
from app.services.foodflow.waste_pattern_detector import (
    WasteEvent as WasteEventData,
    WastePattern,
    analyze_waste_pattern,
)
from app.services.foodflow.stock_flow_model import (
    InventoryAdjustment as AdjustmentData,
    StockFlowSummary,
    analyze_stock_flow,
)
from app.services.foodflow.meal_reliability_analyzer import (
    MealOutcomeEvent as MealOutcomeData,
    analyze_meal_reliability,
    MealReliability,
)
from app.services.foodflow.flow_insight_builder import build_insights
from app.clients.authority_client import get_foodflow_snapshot

router = APIRouter(prefix="/foodflow", tags=["foodflow"])


# --- Request/Response models ---

class AnalyzeRequest(BaseModel):
    household_id: UUID = Field(alias="householdId")
    model_config = {"populate_by_name": True}


class TopIngredientResponse(BaseModel):
    ingredient_id: UUID = Field(alias="ingredientId")
    total_quantity: float = Field(alias="totalQuantity")
    unit: str
    event_count: int = Field(alias="eventCount")
    model_config = {"populate_by_name": True}


class ConsumptionProfileResponse(BaseModel):
    top_ingredients: List[TopIngredientResponse] = Field(alias="topIngredients")
    avg_weekly_calories: float = Field(alias="avgWeeklyCalories")
    meal_type_frequency: Dict[str, int] = Field(alias="mealTypeFrequency")
    peak_consumption_days: List[str] = Field(alias="peakConsumptionDays")
    model_config = {"populate_by_name": True}


class WastePatternResponse(BaseModel):
    frequently_wasted_ingredients: List[UUID] = Field(alias="frequentlyWastedIngredients")
    avg_weekly_waste_grams: float = Field(alias="avgWeeklyWasteGrams")
    top_waste_reasons: Dict[str, int] = Field(alias="topWasteReasons")
    waste_ratio: float = Field(alias="wasteRatio")
    model_config = {"populate_by_name": True}


class StockFlowResponse(BaseModel):
    net_flow_by_ingredient: Dict[str, float] = Field(alias="netFlowByIngredient")
    over_bought_ingredients: List[UUID] = Field(alias="overBoughtIngredients")
    under_supplied_ingredients: List[UUID] = Field(alias="underSuppliedIngredients")
    model_config = {"populate_by_name": True}


class AnalyzeResponse(BaseModel):
    household_id: UUID = Field(alias="householdId")
    consumption_profile: ConsumptionProfileResponse = Field(alias="consumptionProfile")
    waste_pattern: WastePatternResponse = Field(alias="wastePattern")
    stock_flow_summary: StockFlowResponse = Field(alias="stockFlowSummary")
    model_config = {"populate_by_name": True}


@router.post("/analyze", response_model=AnalyzeResponse)
async def analyze_foodflow(request: AnalyzeRequest):
    """
    Run full food flow analysis pipeline.
    Pure intelligence computation — no DB writes.
    """
    # In production, fetch events from Spring Boot via authority client.
    # For now, return analysis on empty data if Spring Boot isn't available.
    try:
        snapshot = get_foodflow_snapshot(str(request.household_id))
    except Exception:
        snapshot = {}

    # Run analysis pipeline with empty data as fallback
    consumption_events: List[ConsumptionEventData] = []
    waste_events: List[WasteEventData] = []
    adjustments: List[AdjustmentData] = []

    consumption_profile = analyze_consumption(consumption_events)
    total_consumed = sum(e.quantity for e in consumption_events)
    waste_pattern = analyze_waste_pattern(waste_events, total_consumed=total_consumed)
    stock_flow = analyze_stock_flow(adjustments)

    return AnalyzeResponse(
        household_id=request.household_id,
        consumption_profile=ConsumptionProfileResponse(
            top_ingredients=[
                TopIngredientResponse(
                    ingredient_id=t.ingredient_id,
                    total_quantity=float(t.total_quantity),
                    unit=t.unit,
                    event_count=t.event_count,
                )
                for t in consumption_profile.top_ingredients
            ],
            avg_weekly_calories=float(consumption_profile.avg_weekly_calories),
            meal_type_frequency=consumption_profile.meal_type_frequency,
            peak_consumption_days=consumption_profile.peak_consumption_days,
        ),
        waste_pattern=WastePatternResponse(
            frequently_wasted_ingredients=waste_pattern.frequently_wasted_ingredients,
            avg_weekly_waste_grams=float(waste_pattern.avg_weekly_waste_grams),
            top_waste_reasons=waste_pattern.top_waste_reasons,
            waste_ratio=float(waste_pattern.waste_ratio),
        ),
        stock_flow_summary=StockFlowResponse(
            net_flow_by_ingredient={str(k): float(v) for k, v in stock_flow.net_flow_by_ingredient.items()},
            over_bought_ingredients=stock_flow.over_bought_ingredients,
            under_supplied_ingredients=stock_flow.under_supplied_ingredients,
        ),
    )


# --- POST /foodflow/insights ---

class InsightsRequest(BaseModel):
    household_id: UUID = Field(alias="householdId")
    snapshot_week: date = Field(alias="snapshotWeek")
    model_config = {"populate_by_name": True}


class InsightResponse(BaseModel):
    category: str
    insight_text: str = Field(alias="insightText")
    model_config = {"populate_by_name": True}


class InsightsResponse(BaseModel):
    household_id: UUID = Field(alias="householdId")
    insights: List[InsightResponse]
    posted_to_spring: bool = Field(alias="postedToSpring")
    model_config = {"populate_by_name": True}


@router.post("/insights", response_model=InsightsResponse)
async def generate_insights(request: InsightsRequest):
    """Generate and POST insights to Spring Boot."""
    result = build_insights(
        household_id=request.household_id,
        snapshot_week=request.snapshot_week,
    )

    # Try to POST to Spring Boot
    posted = False
    try:
        import httpx
        async with httpx.AsyncClient() as client:
            payload = [
                {
                    "householdId": str(i.household_id),
                    "snapshotWeek": str(i.snapshot_week),
                    "category": i.category,
                    "insightText": i.insight_text,
                }
                for i in result.insights
            ]
            resp = await client.post("http://localhost:8080/insights", json=payload, timeout=5.0)
            posted = resp.status_code == 201
    except Exception:
        pass

    return InsightsResponse(
        household_id=request.household_id,
        insights=[
            InsightResponse(category=i.category, insight_text=i.insight_text)
            for i in result.insights
        ],
        posted_to_spring=posted,
    )


# --- POST /foodflow/replenishment-score ---

class ReplenishmentScoreRequest(BaseModel):
    household_id: UUID = Field(alias="householdId")
    ingredient_id: UUID = Field(alias="ingredientId")
    proposed_quantity: float = Field(alias="proposedQuantity")
    model_config = {"populate_by_name": True}


class ReplenishmentScoreResponse(BaseModel):
    ingredient_id: UUID = Field(alias="ingredientId")
    score: float
    is_recurring_waste: bool = Field(alias="isRecurringWaste")
    adjusted_quantity: Optional[float] = Field(None, alias="adjustedQuantity")
    model_config = {"populate_by_name": True}


@router.post("/replenishment-score", response_model=ReplenishmentScoreResponse)
async def replenishment_score(request: ReplenishmentScoreRequest):
    """Score a reorder against flow patterns. Pure computation."""
    # In production, fetch waste events from Spring Boot
    # For now, return a baseline score
    waste_events: List[WasteEventData] = []
    waste_pattern = analyze_waste_pattern(waste_events, total_consumed=Decimal("0"))

    is_recurring = request.ingredient_id in set(waste_pattern.frequently_wasted_ingredients)
    waste_ratio = float(waste_pattern.waste_ratio)

    if is_recurring:
        adjusted = request.proposed_quantity * (1 - waste_ratio)
        score = max(0.0, 1.0 - waste_ratio)
    else:
        adjusted = request.proposed_quantity
        score = 1.0

    return ReplenishmentScoreResponse(
        ingredient_id=request.ingredient_id,
        score=score,
        is_recurring_waste=is_recurring,
        adjusted_quantity=adjusted,
    )


# --- POST /foodflow/meal-reliability ---

class MealReliabilityRequest(BaseModel):
    household_id: UUID = Field(alias="householdId")
    meal_option_ids: List[UUID] = Field(alias="mealOptionIds")
    model_config = {"populate_by_name": True}


class MealReliabilityItem(BaseModel):
    meal_option_id: UUID = Field(alias="mealOptionId")
    completion_rate: float = Field(alias="completionRate")
    reliability_score: float = Field(alias="reliabilityScore")
    is_low_reliability: bool = Field(alias="isLowReliability")
    model_config = {"populate_by_name": True}


class MealReliabilityResponse(BaseModel):
    household_id: UUID = Field(alias="householdId")
    reliability: List[MealReliabilityItem]
    model_config = {"populate_by_name": True}


@router.post("/meal-reliability", response_model=MealReliabilityResponse)
async def meal_reliability(request: MealReliabilityRequest):
    """Reliability data for a set of meal options. Pure computation."""
    # In production, fetch outcome events from Spring Boot
    outcomes: List[MealOutcomeData] = []
    reliability_map = analyze_meal_reliability(outcomes)

    items = []
    for meal_id in request.meal_option_ids:
        rel = reliability_map.get(meal_id)
        if rel:
            items.append(MealReliabilityItem(
                meal_option_id=meal_id,
                completion_rate=rel.completion_rate,
                reliability_score=rel.reliability_score,
                is_low_reliability=rel.is_low_reliability,
            ))
        else:
            items.append(MealReliabilityItem(
                meal_option_id=meal_id,
                completion_rate=0.0,
                reliability_score=0.0,
                is_low_reliability=True,
            ))

    return MealReliabilityResponse(
        household_id=request.household_id,
        reliability=items,
    )
