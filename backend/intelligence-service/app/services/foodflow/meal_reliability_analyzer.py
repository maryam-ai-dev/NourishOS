"""
Meal reliability analyzer — per-meal completion rate, abandonment rate, reliability score.
LOW_RELIABILITY if completionRate < 0.5.
"""

from dataclasses import dataclass, field
from typing import List, Dict
from uuid import UUID
from collections import defaultdict


LOW_RELIABILITY_THRESHOLD = 0.5


@dataclass
class MealOutcomeEvent:
    id: UUID
    meal_option_id: UUID
    outcome: str  # COMPLETED, ABANDONED, SUBSTITUTED, PARTIALLY_COMPLETED


@dataclass
class MealReliability:
    meal_option_id: UUID
    completion_rate: float
    abandonment_rate: float
    substitution_rate: float
    reliability_score: float
    is_low_reliability: bool


def analyze_meal_reliability(outcomes: List[MealOutcomeEvent]) -> Dict[UUID, MealReliability]:
    """
    Per-meal reliability stats.
    - completionRate = COMPLETED / total
    - abandonmentRate = ABANDONED / total
    - substitutionRate = SUBSTITUTED / total
    - reliabilityScore = completionRate * 0.7 + (1 - substitutionRate) * 0.3
    - LOW_RELIABILITY if completionRate < 0.5
    """
    by_meal: Dict[UUID, List[MealOutcomeEvent]] = defaultdict(list)
    for o in outcomes:
        by_meal[o.meal_option_id].append(o)

    results = {}

    for meal_id, events in by_meal.items():
        total = len(events)
        completed = sum(1 for e in events if e.outcome == "COMPLETED")
        abandoned = sum(1 for e in events if e.outcome == "ABANDONED")
        substituted = sum(1 for e in events if e.outcome == "SUBSTITUTED")

        completion_rate = completed / total if total > 0 else 0.0
        abandonment_rate = abandoned / total if total > 0 else 0.0
        substitution_rate = substituted / total if total > 0 else 0.0

        reliability_score = completion_rate * 0.7 + (1 - substitution_rate) * 0.3

        results[meal_id] = MealReliability(
            meal_option_id=meal_id,
            completion_rate=completion_rate,
            abandonment_rate=abandonment_rate,
            substitution_rate=substitution_rate,
            reliability_score=reliability_score,
            is_low_reliability=completion_rate < LOW_RELIABILITY_THRESHOLD,
        )

    return results


def get_reliability_insights(outcomes: List[MealOutcomeEvent]) -> Dict[UUID, MealReliability]:
    """Alias for recommendation ranker integration."""
    return analyze_meal_reliability(outcomes)
