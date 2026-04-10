"""
Subsystem hygiene state — tracks hygieneState and maintenanceRequired per subsystem.
This invariant lives in the simulation layer only — not in Spring Boot.
"""

from dataclasses import dataclass
from typing import Dict
from enum import Enum

from capability_registry import SubsystemType


class HygieneState(str, Enum):
    CLEAN = "CLEAN"
    NEEDS_RINSE = "NEEDS_RINSE"
    NEEDS_DEEP_CLEAN = "NEEDS_DEEP_CLEAN"


@dataclass
class SubsystemStatus:
    subsystem: SubsystemType
    hygiene_state: HygieneState = HygieneState.CLEAN
    maintenance_required: bool = False
    cycles_since_clean: int = 0


class SubsystemStateChecker:
    """Tracks and checks subsystem hygiene state."""

    def __init__(self):
        self._states: Dict[SubsystemType, SubsystemStatus] = {}
        for sub in SubsystemType:
            self._states[sub] = SubsystemStatus(subsystem=sub)

    def get_status(self, subsystem: SubsystemType) -> SubsystemStatus:
        return self._states[subsystem]

    def is_blocked(self, subsystem: SubsystemType) -> bool:
        """Returns True if maintenanceRequired — blocks any primitive on that subsystem."""
        return self._states[subsystem].maintenance_required

    def set_maintenance_required(self, subsystem: SubsystemType, required: bool):
        self._states[subsystem].maintenance_required = required

    def set_hygiene_state(self, subsystem: SubsystemType, state: HygieneState):
        self._states[subsystem].hygiene_state = state
        if state == HygieneState.NEEDS_DEEP_CLEAN:
            self._states[subsystem].maintenance_required = True

    def record_cycle(self, subsystem: SubsystemType):
        """Record a usage cycle. After 5 cycles, flag needs rinse."""
        status = self._states[subsystem]
        status.cycles_since_clean += 1
        if status.cycles_since_clean >= 5:
            status.hygiene_state = HygieneState.NEEDS_RINSE

    def mark_cleaned(self, subsystem: SubsystemType):
        status = self._states[subsystem]
        status.hygiene_state = HygieneState.CLEAN
        status.maintenance_required = False
        status.cycles_since_clean = 0
