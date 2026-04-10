"""Tests for POST /explanations/generate endpoint."""

from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


class TestExplanationsEndpoint:
    def test_returns_200_with_non_empty_explanation(self):
        response = client.post("/explanations/generate", json={
            "decisionType": "MEAL_RANKED",
            "context": {
                "meal_name": "Test Meal",
                "score_breakdown": {
                    "preference_fit": 0.7,
                    "protein_goal": 0.9,
                    "sustainability": 0.6,
                    "availability": 0.8,
                    "reliability": 0.5,
                },
            },
        })
        assert response.status_code == 200
        data = response.json()
        assert data["explanation"] != ""

    def test_unknown_decision_type_returns_422(self):
        response = client.post("/explanations/generate", json={
            "decisionType": "INVALID_TYPE",
            "context": {},
        })
        assert response.status_code == 422

    def test_all_valid_types_return_non_empty(self):
        types = [
            "MEAL_RANKED", "REORDER_SUGGESTED", "REORDER_ADJUSTED_FOR_WASTE",
            "WASTE_PATTERN_DETECTED", "INTERVENTION_REQUIRED",
            "MEAL_RELIABILITY_LOW", "SUBSTITUTION_PROPOSED",
        ]
        for dt in types:
            response = client.post("/explanations/generate", json={
                "decisionType": dt,
                "context": {},
            })
            assert response.status_code == 200
            data = response.json()
            assert data["explanation"] != "", f"Empty explanation for {dt}"
            assert isinstance(data["supportingFactors"], list)

    def test_supporting_factors_present(self):
        response = client.post("/explanations/generate", json={
            "decisionType": "MEAL_RANKED",
            "context": {
                "score_breakdown": {
                    "preference_fit": 0.7,
                    "protein_goal": 0.9,
                    "sustainability": 0.6,
                    "availability": 0.8,
                    "reliability": 0.5,
                },
            },
        })
        data = response.json()
        assert len(data["supportingFactors"]) == 5

    def test_no_db_write(self):
        response = client.post("/explanations/generate", json={
            "decisionType": "REORDER_SUGGESTED",
            "context": {"ingredient_name": "Rice", "quantity": 500, "urgency": "WARNING"},
        })
        assert response.status_code == 200
