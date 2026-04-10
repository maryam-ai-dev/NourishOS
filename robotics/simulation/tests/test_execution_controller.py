"""Tests for execution controller."""

from uuid import uuid4
from unittest.mock import MagicMock, patch
from task_planner import Primitive, ActionType, Heat
from device_bridge import DeviceBridge
from execution_controller import ExecutionController, MAX_RETRIES


def _prim(action=ActionType.HEAT, assigned_to="MACHINE", order=1):
    return Heat(step_id=uuid4(), plan_id=uuid4(), step_order=order, assigned_to=assigned_to)


class TestStepExecution:
    def test_3_step_noop_completes(self):
        prims = [_prim(order=i) for i in range(1, 4)]
        ctrl = ExecutionController()
        result = ctrl.run(str(uuid4()), prims)
        assert result["status"] == "COMPLETED"
        assert result["completed_steps"] == 3

    def test_step_complete_called_per_machine_step(self):
        callback = MagicMock()
        prims = [_prim(order=1), _prim(order=2)]
        ctrl = ExecutionController(on_step_complete=callback)
        ctrl.run(str(uuid4()), prims)
        assert callback.call_count == 2

    def test_session_update_per_step(self):
        session_cb = MagicMock()
        prims = [_prim(order=1)]
        ctrl = ExecutionController(on_session_update=session_cb)
        ctrl.run(str(uuid4()), prims)
        assert session_cb.call_count >= 1


class TestFailureHandling:
    def test_failure_triggers_retry_then_abort(self):
        failing_bridge = MagicMock(spec=DeviceBridge)
        failing_bridge.execute_action.side_effect = RuntimeError("simulated failure")
        abort_cb = MagicMock()

        ctrl = ExecutionController(bridge=failing_bridge, on_abort=abort_cb)
        result = ctrl.run(str(uuid4()), [_prim()])

        assert result["status"] == "ABORTED"
        # Should have attempted MAX_RETRIES + 1 times total
        assert failing_bridge.execute_action.call_count == MAX_RETRIES + 1
        abort_cb.assert_called_once()

    def test_exactly_2_retries_before_abort(self):
        call_count = 0
        def failing_action(p):
            nonlocal call_count
            call_count += 1
            raise RuntimeError("fail")

        bridge = MagicMock(spec=DeviceBridge)
        bridge.execute_action.side_effect = failing_action

        ctrl = ExecutionController(bridge=bridge)
        ctrl.run(str(uuid4()), [_prim()])

        assert call_count == MAX_RETRIES + 1  # 1 initial + 2 retries = 3

    def test_no_unhandled_exception(self):
        failing_bridge = MagicMock(spec=DeviceBridge)
        failing_bridge.execute_action.side_effect = RuntimeError("crash")

        ctrl = ExecutionController(bridge=failing_bridge)
        # Should not raise
        result = ctrl.run(str(uuid4()), [_prim()])
        assert result["error"] is not None

    def test_user_steps_skipped_by_controller(self):
        """USER steps are handled by intervention manager, not controller."""
        user_prim = Primitive(
            step_id=uuid4(), plan_id=uuid4(), step_order=1,
            action_type=ActionType.USER_CONFIRM, assigned_to="USER",
        )
        ctrl = ExecutionController()
        result = ctrl.run(str(uuid4()), [user_prim])
        assert result["status"] == "COMPLETED"
        assert result["completed_steps"] == 1
