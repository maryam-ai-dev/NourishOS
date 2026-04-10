"""
Ingredient lifecycle analyzer — tracks purchase → consumption/waste lifecycle.
Estimates avgDaysToUse and wasteProbability per ingredient.
Pure computation from raw event history.
"""

from dataclasses import dataclass, field
from typing import List, Dict, Optional
from uuid import UUID
from decimal import Decimal
from datetime import datetime, timedelta
from collections import defaultdict


@dataclass
class LifecycleEvent:
    ingredient_id: UUID
    event_type: str  # PURCHASE, CONSUMPTION, WASTE
    quantity: Decimal
    created_at: datetime


@dataclass
class IngredientLifecycle:
    ingredient_id: UUID
    avg_days_to_use: float  # average days from purchase to consumption
    waste_probability: float  # ratio of waste events to total outcome events [0, 1]
    total_purchased: Decimal
    total_consumed: Decimal
    total_wasted: Decimal


def analyze_lifecycle(events: List[LifecycleEvent]) -> Dict[UUID, IngredientLifecycle]:
    """
    Per-ingredient lifecycle stats.
    - avgDaysToUse: average days between first purchase and first consumption
    - wasteProbability: waste_events / (waste_events + consumption_events)
    """
    # Group by ingredient
    by_ingredient: Dict[UUID, List[LifecycleEvent]] = defaultdict(list)
    for e in events:
        by_ingredient[e.ingredient_id].append(e)

    results = {}

    for iid, ing_events in by_ingredient.items():
        purchases = sorted(
            [e for e in ing_events if e.event_type == "PURCHASE"],
            key=lambda e: e.created_at,
        )
        consumptions = sorted(
            [e for e in ing_events if e.event_type == "CONSUMPTION"],
            key=lambda e: e.created_at,
        )
        wastes = [e for e in ing_events if e.event_type == "WASTE"]

        total_purchased = sum(e.quantity for e in purchases)
        total_consumed = sum(e.quantity for e in consumptions)
        total_wasted = sum(e.quantity for e in wastes)

        # Avg days to use: match each consumption to nearest preceding purchase
        days_to_use = []
        for c in consumptions:
            preceding = [p for p in purchases if p.created_at <= c.created_at]
            if preceding:
                latest_purchase = preceding[-1]
                delta = (c.created_at - latest_purchase.created_at).total_seconds() / 86400
                days_to_use.append(delta)

        avg_days = sum(days_to_use) / len(days_to_use) if days_to_use else 0.0

        # Waste probability
        outcome_count = len(consumptions) + len(wastes)
        waste_prob = len(wastes) / outcome_count if outcome_count > 0 else 0.0

        # Clamp to [0, 1]
        waste_prob = max(0.0, min(1.0, waste_prob))
        avg_days = max(0.0, avg_days)

        results[iid] = IngredientLifecycle(
            ingredient_id=iid,
            avg_days_to_use=avg_days,
            waste_probability=waste_prob,
            total_purchased=total_purchased,
            total_consumed=total_consumed,
            total_wasted=total_wasted,
        )

    return results
