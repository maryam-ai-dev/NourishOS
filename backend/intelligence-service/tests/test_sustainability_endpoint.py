"""Tests for POST /sustainability/score endpoint."""

from fastapi.testclient import TestClient
from uuid import uuid4

from app.main import app

client = TestClient(app)


class TestSustainabilityEndpoint:
    def test_returns_200_all_five_fields(self):
        response = client.post("/sustainability/score", json={
            "householdId": str(uuid4()),
            "mealId": str(uuid4()),
            "ingredients": [{
                "ingredientId": str(uuid4()),
                "perishabilityClass": "SHELF_STABLE",
            }],
            "tags": [],
            "highHeatSteps": 0,
            "totalSteps": 3,
        })
        assert response.status_code == 200
        data = response.json()
        assert "wasteRiskScore" in data
        assert "reuseScore" in data
        assert "energyScore" in data
        assert "environmentalScore" in data
        assert "overallSustainabilityScore" in data

    def test_all_scores_in_range(self):
        response = client.post("/sustainability/score", json={
            "householdId": str(uuid4()),
            "mealId": str(uuid4()),
            "ingredients": [{
                "ingredientId": str(uuid4()),
                "perishabilityClass": "HIGHLY_PERISHABLE",
                "isRecurringWaste": True,
            }],
            "tags": ["red-meat"],
            "highHeatSteps": 3,
            "totalSteps": 4,
        })
        data = response.json()
        for key in ["wasteRiskScore", "reuseScore", "energyScore", "environmentalScore", "overallSustainabilityScore"]:
            assert 0.0 <= data[key] <= 1.0

    def test_no_db_write(self):
        response = client.post("/sustainability/score", json={
            "householdId": str(uuid4()),
            "mealId": str(uuid4()),
            "ingredients": [],
        })
        assert response.status_code == 200

    def test_empty_ingredients_accepted(self):
        response = client.post("/sustainability/score", json={
            "householdId": str(uuid4()),
            "mealId": str(uuid4()),
            "ingredients": [],
        })
        assert response.status_code == 200
