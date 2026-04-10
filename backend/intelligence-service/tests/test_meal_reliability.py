"""Tests for meal reliability analyzer."""

from uuid import uuid4

from app.services.foodflow.meal_reliability_analyzer import (
    MealOutcomeEvent,
    analyze_meal_reliability,
    get_reliability_insights,
)


def _outcome(meal_id, outcome_str):
    return MealOutcomeEvent(id=uuid4(), meal_option_id=meal_id, outcome=outcome_str)


class TestMealReliability:
    def test_low_reliability_flag(self):
        """1 COMPLETED + 3 ABANDONED → completionRate 0.25 → LOW_RELIABILITY."""
        meal = uuid4()
        outcomes = [
            _outcome(meal, "COMPLETED"),
            _outcome(meal, "ABANDONED"),
            _outcome(meal, "ABANDONED"),
            _outcome(meal, "ABANDONED"),
        ]
        result = analyze_meal_reliability(outcomes)
        assert result[meal].completion_rate == 0.25
        assert result[meal].is_low_reliability is True

    def test_high_reliability(self):
        meal = uuid4()
        outcomes = [
            _outcome(meal, "COMPLETED"),
            _outcome(meal, "COMPLETED"),
            _outcome(meal, "COMPLETED"),
            _outcome(meal, "ABANDONED"),
        ]
        result = analyze_meal_reliability(outcomes)
        assert result[meal].completion_rate == 0.75
        assert result[meal].is_low_reliability is False

    def test_abandonment_rate(self):
        meal = uuid4()
        outcomes = [
            _outcome(meal, "COMPLETED"),
            _outcome(meal, "ABANDONED"),
            _outcome(meal, "ABANDONED"),
        ]
        result = analyze_meal_reliability(outcomes)
        assert abs(result[meal].abandonment_rate - 2/3) < 0.01

    def test_reliability_score_formula(self):
        """reliabilityScore = completionRate * 0.7 + (1 - substitutionRate) * 0.3"""
        meal = uuid4()
        outcomes = [
            _outcome(meal, "COMPLETED"),
            _outcome(meal, "COMPLETED"),
            _outcome(meal, "COMPLETED"),
            _outcome(meal, "SUBSTITUTED"),
        ]
        result = analyze_meal_reliability(outcomes)
        # completionRate = 0.75, substitutionRate = 0.25
        expected = 0.75 * 0.7 + (1 - 0.25) * 0.3  # 0.525 + 0.225 = 0.75
        assert abs(result[meal].reliability_score - expected) < 0.001

    def test_multiple_meals(self):
        meal_a = uuid4()
        meal_b = uuid4()
        outcomes = [
            _outcome(meal_a, "COMPLETED"),
            _outcome(meal_a, "COMPLETED"),
            _outcome(meal_b, "ABANDONED"),
            _outcome(meal_b, "ABANDONED"),
        ]
        result = analyze_meal_reliability(outcomes)
        assert result[meal_a].completion_rate == 1.0
        assert result[meal_b].completion_rate == 0.0
        assert result[meal_b].is_low_reliability is True

    def test_empty_outcomes(self):
        result = analyze_meal_reliability([])
        assert len(result) == 0

    def test_get_reliability_insights_alias(self):
        meal = uuid4()
        outcomes = [_outcome(meal, "COMPLETED")]
        result = get_reliability_insights(outcomes)
        assert meal in result
