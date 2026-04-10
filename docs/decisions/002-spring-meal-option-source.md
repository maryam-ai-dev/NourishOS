# ADR 002 — Spring Boot Owns MealOption; No Local Catalog in FastAPI

**Date:** 2026-04-10
**Status:** Accepted

---

## Context

NourishOS requires a catalog of meal options that FastAPI uses for ranking, planning, nutrition scoring, substitution, and execution planning. Two storage approaches were evaluated:

1. A local JSON file (`meal_options.json`) in the FastAPI service, loaded at startup
2. `MealOption` as a Spring Boot entity, fetched by FastAPI via HTTP

FastAPI needs meal data to function, but it also proposes plans and execution steps that reference those meals. If FastAPI owns the catalog, there is no single source of truth — Spring and FastAPI could diverge on what meals exist, what ingredients they require, and what scoring parameters apply.

---

## Decision

`MealOption` is a Spring Boot entity. Spring Boot is the single source of truth. There is no `meal_options.json` file anywhere in the FastAPI service.

FastAPI fetches the meal catalog at runtime by calling `GET /meal-catalog` on Spring Boot. The client method is `get_meal_catalog()` in `app/clients/authority_client.py`.

---

## Rationale

**Durability.** Meal options are durable domain data. They have `ingredientRefs`, scoring metadata, and are referenced by `MealPlan`, `MealPlanCandidate`, `MealOutcomeEvent`, and `ExecutionPlan`. This data belongs in a database with migration history, not a flat file.

**Consistency.** Spring Boot persists `MealPlanCandidates` with `mealOptionId` foreign keys. If FastAPI held a divergent catalog, those IDs could reference meals Spring does not recognise. Having one owner eliminates this class of bug entirely.

**Invariant enforcement.** The domain invariant `selectedMealOptionId must be one of the MealPlanCandidate.mealOptionIds` is only enforceable if Spring owns the authoritative list. FastAPI ranking and selection both operate against the same Spring-owned set.

**ingredientRefs schema.** Every `MealOption.ingredientRefs` entry conforms to a typed JSONB schema (ingredientId, baseQuantity, unit, optional, substitutable). Spring validates this on write. A local JSON file would bypass that validation.

---

## Implementation

- `MealOption` is persisted in Spring Boot via Flyway migration `V4__planning.sql`
- `GET /meal-catalog` returns all active meal options with their `ingredientRefs`
- FastAPI calls this endpoint before every ranking or planning operation
- The FastAPI `authority_client.py` handles caching considerations; the catalog is not re-fetched on every individual score call within a single request cycle
- The absence of `meal_options.json` in the FastAPI repository is verified by a test in Sprint 9.3

---

## Consequences

FastAPI ranking, planning, nutrition scoring, and substitution all depend on `GET /meal-catalog` being available. If Spring Boot is unreachable, these operations fail. This is acceptable: FastAPI is an intelligence layer that proposes against Spring-owned data; it does not operate autonomously against a local copy.

Adding a new meal to the catalog requires a Spring Boot write (`POST /meal-catalog`). FastAPI does not have an endpoint for creating meal options.

---

## Rejected Alternatives

**Local `meal_options.json` in FastAPI:** Rejected because it creates a second source of truth, bypasses JSONB schema validation, makes `ingredientRefs` inconsistency possible, and breaks the FastAPI-proposes-Spring-persists contract.

**FastAPI-owned meal database:** Rejected because MealOption is a durable domain entity with foreign key relationships to planning and execution entities that Spring owns. Splitting the persistence layer would require cross-service joins or eventual consistency handling for what is a simple catalog read.
