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
from app.services.execution.execution_planner import generate_execution_steps
from app.clients.authority_client import post_execution_plan
from uuid import uuid4 as new_uuid

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


# --- Execution Plan ---

class ExecutionPlanRequest(BaseModel):
    meal_plan_id: UUID = Field(alias="mealPlanId")
    steps: List[StepInputRequest]
    machine_capabilities: List[str] = Field(default_factory=list, alias="machineCapabilities")
    model_config = {"populate_by_name": True}


class ExecutionStepResponse(BaseModel):
    step_order: int = Field(alias="stepOrder")
    action_type: str = Field(alias="actionType")
    assigned_to: str = Field(alias="assignedTo")
    status: str
    estimated_duration_seconds: int = Field(alias="estimatedDurationSeconds")
    ingredient_ref: Optional[dict] = Field(None, alias="ingredientRef")
    model_config = {"populate_by_name": True}


class UserInterventionResponse(BaseModel):
    step_order: int = Field(alias="stepOrder")
    action_type: str = Field(alias="actionType")
    intervention_type: str = Field(alias="interventionType")
    model_config = {"populate_by_name": True}


class ExecutionPlanResponse(BaseModel):
    execution_plan_id: UUID = Field(alias="executionPlanId")
    steps: List[ExecutionStepResponse]
    estimated_duration_seconds: int = Field(alias="estimatedDurationSeconds")
    user_interventions: List[UserInterventionResponse] = Field(alias="userInterventions")
    model_config = {"populate_by_name": True}


@router.post("/plan", response_model=ExecutionPlanResponse)
async def create_execution_plan(request: ExecutionPlanRequest):
    """Generate execution plan and POST to Spring Boot."""
    try:
        capabilities = {ActionType(c) for c in request.machine_capabilities}
    except ValueError as e:
        raise HTTPException(status_code=422, detail=f"Invalid capability: {e}")

    step_inputs = []
    for s in request.steps:
        try:
            action = ActionType(s.action_type)
        except ValueError:
            raise HTTPException(status_code=422, detail=f"Invalid actionType: {s.action_type}")
        step_inputs.append(StepInput(
            step_id=s.step_id,
            action_type=action,
            ingredient_ref=s.ingredient_ref,
            estimated_duration_seconds=s.estimated_duration_seconds,
        ))

    # Delegate
    delegation_results = plan_delegation(step_inputs, capabilities)

    # Generate steps
    plan_id = new_uuid()
    exec_steps = generate_execution_steps(plan_id, delegation_results)

    # Try POST to Spring Boot
    try:
        payload = {
            "mealPlanId": str(request.meal_plan_id),
            "steps": [{
                "stepOrder": s.step_order,
                "actionType": s.action_type,
                "assignedTo": s.assigned_to,
                "status": s.status,
                "estimatedDurationSeconds": s.estimated_duration_seconds,
                "ingredientRef": s.ingredient_ref,
            } for s in exec_steps],
        }
        spring_result = post_execution_plan(payload)
        plan_id_str = spring_result.get("id", str(plan_id))
    except Exception:
        plan_id_str = str(plan_id)

    total_duration = sum(s.estimated_duration_seconds for s in exec_steps)

    user_interventions = [
        UserInterventionResponse(
            step_order=s.step_order,
            action_type=s.action_type,
            intervention_type=dr.intervention_type.value if dr.intervention_type else "CONFIRM_READY",
        )
        for s, dr in zip(exec_steps, delegation_results)
        if s.assigned_to == "USER"
    ]

    return ExecutionPlanResponse(
        execution_plan_id=plan_id_str if isinstance(plan_id_str, UUID) else UUID(plan_id_str) if len(str(plan_id_str)) == 36 else plan_id,
        steps=[
            ExecutionStepResponse(
                step_order=s.step_order,
                action_type=s.action_type,
                assigned_to=s.assigned_to,
                status=s.status,
                estimated_duration_seconds=s.estimated_duration_seconds,
                ingredient_ref=s.ingredient_ref,
            )
            for s in exec_steps
        ],
        estimated_duration_seconds=total_duration,
        user_interventions=user_interventions,
    )
