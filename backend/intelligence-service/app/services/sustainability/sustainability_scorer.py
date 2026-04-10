"""
Sustainability scorer — waste risk, reuse, energy, environmental sub-scores.
Pure computation; RECURRING_WASTE flag sourced from food flow analysis.
"""

from dataclasses import dataclass, field
from typing import List, Set, Optional
from uuid import UUID
from decimal import Decimal


@dataclass
class IngredientContext:
    ingredient_id: UUID
    perishability_class: str  # HIGHLY_PERISHABLE, PERISHABLE, SHELF_STABLE
    is_opened: bool = False
    is_recurring_waste: bool = False


@dataclass
class MealContext:
    meal_id: UUID
    ingredients: List[IngredientContext]
    tags: Set[str] = field(default_factory=set)  # meat, red-meat, plant-based, dairy, local
    high_heat_steps: int = 0
    total_steps: int = 1


@dataclass
class SustainabilityScore:
    waste_risk_score: float    # [0, 1] higher = less waste risk
    reuse_score: float         # [0, 1] higher = better ingredient reuse
    energy_score: float        # [0, 1] higher = more energy efficient
    environmental_score: float # [0, 1] higher = more environmentally friendly
    overall_sustainability_score: float  # weighted average


# Weights for overall score
WEIGHTS = {
    "waste_risk": 0.30,
    "reuse": 0.25,
    "energy": 0.20,
    "environmental": 0.25,
}


def _waste_risk_score(ingredients: List[IngredientContext]) -> float:
    """Lower waste risk = higher score. Perishable ingredients increase risk."""
    if not ingredients:
        return 1.0
    risk_sum = 0.0
    for ing in ingredients:
        if ing.perishability_class == "HIGHLY_PERISHABLE":
            risk_sum += 0.8
        elif ing.perishability_class == "PERISHABLE":
            risk_sum += 0.4
        else:
            risk_sum += 0.1
    avg_risk = risk_sum / len(ingredients)
    return max(0.0, min(1.0, 1.0 - avg_risk))


def _reuse_score(ingredients: List[IngredientContext]) -> float:
    """
    Higher score for opened ingredients (reuse preference).
    RECURRING_WASTE penalises the score even when available.
    """
    if not ingredients:
        return 1.0

    score_sum = 0.0
    for ing in ingredients:
        base = 0.5
        if ing.is_opened:
            base += 0.3  # boost for reusing opened ingredient
        if ing.is_recurring_waste:
            base -= 0.4  # penalty for recurring waste
        score_sum += max(0.0, min(1.0, base))

    return score_sum / len(ingredients)


def _energy_score(meal: MealContext) -> float:
    """Fewer high-heat steps = higher score."""
    if meal.total_steps == 0:
        return 1.0
    ratio = meal.high_heat_steps / meal.total_steps
    return max(0.0, min(1.0, 1.0 - ratio * 0.6))


def _environmental_score(meal: MealContext) -> float:
    """Red meat penalty, plant-based/local bonus."""
    score = 0.5
    if "red-meat" in meal.tags:
        score -= 0.3
    elif "meat" in meal.tags:
        score -= 0.15
    if "plant-based" in meal.tags:
        score += 0.3
    if "local" in meal.tags:
        score += 0.15
    return max(0.0, min(1.0, score))


def score_sustainability(meal: MealContext) -> SustainabilityScore:
    """Compute all four sub-scores and weighted overall."""
    wr = _waste_risk_score(meal.ingredients)
    rs = _reuse_score(meal.ingredients)
    en = _energy_score(meal)
    ev = _environmental_score(meal)

    overall = (
        WEIGHTS["waste_risk"] * wr
        + WEIGHTS["reuse"] * rs
        + WEIGHTS["energy"] * en
        + WEIGHTS["environmental"] * ev
    )

    return SustainabilityScore(
        waste_risk_score=round(wr, 4),
        reuse_score=round(rs, 4),
        energy_score=round(en, 4),
        environmental_score=round(ev, 4),
        overall_sustainability_score=round(overall, 4),
    )
