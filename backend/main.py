from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import JSONResponse
from supabase import create_client
import os
from dotenv import load_dotenv

load_dotenv()

SUPABASE_URL = os.getenv("SUPABASE_URL")
SUPABASE_KEY = os.getenv("SUPABASE_KEY")

supabase = create_client(SUPABASE_URL, SUPABASE_KEY)

app = FastAPI()

@app.get("/")
async def log_activity(petId: str, userId: str, type: str):
    data = {
        "pet_id": petId,
        "user_id": userId,
        "activityType": type,
        "comment": f"Qr code log for {type}",
    }

    supabase.table("activity-logs").insert(data).execute()
    return JSONResponse(content={"message": "Activity logged successfully."}, status_code=201)
