"""Tests for sustainability scorer."""

from uuid import uuid4
from app.services.sustainability.sustainability_scorer import (
    IngredientContext,
    MealContext,
    score_sustainability,
)


def _ing(perishability="SHELF_STABLE", opened=False, recurring_waste=False):
    return IngredientContext(
        ingredient_id=uuid4(),
        perishability_class=perishability,
        is_opened=opened,
        is_recurring_waste=recurring_waste,
    )


class TestWasteRiskAndReuse:
    def test_returns_sub_scores_without_error(self):
        meal = MealContext(meal_id=uuid4(), ingredients=[_ing()])
        result = score_sustainability(meal)
        assert result is not None
        assert 0.0 <= result.waste_risk_score <= 1.0
        assert 0.0 <= result.reuse_score <= 1.0

    def test_recurring_waste_lowers_reuse_below_05(self):
        meal = MealContext(
            meal_id=uuid4(),
            ingredients=[_ing(recurring_waste=True)],
        )
        result = score_sustainability(meal)
        assert result.reuse_score < 0.5

    def test_opened_ingredient_boosts_reuse(self):
        meal_opened = MealContext(
            meal_id=uuid4(),
            ingredients=[_ing(opened=True)],
        )
        meal_closed = MealContext(
            meal_id=uuid4(),
            ingredients=[_ing(opened=False)],
        )
        opened_score = score_sustainability(meal_opened).reuse_score
        closed_score = score_sustainability(meal_closed).reuse_score
        assert opened_score > closed_score

    def test_perishable_increases_waste_risk(self):
        meal_perishable = MealContext(
            meal_id=uuid4(),
            ingredients=[_ing("HIGHLY_PERISHABLE")],
        )
        meal_stable = MealContext(
            meal_id=uuid4(),
            ingredients=[_ing("SHELF_STABLE")],
        )
        perishable_score = score_sustainability(meal_perishable).waste_risk_score
        stable_score = score_sustainability(meal_stable).waste_risk_score
        assert perishable_score < stable_score

    def test_pure_computation(self):
        meal = MealContext(meal_id=uuid4(), ingredients=[_ing()])
        score_sustainability(meal)


class TestEnergyAndEnvironmental:
    def test_red_meat_lower_than_plant_based(self):
        meat = MealContext(meal_id=uuid4(), ingredients=[], tags={"red-meat"})
        plant = MealContext(meal_id=uuid4(), ingredients=[], tags={"plant-based"})
        assert score_sustainability(meat).environmental_score < score_sustainability(plant).environmental_score

    def test_all_four_sub_scores_present(self):
        meal = MealContext(meal_id=uuid4(), ingredients=[_ing()], tags=set(), high_heat_steps=1, total_steps=3)
        result = score_sustainability(meal)
        assert result.waste_risk_score is not None
        assert result.reuse_score is not None
        assert result.energy_score is not None
        assert result.environmental_score is not None

    def test_overall_is_weighted_average(self):
        meal = MealContext(meal_id=uuid4(), ingredients=[_ing()], tags=set(), high_heat_steps=0, total_steps=1)
        result = score_sustainability(meal)
        expected = (
            0.30 * result.waste_risk_score
            + 0.25 * result.reuse_score
            + 0.20 * result.energy_score
            + 0.25 * result.environmental_score
        )
        assert abs(result.overall_sustainability_score - round(expected, 4)) < 0.001

    def test_fewer_heat_steps_higher_energy_score(self):
        low_heat = MealContext(meal_id=uuid4(), ingredients=[], high_heat_steps=0, total_steps=5)
        high_heat = MealContext(meal_id=uuid4(), ingredients=[], high_heat_steps=4, total_steps=5)
        assert score_sustainability(low_heat).energy_score > score_sustainability(high_heat).energy_score

    def test_all_scores_in_range(self):
        meal = MealContext(meal_id=uuid4(), ingredients=[_ing()], tags={"red-meat"}, high_heat_steps=3, total_steps=4)
        result = score_sustainability(meal)
        for s in [result.waste_risk_score, result.reuse_score, result.energy_score, result.environmental_score, result.overall_sustainability_score]:
            assert 0.0 <= s <= 1.0
