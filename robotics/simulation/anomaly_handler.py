"""
Anomaly handler — wires telemetry → classifier → intervention or abort.
Minor anomaly → intervention. Severe anomaly → abort.
"""

import structlog
from monitoring_feedback import Telemetry
from vision.stage_classifier import classify_stage, Stage

log = structlog.get_logger()


class AnomalyAction:
    INTERVENE = "INTERVENE"
    ABORT = "ABORT"
    PROCEED = "PROCEED"


def handle_anomaly(
    telemetry: Telemetry,
    elapsed_seconds: float = 0,
    expected_duration: float = 60,
    severity: str = "minor",  # minor or severe
) -> dict:
    """
    Process telemetry through classifier, handle anomaly.
    - Minor anomaly → create intervention
    - Severe anomaly → abort
    - Normal → proceed
    """
    result = classify_stage(
        step_index=telemetry.step_index,
        elapsed_seconds=elapsed_seconds,
        anomaly_flag=telemetry.anomaly_flag,
        expected_duration=expected_duration,
    )

    if result.stage == Stage.ANOMALY:
        if severity == "severe":
            log.error("severe_anomaly_abort", step_index=telemetry.step_index)
            return {
                "action": AnomalyAction.ABORT,
                "stage": result.stage.value,
                "message": "Severe anomaly — aborting execution",
            }
        else:
            log.warning("minor_anomaly_intervention", step_index=telemetry.step_index)
            return {
                "action": AnomalyAction.INTERVENE,
                "stage": result.stage.value,
                "message": "Minor anomaly — requesting user intervention",
            }

    return {
        "action": AnomalyAction.PROCEED,
        "stage": result.stage.value,
        "message": result.message,
    }
