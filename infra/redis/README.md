# NourishOS — Redis Key Reference

Redis has exactly **3 jobs** in NourishOS. Do not add keys without updating this document and the architectural decisions table in CLAUDE.md.

## Keys

| Key | Job | Written by | Read by | Deleted by | TTL |
|---|---|---|---|---|---|
| `exec:session:{executionId}` | Execution session state | Spring Boot on `POST /executions/{id}/start` | Spring Boot on `GET /executions/{id}` | Spring Boot on terminal state (COMPLETED, FAILED, ABORTED) | None |
| `exec:intervention:{executionId}` | Active intervention state | Spring Boot on InterventionRequest create | Spring Boot on `GET /executions/{id}` | Spring Boot on intervention resolve | None |
| `rec:cache:{householdId}` | Latest meal recommendation cache | FastAPI on `POST /recommendation/rank` | Spring Boot (enriches recommendation response) | Auto-expiry | 5 min |

## Rules

- **Flutter never reads Redis directly.** All execution and intervention state flows through `GET /executions/{id}`, which Spring Boot enriches with Redis data.
- **Simulation never writes Redis directly.** Simulation calls Spring Boot endpoints for state transitions; Spring Boot writes Redis as a consequence.
- **FastAPI only writes `rec:cache`.** The two `exec:` keys are exclusively owned by Spring Boot.
- **No additional keys** may be added without updating this table, the Redis architectural decision, and the `RedisConfig` class.
