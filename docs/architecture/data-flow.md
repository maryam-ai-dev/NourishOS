# NourishOS — Data Flow

## Core Rule

**FastAPI proposes. Spring Boot persists and confirms.**

FastAPI never owns durable workflow state. Every proposal FastAPI returns is reviewed and persisted by Spring Boot. This applies to meal candidates, execution plans, replenishment suggestions, and weekly schedules.

---

## Meal Request to Outcome

```
POST /planning/meal-requests  (Spring Boot)
  MealRequest created → state: PENDING

  Spring calls FastAPI POST /recommendation/rank
    FastAPI calls GET /meal-catalog (Spring Boot — no local file)
    FastAPI applies food-flow context:
      RECURRING_WASTE penalties, reliability scores, dietary fit
    Returns ranked candidates
    FastAPI writes rec:cache:{householdId} to Redis (5-min TTL)

  Spring persists top 3 as MealPlanCandidates
  MealRequest state → PLANNING

  User selects meal
  PUT /planning/meal-plans/{id}/select
    Invariant: selectedMealOptionId must be one of the candidates
  MealRequest state → PLANNED

  Spring calls FastAPI POST /execution/plan
    FastAPI runs delegation (all steps assigned MACHINE or USER)
    FastAPI proposes ExecutionPlan + ExecutionSteps (shared schema)
  Spring persists ExecutionPlan and steps

  POST /executions/{id}/start
    Spring writes exec:session:{id} to Redis
    Spring calls simulation POST /run
  MealRequest state → EXECUTING
```

## Execution Flow

```
Simulation processes each step:

  MACHINE step:
    Simulation executes action
    Calls Spring POST /executions/{id}/steps/{stepId}/complete
    Spring updates exec:session:{id} in Redis

  USER step:
    Simulation calls Spring to create InterventionRequest
    Spring writes exec:intervention:{id} to Redis
    GET /executions/{id} response includes intervention state (enriched by Spring)
    Flutter reads enriched response → shows user modal
    User resolves → Spring deletes exec:intervention:{id}
    Simulation polls Spring → resumes on RESOLVED

  On completion:
    LotAllocationService selects lots (opened first, nearest expiry second)
    InventoryAdjustmentService records DEDUCTION per lot
    ConsumptionEvent created per ingredient (source: PLANNED_MEAL)
    MealOutcomeEvent COMPLETED created
    UsageRecordService.recompute() for affected ingredients
    FoodFlowSnapshot regenerated
    Spring deletes exec:session:{id} from Redis
    MealRequest state → COMPLETE
```

## Post-Execution Feedback Loop

```
FastAPI forecasting runs:
  Reads ConsumptionEvents + WasteEvents via Spring
  Detects RECURRING_WASTE (2+ WasteEvents in 4 weeks per ingredient)
  Generates ReplenishmentSuggestions:
    adjustedForWaste=true + reduced quantity for RECURRING_WASTE ingredients
  POSTs suggestions to Spring (Spring persists)

Spring generates HouseholdInsight rows from FastAPI flow insight output
FoodFlowSnapshot updated with new waste ratio, top wasted/consumed
```

## Food Flow as Continuous Signal

The food flow layer feeds back into every subsequent meal request:

```
WasteEvent / ConsumptionEvent (raw truth)
  → IngredientUsageRecord recomputed (rolling 4-week totals)
  → FoodFlowSnapshot updated (waste ratio, top items)
  → FastAPI WastePattern detection (RECURRING_WASTE runtime flag)
    → Penalises RECURRING_WASTE ingredients in next ranking call
    → Excludes RECURRING_WASTE as substitution target
    → Reduces replenishment quantity (adjustedForWaste=true)
  → Stockout forecasting against ParLevel minimums
  → ReplenishmentSuggestions with waste-adjusted quantities
```

---

## Redis Interaction Points

| Moment | Key written | Written by | Read by |
|--------|-------------|------------|---------|
| After ranking | `rec:cache:{householdId}` | FastAPI | Spring (enriches responses) |
| Execution start | `exec:session:{executionId}` | Spring | Spring (enriches GET /executions/{id}) |
| Intervention created | `exec:intervention:{executionId}` | Spring | Spring (enriches GET /executions/{id}) |
| Terminal state reached | Keys deleted | Spring | — |

Flutter reads enriched Spring responses. Flutter never connects to Redis.

---

## State Machines

### MealRequest
```
PENDING → PLANNING → PLANNED → EXECUTING → COMPLETE
```

### ExecutionPlan
```
PENDING → IN_PROGRESS → COMPLETED
                      → PAUSED
                      → FAILED
                      → ABORTED
```
Terminal states (COMPLETED, FAILED, ABORTED) cannot transition further.
