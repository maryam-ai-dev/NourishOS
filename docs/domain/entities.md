# NourishOS — Domain Entities

## Classification

Entities are classified as one of three types:

- **Raw truth** — source of truth; never derived from other records; never replaced by summaries
- **Derived summary** — recomputable from raw truth; stored for read performance; never authoritative
- **Runtime output** — produced by FastAPI during analysis; not persisted as a DB entity

---

## Household

| Entity | Type | Notes |
|--------|------|-------|
| Household | Raw truth | Top-level aggregate; owns members and settings |
| HouseholdSettings | Raw truth | Budget, spend threshold, preferences |
| HouseholdMember | Raw truth | Age group, effort sensitivity, dietary restrictions, protein goal |
| MemberPreferenceProfile | Raw truth | Per-member dietary rules, nutrition goals, meal preferences (JSONB) |
| DietaryRule | Raw truth | Allergen or exclusion constraint per member |
| NutritionGoal | Raw truth | Per-member protein and nutrition targets |

---

## Inventory

| Entity | Type | Notes |
|--------|------|-------|
| Ingredient | Raw truth | Named food item; `defaultUnit`, `perishabilityClass`, `category` |
| IngredientLot | Raw truth | A purchase. Status: ACTIVE / DEPLETED / ARCHIVED. No hard deletes |
| StorageLocation | Raw truth | FRIDGE, FREEZER, PANTRY, COUNTER, OPENED |
| InventoryAdjustment | Raw truth | Written on every quantity change. Types: PURCHASE / CONSUMPTION / WASTE / CORRECTION / DEDUCTION |
| ExpiryRecord | Raw truth | Freshness per lot: FRESH / NEAR_EXPIRY (<=3 days) / EXPIRED |
| ParLevel | Raw truth | Per-household, per-ingredient: `preferredQuantity`, `minimumQuantity`. Only valid low-stock definition |
| StockSnapshot | Derived summary | Point-in-time stock totals; recomputable from lots and adjustments |

---

## Planning

| Entity | Type | Notes |
|--------|------|-------|
| MealOption | Raw truth | Spring Boot only. Single source of truth. `ingredientRefs` JSONB with typed schema |
| MealRequest | Raw truth | Planning trigger. Includes duplicate guard (5-min window per household) |
| MealConstraint | Raw truth | Servings invariant and policy constraints for a request |
| MealPlan | Raw truth | Linked to candidates; `selectedMealOptionId` must be one of the candidates |
| MealPlanCandidate | Raw truth | Ranked option; `scoreBreakdown` JSONB. Top 3 persisted by Spring |
| ServingProfile | Raw truth | Serving size and member count for a plan |
| WeeklyMealSchedule | Raw truth | Confirmed weekly schedule. No duplicate (dayOfWeek, mealType) pairs |
| ScheduledMealSlot | Raw truth | A single meal slot within a weekly schedule |

---

## Food Flow

| Entity | Type | Notes |
|--------|------|-------|
| ConsumptionEvent | Raw truth | Food consumed. Source: PLANNED_MEAL / UNPLANNED / SNACK only |
| WasteEvent | Raw truth | Food wasted. Separate table from ConsumptionEvent. Reason: EXPIRED / OVERCOOKED / DISCARDED / LEFTOVER_UNUSED |
| MealOutcomeEvent | Raw truth | Meal result: COMPLETED / ABANDONED / SUBSTITUTED / PARTIALLY_COMPLETED |
| IngredientUsageRecord | Derived summary | Rolling 4-week totals per ingredient. Recomputable from raw events |
| FoodFlowSnapshot | Derived summary | Waste ratio, top wasted/consumed. Stored for UI reads. Recomputable |
| HouseholdInsight | Raw truth | Persisted rows from FastAPI insight output; written by Spring on receipt |

**Critical distinction:** WasteEvent and ConsumptionEvent are never conflated. They have separate tables and separate domain semantics.

---

## FastAPI Runtime Outputs (not DB entities)

| Concept | Type | Notes |
|---------|------|-------|
| WastePattern | Runtime output | RECURRING_WASTE flag when >=2 WasteEvents in 4 weeks. Not a DB column |
| StockFlowSummary | Runtime output | Net flow, over-bought, under-supplied per ingredient |
| MealReliabilityInsight | Runtime output | Per-meal completion rate. LOW_RELIABILITY below 50% |

---

## Replenishment and Policy

| Entity | Type | Notes |
|--------|------|-------|
| ReplenishmentSuggestion | Raw truth | `adjustedForWaste=true` when quantity reduced due to RECURRING_WASTE. Proposed by FastAPI, persisted by Spring |
| ReplenishmentRequest | Raw truth | Bundled suggestions submitted for approval |
| ApprovalDecision | Raw truth | User or policy decision on a replenishment request |
| PolicySet | Raw truth | Household-level policy configuration |
| PolicyRule | Raw truth | Individual rule within a PolicySet |
| PolicyDecision | Raw truth | Written before any autonomous action returns. ALLOW / BLOCK / REQUIRE_APPROVAL |

---

## Execution

| Entity | Type | Notes |
|--------|------|-------|
| ExecutionPlan | Raw truth | State machine: PENDING / IN_PROGRESS / COMPLETED / PAUSED / FAILED / ABORTED |
| ExecutionStep | Raw truth | One action. Assigned to MACHINE or USER. Conforms to shared schema |
| InterventionRequest | Raw truth | Pause requiring user action. One unresolved per step at a time |

---

## Audit

| Entity | Type | Notes |
|--------|------|-------|
| AuditRecord | Raw truth | Immutable record written for every key event. Never deleted |

---

## Derived Summary Rule

`IngredientUsageRecord` and `FoodFlowSnapshot` are stored for read performance but are never the authoritative source for any business decision. If they conflict with raw events, raw events win. Both can be regenerated at any time by replaying raw events.
