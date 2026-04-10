"""Tests for explanation generator — all decision types."""

import pytest

from app.services.explanations.explanation_generator import (
    DecisionType,
    generate_explanation,
)


class TestMealRanked:
    def test_returns_non_empty(self):
        result = generate_explanation(DecisionType.MEAL_RANKED, {
            "meal_name": "Pasta Primavera",
            "score_breakdown": {
                "preference_fit": 0.7,
                "protein_goal": 0.9,
                "sustainability": 0.6,
                "availability": 0.8,
                "reliability": 0.5,
            },
        })
        assert result.explanation != ""

    def test_top_driver_named(self):
        result = generate_explanation(DecisionType.MEAL_RANKED, {
            "meal_name": "Chicken Salad",
            "score_breakdown": {
                "preference_fit": 0.3,
                "protein_goal": 0.95,
                "sustainability": 0.6,
                "availability": 0.8,
                "reliability": 0.5,
            },
        })
        assert "protein_goal" in result.explanation

    def test_all_five_dimensions_in_factors(self):
        result = generate_explanation(DecisionType.MEAL_RANKED, {
            "score_breakdown": {
                "preference_fit": 0.7,
                "protein_goal": 0.9,
                "sustainability": 0.6,
                "availability": 0.8,
                "reliability": 0.5,
            },
        })
        factor_names = {f.name for f in result.supporting_factors}
        assert "preference_fit" in factor_names
        assert "protein_goal" in factor_names
        assert "sustainability" in factor_names
        assert "availability" in factor_names
        assert "reliability" in factor_names


class TestReplenishmentAndWaste:
    def test_reorder_suggested(self):
        result = generate_explanation(DecisionType.REORDER_SUGGESTED, {
            "ingredient_name": "Rice",
            "quantity": 500,
            "urgency": "CRITICAL",
        })
        assert result.explanation != ""
        assert "Rice" in result.explanation

    def test_reorder_adjusted_names_ingredient_and_reduction(self):
        result = generate_explanation(DecisionType.REORDER_ADJUSTED_FOR_WASTE, {
            "ingredient_name": "Milk",
            "original_quantity": 1000,
            "adjusted_quantity": 800,
        })
        assert "Milk" in result.explanation
        assert "200" in result.explanation  # reduction amount

    def test_waste_pattern_names_reason(self):
        result = generate_explanation(DecisionType.WASTE_PATTERN_DETECTED, {
            "ingredient_name": "Spinach",
            "top_reason": "EXPIRED",
            "waste_count": 3,
        })
        assert "EXPIRED" in result.explanation
        assert "Spinach" in result.explanation

    def test_all_three_non_empty(self):
        for dt in [DecisionType.REORDER_SUGGESTED, DecisionType.REORDER_ADJUSTED_FOR_WASTE, DecisionType.WASTE_PATTERN_DETECTED]:
            result = generate_explanation(dt, {"ingredient_name": "Test"})
            assert result.explanation != ""


class TestExecutionAndReliability:
    def test_intervention_names_step_and_type(self):
        result = generate_explanation(DecisionType.INTERVENTION_REQUIRED, {
            "step_name": "Load tray",
            "intervention_type": "LOAD_TRAY",
            "step_order": 3,
        })
        assert "Load tray" in result.explanation
        assert "LOAD_TRAY" in result.explanation

    def test_low_reliability_states_completion_rate(self):
        result = generate_explanation(DecisionType.MEAL_RELIABILITY_LOW, {
            "meal_name": "Soufflé",
            "completion_rate": 0.25,
        })
        assert "25%" in result.explanation

    def test_substitution_proposed(self):
        result = generate_explanation(DecisionType.SUBSTITUTION_PROPOSED, {
            "original_name": "Butter",
            "substitute_name": "Olive Oil",
            "reason": "recurring waste",
        })
        assert "Butter" in result.explanation
        assert "Olive Oil" in result.explanation

    def test_all_three_non_empty(self):
        for dt in [DecisionType.INTERVENTION_REQUIRED, DecisionType.MEAL_RELIABILITY_LOW, DecisionType.SUBSTITUTION_PROPOSED]:
            result = generate_explanation(dt, {})
            assert result.explanation != ""

    def test_unknown_type_raises(self):
        with pytest.raises(ValueError):
            generate_explanation("INVALID_TYPE", {})
