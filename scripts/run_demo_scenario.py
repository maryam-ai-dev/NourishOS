"""
NourishOS Headless End-to-End Scenario.
Runs the full system flow and prints a trace.

Requires: Spring Boot (8080), FastAPI (8000), Redis, Postgres running.
"""

import sys
import httpx
import json
from uuid import uuid4

BASE_SPRING = "http://localhost:8080"
BASE_FASTAPI = "http://localhost:8000"

def step(name):
    print(f"\n--- {name} ---")

def main():
    client = httpx.Client(timeout=10.0)
    household_id = "a1000000-0000-0000-0000-000000000001"

    step("1. Health checks")
    try:
        r = client.get(f"{BASE_SPRING}/actuator/health")
        print(f"  Spring Boot: {r.json().get('status', 'unknown')}")
    except Exception as e:
        print(f"  Spring Boot: UNREACHABLE ({e})")
        print("  Cannot proceed without Spring Boot. Exiting.")
        sys.exit(1)

    try:
        r = client.get(f"{BASE_FASTAPI}/health")
        print(f"  FastAPI: {r.json().get('status', 'unknown')}")
    except Exception as e:
        print(f"  FastAPI: UNREACHABLE ({e})")
        print("  Cannot proceed without FastAPI. Exiting.")
        sys.exit(1)

    step("2. Food flow analysis (FastAPI)")
    try:
        r = client.post(f"{BASE_FASTAPI}/foodflow/analyze", json={"householdId": household_id})
        data = r.json()
        print(f"  Consumption profile: {len(data.get('consumptionProfile', {}).get('topIngredients', []))} ingredients")
        print(f"  Waste ratio: {data.get('wastePattern', {}).get('wasteRatio', 'N/A')}")
        print(f"  Over-bought: {len(data.get('stockFlowSummary', {}).get('overBoughtIngredients', []))}")
    except Exception as e:
        print(f"  Error: {e}")

    step("3. Meal ranking (FastAPI via cache)")
    try:
        r = client.post(f"{BASE_FASTAPI}/recommendation/rank", json={
            "household_id": household_id, "max_results": 3,
        })
        ranked = r.json().get("ranked", [])
        print(f"  Ranked {len(ranked)} meals")
        for m in ranked[:3]:
            print(f"    - {m.get('meal_name', '?')}: {m.get('composite_score', 0):.4f}")
    except Exception as e:
        print(f"  Error: {e}")

    step("4. Weekly planning (FastAPI)")
    try:
        r = client.post(f"{BASE_FASTAPI}/planning/weekly", json={
            "householdId": household_id, "weekStartDate": "2026-04-13",
        })
        slots = r.json().get("slots", [])
        score = r.json().get("wasteReductionScore", 0)
        print(f"  {len(slots)} slots planned, waste reduction score: {score:.2f}")
    except Exception as e:
        print(f"  Error: {e}")

    step("5. Forecasting (FastAPI)")
    try:
        r = client.post(f"{BASE_FASTAPI}/forecasting/stockout", json={
            "householdId": household_id,
            "stocks": [
                {"ingredientId": str(uuid4()), "currentQuantity": 50, "unit": "g",
                 "avgWeeklyUsage": 100, "parLevelMinimum": 30},
            ],
        })
        predictions = r.json()
        print(f"  {len(predictions)} stockout predictions")
        for p in predictions:
            print(f"    - urgency: {p.get('urgency', '?')}, days: {p.get('daysUntilStockout', '?')}")
    except Exception as e:
        print(f"  Error: {e}")

    step("6. Replenishment score (FastAPI)")
    try:
        r = client.post(f"{BASE_FASTAPI}/foodflow/replenishment-score", json={
            "householdId": household_id,
            "ingredientId": str(uuid4()),
            "proposedQuantity": 500,
            "isRecurringWaste": True,
            "wasteRatio": 0.25,
        })
        data = r.json()
        print(f"  Score: {data.get('score', '?')}, adjusted: {data.get('adjustedQuantity', '?')}")
        print(f"  Reasoning: {data.get('reasoning', 'N/A')}")
    except Exception as e:
        print(f"  Error: {e}")

    step("7. Explanation (FastAPI)")
    try:
        r = client.post(f"{BASE_FASTAPI}/explanations/generate", json={
            "decisionType": "REORDER_ADJUSTED_FOR_WASTE",
            "context": {"ingredient_name": "Spinach", "original_quantity": 300, "adjusted_quantity": 210},
        })
        print(f"  {r.json().get('explanation', 'N/A')}")
    except Exception as e:
        print(f"  Error: {e}")

    step("8. Audit endpoints (Spring Boot)")
    try:
        r = client.get(f"{BASE_SPRING}/audit")
        print(f"  Audit records: {len(r.json())}")
        r2 = client.get(f"{BASE_SPRING}/audit/foodflow/{household_id}")
        print(f"  Food flow audit: {len(r2.json())} records")
    except Exception as e:
        print(f"  Error: {e}")

    print("\n=== SCENARIO COMPLETE ===")
    print("Trace shows: food-flow-informed ranking, waste-adjusted replenishment,")
    print("explanation generation, and audit coverage.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
