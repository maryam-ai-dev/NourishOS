# NourishOS

A household food intelligence system that helps homes reduce waste, save money, personalise meal planning across multiple members, and coordinate food replenishment.

## What it does

NourishOS models how food moves through the household over time - what comes in, what gets used, what gets wasted, and what should be planned next.

### Core -- Household Food Intelligence

- Weekly meal planning across household members with dietary fit
- Inventory and stock flow tracking with explicit par levels
- Waste pattern detection and reduction
- Food-flow-aware replenishment with waste-adjusted quantities

### Secondary -- Smart Kitchen Coordination

- Execution planning and task delegation
- Machine vs user step assignment
- Optional smart kitchen execution simulation

## Stack

| Layer | Technology |
|---|---|
| Authority service | Spring Boot (Java 21) |
| Intelligence service | FastAPI (Python 3.12) |
| Frontend | Flutter |
| Database | PostgreSQL 16 |
| Cache | Redis |
| Simulation | Python |

## Project structure

```
nourishos/
├── backend/
│   ├── authority-service/            # Spring Boot -- domain data, persistence, state machines
│   └── intelligence-service/         # FastAPI -- ranking, planning, food flow analysis
├── frontend/                         # Flutter -- household UI
├── robotics/
│   └── simulation/                   # Python -- optional execution simulation
├── contracts/
│   ├── events/                       # Domain event schemas
│   └── openapi/                      # API contracts shared across services
├── docs/
│   ├── architecture/
│   ├── domain/
│   └── decisions/
├── infra/
│   ├── postgres/
│   ├── redis/
│   └── docker-compose.yml
└── scripts/
    ├── seed_demo.sql
    └── run_demo_scenario.py
```

## Architecture

**Spring Boot** owns all durable domain data: households, inventory, meal catalog, execution plans, audit records.

**FastAPI** provides intelligence: meal ranking, weekly planning, food flow analysis, waste detection, substitution, forecasting. FastAPI proposes; Spring Boot persists.

**Flutter** reads from Spring Boot only. Spring Boot enriches responses with Redis-backed state where needed.

**Redis** has exactly three jobs: execution session cache, active intervention state, recommendation cache.

## Running locally

```bash
# Full stack
docker compose up

# Spring Boot
cd backend/authority-service && ./gradlew bootRun

# FastAPI
cd backend/intelligence-service && uvicorn app.main:app --reload --port 8000

# Simulation
cd robotics/simulation && python main.py

# Flutter
cd frontend && flutter run
```
