"""
FastAPI wrapper exposing the AI Director Q&A prototype (qa.py) over HTTP,
so it can run as a persistent service (PM2) instead of only a local CLI.

Run: uvicorn ai_director.app:app --host 127.0.0.1 --port 8041
"""
from fastapi import FastAPI
from pydantic import BaseModel
from groq import Groq

from .qa import GROQ_API_KEY, ask, build_schema_context, get_conn

app = FastAPI(title="Hospital AI Director - Weekly Report Q&A (prototype)")

_groq_client = Groq(api_key=GROQ_API_KEY)
_conn = get_conn()
_schema_context = build_schema_context(_conn)


class Question(BaseModel):
    question: str


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/ask")
def ask_question(payload: Question):
    global _conn
    try:
        answer = ask(payload.question, _conn, _groq_client, _schema_context)
    except Exception:
        # Neon can drop idle connections; reconnect once and retry.
        _conn.close()
        _conn = get_conn()
        answer = ask(payload.question, _conn, _groq_client, _schema_context)
    return {"answer": answer}
