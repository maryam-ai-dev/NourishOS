"""Tests for weekly planner slot assignment."""

import pytest
from datetime import date
from decimal import Decimal
from uuid import uuid4

from app.services.planning.weekly_planner import (
    MealCandidate,
    MemberProfile,
    IngredientInfo,
    plan_weekly_slots,
    SlotAssignment,
)


def _make_candidate(name, meal_type, protein=30, perishable=False, score=0.7, tags=None, allergens=None):
    ing_info = []
    if perishable:
        ing_info.append(IngredientInfo(
            ingredient_id=uuid4(),
            base_quantity=Decimal("200"),
            unit="g",
            perishability_class="HIGHLY_PERISHABLE",
        ))
    else:
        ing_info.append(IngredientInfo(
            ingredient_id=uuid4(),
            base_quantity=Decimal("200"),
            unit="g",
            perishability_class="SHELF_STABLE",
        ))
    return MealCandidate(
        meal_id=uuid4(),
        meal_name=name,
        meal_type=meal_type,
        estimated_protein_grams=Decimal(str(protein)),
        ingredient_infos=ing_info,
        composite_score=score,
        tags=set(tags or []),
        allergens=set(allergens or []),
    )


class TestPlanWeeklySlots:
    def test_returns_7_days_of_slots(self):
        candidates = []
        for i in range(25):
            for mt in ["BREAKFAST", "LUNCH", "DINNER"]:
                candidates.append(_make_candidate(f"Meal {mt} {i}", mt, score=0.8 - i * 0.01))

        result = plan_weekly_slots(
            household_id=uuid4(),
            week_start_date=date(2026, 4, 13),
            candidates=candidates,
            members=[],
        )

        assert len(result.slots) == 21  # 7 days × 3 meals
        days = {s.day for s in result.slots}
        assert days == {1, 2, 3, 4, 5, 6, 7}

    def test_perishable_meals_in_days_1_2(self):
        perishable_breakfast = _make_candidate("Fresh Salmon", "BREAKFAST", perishable=True, score=0.9)
        perishable_lunch = _make_candidate("Fresh Shrimp", "LUNCH", perishable=True, score=0.85)
        shelf_stable = []
        for i in range(25):
            for mt in ["BREAKFAST", "LUNCH", "DINNER"]:
                shelf_stable.append(_make_candidate(f"Stable {mt} {i}", mt, score=0.7 - i * 0.01))

        candidates = [perishable_breakfast, perishable_lunch] + shelf_stable

        result = plan_weekly_slots(
            household_id=uuid4(),
            week_start_date=date(2026, 4, 13),
            candidates=candidates,
            members=[],
        )

        # Find where our perishable meals ended up
        salmon_slot = next((s for s in result.slots if s.meal_name == "Fresh Salmon"), None)
        shrimp_slot = next((s for s in result.slots if s.meal_name == "Fresh Shrimp"), None)

        assert salmon_slot is not None
        assert salmon_slot.day <= 2
        assert salmon_slot.is_perishable_priority is True

        assert shrimp_slot is not None
        assert shrimp_slot.day <= 2
        assert shrimp_slot.is_perishable_priority is True

    def test_vegetarian_member_gets_no_meat_meals(self):
        meat_allergen = uuid4()
        meat_meal = _make_candidate("Grilled Chicken", "LUNCH", tags={"meat"}, score=0.9)
        veggie_meals = []
        for i in range(25):
            for mt in ["BREAKFAST", "LUNCH", "DINNER"]:
                veggie_meals.append(_make_candidate(f"Veggie {mt} {i}", mt, score=0.8 - i * 0.01))

        candidates = [meat_meal] + veggie_meals
        vegetarian = MemberProfile(
            member_id=uuid4(),
            display_name="Alice",
            dietary_restrictions={"vegetarian"},
        )

        result = plan_weekly_slots(
            household_id=uuid4(),
            week_start_date=date(2026, 4, 13),
            candidates=candidates,
            members=[vegetarian],
        )

        # No slot should have the meat meal
        for slot in result.slots:
            assert slot.meal_name != "Grilled Chicken", "Vegetarian member should not get meat meal"

    def test_allergen_exclusion(self):
        allergen_id = uuid4()
        allergenic = MealCandidate(
            meal_id=uuid4(),
            meal_name="Peanut Curry",
            meal_type="DINNER",
            estimated_protein_grams=Decimal("25"),
            ingredient_infos=[IngredientInfo(uuid4(), Decimal("100"), "g")],
            composite_score=0.95,
            allergens={allergen_id},
        )
        safe_meals = []
        for i in range(25):
            for mt in ["BREAKFAST", "LUNCH", "DINNER"]:
                safe_meals.append(_make_candidate(f"Safe {mt} {i}", mt, score=0.7))

        candidates = [allergenic] + safe_meals
        allergic_member = MemberProfile(
            member_id=uuid4(),
            display_name="Bob",
            allergen_ingredient_ids={allergen_id},
        )

        result = plan_weekly_slots(
            household_id=uuid4(),
            week_start_date=date(2026, 4, 13),
            candidates=candidates,
            members=[allergic_member],
        )

        for slot in result.slots:
            assert slot.meal_name != "Peanut Curry"

    def test_member_fit_summary_included(self):
        candidates = []
        for i in range(25):
            for mt in ["BREAKFAST", "LUNCH", "DINNER"]:
                candidates.append(_make_candidate(f"Meal {mt} {i}", mt, score=0.8 - i * 0.01))

        member = MemberProfile(
            member_id=uuid4(),
            display_name="Charlie",
        )

        result = plan_weekly_slots(
            household_id=uuid4(),
            week_start_date=date(2026, 4, 13),
            candidates=candidates,
            members=[member],
        )

        assert str(member.member_id) in result.member_fit_summary
        summary = result.member_fit_summary[str(member.member_id)]
        assert summary["display_name"] == "Charlie"
        assert summary["compliant_slots"] == summary["total_slots"]
        assert summary["compliance_rate"] == 1.0

    def test_missing_ingredients_list_present(self):
        result = plan_weekly_slots(
            household_id=uuid4(),
            week_start_date=date(2026, 4, 13),
            candidates=[],
            members=[],
        )
        assert isinstance(result.missing_ingredients, list)

    def test_slots_sorted_by_day_then_type(self):
        candidates = []
        for i in range(25):
            for mt in ["BREAKFAST", "LUNCH", "DINNER"]:
                candidates.append(_make_candidate(f"Meal {mt} {i}", mt, score=0.8 - i * 0.01))

        result = plan_weekly_slots(
            household_id=uuid4(),
            week_start_date=date(2026, 4, 13),
            candidates=candidates,
            members=[],
        )

        for i in range(len(result.slots) - 1):
            curr = result.slots[i]
            nxt = result.slots[i + 1]
            if curr.day == nxt.day:
                type_order = {"BREAKFAST": 0, "LUNCH": 1, "DINNER": 2}
                assert type_order[curr.meal_type] <= type_order[nxt.meal_type]
            else:
                assert curr.day < nxt.day
