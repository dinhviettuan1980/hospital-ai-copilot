"""
Weekly report management: upload a .pptx briefing deck, let Groq draft a
structured extraction against the report_period/metric_value/incident/
feedback/narrative_section schema, and let staff review/edit that draft
before it is written to Postgres. Also covers list/get/update/delete for
reports already in the DB.

The extraction step never touches the database - it only returns a draft
JSON blob. The client is responsible for sending back the (possibly edited)
draft to create_report/update_report.
"""
import io
import json

from pptx import Presentation

MODEL = "openai/gpt-oss-120b"

FACILITY_CODES = ("CS1", "CS2", "TOTAL")


def extract_text_from_pptx(file_bytes: bytes) -> str:
    prs = Presentation(io.BytesIO(file_bytes))
    lines = []
    for slide_idx, slide in enumerate(prs.slides, start=1):
        lines.append(f"--- Slide {slide_idx} ---")
        for shape in slide.shapes:
            if shape.has_table:
                for row in shape.table.rows:
                    cells = [cell.text.strip() for cell in row.cells]
                    lines.append(" | ".join(cells))
            elif shape.has_text_frame:
                text = shape.text_frame.text.strip()
                if text:
                    lines.append(text)
    return "\n".join(lines)


def load_metric_defs(conn):
    with conn.cursor() as cur:
        cur.execute("SELECT code, category, name, unit FROM metric ORDER BY category, code")
        return cur.fetchall()


def _build_extraction_prompt(metric_defs) -> str:
    metrics_txt = "\n".join(
        f"  - {m['code']} [{m['category']}]: {m['name']} (đơn vị: {m['unit']})" for m in metric_defs
    )
    return f"""Bạn là bot trích xuất dữ liệu từ báo cáo giao ban tuần của bệnh viện. Người dùng sẽ đưa cho bạn
nội dung text đã trích thô từ file PowerPoint (qua python-pptx, gồm cả nội dung bảng biểu). Nhiệm vụ của bạn
là đọc và trả về DUY NHẤT một JSON object theo đúng schema sau, không thêm giải thích:

{{
  "label": "Tuần DD/MM - DD/MM/YYYY",
  "start_date": "YYYY-MM-DD",
  "end_date": "YYYY-MM-DD",
  "metrics": {{
    "<metric_code>": {{"CS1": number|null, "CS2": number|null, "TOTAL": number|null, "note": string|null}}
  }},
  "incidents": [
    {{"department": string, "incident_date": string, "description": string, "cause": string,
      "corrective_action": string, "resolved": boolean, "severity": "low"|"medium"|"high"}}
  ],
  "feedback": [
    {{"date": string, "department": string, "type": "complaint"|"praise", "content": string,
      "cause": string|null, "resolution": string|null}}
  ],
  "narrative_sections": [
    {{"section_name": string, "content": string}}
  ]
}}

DANH SÁCH metric_code HỢP LỆ (chỉ dùng đúng các code này, bỏ qua số liệu không khớp code nào):
{metrics_txt}

Cơ sở (facility) chỉ có 3 mã: CS1, CS2, TOTAL. Chỉ điền giá trị cho facility có số liệu thật trong nguồn,
để null nếu không có (KHÔNG tự suy ra hay cộng trừ để bịa số). Nếu chỉ có TOTAL mà không tách CS1/CS2 thì
chỉ điền TOTAL.

QUAN TRỌNG: Nếu không chắc chắn hoặc không tìm thấy giá trị/thông tin nào, để null (metric) hoặc bỏ qua
(incident/feedback/narrative) thay vì bịa ra. Đây là dữ liệu dùng cho Ban Giám đốc bệnh viện, sai số liệu
là không chấp nhận được."""


def extract_report_data(raw_text: str, groq_client, conn) -> dict:
    metric_defs = load_metric_defs(conn)
    system_prompt = _build_extraction_prompt(metric_defs)
    response = groq_client.chat.completions.create(
        model=MODEL,
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": raw_text[:100000]},
        ],
        response_format={"type": "json_object"},
        temperature=0.1,
    )
    data = json.loads(response.choices[0].message.content)
    data.setdefault("metrics", {})
    data.setdefault("incidents", [])
    data.setdefault("feedback", [])
    data.setdefault("narrative_sections", [])
    return data


def _facility_ids(conn) -> dict:
    with conn.cursor() as cur:
        cur.execute("SELECT id, code FROM facility")
        return {row["code"]: row["id"] for row in cur.fetchall()}


def _metric_ids(conn) -> dict:
    with conn.cursor() as cur:
        cur.execute("SELECT id, code FROM metric")
        return {row["code"]: row["id"] for row in cur.fetchall()}


def list_reports(conn):
    with conn.cursor() as cur:
        cur.execute(
            """
            SELECT rp.id, rp.label, rp.start_date, rp.end_date, rp.source_file,
                   (SELECT count(*) FROM incident i WHERE i.report_period_id = rp.id) AS incident_count,
                   (SELECT count(*) FROM feedback f WHERE f.report_period_id = rp.id) AS feedback_count
            FROM report_period rp
            ORDER BY rp.start_date DESC
            """
        )
        return cur.fetchall()


def find_overlapping_reports(conn, start_date: str, end_date: str, exclude_id: int | None = None):
    with conn.cursor() as cur:
        cur.execute(
            """
            SELECT id, label, start_date, end_date, source_file FROM report_period
            WHERE start_date <= %s AND end_date >= %s AND (%s::int IS NULL OR id != %s)
            ORDER BY start_date
            """,
            (end_date, start_date, exclude_id, exclude_id),
        )
        rows = cur.fetchall()
        for row in rows:
            row["start_date"] = str(row["start_date"])
            row["end_date"] = str(row["end_date"])
        return rows


