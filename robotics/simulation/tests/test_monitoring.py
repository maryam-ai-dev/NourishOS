"""Tests for monitoring feedback, vision stub, and anomaly handling."""

import sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from monitoring_feedback import generate_telemetry
from vision.stage_classifier import classify_stage, Stage
from anomaly_handler import handle_anomaly, AnomalyAction


class TestTelemetry:
    def test_generates_without_error(self):
        t = generate_telemetry(step_index=0)
        assert t is not None

    def test_normal_telemetry_no_anomaly(self):
        t = generate_telemetry(step_index=1)
        assert t.anomaly_flag is False
        assert t.temp_ok is True

    def test_injected_anomaly(self):
        t = generate_telemetry(step_index=1, inject_anomaly=True)
        assert t.anomaly_flag is True


class TestStageClassifier:
    def test_anomaly_flag_returns_anomaly_stage(self):
        result = classify_stage(step_index=0, elapsed_seconds=10, anomaly_flag=True)
        assert result.stage == Stage.ANOMALY
        assert result.anomaly_detected is True

    def test_elapsed_past_duration_returns_complete(self):
        result = classify_stage(step_index=0, elapsed_seconds=65, expected_duration=60)
        assert result.stage == Stage.COMPLETE

    def test_normal_returns_valid_stage(self):
        result = classify_stage(step_index=0, elapsed_seconds=30, expected_duration=60)
        assert result.stage in list(Stage)
        assert result.anomaly_detected is False

    def test_confidence_in_range(self):
        for elapsed in [0, 10, 30, 50, 55, 60, 70]:
            result = classify_stage(step_index=0, elapsed_seconds=elapsed)
            assert 0.0 <= result.confidence <= 1.0

    def test_all_stage_phases(self):
        """Different elapsed values produce different stages."""
        stages = set()
        for elapsed in [0, 5, 15, 40, 55, 60]:
            result = classify_stage(step_index=0, elapsed_seconds=elapsed, expected_duration=60)
            stages.add(result.stage)
        assert len(stages) >= 3  # at least 3 different stages


class TestAnomalyHandler:
    def test_minor_anomaly_creates_intervention(self):
        t = generate_telemetry(step_index=1, inject_anomaly=True)
        result = handle_anomaly(t, severity="minor")
        assert result["action"] == AnomalyAction.INTERVENE

    def test_severe_anomaly_aborts(self):
        t = generate_telemetry(step_index=1, inject_anomaly=True)
        result = handle_anomaly(t, severity="severe")
        assert result["action"] == AnomalyAction.ABORT

    def test_normal_telemetry_proceeds(self):
        t = generate_telemetry(step_index=1, inject_anomaly=False)
        result = handle_anomaly(t, elapsed_seconds=30, expected_duration=60)
        assert result["action"] == AnomalyAction.PROCEED

    def test_no_unhandled_exception(self):
        t = generate_telemetry(step_index=0, inject_anomaly=True)
        # Should not raise
        handle_anomaly(t, severity="severe")
        handle_anomaly(t, severity="minor")
