"""
Replenishment optimizer — converts stockout predictions to reorder suggestions.
Base quantity covers at least one week of avgWeeklyUsage in canonical unit.
Waste adjustment reduces quantity for RECURRING_WASTE ingredients.
"""

from dataclasses import dataclass
from typing import List, Set, Optional
from uuid import UUID
from decimal import Decimal


@dataclass
class StockoutInput:
    ingredient_id: UUID
    weekly_usage_rate: Decimal
    urgency: str  # CRITICAL, WARNING
    unit: str
    is_recurring_waste: bool = False
    waste_ratio: Decimal = Decimal("0")


@dataclass
class ReplenishmentSuggestion:
    ingredient_id: UUID
    base_quantity: Decimal
    adjusted_quantity: Decimal
    unit: str
    urgency: str
    adjusted_for_waste: bool
    reason: str


def optimize_replenishment(
    inputs: List[StockoutInput],
    recurring_waste_ids: Optional[Set[UUID]] = None,
) -> List[ReplenishmentSuggestion]:
    """
    Generate reorder suggestions.
    - Base quantity = at least 1 week of avgWeeklyUsage
    - CRITICAL urgency: 2 weeks coverage
    - WARNING urgency: 1 week coverage
    - RECURRING_WASTE: reduce by wasteRatio, set adjustedForWaste=true
    """
    if recurring_waste_ids is None:
        recurring_waste_ids = set()

    suggestions = []

    for inp in inputs:
        if inp.weekly_usage_rate <= 0:
            continue

        # Base quantity: 2 weeks for CRITICAL, 1 week for WARNING
        if inp.urgency == "CRITICAL":
            base_qty = inp.weekly_usage_rate * Decimal("2")
        else:
            base_qty = inp.weekly_usage_rate

        # Waste adjustment
        is_waste = inp.ingredient_id in recurring_waste_ids or inp.is_recurring_waste
        if is_waste and inp.waste_ratio > 0:
            adjusted_qty = base_qty * (1 - inp.waste_ratio)
            adjusted_for_waste = True
            reason = (
                f"Quantity reduced by {float(inp.waste_ratio):.0%} due to recurring waste history. "
                f"Base: {base_qty}{inp.unit}, adjusted: {adjusted_qty}{inp.unit}"
            )
        else:
            adjusted_qty = base_qty
            adjusted_for_waste = False
            reason = f"Reorder {base_qty}{inp.unit} to cover {'2 weeks' if inp.urgency == 'CRITICAL' else '1 week'} of usage"

        suggestions.append(ReplenishmentSuggestion(
            ingredient_id=inp.ingredient_id,
            base_quantity=base_qty,
            adjusted_quantity=adjusted_qty,
            unit=inp.unit,
            urgency=inp.urgency,
            adjusted_for_waste=adjusted_for_waste,
            reason=reason,
        ))

    return suggestions
