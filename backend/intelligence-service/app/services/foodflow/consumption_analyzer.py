"""
Consumption analyzer — analyses ConsumptionEvent data to produce a ConsumptionProfile.
Pure computation — no DB writes.
"""

from dataclasses import dataclass, field
from typing import List, Dict
from uuid import UUID
from decimal import Decimal
from datetime import datetime
from collections import Counter, defaultdict


@dataclass
class ConsumptionEvent:
    id: UUID
    ingredient_id: UUID
    quantity: Decimal
    unit: str
    source: str  # PLANNED_MEAL, UNPLANNED, SNACK
    calories: Decimal = Decimal("0")
    created_at: datetime = field(default_factory=datetime.now)


@dataclass
class TopIngredient:
    ingredient_id: UUID
    total_quantity: Decimal
    unit: str
    event_count: int


@dataclass
class ConsumptionProfile:
    top_ingredients: List[TopIngredient]
    avg_weekly_calories: Decimal
    meal_type_frequency: Dict[str, int]
    peak_consumption_days: List[str]


WEEKDAY_NAMES = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"]


def analyze_consumption(events: List[ConsumptionEvent], weeks: int = 4) -> ConsumptionProfile:
    """
    Analyze consumption events to produce a profile.
    - topIngredients: sorted by total quantity descending
    - avgWeeklyCalories: total calories / weeks
    - mealTypeFrequency: count per source type
    - peakConsumptionDays: weekdays with most events
    """
    if not events:
        return ConsumptionProfile(
            top_ingredients=[],
            avg_weekly_calories=Decimal("0"),
            meal_type_frequency={},
            peak_consumption_days=[],
        )

    # Aggregate by ingredient
    ingredient_totals: Dict[UUID, Decimal] = defaultdict(lambda: Decimal("0"))
    ingredient_units: Dict[UUID, str] = {}
    ingredient_counts: Dict[UUID, int] = defaultdict(int)

    for event in events:
        ingredient_totals[event.ingredient_id] += event.quantity
        ingredient_units[event.ingredient_id] = event.unit
        ingredient_counts[event.ingredient_id] += 1

    # Build top ingredients sorted by total quantity descending
    top_ingredients = sorted(
        [
            TopIngredient(
                ingredient_id=iid,
                total_quantity=ingredient_totals[iid],
                unit=ingredient_units[iid],
                event_count=ingredient_counts[iid],
            )
            for iid in ingredient_totals
        ],
        key=lambda t: t.total_quantity,
        reverse=True,
    )

    # Average weekly calories
    total_calories = sum(e.calories for e in events)
    avg_weekly_calories = total_calories / Decimal(str(max(weeks, 1)))

    # Meal type frequency
    meal_type_frequency = dict(Counter(e.source for e in events))

    # Peak consumption days
    day_counts = Counter(WEEKDAY_NAMES[e.created_at.weekday()] for e in events)
    if day_counts:
        max_count = max(day_counts.values())
        peak_consumption_days = [day for day, count in day_counts.items() if count == max_count]
    else:
        peak_consumption_days = []

    return ConsumptionProfile(
        top_ingredients=top_ingredients,
        avg_weekly_calories=avg_weekly_calories,
        meal_type_frequency=meal_type_frequency,
        peak_consumption_days=peak_consumption_days,
    )
