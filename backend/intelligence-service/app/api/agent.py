"""
POST /agent/shopping/build-basket — shopping agent (FastAPI).
Reads approved suggestions, finds best-value products. Does NOT place order.
"""

from fastapi import APIRouter
from pydantic import BaseModel, Field
from typing import List, Optional
from uuid import UUID

router = APIRouter(prefix="/agent", tags=["agent"])


class BuildBasketRequest(BaseModel):
    household_id: UUID = Field(alias="householdId")
    replenishment_request_id: UUID = Field(alias="replenishmentRequestId")
    supermarket_name: str = Field(alias="supermarketName")
    model_config = {"populate_by_name": True}


class BasketItem(BaseModel):
    suggestion_id: UUID = Field(alias="suggestionId")
    product_name: Optional[str] = Field(None, alias="productName")
    product_description: Optional[str] = Field(None, alias="productDescription")
    estimated_price: float = Field(alias="estimatedPrice")
    savings_note: Optional[str] = Field(None, alias="savingsNote")
    requires_manual_selection: bool = Field(False, alias="requiresManualSelection")
    model_config = {"populate_by_name": True}


class BuildBasketResponse(BaseModel):
    items: List[BasketItem]
    total_estimated_cost: float = Field(alias="totalEstimatedCost")
    agent_notes: str = Field(alias="agentNotes")
    model_config = {"populate_by_name": True}


@router.post("/shopping/build-basket", response_model=BuildBasketResponse)
async def build_basket(request: BuildBasketRequest):
    """Build supermarket basket from approved suggestions. Stub returns sample basket."""
    # In production, fetch approved suggestions from Spring Boot and map to products
    # For now, return a structured stub
    stub_items = [
        BasketItem(
            suggestion_id=UUID("00000000-0000-0000-0000-000000000001"),
            product_name="Chicken Breast 500g",
            product_description=f"{request.supermarket_name} Own Brand Chicken Breast",
            estimated_price=3.50,
            savings_note="Own brand saves 25% vs name brand",
        ),
        BasketItem(
            suggestion_id=UUID("00000000-0000-0000-0000-000000000002"),
            product_name="Extra Virgin Olive Oil 500ml",
            product_description=f"{request.supermarket_name} Italian Olive Oil",
            estimated_price=4.20,
            savings_note=None,
        ),
    ]
    total = sum(item.estimated_price for item in stub_items)
    return BuildBasketResponse(
        items=stub_items,
        total_estimated_cost=total,
        agent_notes=f"Built basket for {request.supermarket_name}. Selected own-brand where possible to save costs.",
    )
