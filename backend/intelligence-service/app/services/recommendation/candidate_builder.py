"""
Candidate builder — filters meal catalog against member restrictions and inventory.
Pure computation: no DB writes.
"""

from typing import List, Set
from uuid import UUID


def build_candidates(
    meals: list,
    member_allergens: Set[str],
    disliked_ingredient_ids: Set[UUID],
    available_ingredient_ids: Set[UUID],
    max_missing_ingredients: int = 2,
) -> list:
    """
    Filter meals based on:
    1. Member allergen exclusion (any ref ingredient in allergen set → exclude)
    2. Disliked ingredient exclusion
    3. Ingredient availability (> max_missing non-optional ingredients → exclude)
    """
    candidates = []

    for meal in meals:
        refs = meal.ingredient_refs if hasattr(meal, "ingredient_refs") else []

        # Check allergens
        has_allergen = False
        for ref in refs:
            ing_id = ref.ingredient_id if hasattr(ref, "ingredient_id") else ref.get("ingredientId")
            if str(ing_id) in member_allergens:
                has_allergen = True
                break
        if has_allergen:
            continue

        # Check disliked
        has_disliked = False
        for ref in refs:
            ing_id = ref.ingredient_id if hasattr(ref, "ingredient_id") else ref.get("ingredientId")
            if ing_id in disliked_ingredient_ids:
                has_disliked = True
                break
        if has_disliked:
            continue

        # Check availability
        missing = 0
        for ref in refs:
            is_optional = ref.optional if hasattr(ref, "optional") else ref.get("optional", False)
            ing_id = ref.ingredient_id if hasattr(ref, "ingredient_id") else ref.get("ingredientId")
            if not is_optional and ing_id not in available_ingredient_ids:
                missing += 1
        if missing > max_missing_ingredients:
            continue

        candidates.append(meal)

    return candidates
