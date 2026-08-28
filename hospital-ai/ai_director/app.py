"""
FastAPI wrapper exposing the AI Director Q&A prototype (qa.py) over HTTP,
so it can run as a persistent service (PM2) instead of only a local CLI.
Also serves a minimal test chat page at GET / for manual browser testing.

Run: uvicorn ai_director.app:app --host 127.0.0.1 --port 8041
"""
import re
from typing import Any, Optional

from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.responses import HTMLResponse
from pydantic import BaseModel
from groq import Groq

from .qa import GROQ_API_KEY, ask, build_schema_context, get_conn
from . import reports as reports_svc

app = FastAPI(title="Hospital AI Director - Weekly Report Q&A (prototype)")

_groq_client = Groq(api_key=GROQ_API_KEY)
_conn = get_conn()


def _reconnect_if_needed(fn):
    """Neon can drop idle connections; reconnect once and retry."""
    global _conn
    try:
        return fn(_conn)
    except Exception:
        _conn.close()
        _conn = get_conn()
        return fn(_conn)


def _friendly_groq_error(e: Exception, fallback_prefix: str) -> str:
    msg = str(e)
    if "rate_limit" in msg or "429" in msg:
        wait_match = re.search(r"try again in ([\d.]+)(m|s)([\d.]+)?s?", msg)
        if wait_match:
            unit = "phút" if wait_match.group(2) == "m" else "giây"
            wait = f"~{round(float(wait_match.group(1)))} {unit}"
            return f"AI đã dùng hết hạn mức xử lý cho hôm nay/phút này, vui lòng thử lại sau {wait}."
        return "AI đang quá tải hoặc hết hạn mức tạm thời, vui lòng thử lại sau ít phút."
    return f"{fallback_prefix}: {e}"


class Question(BaseModel):
    question: str


class ReportDraft(BaseModel):
    label: str
    start_date: str
    end_date: str
    source_file: Optional[str] = None
    metrics: dict[str, Any] = {}
    incidents: list[dict[str, Any]] = []
    feedback: list[dict[str, Any]] = []
    narrative_sections: list[dict[str, Any]] = []


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
  <div class="note" id="periodsNote">Đang tải danh sách báo cáo...</div>
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

async function loadPeriodsNote() {
  const el = document.getElementById('periodsNote');
  try {
    const rows = await (await fetch('reports')).json();
    const labels = rows
      .slice()
      .sort((a, b) => a.start_date.localeCompare(b.start_date))
      .map(r => r.label)
      .join(', ');
    el.textContent = rows.length
      ? `Dữ liệu từ ${rows.length} tuần: ${labels}. Trả lời có thể sai, đây là bản thử nghiệm.`
      : 'Chưa có báo cáo nào được nạp. Trả lời có thể sai, đây là bản thử nghiệm.';
  } catch (err) {
    el.textContent = 'Trả lời có thể sai, đây là bản thử nghiệm.';
  }
  el.insertAdjacentHTML('beforeend', ' · <a href="manage">Quản lý báo cáo (thêm / sửa / xoá)</a>');
}
loadPeriodsNote();

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
    loadingEl.textContent = res.ok ? data.answer : ('Lỗi: ' + (data.detail || JSON.stringify(data)));
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