def get_report(conn, report_id: int):
    with conn.cursor() as cur:
        cur.execute("SELECT * FROM report_period WHERE id = %s", (report_id,))
        rp = cur.fetchone()
        if rp is None:
            return None

        cur.execute(
            """
            SELECT m.code, f.code AS facility_code, mv.value, mv.note
            FROM metric_value mv
            JOIN metric m ON m.id = mv.metric_id
            JOIN facility f ON f.id = mv.facility_id
            WHERE mv.report_period_id = %s
            """,
            (report_id,),
        )
        metrics: dict = {}
        for row in cur.fetchall():
            entry = metrics.setdefault(row["code"], {"CS1": None, "CS2": None, "TOTAL": None, "note": None})
            entry[row["facility_code"]] = row["value"]
            if row["note"]:
                entry["note"] = row["note"]

        cur.execute(
            """
            SELECT department, incident_date, description, cause, corrective_action, resolved, severity
            FROM incident WHERE report_period_id = %s ORDER BY id
            """,
            (report_id,),
        )
        incidents = cur.fetchall()

        cur.execute(
            """
            SELECT date, department, type, content, cause, resolution
            FROM feedback WHERE report_period_id = %s ORDER BY id
            """,
            (report_id,),
        )
        feedback = cur.fetchall()

        cur.execute(
            "SELECT section_name, content FROM narrative_section WHERE report_period_id = %s ORDER BY id",
            (report_id,),
        )
        narrative_sections = cur.fetchall()

    return {
        "id": rp["id"],
        "label": rp["label"],
        "start_date": str(rp["start_date"]),
        "end_date": str(rp["end_date"]),
        "source_file": rp["source_file"],
        "metrics": metrics,
        "incidents": incidents,
        "feedback": feedback,
        "narrative_sections": narrative_sections,
    }


def _insert_children(conn, report_id: int, data: dict, facility_ids: dict, metric_ids: dict):
    with conn.cursor() as cur:
        for code, values in data.get("metrics", {}).items():
            metric_id = metric_ids.get(code)
            if metric_id is None:
                continue
            note = values.get("note")
            for fac_code in FACILITY_CODES:
                value = values.get(fac_code)
                if value is None:
                    continue
                cur.execute(
                    "INSERT INTO metric_value (report_period_id, facility_id, metric_id, value, note) "
                    "VALUES (%s, %s, %s, %s, %s)",
                    (report_id, facility_ids[fac_code], metric_id, value, note),
                )

        for inc in data.get("incidents", []):
            cur.execute(
                "INSERT INTO incident (report_period_id, department, incident_date, description, cause, "
                "corrective_action, resolved, severity) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
                (
                    report_id, inc.get("department"), inc.get("incident_date"), inc.get("description"),
                    inc.get("cause"), inc.get("corrective_action"), bool(inc.get("resolved")), inc.get("severity"),
                ),
            )

        for fb in data.get("feedback", []):
            cur.execute(
                "INSERT INTO feedback (report_period_id, date, department, type, content, cause, resolution) "
                "VALUES (%s, %s, %s, %s, %s, %s, %s)",
                (
                    report_id, fb.get("date"), fb.get("department"), fb.get("type"), fb.get("content"),
                    fb.get("cause"), fb.get("resolution"),
                ),
            )

        for sec in data.get("narrative_sections", []):
            cur.execute(
                "INSERT INTO narrative_section (report_period_id, section_name, content) VALUES (%s, %s, %s)",
                (report_id, sec.get("section_name"), sec.get("content")),
            )


def create_report(conn, data: dict, source_file: str) -> int:
    facility_ids = _facility_ids(conn)
    metric_ids = _metric_ids(conn)
    try:
        with conn.cursor() as cur:
            cur.execute(
                "INSERT INTO report_period (label, start_date, end_date, source_file) "
                "VALUES (%s, %s, %s, %s) RETURNING id",
                (data["label"], data["start_date"], data["end_date"], source_file),
            )
            report_id = cur.fetchone()["id"]
        _insert_children(conn, report_id, data, facility_ids, metric_ids)
        conn.commit()
        return report_id
    except Exception:
        conn.rollback()
        raise


def _delete_children(conn, report_id: int):
    with conn.cursor() as cur:
        cur.execute("DELETE FROM metric_value WHERE report_period_id = %s", (report_id,))
        cur.execute("DELETE FROM incident WHERE report_period_id = %s", (report_id,))
        cur.execute("DELETE FROM feedback WHERE report_period_id = %s", (report_id,))
        cur.execute("DELETE FROM narrative_section WHERE report_period_id = %s", (report_id,))


def update_report(conn, report_id: int, data: dict) -> bool:
    facility_ids = _facility_ids(conn)
    metric_ids = _metric_ids(conn)
    try:
        with conn.cursor() as cur:
            cur.execute(
                "UPDATE report_period SET label = %s, start_date = %s, end_date = %s WHERE id = %s",
                (data["label"], data["start_date"], data["end_date"], report_id),
            )
            if cur.rowcount == 0:
                conn.rollback()
                return False
        _delete_children(conn, report_id)
        _insert_children(conn, report_id, data, facility_ids, metric_ids)
        conn.commit()
        return True
    except Exception:
        conn.rollback()
        raise


def delete_report(conn, report_id: int) -> bool:
    try:
        _delete_children(conn, report_id)
        with conn.cursor() as cur:
            cur.execute("DELETE FROM report_period WHERE id = %s", (report_id,))
            deleted = cur.rowcount > 0
        conn.commit()
        return deleted
    except Exception:
        conn.rollback()
        raise
