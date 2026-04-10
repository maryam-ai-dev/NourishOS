"""
Meal ranker — composite scoring function.
Weights: 0.25 preference_fit + 0.25 protein_goal + 0.25 sustainability + 0.15 availability + 0.10 reliability
"""

from typing import List, Dict, Optional
from decimal import Decimal


def rank_candidates(
    candidates: list,
    protein_target: Optional[float] = None,
    reliability_scores: Dict[str, float] = None,
    available_ingredient_ids: set = None,
) -> List[dict]:
    """
    Score and rank candidates. Returns list of dicts with meal + composite_score + score_breakdown.
    """
    if reliability_scores is None:
        reliability_scores = {}
    if available_ingredient_ids is None:
        available_ingredient_ids = set()

    scored = []
    for meal in candidates:
        preference_fit = 0.7  # default — would be personalized from member profile
        protein_score = _protein_score(meal, protein_target)
        sustainability = float(meal.sustainability_score or 0.5)
        availability = _availability_score(meal, available_ingredient_ids)
        reliability = reliability_scores.get(str(meal.id), 0.5)  # default 0.5 if no history

        composite = (
            0.25 * preference_fit
            + 0.25 * protein_score
            + 0.25 * sustainability
            + 0.15 * availability
            + 0.10 * reliability
        )
        composite = max(0.0, min(1.0, composite))

        scored.append({
            "meal": meal,
            "composite_score": round(composite, 4),
            "score_breakdown": {
                "preference_fit": round(preference_fit, 4),
                "protein_goal": round(protein_score, 4),
                "sustainability": round(sustainability, 4),
                "availability": round(availability, 4),
                "reliability": round(reliability, 4),
            },
        })

    scored.sort(key=lambda x: x["composite_score"], reverse=True)
    return scored


def _protein_score(meal, target: Optional[float]) -> float:
    if target is None or target <= 0:
        return 0.5
    protein = float(meal.estimated_protein_grams or 0)
    if protein >= target:
        return 1.0
    return protein / target


def _availability_score(meal, available_ids: set) -> float:
    refs = meal.ingredient_refs if hasattr(meal, "ingredient_refs") else []
    if not refs:
        return 1.0
    required = [r for r in refs if not (r.optional if hasattr(r, "optional") else r.get("optional", False))]
    if not required:
        return 1.0
    available_count = sum(
        1 for r in required
        if (r.ingredient_id if hasattr(r, "ingredient_id") else r.get("ingredientId")) in available_ids
    )
    return available_count / len(required)
