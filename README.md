# 🥗 NourishOS

> A household food intelligence system that reduces waste, personalises meal planning, and coordinates smart replenishment -- with a long-term vision as the intelligence layer for smart kitchen appliances.

![Demo](DEMO_GIF_PLACEHOLDER)

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Flutter](https://img.shields.io/badge/Flutter-3.x-02569B.svg)](https://flutter.dev/)

## What it does

NourishOS helps households stop wasting food. It learns what everyone likes, plans meals around what is already in the fridge, and reorders only what you actually need -- with quantities reduced for items you frequently throw away. Scan a receipt to populate your pantry, generate a week of meals in one tap, and let a shopping agent build your supermarket basket.

## Demo

![Demo](DEMO_GIF_PLACEHOLDER)

- Scan receipt to auto-populate pantry
- Generate personalised weekly meal plan in one tap
- Approve reorders with waste-adjusted quantities
- Track savings from reduced food waste

## Why I built this

I wanted to understand what a truly intelligent household system looks like beyond a simple CRUD app. Most household apps are just databases with a nicer interface -- you tell them what food you have, they show it back to you in a list. That is not intelligence. I wanted to build a system that actually reasons about food as it moves through a home: what was bought, what was used, what was wasted, and what should come next. The food flow model and waste-adjusted replenishment logic come out of that thinking.

The smart kitchen vision is where this gets interesting. The backend execution module and lot allocation system are already designed for appliance integration -- ExecutionPlan steps include MACHINE versus USER assignment, LotAllocationService picks the exact lot an appliance should draw from, and the intervention flow handles the handoff between automated and manual steps. Today the app runs on a phone. The same domain model is ready to drive a Samsung ThinQ oven, an LG fridge, or a bespoke countertop cooker -- which is where I think the real value is.

## Architecture

```
Flutter (mobile)
    |
Spring Boot (authority service) -- source of truth for all domain state
    |                    |
FastAPI              Redis
(intelligence)    (session + recommendation cache)
    |
PostgreSQL 16 + pgvector
```

### Key architectural decisions

- FastAPI proposes, Spring Boot persists -- intelligence layer never owns durable state
- LotAllocationService is the single path for all stock deductions -- opened-first, nearest-expiry-fallback strategy
- ParLevel entities define low stock per household -- no hardcoded percentages anywhere in the system
- Redis has exactly 3 jobs defined upfront and documented -- nothing added without updating infra/redis/README.md

## Key engineering challenges

- Lot allocation ordering -- opened-first, nearest-expiry-fallback across unit-converted quantities. LotAllocationService is the only deduction path
- FastAPI/Spring contract -- intelligence layer proposes, Spring Boot persists. FastAPI never owns durable workflow state. Frontend calls Spring Boot only
- Waste-adjusted replenishment -- adjustedForWaste: true requires minimum 2 WasteEvent rows in last 4 weeks, validated server-side before persisting
- Redis discipline -- exactly 3 jobs, documented in infra/redis/README.md. Nothing added without updating documentation
- Unit conversion -- UnitConversionService handles all cross-unit operations. Incompatible category conversions throw IncompatibleUnitsException
- Execution state machine -- ExecutionPlan cannot transition from terminal state. One step IN_PROGRESS at a time. Cannot reach COMPLETED with unresolved InterventionRequest

## Stack

| Layer | Technology |
|---|---|
| Mobile frontend | Flutter 3.x, Riverpod, go_router |
| Authority service | Spring Boot 3.x, Java 21, JPA, Flyway |
| Intelligence service | FastAPI, Python 3.12, pgvector |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |

## Features

- Receipt scanning via OCR -- auto-populates pantry without manual entry
- Personalised weekly meal planning based on household preferences, allergies, and favourite foods
- Waste-adjusted replenishment -- quantities reduced automatically for frequently wasted items
- Online shopping agent -- builds supermarket basket from approved suggestions
- In-store shopping list -- categorised, tap to tick off as you go
- Household member onboarding -- 7-step flow covering dietary preferences, allergies, and favourites
- Weekly savings tracking -- waste savings vs previous week
- Budget management -- spend vs limits per category

## Running locally

```bash
# Start infrastructure
docker compose up

# Start Spring Boot
cd backend/authority-service
./mvnw spring-boot:run

# Start FastAPI
cd backend/intelligence-service
uvicorn app.main:app --reload

# Start Flutter
cd frontend
flutter run
```

## Roadmap

- [ ] Supermarket OAuth integration (Tesco, Sainsbury's, Ocado)
- [ ] Samsung/LG ThinQ API integration
- [ ] Smart fridge pairing via Bluetooth
- [ ] B2B licensing API for appliance manufacturers

## License

Apache 2.0

---

*Built by [Maryam Yousuf](https://github.com/maryam-ai-dev)*
