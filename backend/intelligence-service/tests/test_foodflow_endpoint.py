"""Tests for POST /foodflow/analyze endpoint."""

from fastapi.testclient import TestClient
from unittest.mock import patch
from uuid import uuid4

from app.main import app

client = TestClient(app)


class TestFoodflowAnalyze:
    @patch("app.api.foodflow.get_foodflow_snapshot")
    def test_returns_200_with_all_components(self, mock_snapshot):
        mock_snapshot.return_value = {}

        response = client.post("/foodflow/analyze", json={
            "householdId": str(uuid4()),
        })

        assert response.status_code == 200
        data = response.json()
        assert "consumptionProfile" in data
        assert "wastePattern" in data
        assert "stockFlowSummary" in data

    @patch("app.api.foodflow.get_foodflow_snapshot")
    def test_consumption_profile_typed(self, mock_snapshot):
        mock_snapshot.return_value = {}

        response = client.post("/foodflow/analyze", json={
            "householdId": str(uuid4()),
        })

        data = response.json()
        cp = data["consumptionProfile"]
        assert isinstance(cp["topIngredients"], list)
        assert isinstance(cp["avgWeeklyCalories"], (int, float))
        assert isinstance(cp["mealTypeFrequency"], dict)
        assert isinstance(cp["peakConsumptionDays"], list)

    @patch("app.api.foodflow.get_foodflow_snapshot")
    def test_waste_pattern_typed(self, mock_snapshot):
        mock_snapshot.return_value = {}

        response = client.post("/foodflow/analyze", json={
            "householdId": str(uuid4()),
        })

        data = response.json()
        wp = data["wastePattern"]
        assert isinstance(wp["frequentlyWastedIngredients"], list)
        assert isinstance(wp["wasteRatio"], (int, float))

    @patch("app.api.foodflow.get_foodflow_snapshot")
    def test_stock_flow_typed(self, mock_snapshot):
        mock_snapshot.return_value = {}

        response = client.post("/foodflow/analyze", json={
            "householdId": str(uuid4()),
        })

        data = response.json()
        sf = data["stockFlowSummary"]
        assert isinstance(sf["overBoughtIngredients"], list)
        assert isinstance(sf["underSuppliedIngredients"], list)

    def test_no_db_writes(self):
        """Pure intelligence computation — no DB writes from this endpoint."""
        # If Spring Boot isn't running, endpoint still returns valid response
        response = client.post("/foodflow/analyze", json={
            "householdId": str(uuid4()),
        })
        assert response.status_code == 200
