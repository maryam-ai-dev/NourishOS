"""Tests for subsystem hygiene state."""

from capability_registry import SubsystemType
from subsystem_state import SubsystemStateChecker, HygieneState


class TestSubsystemState:
    def test_runs_without_error(self):
        checker = SubsystemStateChecker()
        assert checker is not None

    def test_maintenance_required_blocks(self):
        checker = SubsystemStateChecker()
        checker.set_maintenance_required(SubsystemType.HEATING_ELEMENT, True)
        assert checker.is_blocked(SubsystemType.HEATING_ELEMENT) is True

    def test_clean_subsystem_not_blocked(self):
        checker = SubsystemStateChecker()
        assert checker.is_blocked(SubsystemType.HEATING_ELEMENT) is False

    def test_deep_clean_auto_blocks(self):
        checker = SubsystemStateChecker()
        checker.set_hygiene_state(SubsystemType.STIRRER, HygieneState.NEEDS_DEEP_CLEAN)
        assert checker.is_blocked(SubsystemType.STIRRER) is True

    def test_mark_cleaned_unblocks(self):
        checker = SubsystemStateChecker()
        checker.set_maintenance_required(SubsystemType.DRY_BIN, True)
        assert checker.is_blocked(SubsystemType.DRY_BIN) is True
        checker.mark_cleaned(SubsystemType.DRY_BIN)
        assert checker.is_blocked(SubsystemType.DRY_BIN) is False

    def test_simulation_layer_only(self):
        """This invariant lives in simulation — not in Spring Boot."""
        checker = SubsystemStateChecker()
        status = checker.get_status(SubsystemType.MODULE_LOADER)
        assert status.hygiene_state == HygieneState.CLEAN
