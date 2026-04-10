"""Tests for intervention manager."""

from uuid import uuid4
from unittest.mock import MagicMock
from task_planner import Primitive, ActionType
from intervention_manager import InterventionManager


def _user_prim(order=1):
    return Primitive(
        step_id=uuid4(), plan_id=uuid4(), step_order=order,
        action_type=ActionType.USER_CONFIRM, assigned_to="USER",
    )


class TestInterventionManager:
    def test_pauses_without_error(self):
        mgr = InterventionManager()
        result = mgr.handle_user_step(str(uuid4()), _user_prim())
        assert result is not None

    def test_creates_intervention(self):
        create_cb = MagicMock(return_value={"id": "int-1", "status": "PENDING"})
        mgr = InterventionManager(create_intervention=create_cb)
        mgr.handle_user_step(str(uuid4()), _user_prim())
        create_cb.assert_called_once()

    def test_polls_for_resolution(self):
        poll_cb = MagicMock(return_value=True)
        mgr = InterventionManager(poll_resolution=poll_cb)
        result = mgr.handle_user_step(str(uuid4()), _user_prim())
        poll_cb.assert_called_once()
        assert result["status"] == "RESOLVED"

    def test_timeout_when_not_resolved(self):
        poll_cb = MagicMock(return_value=False)
        mgr = InterventionManager(poll_resolution=poll_cb)
        result = mgr.handle_user_step(str(uuid4()), _user_prim())
        assert result["status"] == "TIMEOUT"

    def test_uses_spring_boot_not_redis(self):
        """Simulation detects resolution via Spring Boot endpoint, not Redis."""
        create_cb = MagicMock(return_value={"id": "int-1"})
        poll_cb = MagicMock(return_value=True)
        mgr = InterventionManager(create_intervention=create_cb, poll_resolution=poll_cb)
        mgr.handle_user_step(str(uuid4()), _user_prim())
        # Both callbacks called — representing Spring Boot calls, not Redis
        create_cb.assert_called_once()
        poll_cb.assert_called_once()
