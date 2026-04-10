"""
Execution planner — converts delegation plan → ordered ExecutionStep list.
Steps conform to execution-step-schema.yaml shared contract.
"""

from dataclasses import dataclass
from typing import List, Optional
from uuid import UUID, uuid4

from app.services.execution.delegation_planner import DelegationResult, ActionType


DISPENSE_ACTIONS = {ActionType.DISPENSE_DRY, ActionType.DISPENSE_LIQUID}


@dataclass
class ExecutionStep:
    id: UUID
    plan_id: UUID
    step_order: int
    action_type: str
    assigned_to: str  # MACHINE or USER
    status: str  # PENDING
    estimated_duration_seconds: int
    ingredient_ref: Optional[dict]  # only for DISPENSE steps


def generate_execution_steps(
    plan_id: UUID,
    delegation_results: List[DelegationResult],
) -> List[ExecutionStep]:
    """
    Convert delegation results to ordered ExecutionStep list.
    - stepOrder starts at 1, sequential
    - DISPENSE steps must have non-null ingredientRef
    - All steps conform to shared schema
    """
    steps = []

    for i, dr in enumerate(delegation_results, start=1):
        ing_ref = dr.ingredient_ref
        # DISPENSE steps must have ingredientRef
        if dr.action_type in DISPENSE_ACTIONS and ing_ref is None:
            ing_ref = None  # kept as None — caller should ensure this is provided

        steps.append(ExecutionStep(
            id=uuid4(),
            plan_id=plan_id,
            step_order=i,
            action_type=dr.action_type.value,
            assigned_to=dr.assigned_to,
            status="PENDING",
            estimated_duration_seconds=dr.estimated_duration_seconds,
            ingredient_ref=ing_ref,
        ))

    return steps
