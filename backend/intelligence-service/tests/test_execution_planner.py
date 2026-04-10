"""Tests for execution planner step generation."""

from uuid import uuid4

from app.services.execution.delegation_planner import (
    ActionType, InterventionType, DelegationResult,
)
from app.services.execution.execution_planner import generate_execution_steps


def _delegation(action_type, assigned_to="MACHINE", ingredient_ref=None):
    return DelegationResult(
        step_id=uuid4(),
        action_type=action_type,
        assigned_to=assigned_to,
        ingredient_ref=ingredient_ref,
        estimated_duration_seconds=30,
    )


class TestExecutionPlanner:
    def test_produces_step_list(self):
        dr = [_delegation(ActionType.HEAT)]
        plan_id = uuid4()
        steps = generate_execution_steps(plan_id, dr)
        assert len(steps) == 1

    def test_steps_in_correct_order(self):
        dr = [
            _delegation(ActionType.DISPENSE_DRY),
            _delegation(ActionType.HEAT),
            _delegation(ActionType.STIR),
        ]
        steps = generate_execution_steps(uuid4(), dr)
        orders = [s.step_order for s in steps]
        assert orders == [1, 2, 3]

    def test_dispense_steps_have_ingredient_ref(self):
        ref = {"ingredientId": str(uuid4()), "quantity": 200, "unit": "g"}
        dr = [_delegation(ActionType.DISPENSE_DRY, ingredient_ref=ref)]
        steps = generate_execution_steps(uuid4(), dr)
        assert steps[0].ingredient_ref is not None
        assert steps[0].ingredient_ref["quantity"] == 200

    def test_all_steps_pending_status(self):
        dr = [_delegation(ActionType.HEAT), _delegation(ActionType.STIR)]
        steps = generate_execution_steps(uuid4(), dr)
        for s in steps:
            assert s.status == "PENDING"

    def test_plan_id_set(self):
        plan_id = uuid4()
        dr = [_delegation(ActionType.HEAT)]
        steps = generate_execution_steps(plan_id, dr)
        assert steps[0].plan_id == plan_id

    def test_schema_fields_present(self):
        """Steps conform to execution-step-schema.yaml."""
        dr = [_delegation(ActionType.DISPENSE_LIQUID, ingredient_ref={"ingredientId": str(uuid4()), "quantity": 100, "unit": "ml"})]
        steps = generate_execution_steps(uuid4(), dr)
        s = steps[0]
        assert hasattr(s, "id")
        assert hasattr(s, "plan_id")
        assert hasattr(s, "step_order")
        assert hasattr(s, "action_type")
        assert hasattr(s, "assigned_to")
        assert hasattr(s, "status")
        assert hasattr(s, "estimated_duration_seconds")
        assert hasattr(s, "ingredient_ref")
