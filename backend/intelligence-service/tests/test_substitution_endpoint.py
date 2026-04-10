"""Tests for POST /substitution/suggest endpoint."""

from fastapi.testclient import TestClient
from uuid import uuid4

from app.main import app

client = TestClient(app)


class TestSubstitutionEndpoint:
    def test_returns_200(self):
        response = client.post("/substitution/suggest", json={
            "householdId": str(uuid4()),
            "missingIngredients": [{
                "ingredientId": str(uuid4()),
                "name": "Chicken",
                "proteinPer100g": 25.0,
                "category": "dry",
            }],
            "availableIngredients": [{
                "ingredientId": str(uuid4()),
                "name": "Turkey",
                "proteinPer100g": 24.0,
                "category": "dry",
                "isAvailable": True,
            }],
        })
        assert response.status_code == 200
        data = response.json()
        assert len(data) >= 1
        assert data[0]["reason"] != ""

    def test_empty_missing_returns_empty_list(self):
        response = client.post("/substitution/suggest", json={
            "householdId": str(uuid4()),
            "missingIngredients": [],
            "availableIngredients": [],
        })
        assert response.status_code == 200
        assert response.json() == []

    def test_all_substitutes_have_reason(self):
        response = client.post("/substitution/suggest", json={
            "householdId": str(uuid4()),
            "missingIngredients": [{
                "ingredientId": str(uuid4()),
                "name": "Rice",
                "proteinPer100g": 7.0,
                "category": "dry",
            }],
            "availableIngredients": [{
                "ingredientId": str(uuid4()),
                "name": "Quinoa",
                "proteinPer100g": 14.0,
                "category": "dry",
                "isAvailable": True,
            }],
        })
        data = response.json()
        for sub in data:
            assert "reason" in sub
            assert sub["reason"] != ""

    def test_no_db_write(self):
        response = client.post("/substitution/suggest", json={
            "householdId": str(uuid4()),
            "missingIngredients": [],
            "availableIngredients": [],
        })
        assert response.status_code == 200
