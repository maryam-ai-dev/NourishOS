# ADR 004 — Redis Has Exactly Three Key Prefixes

**Date:** 2026-04-10
**Status:** Accepted

---

## Context

Redis is available in the NourishOS stack and could be used for many purposes: caching any Spring Boot response, storing derived summaries, session management, pub/sub for inter-service messaging, feature flags, or rate limiting.

Without explicit constraints, Redis usage tends to expand incrementally. Each addition seems low-risk in isolation, but the cumulative effect is a cache layer with unclear boundaries, poorly defined TTL semantics, and implicit dependencies that are hard to reason about during debugging or recovery.

---

## Decision

Redis has exactly three jobs in NourishOS. No additional key prefixes may be introduced without updating both this document and `infra/redis/README.md`.

| Key prefix | Job | Written by | Deleted by | TTL |
|------------|-----|------------|------------|-----|
| `exec:session:{executionId}` | Execution session state | Spring on execution start | Spring on terminal state | None |
| `exec:intervention:{executionId}` | Active intervention state | Spring on InterventionRequest create | Spring on resolve | None |
| `rec:cache:{householdId}` | Latest recommendation result | FastAPI after ranking | Auto-expiry | 5 minutes |

---

## What Each Key Does

### `exec:session:{executionId}`
Written when `POST /executions/{id}/start` is called. Contains the current session state (step progress, in-progress step, timestamps). Spring reads this key and includes its contents in `GET /executions/{id}` responses. Deleted when the ExecutionPlan reaches a terminal state (COMPLETED, FAILED, ABORTED).

### `exec:intervention:{executionId}`
Written when Spring creates an `InterventionRequest` (triggered by simulation calling the Spring intervention endpoint). Contains the intervention payload (type, message, step reference). Spring includes this in `GET /executions/{id}` responses so Flutter can render the user modal. Deleted when Spring resolves the intervention.

### `rec:cache:{householdId}`
Written by FastAPI immediately after returning ranked meal candidates. Contains the ranked result for the household. TTL of 5 minutes ensures stale recommendations do not persist across food flow state changes. Spring reads this to enrich household-level recommendation endpoints.

---

## Rationale

**Flutter never reads Redis directly.** Flutter polls `GET /executions/{id}` on Spring Boot. Spring reads the Redis keys and includes their state in the HTTP response. This boundary is non-negotiable: if Flutter read Redis directly, any change to key structure or TTL semantics would require coordinated frontend and infrastructure changes.

**Simulation never writes Redis directly.** The simulation calls Spring Boot endpoints for all state transitions. Spring writes Redis as a consequence of those calls. This keeps all Redis write logic in one place (Spring Boot) and makes key lifecycle deterministic.

**FastAPI writes only one key.** FastAPI is a stateless intelligence service. It writes `rec:cache` as a performance optimisation (avoiding re-ranking on every GET). It does not write execution session or intervention state — those belong to Spring as the authority on durable workflow state.

**No derived summaries in Redis.** `FoodFlowSnapshot` and `IngredientUsageRecord` are stored in PostgreSQL, not Redis. They are recomputable and written by Spring. Caching them in Redis would create a second location to invalidate and a possible divergence from the database state.

---

## Consequences

Any engineer proposing a new Redis key must update this ADR and `infra/redis/README.md` as part of the same change. The table in the README is the canonical reference; this document explains the reasoning.

If a use case appears that seems to require a new Redis key, first evaluate whether it can be served by the existing database, by one of the three existing keys, or by a short TTL on an existing endpoint response. Redis expansion is a deliberate architectural decision, not a default caching reflex.

---

## Rejected Alternatives

**Caching all Spring GET responses in Redis:** Rejected because it creates implicit cache invalidation requirements for every entity mutation, and obscures which data is authoritative at any point in time.

**Storing FoodFlowSnapshot in Redis:** Rejected because the snapshot is a derived summary that belongs in PostgreSQL alongside the raw events it summarises. Redis TTL semantics are unsuitable for a summary that must be consistent with database state.

**Using Redis pub/sub for Spring-FastAPI messaging:** Rejected because FastAPI is called synchronously by Spring via HTTP. Adding an async messaging layer would introduce ordering complexity for what is currently a simple request-response contract.
