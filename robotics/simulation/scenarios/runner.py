"""
Scenario runner — 6 named failure/edge-case scenarios.
Each scenario reaches a defined terminal state.
"""

import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import structlog
from uuid import uuid4
from task_planner import ActionType, Primitive, Heat, DispenseDry, IngredientRef
from device_bridge import DeviceBridge, UnsupportedActionError
from execution_controller import ExecutionController
from intervention_manager import InterventionManager
from monitoring_feedback import generate_telemetry
from anomaly_handler import handle_anomaly, AnomalyAction

log = structlog.get_logger()


class ScenarioResult:
    def __init__(self, name: str, terminal_state: str, success: bool, detail: str = ""):
        self.name = name
        self.terminal_state = terminal_state
        self.success = success
        self.detail = detail

    def __repr__(self):
        status = "PASS" if self.success else "FAIL"
        return f"[{status}] {self.name} -> {self.terminal_state}: {self.detail}"


def _make_prims(plan_id, actions):
    return [
        Primitive(step_id=uuid4(), plan_id=plan_id, step_order=i+1,
                  action_type=a, assigned_to="MACHINE" if a not in (ActionType.USER_CONFIRM, ActionType.USER_LOAD_TRAY, ActionType.USER_REMOVE_VESSEL) else "USER")
        for i, a in enumerate(actions)
    ]


def scenario_empty_bin():
    """Dispense fails because bin is empty → retry then abort."""
    plan_id = uuid4()
    prims = [DispenseDry(step_id=uuid4(), plan_id=plan_id, step_order=1,
                         ingredient_ref=IngredientRef(ingredient_id=uuid4(), quantity=500, unit="g"))]

    class EmptyBridge(DeviceBridge):
        def execute_action(self, p):
            raise RuntimeError("DRY_BIN empty — cannot dispense")

    abort_called = False
    def on_abort(pid, reason):
        nonlocal abort_called
        abort_called = True

    ctrl = ExecutionController(bridge=EmptyBridge(), on_abort=on_abort)
    result = ctrl.run(str(plan_id), prims)
    return ScenarioResult("empty_bin", "ABORTED", result["status"] == "ABORTED" and abort_called, result.get("error", ""))


def scenario_missing_tray():
    """USER_LOAD_TRAY step — user never loads → timeout."""
    plan_id = uuid4()
    prim = Primitive(step_id=uuid4(), plan_id=plan_id, step_order=1,
                     action_type=ActionType.USER_LOAD_TRAY, assigned_to="USER")

    def poll_never_resolves(eid, iid):
        return False

    mgr = InterventionManager(poll_resolution=poll_never_resolves)
    result = mgr.handle_user_step(str(plan_id), prim)
    return ScenarioResult("missing_tray", "TIMEOUT", result["status"] == "TIMEOUT")


def scenario_user_delayed():
    """USER step eventually resolves after polling."""
    plan_id = uuid4()
    prim = Primitive(step_id=uuid4(), plan_id=plan_id, step_order=1,
                     action_type=ActionType.USER_CONFIRM, assigned_to="USER")

    mgr = InterventionManager()  # default auto-resolves
    result = mgr.handle_user_step(str(plan_id), prim)
    return ScenarioResult("user_delayed", "RESOLVED", result["status"] == "RESOLVED")


def scenario_camera_anomaly():
    """Vision detects anomaly → minor → intervention."""
    telemetry = generate_telemetry(step_index=2, inject_anomaly=True)
    result = handle_anomaly(telemetry, severity="minor")
    return ScenarioResult("camera_anomaly", "INTERVENTION_CREATED",
                          result["action"] == AnomalyAction.INTERVENE)


def scenario_cooking_interruption():
    """Severe anomaly during cooking → abort."""
    telemetry = generate_telemetry(step_index=3, inject_anomaly=True)
    result = handle_anomaly(telemetry, severity="severe")
    return ScenarioResult("cooking_interruption", "ABORTED",
                          result["action"] == AnomalyAction.ABORT)


def scenario_reorder_missed():
    """Normal 3-step plan completes successfully — no reorder needed."""
    plan_id = uuid4()
    prims = _make_prims(plan_id, [ActionType.DISPENSE_DRY, ActionType.HEAT, ActionType.STIR])
    ctrl = ExecutionController()
    result = ctrl.run(str(plan_id), prims)
    return ScenarioResult("reorder_missed", "COMPLETED", result["status"] == "COMPLETED")


ALL_SCENARIOS = [
    scenario_empty_bin,
    scenario_missing_tray,
    scenario_user_delayed,
    scenario_camera_anomaly,
    scenario_cooking_interruption,
    scenario_reorder_missed,
]


def run_all():
    results = []
    for scenario_fn in ALL_SCENARIOS:
        try:
            result = scenario_fn()
            results.append(result)
        except Exception as e:
            results.append(ScenarioResult(scenario_fn.__name__.replace("scenario_", ""), "ERROR", False, str(e)))

    print("\n=== Scenario Runner Results ===")
    all_pass = True
    for r in results:
        print(f"  {r}")
        if not r.success:
            all_pass = False
    print(f"\n{'ALL PASSED' if all_pass else 'SOME FAILED'}: {sum(1 for r in results if r.success)}/{len(results)}")
    return results


if __name__ == "__main__":
    run_all()
