"""Tests for task planner — primitive definitions and ExecutionPlan conversion."""

from uuid import uuid4
from task_planner import (
    ActionType, Primitive, DispenseDry, DispenseLiquid, Heat, Stir,
    LoadModule, MoveModule, RequestUserAction, MarkActionComplete,
    PRIMITIVE_TYPES, execution_plan_to_primitives,
)


class TestPrimitiveDefinitions:
    def test_all_8_primitives_instantiate(self):
        plan_id = uuid4()
        for i, (action, cls) in enumerate(PRIMITIVE_TYPES.items(), 1):
            p = cls(step_id=uuid4(), plan_id=plan_id, step_order=i)
            assert p.action_type == action
            assert p.step_order == i

    def test_primitive_fields_match_schema(self):
        """Fields conform to execution-step-schema.yaml."""
        p = Heat(step_id=uuid4(), plan_id=uuid4(), step_order=1)
        assert hasattr(p, "step_id")
        assert hasattr(p, "plan_id")
        assert hasattr(p, "step_order")
        assert hasattr(p, "action_type")
        assert hasattr(p, "assigned_to")
        assert hasattr(p, "status")
        assert hasattr(p, "estimated_duration_seconds")
        assert hasattr(p, "ingredient_ref")

    def test_heat_has_target_temp(self):
        p = Heat(step_id=uuid4(), plan_id=uuid4(), step_order=1)
        assert p.target_temp_c is not None
        assert p.target_temp_c > 0

    def test_dispense_accepts_ingredient_ref(self):
        from task_planner import IngredientRef
        ref = IngredientRef(ingredient_id=uuid4(), quantity=200, unit="g")
        p = DispenseDry(step_id=uuid4(), plan_id=uuid4(), step_order=1, ingredient_ref=ref)
        assert p.ingredient_ref is not None
        assert p.ingredient_ref.quantity == 200


class TestPlanToPrimitives:
    def test_4_steps_produce_4_primitives(self):
        plan_id = str(uuid4())
        steps = [
            {"id": str(uuid4()), "planId": plan_id, "stepOrder": 1, "actionType": "DISPENSE_DRY", "assignedTo": "MACHINE", "estimatedDurationSeconds": 15, "ingredientRef": {"ingredientId": str(uuid4()), "quantity": 200, "unit": "g"}},
            {"id": str(uuid4()), "planId": plan_id, "stepOrder": 2, "actionType": "HEAT", "assignedTo": "MACHINE", "estimatedDurationSeconds": 60},
            {"id": str(uuid4()), "planId": plan_id, "stepOrder": 3, "actionType": "STIR", "assignedTo": "MACHINE", "estimatedDurationSeconds": 30},
            {"id": str(uuid4()), "planId": plan_id, "stepOrder": 4, "actionType": "USER_CONFIRM", "assignedTo": "USER", "estimatedDurationSeconds": 20},
        ]
        prims = execution_plan_to_primitives(steps)
        assert len(prims) == 4

    def test_correct_order(self):
        steps = [
            {"id": str(uuid4()), "planId": str(uuid4()), "stepOrder": 1, "actionType": "DISPENSE_DRY", "assignedTo": "MACHINE"},
            {"id": str(uuid4()), "planId": str(uuid4()), "stepOrder": 2, "actionType": "HEAT", "assignedTo": "MACHINE"},
        ]
        prims = execution_plan_to_primitives(steps)
        assert prims[0].step_order == 1
        assert prims[1].step_order == 2

    def test_heat_has_nonzero_duration(self):
        steps = [
            {"id": str(uuid4()), "planId": str(uuid4()), "stepOrder": 1, "actionType": "HEAT", "assignedTo": "MACHINE", "estimatedDurationSeconds": 60},
        ]
        prims = execution_plan_to_primitives(steps)
        assert prims[0].estimated_duration_seconds > 0

    def test_dispense_has_ingredient_ref(self):
        ref = {"ingredientId": str(uuid4()), "quantity": 300, "unit": "ml"}
        steps = [
            {"id": str(uuid4()), "planId": str(uuid4()), "stepOrder": 1, "actionType": "DISPENSE_LIQUID", "assignedTo": "MACHINE", "ingredientRef": ref},
        ]
        prims = execution_plan_to_primitives(steps)
        assert prims[0].ingredient_ref is not None
        assert prims[0].ingredient_ref.quantity == 300
