"""
Explanation generator — human-readable decision explanations.
Covers: MEAL_RANKED, REORDER_SUGGESTED, REORDER_ADJUSTED_FOR_WASTE,
        WASTE_PATTERN_DETECTED, INTERVENTION_REQUIRED, MEAL_RELIABILITY_LOW,
        SUBSTITUTION_PROPOSED
"""

from dataclasses import dataclass, field
from typing import List, Dict, Optional
from enum import Enum


class DecisionType(str, Enum):
    MEAL_RANKED = "MEAL_RANKED"
    REORDER_SUGGESTED = "REORDER_SUGGESTED"
    REORDER_ADJUSTED_FOR_WASTE = "REORDER_ADJUSTED_FOR_WASTE"
    WASTE_PATTERN_DETECTED = "WASTE_PATTERN_DETECTED"
    INTERVENTION_REQUIRED = "INTERVENTION_REQUIRED"
    MEAL_RELIABILITY_LOW = "MEAL_RELIABILITY_LOW"
    SUBSTITUTION_PROPOSED = "SUBSTITUTION_PROPOSED"


@dataclass
class SupportingFactor:
    name: str
    value: str


@dataclass
class Explanation:
    decision_type: str
    explanation: str
    supporting_factors: List[SupportingFactor]


def generate_explanation(
    decision_type: DecisionType,
    context: Dict,
) -> Explanation:
    """
    Generate human-readable explanation for a decision.
    Context dict provides the data needed for each type.
    """
    if decision_type == DecisionType.MEAL_RANKED:
        return _explain_meal_ranked(context)
    elif decision_type == DecisionType.REORDER_SUGGESTED:
        return _explain_reorder_suggested(context)
    elif decision_type == DecisionType.REORDER_ADJUSTED_FOR_WASTE:
        return _explain_reorder_adjusted(context)
    elif decision_type == DecisionType.WASTE_PATTERN_DETECTED:
        return _explain_waste_pattern(context)
    elif decision_type == DecisionType.INTERVENTION_REQUIRED:
        return _explain_intervention(context)
    elif decision_type == DecisionType.MEAL_RELIABILITY_LOW:
        return _explain_low_reliability(context)
    elif decision_type == DecisionType.SUBSTITUTION_PROPOSED:
        return _explain_substitution(context)
    else:
        raise ValueError(f"Unknown decision type: {decision_type}")


def _explain_meal_ranked(ctx: Dict) -> Explanation:
    scores = ctx.get("score_breakdown", {})
    meal_name = ctx.get("meal_name", "this meal")

    # Find top driver
    if scores:
        top = max(scores.items(), key=lambda x: x[1])
        top_driver = top[0]
        top_value = top[1]
    else:
        top_driver = "composite"
        top_value = 0

    explanation = (
        f"{meal_name} was ranked based on a composite score. "
        f"The top driver was {top_driver} ({top_value:.2f})."
    )

    factors = [
        SupportingFactor(name=k, value=f"{v:.4f}")
        for k, v in scores.items()
    ]

    return Explanation(
        decision_type=DecisionType.MEAL_RANKED.value,
        explanation=explanation,
        supporting_factors=factors,
    )


def _explain_reorder_suggested(ctx: Dict) -> Explanation:
    ingredient = ctx.get("ingredient_name", "ingredient")
    quantity = ctx.get("quantity", 0)
    urgency = ctx.get("urgency", "UNKNOWN")

    return Explanation(
        decision_type=DecisionType.REORDER_SUGGESTED.value,
        explanation=f"Reorder {quantity} of {ingredient} suggested due to {urgency} urgency.",
        supporting_factors=[
            SupportingFactor(name="urgency", value=urgency),
            SupportingFactor(name="quantity", value=str(quantity)),
        ],
    )


def _explain_reorder_adjusted(ctx: Dict) -> Explanation:
    ingredient = ctx.get("ingredient_name", "ingredient")
    original = ctx.get("original_quantity", 0)
    adjusted = ctx.get("adjusted_quantity", 0)
    reduction = original - adjusted

    return Explanation(
        decision_type=DecisionType.REORDER_ADJUSTED_FOR_WASTE.value,
        explanation=(
            f"Reorder for {ingredient} reduced by {reduction:.0f} "
            f"(from {original} to {adjusted}) due to recurring waste history."
        ),
        supporting_factors=[
            SupportingFactor(name="ingredient", value=ingredient),
            SupportingFactor(name="original_quantity", value=str(original)),
            SupportingFactor(name="adjusted_quantity", value=str(adjusted)),
            SupportingFactor(name="reduction", value=f"{reduction:.0f}"),
        ],
    )


def _explain_waste_pattern(ctx: Dict) -> Explanation:
    ingredient = ctx.get("ingredient_name", "ingredient")
    reason = ctx.get("top_reason", "UNKNOWN")
    count = ctx.get("waste_count", 0)

    return Explanation(
        decision_type=DecisionType.WASTE_PATTERN_DETECTED.value,
        explanation=(
            f"Recurring waste detected for {ingredient}: "
            f"wasted {count} times, primarily due to {reason}."
        ),
        supporting_factors=[
            SupportingFactor(name="ingredient", value=ingredient),
            SupportingFactor(name="waste_reason", value=reason),
            SupportingFactor(name="waste_count", value=str(count)),
        ],
    )


def _explain_intervention(ctx: Dict) -> Explanation:
    step_name = ctx.get("step_name", "step")
    intervention_type = ctx.get("intervention_type", "UNKNOWN")
    step_order = ctx.get("step_order", 0)

    return Explanation(
        decision_type=DecisionType.INTERVENTION_REQUIRED.value,
        explanation=(
            f"User intervention required at step {step_order} ({step_name}): "
            f"intervention type {intervention_type}."
        ),
        supporting_factors=[
            SupportingFactor(name="step", value=step_name),
            SupportingFactor(name="intervention_type", value=intervention_type),
            SupportingFactor(name="step_order", value=str(step_order)),
        ],
    )


def _explain_low_reliability(ctx: Dict) -> Explanation:
    meal_name = ctx.get("meal_name", "meal")
    completion_rate = ctx.get("completion_rate", 0)

    return Explanation(
        decision_type=DecisionType.MEAL_RELIABILITY_LOW.value,
        explanation=(
            f"{meal_name} has low reliability with a {completion_rate:.0%} completion rate. "
            f"Consider simpler alternatives."
        ),
        supporting_factors=[
            SupportingFactor(name="meal", value=meal_name),
            SupportingFactor(name="completion_rate", value=f"{completion_rate:.2f}"),
        ],
    )


def _explain_substitution(ctx: Dict) -> Explanation:
    original = ctx.get("original_name", "ingredient")
    substitute = ctx.get("substitute_name", "alternative")
    reason = ctx.get("reason", "availability")

    return Explanation(
        decision_type=DecisionType.SUBSTITUTION_PROPOSED.value,
        explanation=f"Substituting {original} with {substitute} due to {reason}.",
        supporting_factors=[
            SupportingFactor(name="original", value=original),
            SupportingFactor(name="substitute", value=substitute),
            SupportingFactor(name="reason", value=reason),
        ],
    )
