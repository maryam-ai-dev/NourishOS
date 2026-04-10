"""Tests for food flow supporting endpoints."""

from fastapi.testclient import TestClient
from uuid import uuid4

from app.main import app

client = TestClient(app)


class TestFoodflowInsights:
    def test_returns_200(self):
        response = client.post("/foodflow/insights", json={
            "householdId": str(uuid4()),
            "snapshotWeek": "2026-04-13",
        })
        assert response.status_code == 200
        data = response.json()
        assert "insights" in data
        assert len(data["insights"]) >= 1


class TestReplenishmentScore:
    def test_returns_200(self):
        response = client.post("/foodflow/replenishment-score", json={
            "householdId": str(uuid4()),
            "ingredientId": str(uuid4()),
            "proposedQuantity": 500.0,
        })
        assert response.status_code == 200
        data = response.json()
        assert "score" in data
        assert "ingredientId" in data

    def test_non_recurring_gets_score_1(self):
        """Non-RECURRING_WASTE ingredient gets higher score than recurring."""
        response = client.post("/foodflow/replenishment-score", json={
            "householdId": str(uuid4()),
            "ingredientId": str(uuid4()),
            "proposedQuantity": 500.0,
        })
        data = response.json()
        assert data["score"] == 1.0
        assert data["isRecurringWaste"] is False

    def test_score_per_meal_option(self):
        response = client.post("/foodflow/replenishment-score", json={
            "householdId": str(uuid4()),
            "ingredientId": str(uuid4()),
            "proposedQuantity": 1000.0,
        })
        data = response.json()
        assert data["adjustedQuantity"] == 1000.0


class TestMealReliability:
    def test_returns_200(self):
        meal_ids = [str(uuid4()), str(uuid4())]
        response = client.post("/foodflow/meal-reliability", json={
            "householdId": str(uuid4()),
            "mealOptionIds": meal_ids,
        })
        assert response.status_code == 200
        data = response.json()
        assert "reliability" in data
        assert len(data["reliability"]) == 2

    def test_score_per_meal_option_id(self):
        meal_id = str(uuid4())
        response = client.post("/foodflow/meal-reliability", json={
            "householdId": str(uuid4()),
            "mealOptionIds": [meal_id],
        })
        data = response.json()
        assert data["reliability"][0]["mealOptionId"] == meal_id

    def test_unknown_meal_returns_default(self):
        response = client.post("/foodflow/meal-reliability", json={
            "householdId": str(uuid4()),
            "mealOptionIds": [str(uuid4())],
        })
        data = response.json()
        item = data["reliability"][0]
        assert item["completionRate"] == 0.0
        assert item["isLowReliability"] is True
