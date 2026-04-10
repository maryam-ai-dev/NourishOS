"""Tests for replenishment POST to Spring Boot integration."""

from decimal import Decimal
from uuid import uuid4
from unittest.mock import patch, MagicMock

from app.services.forecasting.replenishment_optimizer import (
    StockoutInput,
    optimize_replenishment,
)
from app.clients.authority_client import post_replenishment_suggestions


class TestReplenishmentPost:
    def test_optimizer_output_serializable_for_post(self):
        """Optimizer suggestions can be serialized for Spring Boot POST."""
        inp = StockoutInput(
            ingredient_id=uuid4(),
            weekly_usage_rate=Decimal("100"),
            urgency="CRITICAL",
            unit="g",
            is_recurring_waste=True,
            waste_ratio=Decimal("0.2"),
        )
        suggestions = optimize_replenishment([inp])
        assert len(suggestions) == 1

        # Convert to dict for POST
        payload = [{
            "ingredientId": str(s.ingredient_id),
            "suggestedQuantity": float(s.adjusted_quantity),
            "unit": s.unit,
            "urgency": s.urgency,
            "adjustedForWaste": s.adjusted_for_waste,
            "reason": s.reason,
        } for s in suggestions]

        assert payload[0]["adjustedForWaste"] is True
        assert payload[0]["suggestedQuantity"] < 200  # waste-adjusted

    @patch("app.clients.authority_client._get_client")
    def test_post_calls_spring_boot(self, mock_client_factory):
        mock_client = MagicMock()
        mock_response = MagicMock()
        mock_response.status_code = 201
        mock_response.json.return_value = [{"id": str(uuid4())}]
        mock_client.post.return_value = mock_response
        mock_client_factory.return_value = mock_client

        result = post_replenishment_suggestions(str(uuid4()), [
            {"ingredientId": str(uuid4()), "suggestedQuantity": 100, "unit": "g",
             "adjustedForWaste": False, "urgency": "WARNING"},
        ])

        mock_client.post.assert_called_once()
        assert isinstance(result, list)

    def test_adjusted_for_waste_flag_in_payload(self):
        """adjustedForWaste flag correctly set in suggestions."""
        waste_inp = StockoutInput(
            ingredient_id=uuid4(),
            weekly_usage_rate=Decimal("100"),
            urgency="WARNING",
            unit="g",
            is_recurring_waste=True,
            waste_ratio=Decimal("0.3"),
        )
        normal_inp = StockoutInput(
            ingredient_id=uuid4(),
            weekly_usage_rate=Decimal("100"),
            urgency="WARNING",
            unit="g",
        )
        suggestions = optimize_replenishment([waste_inp, normal_inp])
        waste_sug = next(s for s in suggestions if s.ingredient_id == waste_inp.ingredient_id)
        normal_sug = next(s for s in suggestions if s.ingredient_id == normal_inp.ingredient_id)
        assert waste_sug.adjusted_for_waste is True
        assert normal_sug.adjusted_for_waste is False

    def test_urgency_matches_input(self):
        inp = StockoutInput(
            ingredient_id=uuid4(),
            weekly_usage_rate=Decimal("50"),
            urgency="CRITICAL",
            unit="ml",
        )
        suggestions = optimize_replenishment([inp])
        assert suggestions[0].urgency == "CRITICAL"
