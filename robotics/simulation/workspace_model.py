"""
Workspace model — spatial layout with tray slots, bins, chambers, and zones.
In-memory only — not persisted.
"""

import json
from dataclasses import dataclass, field
from typing import Dict, Optional
from enum import Enum


class SlotType(str, Enum):
    TRAY = "TRAY"
    DRY_BIN = "DRY_BIN"
    LIQUID_BIN = "LIQUID_BIN"
    COOKING_CHAMBER = "COOKING_CHAMBER"
    SERVING_ZONE = "SERVING_ZONE"
    HANDOFF_ZONE = "HANDOFF_ZONE"


class SlotOccupiedError(Exception):
    """Raised when trying to occupy an already-occupied slot."""
    pass


@dataclass
class SlotState:
    slot_id: str
    slot_type: SlotType
    occupied: bool = False
    contents: Optional[str] = None  # description of what's in the slot


class WorkspaceState:
    """In-memory workspace with unique slot identifiers."""

    def __init__(self):
        self._slots: Dict[str, SlotState] = {}
        self._init_default_layout()

    def _init_default_layout(self):
        defaults = [
            ("tray-1", SlotType.TRAY),
            ("tray-2", SlotType.TRAY),
            ("dry-bin-1", SlotType.DRY_BIN),
            ("dry-bin-2", SlotType.DRY_BIN),
            ("liquid-bin-1", SlotType.LIQUID_BIN),
            ("chamber-1", SlotType.COOKING_CHAMBER),
            ("serving-1", SlotType.SERVING_ZONE),
            ("handoff-1", SlotType.HANDOFF_ZONE),
        ]
        for slot_id, slot_type in defaults:
            self._slots[slot_id] = SlotState(slot_id=slot_id, slot_type=slot_type)

    def get_slot(self, slot_id: str) -> Optional[SlotState]:
        """Query a slot. Returns None if slot doesn't exist."""
        return self._slots.get(slot_id)

    def is_occupied(self, slot_id: str) -> bool:
        slot = self._slots.get(slot_id)
        if slot is None:
            return False
        return slot.occupied

    def occupy(self, slot_id: str, contents: str = "item"):
        """Mark slot occupied. Raises SlotOccupiedError if already occupied."""
        slot = self._slots.get(slot_id)
        if slot is None:
            raise KeyError(f"Unknown slot: {slot_id}")
        if slot.occupied:
            raise SlotOccupiedError(f"Slot {slot_id} is already occupied by '{slot.contents}'")
        slot.occupied = True
        slot.contents = contents

    def clear(self, slot_id: str):
        """Clear a slot. Idempotent — clearing empty slot is fine."""
        slot = self._slots.get(slot_id)
        if slot is None:
            raise KeyError(f"Unknown slot: {slot_id}")
        slot.occupied = False
        slot.contents = None

    def all_slot_ids(self):
        return list(self._slots.keys())

    def to_json(self) -> str:
        """Serialise to JSON for logging."""
        return json.dumps({
            slot_id: {
                "type": s.slot_type.value,
                "occupied": s.occupied,
                "contents": s.contents,
            }
            for slot_id, s in self._slots.items()
        }, indent=2)
