"""
Capability registry — maps subsystem types to supported action types and constraints.
Pure data registry — no external calls.
"""

from dataclasses import dataclass, field
from typing import List, Dict
from enum import Enum


class SubsystemType(str, Enum):
    DRY_BIN = "DRY_BIN"
    LIQUID_DISPENSER = "LIQUID_DISPENSER"
    HEATING_ELEMENT = "HEATING_ELEMENT"
    STIRRER = "STIRRER"
    MODULE_LOADER = "MODULE_LOADER"
    MODULE_MOVER = "MODULE_MOVER"


class ActionType(str, Enum):
    DISPENSE_DRY = "DISPENSE_DRY"
    DISPENSE_LIQUID = "DISPENSE_LIQUID"
    HEAT = "HEAT"
    STIR = "STIR"
    LOAD_MODULE = "LOAD_MODULE"
    MOVE_MODULE = "MOVE_MODULE"
    USER_LOAD_TRAY = "USER_LOAD_TRAY"
    USER_CONFIRM = "USER_CONFIRM"
    USER_REMOVE_VESSEL = "USER_REMOVE_VESSEL"


@dataclass
class Capability:
    action_type: ActionType
    max_quantity: float = 0  # grams or ml; 0 = no limit
    constraints: Dict[str, str] = field(default_factory=dict)


class CapabilityNotFoundError(Exception):
    """Raised when querying an unknown subsystem."""
    pass


# Registry: subsystem → capabilities
_REGISTRY: Dict[SubsystemType, List[Capability]] = {
    SubsystemType.DRY_BIN: [
        Capability(action_type=ActionType.DISPENSE_DRY, max_quantity=2000),
    ],
    SubsystemType.LIQUID_DISPENSER: [
        Capability(action_type=ActionType.DISPENSE_LIQUID, max_quantity=1000),
    ],
    SubsystemType.HEATING_ELEMENT: [
        Capability(action_type=ActionType.HEAT, constraints={"max_temp_c": "250"}),
    ],
    SubsystemType.STIRRER: [
        Capability(action_type=ActionType.STIR, constraints={"max_rpm": "300"}),
    ],
    SubsystemType.MODULE_LOADER: [
        Capability(action_type=ActionType.LOAD_MODULE),
    ],
    SubsystemType.MODULE_MOVER: [
        Capability(action_type=ActionType.MOVE_MODULE),
    ],
}


def get_capabilities(subsystem: SubsystemType) -> List[Capability]:
    """Get capabilities for a subsystem. Raises CapabilityNotFoundError for unknown."""
    if subsystem not in _REGISTRY:
        raise CapabilityNotFoundError(f"Unknown subsystem: {subsystem}")
    return _REGISTRY[subsystem]


def get_all_machine_action_types() -> set:
    """Return the set of all action types the machine can perform."""
    actions = set()
    for caps in _REGISTRY.values():
        for cap in caps:
            actions.add(cap.action_type)
    return actions
