"""
POST /pantry/receipt/scan — receipt OCR (FastAPI stub).
In production, would call LLM vision. For now, returns a structured stub.
No DB write — returns parsed items for user to confirm.
"""

from fastapi import APIRouter
from pydantic import BaseModel, Field
from typing import List, Optional

router = APIRouter(prefix="/pantry", tags=["pantry"])


class ReceiptScanRequest(BaseModel):
    image_base64: Optional[str] = Field(None, alias="imageBase64")
    model_config = {"populate_by_name": True}


class ReceiptItem(BaseModel):
    name: str
    quantity: float
    unit: str
    estimated_cost_per_unit: float = Field(alias="estimatedCostPerUnit")
    confidence: float
    requires_review: bool = Field(False, alias="requiresReview")
    model_config = {"populate_by_name": True}


class ReceiptScanResponse(BaseModel):
    items: List[ReceiptItem]


@router.post("/receipt/scan", response_model=ReceiptScanResponse)
async def scan_receipt(request: ReceiptScanRequest):
    """Parse receipt image. Returns structured items for user confirmation. No DB write."""
    # In production, this would call LLM vision (e.g., Claude Vision API)
    # For now, return a structured stub proving the shape works
    stub_items = [
        ReceiptItem(
            name="Chicken Breast", quantity=500, unit="g",
            estimated_cost_per_unit=0.012, confidence=0.92,
        ),
        ReceiptItem(
            name="Olive Oil", quantity=500, unit="ml",
            estimated_cost_per_unit=0.008, confidence=0.88,
        ),
        ReceiptItem(
            name="Mystery Item", quantity=1, unit="unit",
            estimated_cost_per_unit=2.50, confidence=0.45, requires_review=True,
        ),
    ]
    return ReceiptScanResponse(items=stub_items)