MANAGE_PAGE = """<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>AI Director - Quản lý báo cáo tuần</title>
<style>
  body { font-family: -apple-system, Roboto, Arial, sans-serif; max-width: 980px; margin: 0 auto; padding: 16px 16px 60px; background: #f6f7f9; color: #1a1a1a; }
  h1 { font-size: 18px; margin-bottom: 4px; }
  h3 { font-size: 14px; margin: 18px 0 8px; }
  .note { color: #666; font-size: 13px; margin-bottom: 16px; }
  a { color: #2563eb; }
  table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
  th, td { text-align: left; padding: 8px 10px; border-bottom: 1px solid #eee; font-size: 13px; vertical-align: top; }
  th { background: #eef1f5; font-size: 12px; color: #555; }
  .cat-row td { background: #eef1f5; font-weight: 600; font-size: 12px; color: #555; }
  button { padding: 6px 12px; border: none; border-radius: 6px; background: #2563eb; color: white; font-size: 13px; cursor: pointer; }
  button.secondary { background: #e5e7eb; color: #1a1a1a; }
  button.danger { background: #dc2626; }
  button:disabled { background: #93c5fd; cursor: default; }
  input[type=text], input[type=number], input[type=date], textarea, select { width: 100%; padding: 5px 7px; border: 1px solid #ccc; border-radius: 5px; font-size: 13px; box-sizing: border-box; font-family: inherit; }
  .m-cs1, .m-cs2, .m-total { width: 90px; }
  .unit { color: #888; font-weight: normal; }
  .card { background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 10px; margin-bottom: 8px; }
  .card .row { display: flex; gap: 8px; margin-bottom: 6px; }
  .card .row > div { flex: 1; }
  .card label { font-size: 11px; color: #666; display: block; margin-bottom: 2px; }
  .top-actions { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
  .form-actions { position: sticky; bottom: 0; background: #f6f7f9; padding: 12px 0; display: flex; gap: 8px; border-top: 1px solid #ddd; margin-top: 16px; }
  .banner { background: #fff7ed; border: 1px solid #fdba74; color: #9a3412; padding: 8px 10px; border-radius: 6px; font-size: 13px; margin-bottom: 12px; }
  .spinner { padding: 60px 20px; text-align: center; color: #666; }
  .spinner-icon { width: 36px; height: 36px; margin: 0 auto 16px; border-radius: 50%; border: 4px solid #dbeafe; border-top-color: #2563eb; animation: spin 0.8s linear infinite; }
  .spinner-msg { font-size: 14px; max-width: 480px; margin: 0 auto; line-height: 1.5; }
  .spinner-elapsed { margin-top: 10px; font-size: 12px; color: #999; font-variant-numeric: tabular-nums; }
  @keyframes spin { to { transform: rotate(360deg); } }
  #fileInput { display: none; }
  .head-grid { display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 8px; margin-bottom: 12px; }
</style>
</head>
<body>
  <div class="top-actions">
    <div>
      <h1>Quản lý báo cáo tuần</h1>
      <div class="note"><a href="./">&larr; Về trang hỏi đáp</a></div>
    </div>
    <div id="listActions">
      <input type="file" id="fileInput" accept=".ppt,.pptx" onchange="onFileChosen(this)">
      <button onclick="document.getElementById('fileInput').click()">+ Upload báo cáo mới</button>
    </div>
  </div>

  <div id="listView">
    <table>
      <thead><tr><th>Tuần</th><th>Từ ngày</th><th>Đến ngày</th><th>Nguồn</th><th>Sự cố</th><th>Khiếu nại</th><th></th></tr></thead>
      <tbody id="listBody"><tr><td colspan="7">Đang tải...</td></tr></tbody>
    </table>
  </div>

  <div id="editView" style="display:none">
    <div id="editBanner"></div>
    <div class="head-grid">
      <div><label>Nhãn tuần</label><input type="text" id="f-label"></div>
      <div><label>Từ ngày</label><input type="date" id="f-start"></div>
      <div><label>Đến ngày</label><input type="date" id="f-end"></div>
    </div>

    <h3>Số liệu định lượng</h3>
    <table><tbody id="metricsTable"></tbody></table>

    <h3>Sự cố y khoa (incident) <button class="secondary" onclick="addIncident()">+ Thêm</button></h3>
    <div id="incidentsList"></div>

    <h3>Khiếu nại / khen ngợi (feedback) <button class="secondary" onclick="addFeedback()">+ Thêm</button></h3>
    <div id="feedbackList"></div>

    <h3>Tường thuật theo khoa/phòng (narrative) <button class="secondary" onclick="addNarrative()">+ Thêm</button></h3>
    <div id="narrativeList"></div>

    <div class="form-actions">
      <button onclick="saveDraft()">Lưu báo cáo</button>
      <button class="secondary" onclick="showList()">Huỷ</button>
    </div>
  </div>

<script>
let metricCatalog = [];
let editingId = null;

function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}

async function init() {
  metricCatalog = await (await fetch('metrics')).json();
  await loadList();
}

async function loadList() {
  const rows = await (await fetch('reports')).json();
  const body = document.getElementById('listBody');
  if (!rows.length) { body.innerHTML = '<tr><td colspan="7">Chưa có báo cáo nào.</td></tr>'; return; }
  body.innerHTML = rows.map(r => `
    <tr>
      <td>${esc(r.label)}</td>
      <td>${esc(r.start_date)}</td>
      <td>${esc(r.end_date)}</td>
      <td>${esc(r.source_file)}</td>
      <td>${r.incident_count}</td>
      <td>${r.feedback_count}</td>
      <td style="white-space:nowrap">
        <button class="secondary" onclick="openEdit(${r.id})">Sửa</button>
        <button class="danger" onclick="deleteReport(${r.id}, '${esc(r.label).replace(/'/g, "\\\\'")}')">Xoá</button>
      </td>
    </tr>`).join('');
}

function showList() {
  stopSpinner();
  document.getElementById('listView').style.display = '';
  document.getElementById('listActions').style.display = '';
  document.getElementById('editView').style.display = 'none';
}

let spinnerTimer = null;

function stopSpinner() {
  if (spinnerTimer) { clearInterval(spinnerTimer); spinnerTimer = null; }
}

function showSpinner(msg) {
  document.getElementById('listView').style.display = 'none';
  document.getElementById('listActions').style.display = 'none';
  document.getElementById('editView').style.display = '';
  document.getElementById('editBanner').innerHTML = '';
  stopSpinner();
  document.getElementById('editView').innerHTML = `
    <div class="spinner">
      <div class="spinner-icon"></div>
      <div class="spinner-msg">${esc(msg)}</div>
      <div class="spinner-elapsed" id="spinnerElapsed">Đã chờ 0 giây...</div>
    </div>`;
  const startedAt = Date.now();
  spinnerTimer = setInterval(() => {
    const el = document.getElementById('spinnerElapsed');
    if (!el) { stopSpinner(); return; }
    el.textContent = `Đã chờ ${Math.floor((Date.now() - startedAt) / 1000)} giây...`;
  }, 500);
}

async function onFileChosen(input) {
  const file = input.files[0];
  input.value = '';
  if (!file) return;
  showSpinner('Đang đọc file và trích xuất dữ liệu bằng AI (báo cáo dài có thể mất 1-2 phút, do dữ liệu được xử lý theo từng phần)...');
  try {
    const fd = new FormData();
    fd.append('file', file);
    const res = await fetch('reports/extract', { method: 'POST', body: fd });
    if (!res.ok) throw new Error((await res.json()).detail || res.statusText);
    const draft = await res.json();
    editingId = null;
    renderEditForm(draft);
  } catch (err) {
    alert('Lỗi trích xuất file: ' + err.message);
    location.reload();
  }
}

async function openEdit(id) {
  showSpinner('Đang tải báo cáo...');
  try {
    const res = await fetch(`reports/${id}`);
    if (!res.ok) throw new Error((await res.json()).detail || res.statusText);
    const data = await res.json();
    editingId = id;
    renderEditForm(data);
  } catch (err) {
    alert('Lỗi tải báo cáo: ' + err.message);
    location.reload();
  }
}

function renderEditForm(draft) {
  stopSpinner();
  document.getElementById('editView').innerHTML = `
    <div id="editBanner"></div>
    <div class="head-grid">
      <div><label>Nhãn tuần</label><input type="text" id="f-label" value="${esc(draft.label)}"></div>
      <div><label>Từ ngày</label><input type="date" id="f-start" value="${esc(draft.start_date)}"></div>
      <div><label>Đến ngày</label><input type="date" id="f-end" value="${esc(draft.end_date)}"></div>
    </div>
    <h3>Số liệu định lượng</h3>
    <table><thead><tr><th>Chỉ số</th><th>CS1</th><th>CS2</th><th>TOTAL</th><th>Ghi chú</th></tr></thead>
    <tbody id="metricsTable"></tbody></table>
    <h3>Sự cố y khoa (incident) <button class="secondary" onclick="addIncident()">+ Thêm</button></h3>
    <div id="incidentsList"></div>
    <h3>Khiếu nại / khen ngợi (feedback) <button class="secondary" onclick="addFeedback()">+ Thêm</button></h3>
    <div id="feedbackList"></div>
    <h3>Tường thuật theo khoa/phòng (narrative) <button class="secondary" onclick="addNarrative()">+ Thêm</button></h3>
    <div id="narrativeList"></div>
    <div class="form-actions">
      <button onclick="saveDraft()">Lưu báo cáo</button>
      <button class="secondary" onclick="showList()">Huỷ</button>
    </div>
  `;

  const banners = [];
  const overlaps = draft.overlaps || [];
  if (overlaps.length) {
    banners.push(`⚠️ Đã có báo cáo trùng khoảng ngày này trong hệ thống: ${overlaps.map(o => esc(o.label)).join(', ')}. Kiểm tra kỹ trước khi lưu để tránh trùng dữ liệu.`);
  }
  const truncated = draft.truncated || {};
  if (truncated.table) banners.push('⚠️ Nội dung bảng số liệu trong file quá dài, đã bị cắt bớt khi trích xuất - kiểm tra kỹ phần số liệu bên dưới, có thể thiếu.');
  if (truncated.narrative) banners.push('⚠️ Nội dung tường thuật/sự cố/khiếu nại trong file quá dài, đã bị cắt bớt khi trích xuất - có thể thiếu mục.');
  if (banners.length) {
    document.getElementById('editBanner').innerHTML = banners.map(b => `<div class="banner">${b}</div>`).join('');
  }

  let byCat = {};
  metricCatalog.forEach(m => { (byCat[m.category] ||= []).push(m); });
  const values = draft.metrics || {};
  let html = '';
  for (const cat in byCat) {
    html += `<tr class="cat-row"><td colspan="5">${esc(cat)}</td></tr>`;
    byCat[cat].forEach(m => {
      const v = values[m.code] || {};
      html += `<tr data-code="${esc(m.code)}">
        <td>${esc(m.name)} <span class="unit">(${esc(m.unit || '')})</span></td>
        <td><input type="number" step="any" class="m-cs1" value="${v.CS1 ?? ''}"></td>
        <td><input type="number" step="any" class="m-cs2" value="${v.CS2 ?? ''}"></td>
        <td><input type="number" step="any" class="m-total" value="${v.TOTAL ?? ''}"></td>
        <td><input type="text" class="m-note" value="${esc(v.note ?? '')}"></td>
      </tr>`;
    });
  }
  document.getElementById('metricsTable').innerHTML = html;

  (draft.incidents || []).forEach(addIncident);
  (draft.feedback || []).forEach(addFeedback);
  (draft.narrative_sections || []).forEach(addNarrative);
}

function addIncident(data) {
  data = data || {};
  document.getElementById('incidentsList').insertAdjacentHTML('beforeend', `
    <div class="card">
      <div class="row">
        <div><label>Khoa/phòng</label><input type="text" class="i-department" value="${esc(data.department)}"></div>
        <div><label>Ngày</label><input type="text" class="i-date" value="${esc(data.incident_date)}"></div>
        <div><label>Mức độ</label>
          <select class="i-severity">
            ${['low', 'medium', 'high'].map(s => `<option value="${s}" ${data.severity === s ? 'selected' : ''}>${s}</option>`).join('')}
          </select>
        </div>
        <div><label>Đã xử lý?</label><input type="checkbox" class="i-resolved" ${data.resolved ? 'checked' : ''} style="width:auto;margin-top:6px"></div>
      </div>
      <label>Mô tả</label><textarea class="i-description" rows="2">${esc(data.description)}</textarea>
      <div class="row" style="margin-top:6px">
        <div><label>Nguyên nhân</label><textarea class="i-cause" rows="2">${esc(data.cause)}</textarea></div>
        <div><label>Khắc phục</label><textarea class="i-action" rows="2">${esc(data.corrective_action)}</textarea></div>
      </div>
      <button class="secondary" style="margin-top:6px" onclick="this.closest('.card').remove()">Xoá dòng</button>
    </div>`);
}

function addFeedback(data) {
  data = data || {};
  document.getElementById('feedbackList').insertAdjacentHTML('beforeend', `
    <div class="card">
      <div class="row">
        <div><label>Ngày</label><input type="text" class="fb-date" value="${esc(data.date)}"></div>
        <div><label>Khoa/phòng</label><input type="text" class="fb-department" value="${esc(data.department)}"></div>
        <div><label>Loại</label>
          <select class="fb-type">
            ${['complaint', 'praise'].map(t => `<option value="${t}" ${data.type === t ? 'selected' : ''}>${t}</option>`).join('')}
          </select>
        </div>
      </div>
      <label>Nội dung</label><textarea class="fb-content" rows="2">${esc(data.content)}</textarea>
      <div class="row" style="margin-top:6px">
        <div><label>Nguyên nhân</label><textarea class="fb-cause" rows="2">${esc(data.cause)}</textarea></div>
        <div><label>Hướng xử lý</label><textarea class="fb-resolution" rows="2">${esc(data.resolution)}</textarea></div>
      </div>
      <button class="secondary" style="margin-top:6px" onclick="this.closest('.card').remove()">Xoá dòng</button>
    </div>`);
}

function addNarrative(data) {
  data = data || {};
  document.getElementById('narrativeList').insertAdjacentHTML('beforeend', `
    <div class="card">
      <label>Khoa/phòng</label><input type="text" class="n-section" value="${esc(data.section_name)}">
      <label>Nội dung</label><textarea class="n-content" rows="2">${esc(data.content)}</textarea>
      <button class="secondary" style="margin-top:6px" onclick="this.closest('.card').remove()">Xoá dòng</button>
    </div>`);
}

function numOrNull(v) { return v === '' || v === null || v === undefined ? null : Number(v); }
function strOrNull(v) { return v === '' ? null : v; }

function collectDraft() {
  const metrics = {};
  document.querySelectorAll('#metricsTable tr[data-code]').forEach(tr => {
    const code = tr.dataset.code;
    const cs1 = numOrNull(tr.querySelector('.m-cs1').value);
    const cs2 = numOrNull(tr.querySelector('.m-cs2').value);
    const total = numOrNull(tr.querySelector('.m-total').value);
    const note = strOrNull(tr.querySelector('.m-note').value);
    if (cs1 === null && cs2 === null && total === null && note === null) return;
    metrics[code] = { CS1: cs1, CS2: cs2, TOTAL: total, note };
  });

  const incidents = [...document.querySelectorAll('#incidentsList .card')].map(c => ({
    department: c.querySelector('.i-department').value,
    incident_date: c.querySelector('.i-date').value,
    description: c.querySelector('.i-description').value,
    cause: c.querySelector('.i-cause').value,
    corrective_action: c.querySelector('.i-action').value,
    resolved: c.querySelector('.i-resolved').checked,
    severity: c.querySelector('.i-severity').value,
  }));

  const feedback = [...document.querySelectorAll('#feedbackList .card')].map(c => ({
    date: c.querySelector('.fb-date').value,
    department: c.querySelector('.fb-department').value,
    type: c.querySelector('.fb-type').value,
    content: c.querySelector('.fb-content').value,
    cause: strOrNull(c.querySelector('.fb-cause').value),
    resolution: strOrNull(c.querySelector('.fb-resolution').value),
  }));

  const narrative_sections = [...document.querySelectorAll('#narrativeList .card')].map(c => ({
    section_name: c.querySelector('.n-section').value,
    content: c.querySelector('.n-content').value,
  }));

  return {
    label: document.getElementById('f-label').value,
    start_date: document.getElementById('f-start').value,
    end_date: document.getElementById('f-end').value,
    metrics, incidents, feedback, narrative_sections,
  };
}

async function saveDraft(force) {
  const draft = collectDraft();
  if (!draft.label || !draft.start_date || !draft.end_date) {
    alert('Cần nhập đủ nhãn tuần, từ ngày, đến ngày.');
    return;
  }
  const url = editingId ? `reports/${editingId}` : 'reports';
  const method = editingId ? 'PUT' : 'POST';
  const qs = force ? '?force=true' : '';
  const res = await fetch(url + qs, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(draft) });
  if (res.status === 409) {
    const body = await res.json();
    const names = (body.detail?.overlaps || []).map(o => o.label).join(', ');
    if (confirm(`Đã có báo cáo trùng tuần: ${names}. Vẫn lưu?`)) return saveDraft(true);
    return;
  }
  if (!res.ok) { alert('Lỗi lưu báo cáo: ' + JSON.stringify(await res.json())); return; }
  await loadList();
  showList();
}

async function deleteReport(id, label) {
  if (!confirm(`Xoá báo cáo "${label}"? Hành động không thể hoàn tác.`)) return;
  const res = await fetch(`reports/${id}`, { method: 'DELETE' });
  if (!res.ok) { alert('Lỗi xoá: ' + JSON.stringify(await res.json())); return; }
  loadList();
}

init();
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
    # Rebuilt fresh each call (cheap - 3 small SELECTs) rather than cached at
    # startup, so newly uploaded/edited/deleted reports show up immediately
    # instead of only after a server restart.
    def do_ask(conn):
        schema_context = build_schema_context(conn)
        return ask(payload.question, conn, _groq_client, schema_context)

    try:
        answer = _reconnect_if_needed(do_ask)
    except Exception as e:
        raise HTTPException(status_code=502, detail=_friendly_groq_error(e, "Lỗi khi hỏi AI"))
    return {"answer": answer}


@app.get("/metrics")
def list_metrics_route():
    return _reconnect_if_needed(reports_svc.load_metric_defs)


@app.get("/reports")
def list_reports_route():
    return _reconnect_if_needed(reports_svc.list_reports)


@app.get("/reports/{report_id}")
def get_report_route(report_id: int):
    result = _reconnect_if_needed(lambda c: reports_svc.get_report(c, report_id))
    if result is None:
        raise HTTPException(status_code=404, detail="Không tìm thấy báo cáo")
    return result


@app.post("/reports/extract")
async def extract_report_route(file: UploadFile = File(...)):
    if not file.filename.lower().endswith((".ppt", ".pptx")):
        raise HTTPException(status_code=400, detail="Chỉ hỗ trợ file .ppt/.pptx")
    file_bytes = await file.read()
    try:
        table_text, narrative_text = reports_svc.extract_pptx_streams(file_bytes)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Không đọc được file PowerPoint: {e}")
    if not table_text.strip() and not narrative_text.strip():
        raise HTTPException(status_code=400, detail="File không có nội dung text để trích xuất")

    def do_extract(conn):
        return reports_svc.extract_report_data(table_text, narrative_text, _groq_client, conn)

    try:
        draft = _reconnect_if_needed(do_extract)
    except Exception as e:
        raise HTTPException(status_code=502, detail=_friendly_groq_error(e, "Lỗi khi gọi AI trích xuất dữ liệu"))
    draft["source_file"] = file.filename
    draft["overlaps"] = _reconnect_if_needed(
        lambda c: reports_svc.find_overlapping_reports(c, draft["start_date"], draft["end_date"])
    )
    return draft


@app.post("/reports")
def create_report_route(payload: ReportDraft, force: bool = False):
    if not force:
        overlaps = _reconnect_if_needed(
            lambda c: reports_svc.find_overlapping_reports(c, payload.start_date, payload.end_date)
        )
        if overlaps:
            raise HTTPException(status_code=409, detail={"overlaps": overlaps})
    report_id = _reconnect_if_needed(
        lambda c: reports_svc.create_report(c, payload.model_dump(), payload.source_file or payload.label)
    )
    return {"id": report_id}


@app.put("/reports/{report_id}")
def update_report_route(report_id: int, payload: ReportDraft, force: bool = False):
    if not force:
        overlaps = _reconnect_if_needed(
            lambda c: reports_svc.find_overlapping_reports(c, payload.start_date, payload.end_date, report_id)
        )
        if overlaps:
            raise HTTPException(status_code=409, detail={"overlaps": overlaps})
    ok = _reconnect_if_needed(lambda c: reports_svc.update_report(c, report_id, payload.model_dump()))
    if not ok:
        raise HTTPException(status_code=404, detail="Không tìm thấy báo cáo")
    return {"id": report_id}


@app.delete("/reports/{report_id}")
def delete_report_route(report_id: int):
    ok = _reconnect_if_needed(lambda c: reports_svc.delete_report(c, report_id))
    if not ok:
        raise HTTPException(status_code=404, detail="Không tìm thấy báo cáo")
    return {"ok": True}


@app.get("/manage", response_class=HTMLResponse)
def manage_page():
    return MANAGE_PAGE
