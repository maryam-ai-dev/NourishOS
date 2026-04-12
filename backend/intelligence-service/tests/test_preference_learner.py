"""Tests for preference learner (Sprint 23A.4)."""

from app.services.recommendation.preference_learner import (
    compute_preference_modifiers, apply_to_preference_fit,
    CONFIRMED_BOOST, SWAPPED_OUT_PENALTY,
)


class TestPreferenceLearner:
    def test_confirmed_feedback_boosts(self):
        events = [
            {"mealOptionId": "meal-1", "feedbackType": "CONFIRMED"},
            {"mealOptionId": "meal-1", "feedbackType": "CONFIRMED"},
            {"mealOptionId": "meal-1", "feedbackType": "CONFIRMED"},
        ]
        mods = compute_preference_modifiers(events)
        assert mods["meal-1"] == 3 * CONFIRMED_BOOST

    def test_swapped_out_penalises(self):
        events = [
            {"mealOptionId": "meal-1", "feedbackType": "SWAPPED_OUT"},
            {"mealOptionId": "meal-1", "feedbackType": "SWAPPED_OUT"},
        ]
        mods = compute_preference_modifiers(events)
        assert mods["meal-1"] == 2 * SWAPPED_OUT_PENALTY
        assert mods["meal-1"] < 0

    def test_confirmed_higher_than_zero_feedback(self):
        """Meal with 3 CONFIRMED scores higher than meal with no feedback."""
        events = [
            {"mealOptionId": "confirmed-meal", "feedbackType": "CONFIRMED"},
            {"mealOptionId": "confirmed-meal", "feedbackType": "CONFIRMED"},
            {"mealOptionId": "confirmed-meal", "feedbackType": "CONFIRMED"},
        ]
        mods = compute_preference_modifiers(events)
        confirmed_score = apply_to_preference_fit(0.7, mods.get("confirmed-meal", 0))
        no_feedback_score = apply_to_preference_fit(0.7, mods.get("unknown-meal", 0))
        assert confirmed_score > no_feedback_score

    def test_swapped_lower_than_zero_feedback(self):
        events = [
            {"mealOptionId": "bad-meal", "feedbackType": "SWAPPED_OUT"},
            {"mealOptionId": "bad-meal", "feedbackType": "SWAPPED_OUT"},
        ]
        mods = compute_preference_modifiers(events)
        bad_score = apply_to_preference_fit(0.7, mods.get("bad-meal", 0))
        no_feedback_score = apply_to_preference_fit(0.7, 0)
        assert bad_score < no_feedback_score

    def test_score_clamped_to_0_1(self):
        assert apply_to_preference_fit(0.9, 0.5) == 1.0
        assert apply_to_preference_fit(0.1, -0.5) == 0.0

    def test_modifier_clamped(self):
        events = [{"mealOptionId": "meal-1", "feedbackType": "CONFIRMED"} for _ in range(20)]
        mods = compute_preference_modifiers(events)
        assert mods["meal-1"] <= 0.5

    def test_empty_feedback_returns_empty(self):
        mods = compute_preference_modifiers([])
        assert mods == {}
