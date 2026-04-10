from fastapi import FastAPI
from app.api.recommendation import router as recommendation_router
from app.api.planning import router as planning_router

app = FastAPI(
    title="NourishOS Intelligence Service",
    description="Meal ranking, planning, food flow analysis, and forecasting",
    version="0.1.0",
)

app.include_router(recommendation_router)
app.include_router(planning_router)


@app.get("/health")
async def health():
    return {"status": "ok"}
