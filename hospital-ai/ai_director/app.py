"""
FastAPI wrapper exposing the AI Director Q&A prototype (qa.py) over HTTP,
so it can run as a persistent service (PM2) instead of only a local CLI.
Also serves a minimal test chat page at GET / for manual browser testing.

Run: uvicorn ai_director.app:app --host 127.0.0.1 --port 8041
"""
from fastapi import FastAPI
from fastapi.responses import HTMLResponse
from pydantic import BaseModel
from groq import Groq

from .qa import GROQ_API_KEY, ask, build_schema_context, get_conn

app = FastAPI(title="Hospital AI Director - Weekly Report Q&A (prototype)")

_groq_client = Groq(api_key=GROQ_API_KEY)
_conn = get_conn()
_schema_context = build_schema_context(_conn)


class Question(BaseModel):
    question: str


TEST_PAGE = """<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>AI Director - Hỏi đáp báo cáo tuần (thử nghiệm)</title>
<style>
  body { font-family: -apple-system, Roboto, Arial, sans-serif; max-width: 720px; margin: 0 auto; padding: 16px; background: #f6f7f9; color: #1a1a1a; }
  h1 { font-size: 18px; margin-bottom: 4px; }
  .note { color: #666; font-size: 13px; margin-bottom: 16px; }
  #log { display: flex; flex-direction: column; gap: 10px; margin-bottom: 90px; }
  .msg { padding: 10px 14px; border-radius: 12px; max-width: 85%; white-space: pre-wrap; line-height: 1.4; }
  .user { align-self: flex-end; background: #2563eb; color: white; }
  .bot { align-self: flex-start; background: white; border: 1px solid #ddd; }
  .bot.loading { color: #888; font-style: italic; }
  form { position: fixed; bottom: 0; left: 0; right: 0; display: flex; gap: 8px; padding: 12px; background: #fff; border-top: 1px solid #ddd; max-width: 720px; margin: 0 auto; }
  input { flex: 1; padding: 10px 12px; border: 1px solid #ccc; border-radius: 8px; font-size: 15px; }
  button { padding: 10px 16px; border: none; border-radius: 8px; background: #2563eb; color: white; font-size: 15px; }
  button:disabled { background: #93c5fd; }
</style>
</head>
<body>
  <h1>AI Director - Hỏi đáp báo cáo tuần (thử nghiệm)</h1>
  <div class="note">Dữ liệu mẫu từ 3 tuần: 20/04-26/04/2022, 09/07-15/07/2025, 03/06-09/06/2026. Trả lời có thể sai, đây là bản thử nghiệm.</div>
  <div id="log"></div>
  <form id="f">
    <input id="q" type="text" placeholder="Hỏi gì đó, vd: tuần rồi bao nhiêu bệnh nhân ra viện?" autocomplete="off">
    <button type="submit" id="send">Gửi</button>
  </form>
<script>
const log = document.getElementById('log');
const form = document.getElementById('f');
const input = document.getElementById('q');
const send = document.getElementById('send');

function addMsg(text, cls) {
  const div = document.createElement('div');
  div.className = 'msg ' + cls;
  div.textContent = text;
  log.appendChild(div);
  window.scrollTo(0, document.body.scrollHeight);
  return div;
}

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  const question = input.value.trim();
  if (!question) return;
  addMsg(question, 'user');
  input.value = '';
  send.disabled = true;
  const loadingEl = addMsg('Đang suy nghĩ...', 'bot loading');
  try {
    const res = await fetch('ask', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question })
    });
    const data = await res.json();
    loadingEl.textContent = data.answer || ('Lỗi: ' + JSON.stringify(data));
    loadingEl.classList.remove('loading');
  } catch (err) {
    loadingEl.textContent = 'Lỗi kết nối: ' + err;
    loadingEl.classList.remove('loading');
  }
  send.disabled = false;
});
</script>
</body>
</html>
"""


@app.get("/", response_class=HTMLResponse)
def test_page():
    return TEST_PAGE


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
