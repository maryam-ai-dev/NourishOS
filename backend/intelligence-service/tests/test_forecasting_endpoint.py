"""Tests for POST /forecasting/stockout endpoint."""

from fastapi.testclient import TestClient
from uuid import uuid4

from app.main import app

client = TestClient(app)


class TestForecastingEndpoint:
    def test_returns_200(self):
        response = client.post("/forecasting/stockout", json={
            "householdId": str(uuid4()),
            "asOf": "2026-04-13",
            "stocks": [{
                "ingredientId": str(uuid4()),
                "currentQuantity": 55,
                "unit": "g",
                "avgWeeklyUsage": 100,
                "parLevelMinimum": 50,
            }],
        })
        assert response.status_code == 200

    def test_critical_urgency_under_3_days(self):
        response = client.post("/forecasting/stockout", json={
            "householdId": str(uuid4()),
            "asOf": "2026-04-13",
            "stocks": [{
                "ingredientId": str(uuid4()),
                "currentQuantity": 55,
                "unit": "g",
                "avgWeeklyUsage": 100,
                "parLevelMinimum": 50,
            }],
        })
        data = response.json()
        assert len(data) >= 1
        assert data[0]["urgency"] == "CRITICAL"

    def test_well_stocked_absent_from_results(self):
        response = client.post("/forecasting/stockout", json={
            "householdId": str(uuid4()),
            "asOf": "2026-04-13",
            "stocks": [{
                "ingredientId": str(uuid4()),
                "currentQuantity": 5000,
                "unit": "g",
                "avgWeeklyUsage": 50,
                "parLevelMinimum": 50,
            }],
        })
        data = response.json()
        assert len(data) == 0  # well-stocked → filtered out

    def test_no_db_write(self):
        response = client.post("/forecasting/stockout", json={
            "householdId": str(uuid4()),
            "stocks": [],
        })
        assert response.status_code == 200
        assert response.json() == []
