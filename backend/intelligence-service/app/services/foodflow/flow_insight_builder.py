"""
Flow insight builder — generates WeeklyHouseholdInsight.
One top insight per category: WASTE, REPLENISHMENT, RELIABILITY, NUTRITION.
POSTs insights to Spring Boot via authority client.
"""

from dataclasses import dataclass, field
from typing import List, Optional, Dict
from uuid import UUID
from decimal import Decimal
from datetime import date

from app.services.foodflow.waste_pattern_detector import WastePattern
from app.services.foodflow.stock_flow_model import StockFlowSummary
from app.services.foodflow.meal_reliability_analyzer import MealReliability
from app.services.foodflow.consumption_analyzer import ConsumptionProfile


CATEGORIES = ["WASTE", "REPLENISHMENT", "RELIABILITY", "NUTRITION"]


@dataclass
class HouseholdInsight:
    household_id: UUID
    snapshot_week: date
    category: str
    insight_text: str


@dataclass
class WeeklyHouseholdInsights:
    household_id: UUID
    snapshot_week: date
    insights: List[HouseholdInsight]


def build_insights(
    household_id: UUID,
    snapshot_week: date,
    waste_pattern: Optional[WastePattern] = None,
    stock_flow: Optional[StockFlowSummary] = None,
    meal_reliability: Optional[Dict[UUID, MealReliability]] = None,
    consumption_profile: Optional[ConsumptionProfile] = None,
) -> WeeklyHouseholdInsights:
    """
    Build one insight per category from food flow analysis results.
    """
    insights = []

    # WASTE insight
    if waste_pattern and waste_pattern.frequently_wasted_ingredients:
        wasted_ids = waste_pattern.frequently_wasted_ingredients
        insight_text = (
            f"Recurring waste detected for {len(wasted_ids)} ingredient(s). "
            f"Weekly waste average: {waste_pattern.avg_weekly_waste_grams}g. "
            f"Consider reducing purchase quantities or using these ingredients earlier."
        )
        insights.append(HouseholdInsight(
            household_id=household_id,
            snapshot_week=snapshot_week,
            category="WASTE",
            insight_text=insight_text,
        ))
    elif waste_pattern:
        insights.append(HouseholdInsight(
            household_id=household_id,
            snapshot_week=snapshot_week,
            category="WASTE",
            insight_text=f"Waste ratio: {float(waste_pattern.waste_ratio):.1%}. No recurring waste patterns detected.",
        ))

    # REPLENISHMENT insight
    if stock_flow and stock_flow.under_supplied_ingredients:
        under = stock_flow.under_supplied_ingredients
        insights.append(HouseholdInsight(
            household_id=household_id,
            snapshot_week=snapshot_week,
            category="REPLENISHMENT",
            insight_text=(
                f"{len(under)} ingredient(s) ran out of stock before reorder. "
                f"Consider adjusting par levels or reorder frequency."
            ),
        ))
    elif stock_flow and stock_flow.over_bought_ingredients:
        over = stock_flow.over_bought_ingredients
        insights.append(HouseholdInsight(
            household_id=household_id,
            snapshot_week=snapshot_week,
            category="REPLENISHMENT",
            insight_text=f"{len(over)} ingredient(s) appear over-bought with low usage rates.",
        ))
    else:
        insights.append(HouseholdInsight(
            household_id=household_id,
            snapshot_week=snapshot_week,
            category="REPLENISHMENT",
            insight_text="Stock levels are well-balanced this week.",
        ))

    # RELIABILITY insight
    if meal_reliability:
        low_rel = [m for m in meal_reliability.values() if m.is_low_reliability]
        if low_rel:
            names = ", ".join(str(m.meal_option_id)[:8] for m in low_rel[:3])
            insights.append(HouseholdInsight(
                household_id=household_id,
                snapshot_week=snapshot_week,
                category="RELIABILITY",
                insight_text=(
                    f"{len(low_rel)} meal(s) have low reliability (completion < 50%). "
                    f"Consider simpler alternatives or reviewing preparation steps."
                ),
            ))
        else:
            best = max(meal_reliability.values(), key=lambda m: m.reliability_score)
            insights.append(HouseholdInsight(
                household_id=household_id,
                snapshot_week=snapshot_week,
                category="RELIABILITY",
                insight_text=f"All meals have acceptable reliability. Best score: {best.reliability_score:.2f}.",
            ))
    else:
        insights.append(HouseholdInsight(
            household_id=household_id,
            snapshot_week=snapshot_week,
            category="RELIABILITY",
            insight_text="No meal outcome data available yet.",
        ))

    # NUTRITION insight
    if consumption_profile and consumption_profile.avg_weekly_calories > 0:
        insights.append(HouseholdInsight(
            household_id=household_id,
            snapshot_week=snapshot_week,
            category="NUTRITION",
            insight_text=(
                f"Average weekly calories: {consumption_profile.avg_weekly_calories}. "
                f"Top consumption days: {', '.join(consumption_profile.peak_consumption_days)}."
            ),
        ))
    else:
        insights.append(HouseholdInsight(
            household_id=household_id,
            snapshot_week=snapshot_week,
            category="NUTRITION",
            insight_text="No consumption data available yet.",
        ))

    return WeeklyHouseholdInsights(
        household_id=household_id,
        snapshot_week=snapshot_week,
        insights=insights,
    )
