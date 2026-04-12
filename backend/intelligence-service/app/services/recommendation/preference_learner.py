"""
Preference learner — applies MealPreferenceFeedback to preference_fit scoring.
Boosts meals frequently CONFIRMED/COMPLETED; penalises SWAPPED_OUT/REJECTED.
"""

from typing import Dict, List
from collections import defaultdict


CONFIRMED_BOOST = 0.1
SWAPPED_OUT_PENALTY = -0.15
REJECTED_PENALTY = -0.15
COMPLETED_BOOST = 0.05


def compute_preference_modifiers(feedback_events: List[dict]) -> Dict[str, float]:
    """
    Input: list of feedback events with shape { mealOptionId, feedbackType }
    Output: { meal_option_id_str: modifier } where modifier is clamped to [-0.5, 0.5]
    """
    modifiers: Dict[str, float] = defaultdict(float)

    for event in feedback_events:
        meal_id = str(event.get("mealOptionId", ""))
        feedback_type = event.get("feedbackType", "")

        if not meal_id:
            continue

        if feedback_type == "CONFIRMED":
            modifiers[meal_id] += CONFIRMED_BOOST
        elif feedback_type == "COMPLETED":
            modifiers[meal_id] += COMPLETED_BOOST
        elif feedback_type == "SWAPPED_OUT":
            modifiers[meal_id] += SWAPPED_OUT_PENALTY
        elif feedback_type == "REJECTED":
            modifiers[meal_id] += REJECTED_PENALTY

    # Clamp each modifier
    return {mid: max(-0.5, min(0.5, m)) for mid, m in modifiers.items()}


def apply_to_preference_fit(base_score: float, modifier: float) -> float:
    """Apply modifier to the preference_fit score, clamped to [0, 1]."""
    return max(0.0, min(1.0, base_score + modifier))
