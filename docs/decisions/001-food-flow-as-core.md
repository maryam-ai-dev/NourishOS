# ADR 001 — Food Flow Intelligence Is the Core Product

**Date:** 2026-04-10
**Status:** Accepted

---

## Context

NourishOS integrates food management, meal planning, smart kitchen execution, and robotics simulation into one system. Early prototypes described the product primarily through its smart kitchen and execution capabilities, which risked positioning it as a robotics product with food as a secondary concern.

The actual household value is not execution automation. It is the intelligence that reduces food waste, personalises planning, improves replenishment decisions, and builds a persistent model of how food moves through the household over time.

---

## Decision

Food flow intelligence is the core product. Execution simulation is a downstream capability.

The primary product hierarchy is:

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

---

## What "Food Flow" Means

Food flow is the model of how food moves through the household over time:

- **Inflow:** purchases, deliveries (IngredientLot, InventoryAdjustment PURCHASE)
- **Outflow:** consumption (ConsumptionEvent), waste (WasteEvent)
- **State:** current inventory per storage location, expiry status, par levels
- **Pattern:** RECURRING_WASTE detection, over-bought ingredients, stockout risk
- **Feedback:** waste history feeds back into the next replenishment and meal ranking cycle

The `FoodFlowSnapshot` and `IngredientUsageRecord` are derived summaries of this continuous model. The raw events (ConsumptionEvent, WasteEvent, MealOutcomeEvent, InventoryAdjustment) are the authoritative record.

---

## Consequences

**Documentation and code comments** must not lead with robotics or execution. The product introduction always leads with waste reduction, personalised planning, and smarter replenishment.

**The V1 useful checkpoint** is reachable without any smart kitchen hardware: weekly planning, inventory with ParLevel thresholds, food flow history, RECURRING_WASTE detection, stockout forecasting, and insights. Phases 13–14 add execution; they are not required to deliver household value.

**FastAPI food flow services** (waste pattern detection, stock flow model, meal reliability, replenishment optimiser) are first-class services, not supporting infrastructure for the execution layer.

**The feedback loop is non-optional.** Every meal execution must produce ConsumptionEvents, WasteEvents, and MealOutcomeEvents. These feed back into the next planning and replenishment cycle. A system that executes meals without capturing food flow outcomes is incomplete.

**Ranking uses food flow context.** FastAPI applies RECURRING_WASTE penalties, perishable sequencing, and reliability scores when ranking meal candidates. Ranking is not a simple preference match — it incorporates the household's actual food history.

---

## Rejected Alternatives

**"Execution-first" framing:** Describing NourishOS as a smart kitchen automation product with food management features. Rejected because it misrepresents the core value and deprioritises the intelligence layer that works without any smart kitchen hardware.

**"Inventory management" framing:** Describing NourishOS as an inventory tracker with meal planning. Rejected because it omits the waste detection, reliability analysis, and demand forecasting that make the system adaptive over time.
