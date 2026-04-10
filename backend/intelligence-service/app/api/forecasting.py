"""
POST /forecasting/stockout — predict stockout dates per ingredient.
"""

from fastapi import APIRouter
from pydantic import BaseModel, Field
from typing import Optional, List
from uuid import UUID
from decimal import Decimal
from datetime import date

from app.services.forecasting.demand_model import (
    IngredientStock,
    predict_stockouts,
)

router = APIRouter(prefix="/forecasting", tags=["forecasting"])


class StockoutRequest(BaseModel):
    household_id: UUID = Field(alias="householdId")
    as_of: Optional[date] = Field(None, alias="asOf")
    stocks: List["StockItem"] = Field(default_factory=list)
    model_config = {"populate_by_name": True}


class StockItem(BaseModel):
    ingredient_id: UUID = Field(alias="ingredientId")
    current_quantity: float = Field(alias="currentQuantity")
    unit: str
    avg_weekly_usage: float = Field(alias="avgWeeklyUsage")
    par_level_minimum: float = Field(alias="parLevelMinimum")
    planned_usage_this_week: float = Field(0, alias="plannedUsageThisWeek")
    model_config = {"populate_by_name": True}


class StockoutPredictionResponse(BaseModel):
    ingredient_id: UUID = Field(alias="ingredientId")
    predicted_stockout_date: Optional[date] = Field(None, alias="predictedStockoutDate")
    days_until_stockout: Optional[int] = Field(None, alias="daysUntilStockout")
    urgency: str
    model_config = {"populate_by_name": True}


@router.post("/stockout", response_model=List[StockoutPredictionResponse])
async def forecast_stockouts(request: StockoutRequest):
    as_of = request.as_of or date.today()

    stocks = [
        IngredientStock(
            ingredient_id=s.ingredient_id,
            current_quantity=Decimal(str(s.current_quantity)),
            unit=s.unit,
            avg_weekly_usage=Decimal(str(s.avg_weekly_usage)),
            par_level_minimum=Decimal(str(s.par_level_minimum)),
            planned_usage_this_week=Decimal(str(s.planned_usage_this_week)),
        )
        for s in request.stocks
    ]

    predictions = predict_stockouts(stocks, as_of)

    # Filter out well-stocked (OK with >7 days) — only return actionable predictions
    actionable = [p for p in predictions if p.urgency != "OK"]

    return [
        StockoutPredictionResponse(
            ingredient_id=p.ingredient_id,
            predicted_stockout_date=p.predicted_stockout_date,
            days_until_stockout=p.days_until_stockout,
            urgency=p.urgency,
        )
        for p in actionable
    ]
