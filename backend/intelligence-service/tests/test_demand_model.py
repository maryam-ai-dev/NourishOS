"""Tests for demand model."""

from datetime import date
from decimal import Decimal
from uuid import uuid4

from app.services.forecasting.demand_model import (
    IngredientStock,
    predict_stockouts,
)


def _stock(quantity=500, usage=100, minimum=50, planned=0):
    return IngredientStock(
        ingredient_id=uuid4(),
        current_quantity=Decimal(str(quantity)),
        unit="g",
        avg_weekly_usage=Decimal(str(usage)),
        par_level_minimum=Decimal(str(minimum)),
        planned_usage_this_week=Decimal(str(planned)),
    )


class TestDemandModel:
    def test_returns_predictions_without_error(self):
        stocks = [_stock()]
        result = predict_stockouts(stocks, date(2026, 4, 13))
        assert len(result) == 1

    def test_high_usage_stockout_within_range(self):
        """500g current, 100g/week usage, 50g minimum → buffer=450g, ~31 days."""
        stock = _stock(quantity=500, usage=100, minimum=50)
        result = predict_stockouts([stock], date(2026, 4, 13))
        pred = result[0]
        assert pred.predicted_stockout_date is not None
        assert pred.days_until_stockout is not None
        # 450g buffer / (100/7 daily) ≈ 31 days
        assert 28 <= pred.days_until_stockout <= 35

    def test_zero_usage_returns_null_stockout(self):
        stock = _stock(quantity=500, usage=0, minimum=50)
        result = predict_stockouts([stock], date(2026, 4, 13))
        assert result[0].predicted_stockout_date is None
        assert result[0].days_until_stockout is None
        assert result[0].urgency == "OK"

    def test_critical_urgency_under_3_days(self):
        """Low buffer → stockout in < 3 days → CRITICAL."""
        stock = _stock(quantity=55, usage=100, minimum=50)
        result = predict_stockouts([stock], date(2026, 4, 13))
        assert result[0].urgency == "CRITICAL"
        assert result[0].days_until_stockout < 3

    def test_warning_urgency_3_to_7_days(self):
        stock = _stock(quantity=120, usage=100, minimum=50)
        result = predict_stockouts([stock], date(2026, 4, 13))
        # buffer=70g, daily~14.3g → ~4.9 days
        assert result[0].urgency == "WARNING"

    def test_ok_urgency_over_7_days(self):
        stock = _stock(quantity=500, usage=50, minimum=50)
        result = predict_stockouts([stock], date(2026, 4, 13))
        assert result[0].urgency == "OK"
        assert result[0].days_until_stockout > 7

    def test_already_below_minimum(self):
        stock = _stock(quantity=30, usage=100, minimum=50)
        result = predict_stockouts([stock], date(2026, 4, 13))
        assert result[0].days_until_stockout == 0
        assert result[0].urgency == "CRITICAL"

    def test_uses_par_level_not_hardcoded_percentage(self):
        """Different minimums → different stockout predictions."""
        stock_low = _stock(quantity=200, usage=50, minimum=10)
        stock_high = _stock(quantity=200, usage=50, minimum=150)
        result_low = predict_stockouts([stock_low], date(2026, 4, 13))
        result_high = predict_stockouts([stock_high], date(2026, 4, 13))
        assert result_low[0].days_until_stockout > result_high[0].days_until_stockout

    def test_planned_usage_included(self):
        stock_no_plan = _stock(quantity=200, usage=50, minimum=50, planned=0)
        stock_with_plan = _stock(quantity=200, usage=50, minimum=50, planned=50)
        r1 = predict_stockouts([stock_no_plan], date(2026, 4, 13))
        r2 = predict_stockouts([stock_with_plan], date(2026, 4, 13))
        assert r1[0].days_until_stockout > r2[0].days_until_stockout
