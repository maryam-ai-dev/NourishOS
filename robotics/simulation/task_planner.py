"""
Task planner — defines action primitives and converts ExecutionPlan steps to primitives.
Each primitive conforms to execution-step-schema.yaml shared contract.
"""

from pydantic import BaseModel
from typing import Optional, List
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


class IngredientRef(BaseModel):
    ingredient_id: UUID
    quantity: float
    unit: str


class Primitive(BaseModel):
    """Base primitive conforming to shared execution-step-schema.yaml."""
    step_id: UUID
    plan_id: UUID
    step_order: int
    action_type: ActionType
    assigned_to: str  # MACHINE or USER
    status: str = "PENDING"
    estimated_duration_seconds: int = 30
    ingredient_ref: Optional[IngredientRef] = None

    # Extended fields for simulation
    target_temp_c: Optional[float] = None  # for HEAT
    rpm: Optional[int] = None  # for STIR
    slot_id: Optional[str] = None  # for LOAD/MOVE


class DispenseDry(Primitive):
    action_type: ActionType = ActionType.DISPENSE_DRY
    assigned_to: str = "MACHINE"


class DispenseLiquid(Primitive):
    action_type: ActionType = ActionType.DISPENSE_LIQUID
    assigned_to: str = "MACHINE"


class Heat(Primitive):
    action_type: ActionType = ActionType.HEAT
    assigned_to: str = "MACHINE"
    target_temp_c: float = 180.0


class Stir(Primitive):
    action_type: ActionType = ActionType.STIR
    assigned_to: str = "MACHINE"
    rpm: int = 100


class LoadModule(Primitive):
    action_type: ActionType = ActionType.LOAD_MODULE
    assigned_to: str = "MACHINE"


class MoveModule(Primitive):
    action_type: ActionType = ActionType.MOVE_MODULE
    assigned_to: str = "MACHINE"


class RequestUserAction(Primitive):
    action_type: ActionType = ActionType.USER_CONFIRM
    assigned_to: str = "USER"


class MarkActionComplete(Primitive):
    action_type: ActionType = ActionType.USER_REMOVE_VESSEL
    assigned_to: str = "USER"


# All 8 primitive types
PRIMITIVE_TYPES = {
    ActionType.DISPENSE_DRY: DispenseDry,
    ActionType.DISPENSE_LIQUID: DispenseLiquid,
    ActionType.HEAT: Heat,
    ActionType.STIR: Stir,
    ActionType.LOAD_MODULE: LoadModule,
    ActionType.MOVE_MODULE: MoveModule,
    ActionType.USER_CONFIRM: RequestUserAction,
    ActionType.USER_REMOVE_VESSEL: MarkActionComplete,
}


def execution_plan_to_primitives(steps: List[dict]) -> List[Primitive]:
    """
    Convert ExecutionPlan steps (from Spring Boot) to ordered primitives.
    Each step dict has: stepOrder, actionType, assignedTo, estimatedDurationSeconds, ingredientRef, id, planId
    """
    primitives = []

    for step in steps:
        action_str = step.get("actionType", step.get("action_type", ""))
        try:
            action = ActionType(action_str)
        except ValueError:
            action = ActionType.USER_CONFIRM  # fallback

        prim_class = PRIMITIVE_TYPES.get(action, Primitive)

        ing_ref = None
        raw_ref = step.get("ingredientRef", step.get("ingredient_ref"))
        if raw_ref and isinstance(raw_ref, dict):
            ing_ref = IngredientRef(
                ingredient_id=raw_ref.get("ingredientId", raw_ref.get("ingredient_id", "00000000-0000-0000-0000-000000000000")),
                quantity=raw_ref.get("quantity", 0),
                unit=raw_ref.get("unit", "g"),
            )

        prim = prim_class(
            step_id=step.get("id", step.get("step_id", "00000000-0000-0000-0000-000000000000")),
            plan_id=step.get("planId", step.get("plan_id", "00000000-0000-0000-0000-000000000000")),
            step_order=step.get("stepOrder", step.get("step_order", 0)),
            assigned_to=step.get("assignedTo", step.get("assigned_to", "MACHINE")),
            status=step.get("status", "PENDING"),
            estimated_duration_seconds=step.get("estimatedDurationSeconds", step.get("estimated_duration_seconds", 30)),
            ingredient_ref=ing_ref,
        )
        primitives.append(prim)

    return primitives
