"""Tests for workspace model."""

import json
import pytest
from workspace_model import WorkspaceState, SlotOccupiedError


class TestSlotStructure:
    def test_initialises_without_error(self):
        ws = WorkspaceState()
        assert ws is not None

    def test_unique_slot_ids(self):
        ws = WorkspaceState()
        ids = ws.all_slot_ids()
        assert len(ids) == len(set(ids))

    def test_unoccupied_slot_returns_none_contents(self):
        ws = WorkspaceState()
        slot = ws.get_slot("tray-1")
        assert slot is not None
        assert slot.contents is None
        assert slot.occupied is False

    def test_unknown_slot_returns_none(self):
        ws = WorkspaceState()
        assert ws.get_slot("nonexistent") is None


class TestSlotTransitions:
    def test_mark_occupied_then_clear(self):
        ws = WorkspaceState()
        ws.occupy("tray-1", "rice bowl")
        assert ws.is_occupied("tray-1") is True
        ws.clear("tray-1")
        assert ws.is_occupied("tray-1") is False

    def test_clear_empty_slot_is_idempotent(self):
        ws = WorkspaceState()
        ws.clear("tray-1")  # already empty — no error
        assert ws.is_occupied("tray-1") is False

    def test_double_occupy_raises(self):
        ws = WorkspaceState()
        ws.occupy("tray-1", "item A")
        with pytest.raises(SlotOccupiedError):
            ws.occupy("tray-1", "item B")

    def test_serialises_to_json(self):
        ws = WorkspaceState()
        ws.occupy("chamber-1", "heating pasta")
        j = ws.to_json()
        data = json.loads(j)
        assert data["chamber-1"]["occupied"] is True
        assert data["chamber-1"]["contents"] == "heating pasta"

    def test_in_memory_only(self):
        """State is in-memory — two instances are independent."""
        ws1 = WorkspaceState()
        ws2 = WorkspaceState()
        ws1.occupy("tray-1", "item")
        assert ws2.is_occupied("tray-1") is False
