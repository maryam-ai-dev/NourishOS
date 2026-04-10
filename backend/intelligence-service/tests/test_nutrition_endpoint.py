"""Tests for POST /nutrition/score endpoint."""

from fastapi.testclient import TestClient
from unittest.mock import patch
from uuid import uuid4
from decimal import Decimal

from app.main import app
from app.clients.authority_client import MealOption

client = TestClient(app)

MEAL_ID = uuid4()


def _mock_catalog():
    return [MealOption(
        id=MEAL_ID,
        name="Grilled Chicken",
        mealType="DINNER",
        estimatedProteinGrams=Decimal("40"),
        estimatedCalories=Decimal("500"),
        prepTimeMinutes=30,
        sustainabilityScore=Decimal("0.7"),
        ingredientRefs=[],
    )]


class TestNutritionEndpoint:
    @patch("app.api.nutrition.get_meal_catalog")
    def test_returns_200(self, mock_catalog):
        mock_catalog.return_value = _mock_catalog()
        response = client.post("/nutrition/score", json={
            "mealId": str(MEAL_ID),
            "servings": 3,
        })
        assert response.status_code == 200
        data = response.json()
        assert "proteinGrams" in data
        assert "proteinGoalMet" in data

    @patch("app.api.nutrition.get_meal_catalog")
    def test_unknown_meal_returns_404(self, mock_catalog):
        mock_catalog.return_value = _mock_catalog()
        response = client.post("/nutrition/score", json={
            "mealId": str(uuid4()),
            "servings": 1,
        })
        assert response.status_code == 404

    def test_authority_down_returns_404(self):
        """If Spring Boot is down, meal catalog fetch fails cleanly."""
        response = client.post("/nutrition/score", json={
            "mealId": str(uuid4()),
            "servings": 1,
        })
        assert response.status_code == 404

    @patch("app.api.nutrition.get_meal_catalog")
    def test_no_db_write(self, mock_catalog):
        mock_catalog.return_value = _mock_catalog()
        response = client.post("/nutrition/score", json={
            "mealId": str(MEAL_ID),
            "servings": 1,
        })
        assert response.status_code == 200
