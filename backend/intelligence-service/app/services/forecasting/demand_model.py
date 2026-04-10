"""
Demand model — predicts per-ingredient stockout date vs ParLevel.minimumQuantity.
Uses IngredientUsageRecord + current weekly plan requirements.
Prediction uses ParLevel minimumQuantity as threshold — not a hardcoded percentage.
"""

from dataclasses import dataclass
from typing import List, Optional, Dict
from uuid import UUID
from decimal import Decimal
from datetime import date, timedelta
import math


@dataclass
class IngredientStock:
    ingredient_id: UUID
    current_quantity: Decimal
    unit: str
    avg_weekly_usage: Decimal  # from IngredientUsageRecord
    par_level_minimum: Decimal  # ParLevel.minimumQuantity
    planned_usage_this_week: Decimal = Decimal("0")


@dataclass
class StockoutPrediction:
    ingredient_id: UUID
    predicted_stockout_date: Optional[date]  # None if usage is 0
    days_until_stockout: Optional[int]
    urgency: str  # CRITICAL (<3 days), WARNING (3-7 days), OK (>7 days)
    current_quantity: Decimal
    weekly_usage_rate: Decimal


def predict_stockouts(
    stocks: List[IngredientStock],
    as_of: date,
) -> List[StockoutPrediction]:
    """
    Predict when each ingredient will hit ParLevel.minimumQuantity.
    - Uses avg_weekly_usage + planned_usage_this_week
    - Threshold is ParLevel.minimumQuantity, not a hardcoded percentage
    - avgWeeklyUsage: 0 → predicted_stockout_date: None
    """
    predictions = []

    for stock in stocks:
        total_weekly = stock.avg_weekly_usage + stock.planned_usage_this_week

        if total_weekly <= 0:
            # No usage → no stockout predicted
            predictions.append(StockoutPrediction(
                ingredient_id=stock.ingredient_id,
                predicted_stockout_date=None,
                days_until_stockout=None,
                urgency="OK",
                current_quantity=stock.current_quantity,
                weekly_usage_rate=total_weekly,
            ))
            continue

        # How much above the minimum threshold?
        buffer = stock.current_quantity - stock.par_level_minimum
        if buffer <= 0:
            # Already at or below minimum
            days = 0
        else:
            daily_usage = total_weekly / Decimal("7")
            days = int(math.floor(float(buffer / daily_usage)))

        stockout_date = as_of + timedelta(days=days)

        if days < 3:
            urgency = "CRITICAL"
        elif days <= 7:
            urgency = "WARNING"
        else:
            urgency = "OK"

        predictions.append(StockoutPrediction(
            ingredient_id=stock.ingredient_id,
            predicted_stockout_date=stockout_date,
            days_until_stockout=days,
            urgency=urgency,
            current_quantity=stock.current_quantity,
            weekly_usage_rate=total_weekly,
        ))

    return predictions
