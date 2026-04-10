"""Tests for ingredient expander."""

import pytest
from decimal import Decimal
from uuid import uuid4

from app.services.planning.ingredient_expander import (
    IngredientRef,
    expand_ingredients,
)


class TestExpandIngredients:
    def test_double_servings_doubles_all_quantities(self):
        refs = [
            IngredientRef(ingredient_id=uuid4(), base_quantity=Decimal("200"), unit="g"),
            IngredientRef(ingredient_id=uuid4(), base_quantity=Decimal("100"), unit="ml"),
        ]

        result = expand_ingredients(refs, servings=2)

        assert result[0].quantity == Decimal("400")
        assert result[1].quantity == Decimal("200")

    def test_scaling_factor_applied(self):
        refs = [
            IngredientRef(ingredient_id=uuid4(), base_quantity=Decimal("100"), unit="g"),
        ]

        result = expand_ingredients(refs, servings=2, scaling_factor=Decimal("1.5"))

        assert result[0].quantity == Decimal("300")  # 100 × 2 × 1.5

    def test_units_in_canonical_form_grams(self):
        refs = [
            IngredientRef(ingredient_id=uuid4(), base_quantity=Decimal("1"), unit="kg"),
        ]

        result = expand_ingredients(refs, servings=1)

        assert result[0].quantity == Decimal("1000")
        assert result[0].canonical_unit == "g"

    def test_units_in_canonical_form_ml(self):
        refs = [
            IngredientRef(ingredient_id=uuid4(), base_quantity=Decimal("2"), unit="l"),
        ]

        result = expand_ingredients(refs, servings=1)

        assert result[0].quantity == Decimal("2000")
        assert result[0].canonical_unit == "ml"

    def test_units_in_canonical_form_unit(self):
        refs = [
            IngredientRef(ingredient_id=uuid4(), base_quantity=Decimal("3"), unit="unit"),
        ]

        result = expand_ingredients(refs, servings=2)

        assert result[0].quantity == Decimal("6")
        assert result[0].canonical_unit == "unit"

    def test_unknown_unit_returns_quantity_none(self):
        refs = [
            IngredientRef(ingredient_id=uuid4(), base_quantity=Decimal("5"), unit="cups"),
        ]

        result = expand_ingredients(refs, servings=1)

        assert result[0].quantity is None
        assert result[0].canonical_unit == "cups"

    def test_preserves_optional_flag(self):
        refs = [
            IngredientRef(ingredient_id=uuid4(), base_quantity=Decimal("50"), unit="g", optional=True),
            IngredientRef(ingredient_id=uuid4(), base_quantity=Decimal("100"), unit="g", optional=False),
        ]

        result = expand_ingredients(refs, servings=1)

        assert result[0].optional is True
        assert result[1].optional is False

    def test_returns_list_without_error(self):
        refs = [
            IngredientRef(ingredient_id=uuid4(), base_quantity=Decimal("200"), unit="g"),
            IngredientRef(ingredient_id=uuid4(), base_quantity=Decimal("500"), unit="ml"),
            IngredientRef(ingredient_id=uuid4(), base_quantity=Decimal("2"), unit="unit"),
        ]

        result = expand_ingredients(refs, servings=4)

        assert len(result) == 3
        assert result[0].quantity == Decimal("800")
        assert result[1].quantity == Decimal("2000")
        assert result[2].quantity == Decimal("8")

    def test_zero_servings_raises(self):
        refs = [IngredientRef(ingredient_id=uuid4(), base_quantity=Decimal("100"), unit="g")]
        with pytest.raises(ValueError, match="servings must be > 0"):
            expand_ingredients(refs, servings=0)

    def test_original_unit_preserved(self):
        refs = [
            IngredientRef(ingredient_id=uuid4(), base_quantity=Decimal("1"), unit="kg"),
        ]

        result = expand_ingredients(refs, servings=1)

        assert result[0].original_unit == "kg"
        assert result[0].canonical_unit == "g"
