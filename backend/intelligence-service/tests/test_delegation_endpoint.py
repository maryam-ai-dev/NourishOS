"""Tests for POST /execution/delegation-plan endpoint."""

from fastapi.testclient import TestClient
from uuid import uuid4

from app.main import app

client = TestClient(app)


class TestDelegationEndpoint:
    def test_returns_200(self):
        response = client.post("/execution/delegation-plan", json={
            "mealPlanId": str(uuid4()),
            "steps": [{
                "stepId": str(uuid4()),
                "actionType": "DISPENSE_DRY",
            }],
            "machineCapabilities": ["DISPENSE_DRY"],
        })
        assert response.status_code == 200
        data = response.json()
        assert len(data) == 1

    def test_all_entries_have_assigned_to(self):
        response = client.post("/execution/delegation-plan", json={
            "mealPlanId": str(uuid4()),
            "steps": [
                {"stepId": str(uuid4()), "actionType": "HEAT"},
                {"stepId": str(uuid4()), "actionType": "STIR"},
                {"stepId": str(uuid4()), "actionType": "USER_CONFIRM"},
            ],
            "machineCapabilities": ["HEAT"],
        })
        data = response.json()
        for entry in data:
            assert entry["assignedTo"] in ("MACHINE", "USER")

    def test_invalid_action_type_returns_422(self):
        response = client.post("/execution/delegation-plan", json={
            "mealPlanId": str(uuid4()),
            "steps": [{"stepId": str(uuid4()), "actionType": "INVALID_ACTION"}],
            "machineCapabilities": [],
        })
        assert response.status_code == 422

    def test_no_db_write(self):
        response = client.post("/execution/delegation-plan", json={
            "mealPlanId": str(uuid4()),
            "steps": [],
            "machineCapabilities": [],
        })
        assert response.status_code == 200
        assert response.json() == []
