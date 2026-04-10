"""
Stock flow model — net flow and mismatch detection per ingredient.
Pure computation using InventoryAdjustment history.
"""

from dataclasses import dataclass, field
from typing import List, Dict, Set
from uuid import UUID
from decimal import Decimal
from collections import defaultdict


@dataclass
class InventoryAdjustment:
    id: UUID
    ingredient_id: UUID
    adjustment_type: str  # PURCHASE, CONSUMPTION, WASTE, CORRECTION, DEDUCTION
    quantity: Decimal
    unit: str


@dataclass
class StockFlowSummary:
    net_flow_by_ingredient: Dict[UUID, Decimal]
    over_bought_ingredients: List[UUID]
    under_supplied_ingredients: List[UUID]


def compute_net_flow(adjustments: List[InventoryAdjustment]) -> Dict[UUID, Decimal]:
    """
    Net flow per ingredient: PURCHASE - CONSUMPTION - WASTE - DEDUCTION
    CORRECTION adjustments are included as-is (can be positive or negative).
    """
    flows: Dict[UUID, Decimal] = defaultdict(lambda: Decimal("0"))

    for adj in adjustments:
        iid = adj.ingredient_id
        if adj.adjustment_type == "PURCHASE":
            flows[iid] += adj.quantity
        elif adj.adjustment_type in ("CONSUMPTION", "WASTE", "DEDUCTION"):
            flows[iid] -= adj.quantity
        elif adj.adjustment_type == "CORRECTION":
            flows[iid] += adj.quantity  # corrections can be +/-

    return dict(flows)


def detect_over_bought(
    net_flows: Dict[UUID, Decimal],
    avg_weekly_usage: Dict[UUID, Decimal],
    purchased_totals: Dict[UUID, Decimal],
) -> List[UUID]:
    """
    Flag overBought if net flow consistently positive AND
    avgWeeklyUsage < 20% of purchased quantity.
    """
    over_bought = []
    for iid, net in net_flows.items():
        if net <= 0:
            continue
        usage = avg_weekly_usage.get(iid, Decimal("0"))
        purchased = purchased_totals.get(iid, Decimal("0"))
        if purchased > 0 and usage < purchased * Decimal("0.2"):
            over_bought.append(iid)
    return over_bought


def detect_under_supplied(
    adjustments: List[InventoryAdjustment],
) -> List[UUID]:
    """
    Flag underSupplied if lot quantity reached 0 before reorder in last 4 weeks.
    Approximated: ingredient with DEDUCTION bringing running total to 0 or below.
    """
    running: Dict[UUID, Decimal] = defaultdict(lambda: Decimal("0"))
    hit_zero: Set[UUID] = set()

    for adj in adjustments:
        iid = adj.ingredient_id
        if adj.adjustment_type == "PURCHASE":
            running[iid] += adj.quantity
        elif adj.adjustment_type in ("CONSUMPTION", "WASTE", "DEDUCTION"):
            running[iid] -= adj.quantity
            if running[iid] <= 0:
                hit_zero.add(iid)

    return list(hit_zero)


def analyze_stock_flow(
    adjustments: List[InventoryAdjustment],
    avg_weekly_usage: Dict[UUID, Decimal] = None,
) -> StockFlowSummary:
    """Full stock flow analysis."""
    if avg_weekly_usage is None:
        avg_weekly_usage = {}

    net_flows = compute_net_flow(adjustments)

    # Compute purchased totals
    purchased: Dict[UUID, Decimal] = defaultdict(lambda: Decimal("0"))
    for adj in adjustments:
        if adj.adjustment_type == "PURCHASE":
            purchased[adj.ingredient_id] += adj.quantity

    over_bought = detect_over_bought(net_flows, avg_weekly_usage, dict(purchased))
    under_supplied = detect_under_supplied(adjustments)

    return StockFlowSummary(
        net_flow_by_ingredient=net_flows,
        over_bought_ingredients=over_bought,
        under_supplied_ingredients=under_supplied,
    )
