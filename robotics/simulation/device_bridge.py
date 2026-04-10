"""
Device bridge — dispatches primitives to simulation handlers.
Only point of dispatch — execution controller never calls handlers directly.
"""

import structlog
from task_planner import Primitive, ActionType

log = structlog.get_logger()


class UnsupportedActionError(Exception):
    """Raised for unknown action types."""
    pass


class DeviceBridge:
    """Hardware-agnostic dispatch to simulation handlers."""

    def execute_action(self, primitive: Primitive) -> dict:
        """Dispatch to correct handler. Returns result dict."""
        handler = _HANDLERS.get(primitive.action_type)
        if handler is None:
            raise UnsupportedActionError(f"No handler for action: {primitive.action_type}")
        return handler(primitive)


def _handle_dispense_dry(p: Primitive) -> dict:
    qty = p.ingredient_ref.quantity if p.ingredient_ref else 0
    log.info("dispense_dry", step_order=p.step_order, quantity=qty)
    return {"action": "DISPENSE_DRY", "dispensed": qty, "success": True}


def _handle_dispense_liquid(p: Primitive) -> dict:
    qty = p.ingredient_ref.quantity if p.ingredient_ref else 0
    log.info("dispense_liquid", step_order=p.step_order, quantity=qty)
    return {"action": "DISPENSE_LIQUID", "dispensed": qty, "success": True}


def _handle_heat(p: Primitive) -> dict:
    temp = p.target_temp_c or 180
    log.info("heat", step_order=p.step_order, target_temp=temp, duration=p.estimated_duration_seconds)
    return {"action": "HEAT", "target_temp": temp, "success": True}


def _handle_stir(p: Primitive) -> dict:
    rpm = p.rpm or 100
    log.info("stir", step_order=p.step_order, rpm=rpm)
    return {"action": "STIR", "rpm": rpm, "success": True}


def _handle_load_module(p: Primitive) -> dict:
    log.info("load_module", step_order=p.step_order, slot=p.slot_id)
    return {"action": "LOAD_MODULE", "success": True}


def _handle_move_module(p: Primitive) -> dict:
    log.info("move_module", step_order=p.step_order, slot=p.slot_id)
    return {"action": "MOVE_MODULE", "success": True}


def _handle_user_confirm(p: Primitive) -> dict:
    log.info("user_confirm", step_order=p.step_order)
    return {"action": "USER_CONFIRM", "success": True, "requires_user": True}


def _handle_user_remove(p: Primitive) -> dict:
    log.info("user_remove_vessel", step_order=p.step_order)
    return {"action": "USER_REMOVE_VESSEL", "success": True, "requires_user": True}


def _handle_user_load_tray(p: Primitive) -> dict:
    log.info("user_load_tray", step_order=p.step_order)
    return {"action": "USER_LOAD_TRAY", "success": True, "requires_user": True}


_HANDLERS = {
    ActionType.DISPENSE_DRY: _handle_dispense_dry,
    ActionType.DISPENSE_LIQUID: _handle_dispense_liquid,
    ActionType.HEAT: _handle_heat,
    ActionType.STIR: _handle_stir,
    ActionType.LOAD_MODULE: _handle_load_module,
    ActionType.MOVE_MODULE: _handle_move_module,
    ActionType.USER_CONFIRM: _handle_user_confirm,
    ActionType.USER_REMOVE_VESSEL: _handle_user_remove,
    ActionType.USER_LOAD_TRAY: _handle_user_load_tray,
}
