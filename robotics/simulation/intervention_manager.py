"""
Intervention manager — pauses on USER step, creates InterventionRequest via Spring Boot,
polls for resolution via Spring Boot GET /executions/{id} (not Redis directly).
"""

import structlog
from typing import Optional, Callable
from task_planner import Primitive

log = structlog.get_logger()


class InterventionManager:
    """Manages USER step interventions."""

    def __init__(
        self,
        create_intervention: Optional[Callable] = None,
        poll_resolution: Optional[Callable] = None,
    ):
        self.create_intervention = create_intervention or self._default_create
        self.poll_resolution = poll_resolution or self._default_poll

    def handle_user_step(self, execution_id: str, primitive: Primitive) -> dict:
        """
        Pause on USER step:
        1. Call Spring Boot to create InterventionRequest
        2. Poll Spring Boot for resolution (Spring reads Redis, not us)
        3. Resume on RESOLVED
        """
        log.info("intervention_pause", step_order=primitive.step_order, action=primitive.action_type)

        # Create intervention via Spring Boot
        intervention = self.create_intervention(execution_id, primitive)
        intervention_id = intervention.get("id", "unknown")

        log.info("intervention_created", intervention_id=intervention_id, step_order=primitive.step_order)

        # Poll for resolution
        resolved = self.poll_resolution(execution_id, intervention_id)

        if resolved:
            log.info("intervention_resolved", intervention_id=intervention_id)
            return {"status": "RESOLVED", "intervention_id": intervention_id}
        else:
            log.warning("intervention_timeout", intervention_id=intervention_id)
            return {"status": "TIMEOUT", "intervention_id": intervention_id}

    def _default_create(self, execution_id: str, primitive: Primitive) -> dict:
        """Default stub — in production, calls Spring Boot POST."""
        return {
            "id": f"intervention-{primitive.step_order}",
            "executionId": execution_id,
            "stepOrder": primitive.step_order,
            "status": "PENDING",
        }

    def _default_poll(self, execution_id: str, intervention_id: str) -> bool:
        """Default stub — in production, polls Spring Boot GET."""
        return True  # auto-resolve in simulation
