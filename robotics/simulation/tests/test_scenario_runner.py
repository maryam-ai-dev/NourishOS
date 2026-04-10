"""Tests for scenario runner — all 6 scenarios reach terminal states."""

import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "scenarios"))
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from scenarios.runner import (
    scenario_empty_bin, scenario_missing_tray, scenario_user_delayed,
    scenario_camera_anomaly, scenario_cooking_interruption, scenario_reorder_missed,
    ALL_SCENARIOS, run_all,
)


class TestScenarioRunner:
    def test_empty_bin_aborts(self):
        result = scenario_empty_bin()
        assert result.terminal_state == "ABORTED"
        assert result.success is True

    def test_missing_tray_timeout(self):
        result = scenario_missing_tray()
        assert result.terminal_state == "TIMEOUT"
        assert result.success is True

    def test_user_delayed_resolves(self):
        result = scenario_user_delayed()
        assert result.terminal_state == "RESOLVED"
        assert result.success is True

    def test_camera_anomaly_intervention(self):
        result = scenario_camera_anomaly()
        assert result.terminal_state == "INTERVENTION_CREATED"
        assert result.success is True

    def test_cooking_interruption_aborts(self):
        result = scenario_cooking_interruption()
        assert result.terminal_state == "ABORTED"
        assert result.success is True

    def test_reorder_missed_completes(self):
        result = scenario_reorder_missed()
        assert result.terminal_state == "COMPLETED"
        assert result.success is True

    def test_all_6_scenarios_pass(self):
        results = run_all()
        assert len(results) == 6
        for r in results:
            assert r.success is True, f"Scenario {r.name} failed: {r.detail}"

    def test_no_unhandled_exceptions(self):
        """All scenarios run without unhandled exceptions."""
        for fn in ALL_SCENARIOS:
            result = fn()
            assert result is not None
