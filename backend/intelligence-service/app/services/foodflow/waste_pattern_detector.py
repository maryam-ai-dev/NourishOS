"""
Waste pattern detector — flags RECURRING_WASTE and produces waste summaries.
Pure computation — RECURRING_WASTE is a runtime flag, not a DB column.
"""

from dataclasses import dataclass, field
from typing import List, Dict, Set
from uuid import UUID
from decimal import Decimal
from datetime import datetime
from collections import Counter, defaultdict


RECURRING_WASTE_THRESHOLD = 2  # ≥2 WasteEvents in 4 weeks


@dataclass
class WasteEvent:
    id: UUID
    ingredient_id: UUID
    quantity: Decimal
    unit: str
    reason: str  # EXPIRED, OVERCOOKED, DISCARDED, LEFTOVER_UNUSED
    created_at: datetime = field(default_factory=datetime.now)


@dataclass
class WastePattern:
    frequently_wasted_ingredients: List[UUID]
    avg_weekly_waste_grams: Decimal
    top_waste_reasons: Dict[str, int]
    waste_ratio: Decimal  # totalWaste / (totalWaste + totalConsumed)


def detect_recurring_waste(waste_events: List[WasteEvent]) -> Set[UUID]:
    """
    Flag ingredients as RECURRING_WASTE if wasted ≥2 times in the event window.
    Boundary: exactly 2 = flagged, exactly 1 = not flagged.
    """
    counts = Counter(e.ingredient_id for e in waste_events)
    return {iid for iid, count in counts.items() if count >= RECURRING_WASTE_THRESHOLD}


def analyze_waste_pattern(
    waste_events: List[WasteEvent],
    total_consumed: Decimal = Decimal("0"),
    weeks: int = 4,
) -> WastePattern:
    """
    Produce a waste pattern summary.
    - frequentlyWastedIngredients: ids flagged RECURRING_WASTE
    - avgWeeklyWasteGrams: total wasted / weeks
    - topWasteReasons: count per reason
    - wasteRatio: totalWaste / (totalWaste + totalConsumed)
    """
    recurring = detect_recurring_waste(waste_events)

    total_waste = sum(e.quantity for e in waste_events) if waste_events else Decimal("0")
    avg_weekly = total_waste / Decimal(str(max(weeks, 1)))

    reason_counts = dict(Counter(e.reason for e in waste_events))

    denominator = total_waste + total_consumed
    waste_ratio = total_waste / denominator if denominator > 0 else Decimal("0")

    return WastePattern(
        frequently_wasted_ingredients=list(recurring),
        avg_weekly_waste_grams=avg_weekly,
        top_waste_reasons=reason_counts,
        waste_ratio=waste_ratio,
    )
