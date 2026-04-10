"""Tests for delegation planner."""

from uuid import uuid4

from app.services.execution.delegation_planner import (
    ActionType,
    InterventionType,
    StepInput,
    plan_delegation,
)


def _step(action_type, ingredient_ref=None):
    return StepInput(step_id=uuid4(), action_type=action_type, ingredient_ref=ingredient_ref)


class TestDelegationCapabilityCheck:
    def test_assigns_without_error(self):
        steps = [_step(ActionType.DISPENSE_DRY)]
        result = plan_delegation(steps, {ActionType.DISPENSE_DRY})
        assert len(result) == 1

    def test_capable_device_assigned_machine(self):
        steps = [_step(ActionType.HEAT)]
        result = plan_delegation(steps, {ActionType.HEAT})
        assert result[0].assigned_to == "MACHINE"

    def test_unavailable_device_assigned_user(self):
        steps = [_step(ActionType.HEAT)]
        result = plan_delegation(steps, set())  # no capabilities
        assert result[0].assigned_to == "USER"

    def test_no_unassigned_steps(self):
        steps = [
            _step(ActionType.DISPENSE_DRY),
            _step(ActionType.HEAT),
            _step(ActionType.USER_CONFIRM),
        ]
        result = plan_delegation(steps, {ActionType.DISPENSE_DRY})
        for r in result:
            assert r.assigned_to in ("MACHINE", "USER")

    def test_all_steps_assigned_before_execution(self):
        """All steps assigned — never mid-execution."""
        steps = [_step(ActionType.STIR), _step(ActionType.USER_LOAD_TRAY)]
        result = plan_delegation(steps, {ActionType.STIR})
        assert len(result) == len(steps)


class TestInterventionTyping:
    def test_user_steps_have_intervention_type(self):
        steps = [_step(ActionType.USER_LOAD_TRAY)]
        result = plan_delegation(steps, set())
        assert result[0].intervention_type is not None
        assert result[0].intervention_type == InterventionType.LOAD_TRAY

    def test_all_user_steps_have_non_null_intervention(self):
        steps = [
            _step(ActionType.USER_LOAD_TRAY),
            _step(ActionType.USER_CONFIRM),
            _step(ActionType.USER_REMOVE_VESSEL),
            _step(ActionType.HEAT),  # no capability → USER fallback
        ]
        result = plan_delegation(steps, set())
        for r in result:
            if r.assigned_to == "USER":
                assert r.intervention_type is not None

    def test_machine_steps_have_no_intervention(self):
        steps = [_step(ActionType.DISPENSE_DRY)]
        result = plan_delegation(steps, {ActionType.DISPENSE_DRY})
        assert result[0].intervention_type is None

    def test_valid_intervention_enum_values(self):
        steps = [_step(ActionType.USER_CONFIRM)]
        result = plan_delegation(steps, set())
        assert isinstance(result[0].intervention_type, InterventionType)
