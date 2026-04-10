# NourishOS — Architecture Layers

## Product Hierarchy

Food intelligence is the core product. Execution simulation is a downstream capability.

```
CORE — household food intelligence
  weekly meal planning across household members
  inventory and stock flow tracking (explicit ParLevel thresholds)
  waste pattern detection and reduction
  food-flow-aware replenishment (waste-adjusted quantities)

DOWNSTREAM — smart kitchen coordination
  execution planning and task delegation
  machine vs user step assignment
  optional smart kitchen execution simulation
```

Never lead with robotics in documentation or code comments.

---

## The Four Layers

```
┌─────────────────────────────────────────────────┐
│  Flutter (frontend)                             │
│  Screens: Home, Planner, Pantry, Cooking,       │
│  Reorders, Household, Insights                  │
│  State source: Spring Boot (Redis-enriched)     │
│  Never reads Redis directly                     │
└──────────────────────┬──────────────────────────┘
                       │ HTTP / REST
┌──────────────────────▼──────────────────────────┐
│  Spring Boot — authority-service (port 8080)    │
│  Owns all durable domain data                   │
│  Persists proposals from FastAPI                │
│  Reads Redis; enriches GET responses            │
│  Writes Redis on execution state transitions    │
└──────┬────────────────────────────┬─────────────┘
       │ FastAPI proposes           │ simulation
       │ Spring persists            │ calls Spring
┌──────▼──────────────┐  ┌──────────▼────────────┐
│  FastAPI            │  │  Robotics Simulation   │
│  intelligence-      │  │  (port 8001)           │
│  service (port 8000)│  │  Capability registry   │
│  Meal ranking       │  │  Workspace model       │
│  Food flow analysis │  │  Task planner          │
│  Nutrition scoring  │  │  Execution controller  │
│  Forecasting        │  │  Intervention manager  │
│  Replenishment opt. │  │  Calls Spring for all  │
│  Execution planning │  │  state transitions     │
│  Explanations       │  │  Never writes Redis    │
└─────────────────────┘  └────────────────────────┘
```

---

## Layer Responsibilities

### Flutter
- Renders household food intelligence: inventory, planning, waste insights, replenishment
- Polls `GET /executions/{id}` for execution state — Spring enriches with Redis data
- Never connects to Redis or FastAPI directly

### Spring Boot
- Single source of truth for all entities (see `docs/domain/entities.md`)
- Calls FastAPI for intelligence proposals; decides what to persist
- Writes and reads Redis for exactly 3 key prefixes (see `docs/decisions/004-redis-three-jobs.md`)
- Enforces all domain invariants (ParLevel, state machines, audit trail)

### FastAPI
- Proposes: ranked meal candidates, weekly plans, execution steps, replenishment quantities
- Fetches `MealOption` catalog from Spring — no local catalog file
- Writes `rec:cache:{householdId}` to Redis after ranking (5-min TTL)
- Never persists durable workflow state

### Robotics Simulation
- Reads `ExecutionPlan` and `ExecutionStep` from Spring
- Calls Spring Boot endpoints for all state transitions (step-complete, intervention, abort)
- Does not write to Redis directly; Spring writes Redis as a consequence of simulation callbacks
- Subsystem hygiene state (`subsystem_state.py`) lives here, not in Spring Boot

---

## Inter-layer Contract Rules

| Rule | Detail |
|------|--------|
| FastAPI proposes, Spring persists | FastAPI returns proposals; Spring decides what to write |
| Spring enriches with Redis | Flutter sees Redis state only via Spring GET responses |
| Simulation calls Spring | All execution state transitions go through Spring endpoints |
| Shared execution schema | `contracts/openapi/execution-step-schema.yaml` — all layers conform |
| MealOption ownership | Spring only; `GET /meal-catalog` is the source; no JSON file in FastAPI |
