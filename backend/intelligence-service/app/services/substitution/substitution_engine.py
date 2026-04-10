"""
Substitution engine — pantry-based, protein-preserving, waste-aware substitutions.
Pure computation — proposals returned for Spring Boot to persist on user approval.
"""

from dataclasses import dataclass, field
from typing import List, Set, Optional
from uuid import UUID
from decimal import Decimal


PROTEIN_TOLERANCE = Decimal("0.10")  # 10% tolerance


@dataclass
class IngredientOption:
    ingredient_id: UUID
    name: str
    protein_per_100g: Decimal
    category: str  # dry, liquid, countable
    is_available: bool = False
    is_recurring_waste: bool = False


@dataclass
class SubstitutionResult:
    original_id: UUID
    original_name: str
    substitute_id: UUID
    substitute_name: str
    requires_approval: bool
    reason: str


def suggest_substitutions(
    missing_ingredients: List[IngredientOption],
    available_ingredients: List[IngredientOption],
    recurring_waste_ids: Set[UUID] = None,
) -> List[SubstitutionResult]:
    """
    For each missing ingredient, find the best available substitute.
    - Protein-preserving: within 10% protein of original
    - Waste-aware: RECURRING_WASTE ingredients excluded as substitutes
    - requiresApproval: true if protein difference > 10%
    """
    if recurring_waste_ids is None:
        recurring_waste_ids = set()

    results = []

    for missing in missing_ingredients:
        # Filter candidates: available, same category, not recurring waste
        candidates = [
            a for a in available_ingredients
            if a.is_available
            and a.category == missing.category
            and a.ingredient_id not in recurring_waste_ids
            and a.ingredient_id != missing.ingredient_id
        ]

        if not candidates:
            continue

        # Sort by protein proximity to original
        candidates.sort(
            key=lambda c: abs(c.protein_per_100g - missing.protein_per_100g)
        )

        best = candidates[0]
        protein_diff = abs(best.protein_per_100g - missing.protein_per_100g)
        within_tolerance = (
            protein_diff <= missing.protein_per_100g * PROTEIN_TOLERANCE
            if missing.protein_per_100g > 0
            else True
        )

        if within_tolerance:
            reason = f"Same category, protein within 10% ({best.protein_per_100g}g vs {missing.protein_per_100g}g per 100g)"
            requires_approval = False
        else:
            reason = f"Best available in category, protein differs ({best.protein_per_100g}g vs {missing.protein_per_100g}g per 100g)"
            requires_approval = True

        results.append(SubstitutionResult(
            original_id=missing.ingredient_id,
            original_name=missing.name,
            substitute_id=best.ingredient_id,
            substitute_name=best.name,
            requires_approval=requires_approval,
            reason=reason,
        ))

    return results
