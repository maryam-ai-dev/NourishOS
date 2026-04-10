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
