"""Tests for enhanced POST /foodflow/replenishment-score endpoint."""

from fastapi.testclient import TestClient
from uuid import uuid4

from app.main import app

client = TestClient(app)


class TestReplenishmentScoreEndpoint:
    def test_returns_200(self):
        response = client.post("/foodflow/replenishment-score", json={
            "householdId": str(uuid4()),
            "ingredientId": str(uuid4()),
            "proposedQuantity": 500.0,
        })
        assert response.status_code == 200
        data = response.json()
        assert "score" in data
        assert "reasoning" in data
        assert "adjustedQuantity" in data

    def test_recurring_waste_scores_lower(self):
        response = client.post("/foodflow/replenishment-score", json={
            "householdId": str(uuid4()),
            "ingredientId": str(uuid4()),
            "proposedQuantity": 500.0,
            "isRecurringWaste": True,
            "wasteRatio": 0.3,
        })
        data = response.json()
        assert data["score"] < 1.0
        assert data["isRecurringWaste"] is True
        assert data["adjustedQuantity"] < 500.0

    def test_non_waste_scores_1(self):
        response = client.post("/foodflow/replenishment-score", json={
            "householdId": str(uuid4()),
            "ingredientId": str(uuid4()),
            "proposedQuantity": 500.0,
        })
        data = response.json()
        assert data["score"] == 1.0
        assert data["isRecurringWaste"] is False

    def test_reasoning_non_empty(self):
        response = client.post("/foodflow/replenishment-score", json={
            "householdId": str(uuid4()),
            "ingredientId": str(uuid4()),
            "proposedQuantity": 500.0,
            "isRecurringWaste": True,
            "wasteRatio": 0.2,
        })
        data = response.json()
        assert data["reasoning"] != ""
        assert "waste" in data["reasoning"].lower()

    def test_reasoning_names_driver(self):
        """reasoning is non-empty string naming the driver."""
        response = client.post("/foodflow/replenishment-score", json={
            "householdId": str(uuid4()),
            "ingredientId": str(uuid4()),
            "proposedQuantity": 1000.0,
        })
        data = response.json()
        assert len(data["reasoning"]) > 0

    def test_no_db_write(self):
        response = client.post("/foodflow/replenishment-score", json={
            "householdId": str(uuid4()),
            "ingredientId": str(uuid4()),
            "proposedQuantity": 100.0,
        })
        assert response.status_code == 200
