"""Tests for substitution engine."""

from decimal import Decimal
from uuid import uuid4

from app.services.substitution.substitution_engine import (
    IngredientOption,
    suggest_substitutions,
)


def _ing(name, protein=20, category="dry", available=True, recurring_waste=False):
    return IngredientOption(
        ingredient_id=uuid4(),
        name=name,
        protein_per_100g=Decimal(str(protein)),
        category=category,
        is_available=available,
        is_recurring_waste=recurring_waste,
    )


class TestSubstitutionEngine:
    def test_returns_without_error(self):
        missing = [_ing("Chicken", 25, available=False)]
        available = [_ing("Turkey", 24)]
        result = suggest_substitutions(missing, available)
        assert len(result) >= 1

    def test_protein_within_10_percent(self):
        missing = [_ing("Chicken", 25, available=False)]
        available = [_ing("Turkey", 24)]  # 4% diff
        result = suggest_substitutions(missing, available)
        assert result[0].requires_approval is False
        assert "within 10%" in result[0].reason

    def test_protein_over_10_percent_requires_approval(self):
        missing = [_ing("Chicken", 25, available=False)]
        available = [_ing("Tofu", 8)]  # 68% diff
        result = suggest_substitutions(missing, available)
        assert result[0].requires_approval is True

    def test_same_category_only(self):
        missing = [_ing("Rice", 7, category="dry", available=False)]
        available = [_ing("Milk", 3, category="liquid")]
        result = suggest_substitutions(missing, available)
        assert len(result) == 0

    def test_no_substitute_when_none_available(self):
        missing = [_ing("Chicken", 25, available=False)]
        result = suggest_substitutions(missing, [])
        assert len(result) == 0

    def test_recurring_waste_excluded(self):
        missing_ing = _ing("Chicken", 25, available=False)
        waste_ing = _ing("Turkey", 24, recurring_waste=True)
        good_ing = _ing("Duck", 22)
        result = suggest_substitutions(
            [missing_ing], [waste_ing, good_ing],
            recurring_waste_ids={waste_ing.ingredient_id},
        )
        assert len(result) == 1
        assert result[0].substitute_name == "Duck"

    def test_non_waste_preferred_over_waste(self):
        """With two valid substitutes, non-waste returned (waste excluded entirely)."""
        missing_ing = _ing("Chicken", 25, available=False)
        waste_sub = _ing("Turkey", 24, recurring_waste=True)
        good_sub = _ing("Duck", 23)
        result = suggest_substitutions(
            [missing_ing], [waste_sub, good_sub],
            recurring_waste_ids={waste_sub.ingredient_id},
        )
        assert result[0].substitute_name == "Duck"

    def test_all_substitutes_have_reason(self):
        missing = [_ing("Chicken", 25, available=False)]
        available = [_ing("Turkey", 24)]
        result = suggest_substitutions(missing, available)
        for sub in result:
            assert sub.reason != ""
