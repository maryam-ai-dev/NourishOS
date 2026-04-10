"""
Execution controller — runs primitives one at a time.
On success: calls Spring Boot step-complete.
On failure: retry up to 2 times, then abort.
"""

import structlog
from typing import List, Optional, Callable
from task_planner import Primitive
from device_bridge import DeviceBridge

log = structlog.get_logger()

MAX_RETRIES = 2


class ExecutionController:
    """Runs a sequence of primitives through the device bridge."""

    def __init__(
        self,
        bridge: Optional[DeviceBridge] = None,
        on_step_complete: Optional[Callable] = None,
        on_abort: Optional[Callable] = None,
        on_session_update: Optional[Callable] = None,
    ):
        self.bridge = bridge or DeviceBridge()
        self.on_step_complete = on_step_complete or (lambda step, result: None)
        self.on_abort = on_abort or (lambda plan_id, reason: None)
        self.on_session_update = on_session_update or (lambda plan_id, step_index, status: None)

    def run(self, plan_id: str, primitives: List[Primitive]) -> dict:
        """
        Execute all primitives sequentially.
        Returns summary with status, completed count, and any error.
        """
        completed = 0
        status = "COMPLETED"
        error = None

        for i, prim in enumerate(primitives):
            if prim.assigned_to == "USER":
                # USER steps handled by intervention manager, not here
                log.info("user_step_skipped", step_order=prim.step_order)
                self.on_session_update(plan_id, i, "WAITING_USER")
                completed += 1
                continue

            success = self._execute_with_retry(prim)

            if success:
                log.info("step_completed", step_order=prim.step_order, action=prim.action_type)
                self.on_step_complete(prim, {"success": True})
                self.on_session_update(plan_id, i, "IN_PROGRESS")
                completed += 1
            else:
                log.error("step_failed_after_retries", step_order=prim.step_order)
                status = "ABORTED"
                error = f"Step {prim.step_order} failed after {MAX_RETRIES} retries"
                self.on_abort(plan_id, error)
                break

        return {
            "plan_id": plan_id,
            "status": status,
            "completed_steps": completed,
            "total_steps": len(primitives),
            "error": error,
        }

    def _execute_with_retry(self, prim: Primitive) -> bool:
        """Try execution, retry up to MAX_RETRIES on failure."""
        attempts = 0
        while attempts <= MAX_RETRIES:
            try:
                result = self.bridge.execute_action(prim)
                if result.get("success"):
                    return True
            except Exception as e:
                log.warning("step_attempt_failed", step_order=prim.step_order, attempt=attempts, error=str(e))
            attempts += 1
        return False
