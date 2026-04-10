"""Tests for capability registry."""

import pytest
from capability_registry import (
    SubsystemType, ActionType, Capability,
    get_capabilities, CapabilityNotFoundError,
    get_all_machine_action_types,
)


class TestCapabilityRegistry:
    def test_dry_bin_returns_dispense_dry(self):
        caps = get_capabilities(SubsystemType.DRY_BIN)
        assert len(caps) >= 1
        assert caps[0].action_type == ActionType.DISPENSE_DRY

    def test_known_subsystem_returns_typed_list(self):
        caps = get_capabilities(SubsystemType.HEATING_ELEMENT)
        assert isinstance(caps, list)
        assert all(isinstance(c, Capability) for c in caps)

    def test_unknown_subsystem_raises(self):
        with pytest.raises(CapabilityNotFoundError):
            get_capabilities("UNKNOWN_SUBSYSTEM")

    def test_all_subsystems_have_capabilities(self):
        for sub in SubsystemType:
            caps = get_capabilities(sub)
            assert len(caps) >= 1

    def test_registry_is_pure_data(self):
        """No external calls — pure data registry."""
        caps1 = get_capabilities(SubsystemType.DRY_BIN)
        caps2 = get_capabilities(SubsystemType.DRY_BIN)
        assert caps1[0].action_type == caps2[0].action_type

    def test_get_all_machine_action_types(self):
        actions = get_all_machine_action_types()
        assert ActionType.DISPENSE_DRY in actions
        assert ActionType.HEAT in actions
        assert ActionType.USER_CONFIRM not in actions
