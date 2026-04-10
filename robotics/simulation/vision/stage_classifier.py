"""
Vision stub — stage classifier.
Input: step_index, elapsed_seconds, anomaly_flag
Output: stage, confidence, anomaly_detected, message
"""

from dataclasses import dataclass
from enum import Enum


class Stage(str, Enum):
    IDLE = "IDLE"
    LOADING = "LOADING"
    HEATING = "HEATING"
    SIMMERING = "SIMMERING"
    STIRRING = "STIRRING"
    COMPLETE = "COMPLETE"
    ANOMALY = "ANOMALY"


@dataclass
class ClassifierResult:
    stage: Stage
    confidence: float  # [0, 1]
    anomaly_detected: bool
    message: str


def classify_stage(
    step_index: int,
    elapsed_seconds: float,
    anomaly_flag: bool = False,
    expected_duration: float = 60.0,
) -> ClassifierResult:
    """
    Classify the current stage based on step state.
    - anomaly_flag=True → ANOMALY
    - elapsed past expected duration → COMPLETE
    - Otherwise based on elapsed ratio
    """
    if anomaly_flag:
        return ClassifierResult(
            stage=Stage.ANOMALY,
            confidence=0.95,
            anomaly_detected=True,
            message="Anomaly detected — check equipment state",
        )

    if elapsed_seconds >= expected_duration:
        return ClassifierResult(
            stage=Stage.COMPLETE,
            confidence=0.99,
            anomaly_detected=False,
            message="Step complete",
        )

    ratio = elapsed_seconds / expected_duration if expected_duration > 0 else 0

    if ratio < 0.1:
        stage = Stage.IDLE
        confidence = 0.9
        msg = "Waiting to start"
    elif ratio < 0.3:
        stage = Stage.LOADING
        confidence = 0.85
        msg = "Loading materials"
    elif ratio < 0.7:
        stage = Stage.HEATING
        confidence = 0.88
        msg = "Active heating"
    elif ratio < 0.9:
        stage = Stage.SIMMERING
        confidence = 0.82
        msg = "Simmering phase"
    else:
        stage = Stage.STIRRING
        confidence = 0.8
        msg = "Final stirring"

    return ClassifierResult(
        stage=stage,
        confidence=max(0.0, min(1.0, confidence)),
        anomaly_detected=False,
        message=msg,
    )
