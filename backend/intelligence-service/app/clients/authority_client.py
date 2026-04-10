"""
Authority client — fetches domain data from Spring Boot authority-service.
FastAPI never owns durable domain data; all reads go through this client.
"""

import httpx
from pydantic import BaseModel, Field
from typing import Optional, List
from uuid import UUID
from decimal import Decimal


AUTHORITY_BASE_URL = "http://localhost:8080"

_client: Optional[httpx.Client] = None


def _get_client() -> httpx.Client:
    global _client
    if _client is None:
        _client = httpx.Client(base_url=AUTHORITY_BASE_URL, timeout=10.0)
    return _client


class AuthorityClientError(Exception):
    def __init__(self, status_code: int, detail: str):
        self.status_code = status_code
        self.detail = detail
        super().__init__(f"Authority API error {status_code}: {detail}")


# --- Pydantic Models ---

class HouseholdSettings(BaseModel):
    id: UUID
    effort_tolerance: Optional[str] = Field(None, alias="effortTolerance")
    sustainability_priority: Optional[str] = Field(None, alias="sustainabilityPriority")
    weekly_budget_limit: Optional[Decimal] = Field(None, alias="weeklyBudgetLimit")
    default_servings: Optional[int] = Field(None, alias="defaultServings")

    model_config = {"populate_by_name": True}


class Household(BaseModel):
    id: UUID
    name: str
    weekly_budget_limit: Optional[Decimal] = Field(None, alias="weeklyBudgetLimit")
    settings: Optional[HouseholdSettings] = None

    model_config = {"populate_by_name": True}


class HouseholdMember(BaseModel):
    id: UUID
    household_id: UUID = Field(alias="householdId")
    display_name: str = Field(alias="displayName")
    age_group: str = Field(alias="ageGroup")
    effort_sensitivity: Optional[str] = Field(None, alias="effortSensitivity")
    participates_in_meal_planning: bool = Field(True, alias="participatesInMealPlanning")

    model_config = {"populate_by_name": True}


# --- Client Methods ---

def _handle_response(response: httpx.Response) -> dict | list:
    if response.status_code == 404:
        raise AuthorityClientError(404, response.text)
    if response.status_code >= 400:
        raise AuthorityClientError(response.status_code, response.text)
    return response.json()


def get_household(household_id: str) -> Household:
    resp = _get_client().get(f"/households/{household_id}")
    data = _handle_response(resp)
    return Household.model_validate(data)


def get_members(household_id: str) -> List[HouseholdMember]:
    resp = _get_client().get(f"/households/{household_id}/members")
    data = _handle_response(resp)
    return [HouseholdMember.model_validate(m) for m in data]


# --- Sprint 9.2: Inventory & Food Flow ---

class ParLevel(BaseModel):
    id: UUID
    household_id: UUID = Field(alias="householdId")
    ingredient_id: UUID = Field(alias="ingredientId")
    preferred_quantity: Decimal = Field(alias="preferredQuantity")
    minimum_quantity: Decimal = Field(alias="minimumQuantity")
    unit: str

    model_config = {"populate_by_name": True}


class IngredientUsageRecord(BaseModel):
    id: UUID
    total_consumed_last_4_weeks: Decimal = Field(alias="totalConsumedLast4Weeks")
    total_wasted_last_4_weeks: Decimal = Field(alias="totalWastedLast4Weeks")
    avg_weekly_usage: Decimal = Field(alias="avgWeeklyUsage")

    model_config = {"populate_by_name": True}


def get_inventory_snapshot(household_id: str) -> dict:
    resp = _get_client().get("/inventory/snapshot", params={"householdId": household_id})
    return _handle_response(resp)


def get_foodflow_snapshot(household_id: str) -> dict:
    resp = _get_client().get("/foodflow/snapshot", params={"householdId": household_id})
    return _handle_response(resp)


def get_par_levels(household_id: str) -> List[ParLevel]:
    resp = _get_client().get(f"/households/{household_id}/par-levels")
    data = _handle_response(resp)
    return [ParLevel.model_validate(p) for p in data]


def get_usage_records(household_id: str) -> List[IngredientUsageRecord]:
    resp = _get_client().get(f"/foodflow/usage", params={"householdId": household_id})
    # This endpoint returns a single record, but we wrap for consistency
    data = _handle_response(resp)
    if isinstance(data, list):
        return [IngredientUsageRecord.model_validate(r) for r in data]
    return [IngredientUsageRecord.model_validate(data)]


# --- Sprint 9.3: Meal Catalog ---

class IngredientRef(BaseModel):
    ingredient_id: UUID = Field(alias="ingredientId")
    base_quantity: Decimal = Field(alias="baseQuantity")
    unit: str
    optional: bool = False
    substitutable: bool = True

    model_config = {"populate_by_name": True}


class MealOption(BaseModel):
    id: UUID
    name: str
    meal_type: str = Field(alias="mealType")
    estimated_protein_grams: Optional[Decimal] = Field(None, alias="estimatedProteinGrams")
    estimated_calories: Optional[Decimal] = Field(None, alias="estimatedCalories")
    prep_time_minutes: Optional[int] = Field(None, alias="prepTimeMinutes")
    sustainability_score: Optional[Decimal] = Field(None, alias="sustainabilityScore")
    ingredient_refs: List[IngredientRef] = Field(default_factory=list, alias="ingredientRefs")

    model_config = {"populate_by_name": True}

    @classmethod
    def _parse_refs(cls, v):
        if isinstance(v, str):
            import json
            return [IngredientRef.model_validate(r) for r in json.loads(v)]
        if isinstance(v, list):
            return [IngredientRef.model_validate(r) if isinstance(r, dict) else r for r in v]
        return v


def get_meal_catalog() -> List[MealOption]:
    resp = _get_client().get("/meal-catalog")
    data = _handle_response(resp)
    results = []
    for item in data:
        # Handle ingredientRefs being a JSON string from Spring
        if isinstance(item.get("ingredientRefs"), str):
            import json
            item["ingredientRefs"] = json.loads(item["ingredientRefs"])
        results.append(MealOption.model_validate(item))
    return results


# --- Sprint 12.5: Replenishment POST ---

def post_replenishment_suggestions(household_id: str, suggestions: List[dict]) -> dict:
    """POST optimised suggestions to Spring Boot."""
    resp = _get_client().post(
        "/replenishment/suggestions",
        json=suggestions,
        params={"householdId": household_id},
    )
    return _handle_response(resp)


# --- Sprint 13.5: Execution Plan POST ---

def post_execution_plan(plan_payload: dict) -> dict:
    """POST execution plan to Spring Boot. FastAPI proposes — Spring Boot persists."""
    resp = _get_client().post("/executions", json=plan_payload)
    return _handle_response(resp)
