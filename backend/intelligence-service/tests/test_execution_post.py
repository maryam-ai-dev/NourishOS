"""Tests for execution plan POST to Spring Boot."""

from uuid import uuid4
from unittest.mock import patch, MagicMock

from app.services.execution.delegation_planner import ActionType, DelegationResult
from app.services.execution.execution_planner import generate_execution_steps
from app.clients.authority_client import post_execution_plan


class TestExecutionPost:
    def test_steps_serializable_for_post(self):
        plan_id = uuid4()
        dr = [DelegationResult(
            step_id=uuid4(),
            action_type=ActionType.DISPENSE_DRY,
            assigned_to="MACHINE",
            ingredient_ref={"ingredientId": str(uuid4()), "quantity": 200, "unit": "g"},
            estimated_duration_seconds=15,
        )]
        steps = generate_execution_steps(plan_id, dr)

        payload = {
            "mealPlanId": str(uuid4()),
            "steps": [{
                "stepOrder": s.step_order,
                "actionType": s.action_type,
                "assignedTo": s.assigned_to,
                "status": s.status,
                "estimatedDurationSeconds": s.estimated_duration_seconds,
                "ingredientRef": s.ingredient_ref,
            } for s in steps],
        }
        assert len(payload["steps"]) == 1
        assert payload["steps"][0]["ingredientRef"] is not None

    @patch("app.clients.authority_client._get_client")
    def test_post_calls_spring_boot(self, mock_client_factory):
        mock_client = MagicMock()
        mock_response = MagicMock()
        mock_response.status_code = 201
        mock_response.json.return_value = {"id": str(uuid4()), "status": "PENDING"}
        mock_client.post.return_value = mock_response
        mock_client_factory.return_value = mock_client

        result = post_execution_plan({
            "mealPlanId": str(uuid4()),
            "steps": [],
        })

        mock_client.post.assert_called_once()
        assert "id" in result

    def test_step_count_matches_delegation(self):
        plan_id = uuid4()
        dr = [
            DelegationResult(step_id=uuid4(), action_type=ActionType.DISPENSE_DRY, assigned_to="MACHINE", estimated_duration_seconds=10),
            DelegationResult(step_id=uuid4(), action_type=ActionType.HEAT, assigned_to="MACHINE", estimated_duration_seconds=60),
            DelegationResult(step_id=uuid4(), action_type=ActionType.USER_CONFIRM, assigned_to="USER", estimated_duration_seconds=30),
        ]
        steps = generate_execution_steps(plan_id, dr)
        assert len(steps) == len(dr)
