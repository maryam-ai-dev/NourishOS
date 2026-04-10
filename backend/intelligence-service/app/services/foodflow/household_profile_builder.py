"""
Household profile builder — assembles HouseholdFoodProfile from all food flow components.
No DB write — used as input to weekly planner and recommendation ranker.
"""

from dataclasses import dataclass, field
from typing import List, Optional, Dict
from uuid import UUID

from app.services.foodflow.consumption_analyzer import ConsumptionProfile
from app.services.foodflow.waste_pattern_detector import WastePattern
from app.services.foodflow.stock_flow_model import StockFlowSummary
from app.services.foodflow.meal_reliability_analyzer import MealReliability


@dataclass
class HouseholdFoodProfile:
    household_id: UUID
    consumption_profile: ConsumptionProfile
    waste_pattern: WastePattern
    stock_flow_summary: StockFlowSummary
    top_reliable_meals: List[MealReliability]


def build_household_profile(
    household_id: UUID,
    consumption_profile: ConsumptionProfile,
    waste_pattern: WastePattern,
    stock_flow_summary: StockFlowSummary,
    meal_reliability: Dict[UUID, MealReliability],
) -> HouseholdFoodProfile:
    """
    Assemble household food profile from all four components.
    Top reliable meals sorted by reliabilityScore descending.
    """
    sorted_meals = sorted(
        meal_reliability.values(),
        key=lambda m: m.reliability_score,
        reverse=True,
    )

    return HouseholdFoodProfile(
        household_id=household_id,
        consumption_profile=consumption_profile,
        waste_pattern=waste_pattern,
        stock_flow_summary=stock_flow_summary,
        top_reliable_meals=sorted_meals,
    )
