from fastapi import FastAPI
from app.api.recommendation import router as recommendation_router
from app.api.planning import router as planning_router
from app.api.foodflow import router as foodflow_router
from app.api.nutrition import router as nutrition_router
from app.api.sustainability import router as sustainability_router

app = FastAPI(
    title="NourishOS Intelligence Service",
    description="Meal ranking, planning, food flow analysis, and forecasting",
    version="0.1.0",
)

app.include_router(recommendation_router)
app.include_router(planning_router)
app.include_router(foodflow_router)
app.include_router(nutrition_router)
app.include_router(sustainability_router)


@app.get("/health")
async def health():
    return {"status": "ok"}
