"""Tests for POST /execution/plan endpoint."""

from fastapi.testclient import TestClient
from uuid import uuid4

from app.main import app

client = TestClient(app)


class TestExecutionPlanEndpoint:
    def test_returns_200(self):
        response = client.post("/execution/plan", json={
            "mealPlanId": str(uuid4()),
            "steps": [
                {"stepId": str(uuid4()), "actionType": "DISPENSE_DRY", "estimatedDurationSeconds": 15},
                {"stepId": str(uuid4()), "actionType": "HEAT", "estimatedDurationSeconds": 60},
                {"stepId": str(uuid4()), "actionType": "USER_CONFIRM", "estimatedDurationSeconds": 30},
            ],
            "machineCapabilities": ["DISPENSE_DRY", "HEAT"],
        })
        assert response.status_code == 200
        data = response.json()
        assert "executionPlanId" in data
        assert "steps" in data

    def test_user_interventions_lists_user_steps_only(self):
        response = client.post("/execution/plan", json={
            "mealPlanId": str(uuid4()),
            "steps": [
                {"stepId": str(uuid4()), "actionType": "HEAT", "estimatedDurationSeconds": 60},
                {"stepId": str(uuid4()), "actionType": "USER_CONFIRM", "estimatedDurationSeconds": 30},
            ],
            "machineCapabilities": ["HEAT"],
        })
        data = response.json()
        assert len(data["userInterventions"]) == 1
        assert data["userInterventions"][0]["actionType"] == "USER_CONFIRM"

    def test_total_duration_is_sum(self):
        response = client.post("/execution/plan", json={
            "mealPlanId": str(uuid4()),
            "steps": [
                {"stepId": str(uuid4()), "actionType": "DISPENSE_DRY", "estimatedDurationSeconds": 15},
                {"stepId": str(uuid4()), "actionType": "HEAT", "estimatedDurationSeconds": 60},
            ],
            "machineCapabilities": ["DISPENSE_DRY", "HEAT"],
        })
        data = response.json()
        total = sum(s["estimatedDurationSeconds"] for s in data["steps"])
        assert data["estimatedDurationSeconds"] == total

    def test_empty_steps_returns_valid_response(self):
        response = client.post("/execution/plan", json={
            "mealPlanId": str(uuid4()),
            "steps": [],
            "machineCapabilities": [],
        })
        assert response.status_code == 200
        data = response.json()
        assert data["estimatedDurationSeconds"] == 0
        assert data["steps"] == []
