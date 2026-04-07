from fastapi import FastAPI

app = FastAPI(
    title="NourishOS Intelligence Service",
    description="Meal ranking, planning, food flow analysis, and forecasting",
    version="0.1.0",
)


@app.get("/health")
async def health():
    return {"status": "ok"}
