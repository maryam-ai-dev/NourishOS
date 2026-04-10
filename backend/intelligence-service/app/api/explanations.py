"""
POST /explanations/generate — human-readable decision explanation.
"""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import List, Dict

from app.services.explanations.explanation_generator import (
    DecisionType,
    generate_explanation,
)

router = APIRouter(prefix="/explanations", tags=["explanations"])


class ExplanationRequest(BaseModel):
    decision_type: str = Field(alias="decisionType")
    context: Dict = Field(default_factory=dict)
    model_config = {"populate_by_name": True}


class SupportingFactorResponse(BaseModel):
    name: str
    value: str


class ExplanationResponse(BaseModel):
    decision_type: str = Field(alias="decisionType")
    explanation: str
    supporting_factors: List[SupportingFactorResponse] = Field(alias="supportingFactors")
    model_config = {"populate_by_name": True}


@router.post("/generate", response_model=ExplanationResponse)
async def generate(request: ExplanationRequest):
    try:
        dt = DecisionType(request.decision_type)
    except ValueError:
        raise HTTPException(status_code=422, detail=f"Unknown decision_type: {request.decision_type}")

    result = generate_explanation(dt, request.context)

    return ExplanationResponse(
        decision_type=result.decision_type,
        explanation=result.explanation,
        supporting_factors=[
            SupportingFactorResponse(name=f.name, value=f.value)
            for f in result.supporting_factors
        ],
    )
