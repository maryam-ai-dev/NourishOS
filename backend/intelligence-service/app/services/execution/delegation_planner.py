"""
Delegation planner — assigns MACHINE or USER to each step based on device capabilities.
All steps assigned before execution starts — never mid-execution.
"""

from dataclasses import dataclass, field
from typing import List, Optional, Set
from uuid import UUID
from enum import Enum


class ActionType(str, Enum):
    DISPENSE_DRY = "DISPENSE_DRY"
    DISPENSE_LIQUID = "DISPENSE_LIQUID"
    HEAT = "HEAT"
    STIR = "STIR"
    LOAD_MODULE = "LOAD_MODULE"
    MOVE_MODULE = "MOVE_MODULE"
    USER_LOAD_TRAY = "USER_LOAD_TRAY"
    USER_CONFIRM = "USER_CONFIRM"
    USER_REMOVE_VESSEL = "USER_REMOVE_VESSEL"


class InterventionType(str, Enum):
    LOAD_TRAY = "LOAD_TRAY"
    CONFIRM_READY = "CONFIRM_READY"
    REMOVE_VESSEL = "REMOVE_VESSEL"
    CHECK_DONENESS = "CHECK_DONENESS"
    ADD_INGREDIENT = "ADD_INGREDIENT"


# Map USER action types to their intervention types
USER_ACTION_INTERVENTIONS = {
    ActionType.USER_LOAD_TRAY: InterventionType.LOAD_TRAY,
    ActionType.USER_CONFIRM: InterventionType.CONFIRM_READY,
    ActionType.USER_REMOVE_VESSEL: InterventionType.REMOVE_VESSEL,
}


@dataclass
class StepInput:
    step_id: UUID
    action_type: ActionType
    ingredient_ref: Optional[dict] = None
    estimated_duration_seconds: int = 30


@dataclass
class DelegationResult:
    step_id: UUID
    action_type: ActionType
    assigned_to: str  # MACHINE or USER
    intervention_type: Optional[InterventionType] = None
    notes: str = ""
    ingredient_ref: Optional[dict] = None
    estimated_duration_seconds: int = 30


def plan_delegation(
    steps: List[StepInput],
    machine_capabilities: Set[ActionType],
) -> List[DelegationResult]:
    """
    Assign MACHINE or USER to each step.
    - MACHINE if action_type in machine_capabilities
    - USER otherwise, with appropriate interventionType
    - All steps assigned — no unassigned steps allowed
    """
    results = []

    for step in steps:
        if step.action_type in machine_capabilities:
            results.append(DelegationResult(
                step_id=step.step_id,
                action_type=step.action_type,
                assigned_to="MACHINE",
                notes=f"Device capable of {step.action_type.value}",
                ingredient_ref=step.ingredient_ref,
                estimated_duration_seconds=step.estimated_duration_seconds,
            ))
        else:
            # USER step — determine intervention type
            intervention = USER_ACTION_INTERVENTIONS.get(step.action_type)
            if intervention is None:
                # Default for machine actions that need user fallback
                intervention = InterventionType.CONFIRM_READY

            results.append(DelegationResult(
                step_id=step.step_id,
                action_type=step.action_type,
                assigned_to="USER",
                intervention_type=intervention,
                notes=f"No device capability for {step.action_type.value}; user intervention required",
                ingredient_ref=step.ingredient_ref,
                estimated_duration_seconds=step.estimated_duration_seconds,
            ))

    return results
