"""
Monitoring feedback — generates synthetic telemetry per step.
Telemetry logged via structlog.
"""

import structlog
from dataclasses import dataclass
from typing import Optional

log = structlog.get_logger()


@dataclass
class Telemetry:
    step_index: int
    temp_ok: bool = True
    tray_present: bool = True
    dispense_verified: bool = True
    anomaly_flag: bool = False


def generate_telemetry(step_index: int, inject_anomaly: bool = False) -> Telemetry:
    """Generate synthetic telemetry for a step."""
    telemetry = Telemetry(
        step_index=step_index,
        temp_ok=not inject_anomaly,
        tray_present=True,
        dispense_verified=not inject_anomaly,
        anomaly_flag=inject_anomaly,
    )
    log.info("telemetry_generated", step_index=step_index, anomaly=inject_anomaly)
    return telemetry
