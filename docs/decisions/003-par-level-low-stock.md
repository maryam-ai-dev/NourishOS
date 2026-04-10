# ADR 003 — ParLevel Entity Is the Only Valid Low-Stock Definition

**Date:** 2026-04-10
**Status:** Accepted

---

## Context

A low-stock alert requires a threshold. Several approaches are commonly used in inventory systems:

1. A hardcoded percentage of a reference quantity (e.g. "below 20% of last purchase")
2. A category-level default (e.g. "all dry goods alert below 500g")
3. A per-item entity storing explicit quantities per household

NourishOS serves multiple households with different sizes, dietary needs, and consumption patterns. A percentage or category default cannot account for the fact that one household uses 2kg of rice per week and another uses 200g.

---

## Decision

`ParLevel` is the only valid definition of low-stock in NourishOS. No percentage fallbacks. No category defaults.

A `ParLevel` is a per-household, per-ingredient entity with two fields:

- `minimumQuantity` — the threshold below which the ingredient is considered low-stock
- `preferredQuantity` — the target quantity after replenishment

The invariant `minimumQuantity <= preferredQuantity` is enforced at write time in `ParLevelService`.

An ingredient without a `ParLevel` cannot appear in the low-stock list, regardless of its current quantity.

---

## Rationale

**Household specificity.** A household of one person and a household of six have fundamentally different threshold requirements for the same ingredient. No static rule can capture this; only explicit per-household configuration can.

**Replenishment precision.** `ReplenishmentSuggestion` quantities are derived from `ParLevel.preferredQuantity` minus current stock. This calculation is only possible with explicit per-ingredient preferred quantities. A percentage-based threshold cannot produce a meaningful restock quantity.

**Forecasting accuracy.** Stockout forecasting (`POST /forecasting/stockout`) predicts when stock will drop below `ParLevel.minimumQuantity` given consumption rate. This requires an exact minimum, not an approximation.

**Waste adjustment.** For RECURRING_WASTE ingredients, the replenishment optimiser reduces `suggestedQuantity` and sets `adjustedForWaste=true`. This adjustment is relative to the ParLevel preferred quantity — it has no meaning without an explicit baseline.

**No ambiguity.** A percentage-based system requires defining a reference quantity (last purchase? average purchase? initial stock?) and handling the case where that reference is missing or stale. ParLevel eliminates this ambiguity: the minimum is what the household says it is.

---

## Implementation

- `ParLevel` is persisted via Flyway migration `V3__inventory.sql`
- `GET /inventory/low-stock` returns only ingredients where current total active quantity < `ParLevel.minimumQuantity`
- `PUT /households/{id}/par-levels/{ingredientId}` enforces the `minimumQuantity <= preferredQuantity` invariant and rejects writes that violate it
- `UnitConversionService` ensures `ParLevel.unit` is compatible with `Ingredient.defaultUnit`
- Flutter's Pantry screen shows three signal states relative to ParLevel: above preferred (green), between minimum and preferred (amber), below minimum (red)

---

## Consequences

Every ingredient that should generate low-stock alerts must have a `ParLevel` configured. The system does not infer thresholds. Onboarding a new household requires setting ParLevels for the ingredients that matter to that household.

This is intentional: an alert without a meaningful threshold is noise. The ParLevel configuration step is part of household setup.

---

## Rejected Alternatives

**Percentage of last purchase:** Rejected because it produces different thresholds depending on purchase batch size, cannot produce a deterministic restock quantity, and fails when purchase history is empty.

**Category defaults:** Rejected because they cannot reflect household-specific consumption rates, and silently produce misleading alerts for households whose usage differs from the assumed default.

**Ingredient-level global minimum:** Rejected because it is not household-specific. A global minimum for "rice" is meaningless when different households consume radically different quantities.
