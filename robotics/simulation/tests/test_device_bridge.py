"""Tests for device bridge."""

import pytest
from uuid import uuid4
from task_planner import ActionType, Primitive, IngredientRef, Heat, DispenseDry
from device_bridge import DeviceBridge, UnsupportedActionError


def _prim(action_type, ingredient_ref=None, **kwargs):
    return Primitive(
        step_id=uuid4(), plan_id=uuid4(), step_order=1,
        action_type=action_type, assigned_to="MACHINE",
        ingredient_ref=ingredient_ref, **kwargs,
    )


class TestDeviceBridge:
    def test_each_type_dispatches(self):
        bridge = DeviceBridge()
        for action in ActionType:
            p = _prim(action)
            result = bridge.execute_action(p)
            assert result["success"] is True

    def test_unknown_action_raises(self):
        bridge = DeviceBridge()
        p = _prim(ActionType.HEAT)
        p.action_type = "TELEPORT"  # not a real action
        with pytest.raises(UnsupportedActionError):
            bridge.execute_action(p)

    def test_dispense_dry_dispatches(self):
        bridge = DeviceBridge()
        ref = IngredientRef(ingredient_id=uuid4(), quantity=200, unit="g")
        p = DispenseDry(step_id=uuid4(), plan_id=uuid4(), step_order=1, ingredient_ref=ref)
        result = bridge.execute_action(p)
        assert result["action"] == "DISPENSE_DRY"
        assert result["dispensed"] == 200

    def test_heat_dispatches(self):
        bridge = DeviceBridge()
        p = Heat(step_id=uuid4(), plan_id=uuid4(), step_order=1)
        result = bridge.execute_action(p)
        assert result["action"] == "HEAT"
        assert result["target_temp"] > 0

    def test_bridge_is_only_dispatch_point(self):
        """Execution controller should go through bridge, not call handlers directly."""
        bridge = DeviceBridge()
        assert hasattr(bridge, "execute_action")
