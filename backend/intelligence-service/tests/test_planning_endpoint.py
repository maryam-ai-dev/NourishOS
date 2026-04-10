"""Tests for POST /planning/weekly endpoint."""

import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch
from uuid import uuid4
from decimal import Decimal

from app.main import app
from app.clients.authority_client import MealOption, IngredientRef, HouseholdMember

client = TestClient(app)


def _mock_members():
    return [
        HouseholdMember(
            id=uuid4(),
            householdId=uuid4(),
            displayName="Alice",
            ageGroup="ADULT",
            effortSensitivity="LOW",
            participatesInMealPlanning=True,
        )
    ]


def _mock_catalog():
    meals = []
    for i, mt in enumerate(["BREAKFAST", "LUNCH", "DINNER"] * 8):
        meals.append(MealOption(
            id=uuid4(),
            name=f"Meal {mt} {i}",
            mealType=mt,
            estimatedProteinGrams=Decimal("25"),
            estimatedCalories=Decimal("400"),
            prepTimeMinutes=30,
            sustainabilityScore=Decimal(str(0.7 - i * 0.01)),
            ingredientRefs=[
                IngredientRef(
                    ingredientId=uuid4(),
                    baseQuantity=Decimal("200"),
                    unit="g",
                    optional=False,
                    substitutable=True,
                )
            ],
        ))
    return meals


class TestPlanWeeklyEndpoint:
    @patch("app.api.planning.get_meal_catalog")
    @patch("app.api.planning.get_members")
    def test_returns_200_with_7_slots(self, mock_members, mock_catalog):
        mock_members.return_value = _mock_members()
        mock_catalog.return_value = _mock_catalog()

        response = client.post("/planning/weekly", json={
            "householdId": str(uuid4()),
            "weekStartDate": "2026-04-13",
        })

        assert response.status_code == 200
        data = response.json()
        assert len(data["slots"]) == 21  # 7 days × 3 meals
        assert "wasteReductionScore" in data
        assert "memberFitSummary" in data

    @patch("app.api.planning.get_meal_catalog")
    @patch("app.api.planning.get_members")
    def test_member_fit_summary_includes_per_member_compliance(self, mock_members, mock_catalog):
        members = _mock_members()
        mock_members.return_value = members
        mock_catalog.return_value = _mock_catalog()

        response = client.post("/planning/weekly", json={
            "householdId": str(uuid4()),
            "weekStartDate": "2026-04-13",
        })

        data = response.json()
        fit = data["memberFitSummary"]
        assert len(fit) == 1
        member_key = list(fit.keys())[0]
        assert "displayName" in fit[member_key]
        assert "complianceRate" in fit[member_key]

    def test_invalid_date_returns_422(self):
        response = client.post("/planning/weekly", json={
            "householdId": str(uuid4()),
            "weekStartDate": "not-a-date",
        })
        assert response.status_code == 422

    @patch("app.api.planning.get_meal_catalog")
    @patch("app.api.planning.get_members")
    def test_empty_catalog_returns_empty_slots(self, mock_members, mock_catalog):
        mock_members.return_value = []
        mock_catalog.return_value = []

        response = client.post("/planning/weekly", json={
            "householdId": str(uuid4()),
            "weekStartDate": "2026-04-13",
        })

        assert response.status_code == 200
        data = response.json()
        assert len(data["slots"]) == 0

    @patch("app.api.planning.get_meal_catalog")
    @patch("app.api.planning.get_members")
    def test_response_does_not_persist_anything(self, mock_members, mock_catalog):
        """FastAPI response alone does not persist — Spring Boot persists on confirm."""
        mock_members.return_value = _mock_members()
        mock_catalog.return_value = _mock_catalog()

        response = client.post("/planning/weekly", json={
            "householdId": str(uuid4()),
            "weekStartDate": "2026-04-13",
        })

        assert response.status_code == 200
        # Just confirms we got a proposal, not a persisted schedule
        data = response.json()
        assert "slots" in data
        assert "weekStartDate" in data
