"""
Execution planning endpoints.
POST /execution/delegation-plan — assign MACHINE/USER to steps
POST /execution/plan — generate and POST execution plan to Spring Boot
"""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import Optional, List
from uuid import UUID

from app.services.execution.delegation_planner import (
    ActionType,
    InterventionType,
    StepInput,
    plan_delegation,
)

router = APIRouter(prefix="/execution", tags=["execution"])


# --- Delegation Plan ---

class StepInputRequest(BaseModel):
    step_id: UUID = Field(alias="stepId")
    action_type: str = Field(alias="actionType")
    ingredient_ref: Optional[dict] = Field(None, alias="ingredientRef")
    estimated_duration_seconds: int = Field(30, alias="estimatedDurationSeconds")
    model_config = {"populate_by_name": True}


class DelegationRequest(BaseModel):
    meal_plan_id: UUID = Field(alias="mealPlanId")
    steps: List[StepInputRequest]
    machine_capabilities: List[str] = Field(default_factory=list, alias="machineCapabilities")
    model_config = {"populate_by_name": True}


class DelegationResultResponse(BaseModel):
    step_id: UUID = Field(alias="stepId")
    action_type: str = Field(alias="actionType")
    assigned_to: str = Field(alias="assignedTo")
    intervention_type: Optional[str] = Field(None, alias="interventionType")
    notes: str = ""
    model_config = {"populate_by_name": True}


@router.post("/delegation-plan", response_model=List[DelegationResultResponse])
async def delegation_plan(request: DelegationRequest):
    """Assign MACHINE/USER to each step. No DB write — proposal only."""
    try:
        capabilities = {ActionType(c) for c in request.machine_capabilities}
    except ValueError as e:
        raise HTTPException(status_code=422, detail=f"Invalid capability: {e}")

    steps = []
    for s in request.steps:
        try:
            action = ActionType(s.action_type)
        except ValueError:
            raise HTTPException(status_code=422, detail=f"Invalid actionType: {s.action_type}")
        steps.append(StepInput(
            step_id=s.step_id,
            action_type=action,
            ingredient_ref=s.ingredient_ref,
            estimated_duration_seconds=s.estimated_duration_seconds,
        ))

    results = plan_delegation(steps, capabilities)

    return [
        DelegationResultResponse(
            step_id=r.step_id,
            action_type=r.action_type.value,
            assigned_to=r.assigned_to,
            intervention_type=r.intervention_type.value if r.intervention_type else None,
            notes=r.notes,
        )
        for r in results
    ]
