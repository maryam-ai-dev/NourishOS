"""
Ingredient expander — expands MealOption.ingredientRefs into per-ingredient quantities.
Takes ingredientRefs + servings + scalingFactor → quantities in canonical units.
"""

from dataclasses import dataclass
from typing import List, Optional
from uuid import UUID
from decimal import Decimal


# Canonical units per category
CANONICAL_UNITS = {
    "g": "g", "kg": "g", "grams": "g",
    "ml": "ml", "l": "ml", "litres": "ml", "liters": "ml",
    "unit": "unit", "units": "unit", "whole": "unit",
}

CONVERSION_FACTORS = {
    ("kg", "g"): Decimal("1000"),
    ("g", "kg"): Decimal("0.001"),
    ("l", "ml"): Decimal("1000"),
    ("ml", "l"): Decimal("0.001"),
    ("g", "g"): Decimal("1"),
    ("ml", "ml"): Decimal("1"),
    ("kg", "kg"): Decimal("1"),
    ("l", "l"): Decimal("1"),
    ("unit", "unit"): Decimal("1"),
    ("units", "unit"): Decimal("1"),
    ("whole", "unit"): Decimal("1"),
    ("grams", "g"): Decimal("1"),
    ("litres", "ml"): Decimal("1000"),
    ("liters", "ml"): Decimal("1000"),
}


@dataclass
class IngredientRef:
    ingredient_id: UUID
    base_quantity: Decimal
    unit: str
    optional: bool = False


@dataclass
class ExpandedIngredient:
    ingredient_id: UUID
    quantity: Optional[Decimal]
    canonical_unit: str
    original_unit: str
    optional: bool


def _to_canonical(quantity: Decimal, unit: str) -> tuple[Optional[Decimal], str]:
    """Convert quantity to canonical unit. Returns (quantity, canonical_unit)."""
    canonical = CANONICAL_UNITS.get(unit.lower())
    if canonical is None:
        return None, unit

    factor_key = (unit.lower(), canonical)
    factor = CONVERSION_FACTORS.get(factor_key)
    if factor is None:
        return None, unit

    return quantity * factor, canonical


def expand_ingredients(
    ingredient_refs: List[IngredientRef],
    servings: int,
    scaling_factor: Decimal = Decimal("1"),
) -> List[ExpandedIngredient]:
    """
    Expand ingredient refs into per-ingredient quantities in canonical units.

    - baseQuantity is for 1 serving
    - Total = baseQuantity × servings × scalingFactor
    - Units are converted to canonical form (g, ml, unit)
    - Unknown ingredientId returns quantity=None without crash
    """
    if servings <= 0:
        raise ValueError("servings must be > 0")

    results = []
    for ref in ingredient_refs:
        total_quantity = ref.base_quantity * Decimal(str(servings)) * scaling_factor
        canonical_qty, canonical_unit = _to_canonical(total_quantity, ref.unit)

        results.append(ExpandedIngredient(
            ingredient_id=ref.ingredient_id,
            quantity=canonical_qty,
            canonical_unit=canonical_unit,
            original_unit=ref.unit,
            optional=ref.optional,
        ))

    return results
