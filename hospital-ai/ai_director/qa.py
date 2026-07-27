"""
AI Director Q&A prototype for the weekly report data (Neon Postgres).

Groq-backed tool-calling loop with two tools:
  - query_metrics(sql):    run a read-only SELECT against the structured
                            metric_value fact table (quantitative questions)
  - search_reports(keyword): keyword search over incidents, feedback, and
                            free-text departmental narrative (qualitative
                            questions: serious issues, ICU, complaints...)

This is a fast prototype standing in for the future rag/ and text_to_sql/
services described in hospital-ai/README.md. Wrong answers are expected and
acceptable at this stage - the goal is an end-to-end testable slice.

Usage:
    python3 qa.py "tuần vừa rồi có bao nhiêu bệnh nhân ra viện?"
    python3 qa.py          # interactive REPL
"""
import json
import os
import sys
from pathlib import Path

import psycopg
from psycopg.rows import dict_row
from dotenv import load_dotenv
from groq import Groq

load_dotenv(Path(__file__).parent.parent / ".env")

DATABASE_URL = os.environ["DATABASE_URL"]
GROQ_API_KEY = os.environ["GROQ_API_KEY"]
MODEL = "openai/gpt-oss-120b"

FORBIDDEN_SQL_KEYWORDS = (
    "insert", "update", "delete", "drop", "alter", "truncate", "create",
    "grant", "revoke", "--", ";",
)


def get_conn():
    return psycopg.connect(DATABASE_URL, row_factory=dict_row)


def build_schema_context(conn) -> str:
    with conn.cursor() as cur:
        cur.execute("SELECT id, label, start_date, end_date FROM report_period ORDER BY start_date")
        periods = cur.fetchall()
        cur.execute("SELECT id, code, name FROM facility ORDER BY id")
        facilities = cur.fetchall()
        cur.execute("SELECT code, category, name, unit FROM metric ORDER BY category, code")
        metrics = cur.fetchall()

    periods_txt = "\n".join(
        f"  - id={p['id']}: {p['label']} ({p['start_date']} -> {p['end_date']})" for p in periods
    )
    facilities_txt = "\n".join(f"  - id={f['id']}: {f['code']} ({f['name']})" for f in facilities)
    metrics_txt = "\n".join(
        f"  - {m['code']} [{m['category']}]: {m['name']} (đơn vị: {m['unit']})" for m in metrics
    )

    return f"""
SCHEMA (Postgres):
  report_period(id, label, start_date, end_date, source_file)
  facility(id, code, name)                 -- code in ('CS1','CS2','TOTAL')
  metric(id, code, category, name, unit)
  metric_value(report_period_id, facility_id, metric_id, value, note)
  incident(report_period_id, department, incident_date, description, cause, corrective_action, resolved, severity)
  feedback(report_period_id, date, department, type, content, cause, resolution)  -- type in ('complaint','praise')
  narrative_section(report_period_id, section_name, content)

REPORT PERIODS (3 tuần đã có dữ liệu, KHÔNG liên tục - cách nhau nhiều tháng/năm):
{periods_txt}

FACILITIES:
{facilities_txt}

METRICS AVAILABLE (dùng metric.code trong SQL, join qua metric_value.metric_id = metric.id):
{metrics_txt}

GHI CHÚ QUAN TRỌNG:
- Dữ liệu KHÔNG có bảng số liệu ICU/Hồi sức riêng (số giường, công suất, ca nặng). ICU chỉ xuất hiện
  rải rác trong narrative_section/incident dạng text (vd đào tạo điều dưỡng khối Hồi sức - Cấp cứu,
  1 sự cố tại khoa Hồi sức ngoại). Nếu được hỏi số liệu ICU, PHẢI nói rõ là không có bảng số liệu ICU
  trong nguồn dữ liệu hiện tại, chỉ có thể trả lời dựa trên các đoạn text nhắc đến liên quan.
- facility_id=TOTAL nghĩa là số liệu toàn viện (2 cơ sở gộp), không phải luôn bằng CS1+CS2 cộng lại
  (một số dòng chỉ có TOTAL, không tách CS1/CS2).
- QUAN TRỌNG - TRÁNH NHÂN ĐÔI: mỗi metric_value đã có SẴN 3 dòng riêng (CS1, CS2, TOTAL) cho cùng
  1 report_period + metric. Khi câu hỏi hỏi số liệu "toàn viện"/"tổng"/không chỉ rõ cơ sở nào, PHẢI
  lọc facility.code = 'TOTAL' và lấy value của dòng đó, TUYỆT ĐỐI KHÔNG SUM() nhiều dòng facility lại
  với nhau (SUM(CS1+CS2+TOTAL) sẽ ra gấp đôi giá trị đúng). Chỉ cộng CS1+CS2 thủ công nếu dòng TOTAL
  không tồn tại cho metric đó.
"""


def query_metrics(conn, sql: str):
    lowered = sql.strip().lower()
    if not lowered.startswith("select"):
        return {"error": "Chỉ được chạy câu lệnh SELECT."}
    for kw in FORBIDDEN_SQL_KEYWORDS:
        if kw in lowered:
            return {"error": f"Từ khóa không được phép trong SQL: {kw}"}
    try:
        with conn.cursor() as cur:
            cur.execute(sql)
            rows = cur.fetchmany(200)
            return {"rows": rows, "row_count": len(rows)}
    except Exception as e:
        conn.rollback()
        return {"error": str(e)}


def search_reports(conn, keyword: str):
    like = f"%{keyword}%"
    results = []
    with conn.cursor() as cur:
        cur.execute(
            """
            SELECT rp.label, n.section_name, n.content
            FROM narrative_section n JOIN report_period rp ON rp.id = n.report_period_id
            WHERE n.content ILIKE %s OR n.section_name ILIKE %s
            LIMIT 20
            """,
            (like, like),
        )
        results.append({"source": "narrative_section", "matches": cur.fetchall()})

        cur.execute(
            """
            SELECT rp.label, i.department, i.incident_date, i.description, i.cause,
                   i.corrective_action, i.resolved, i.severity
            FROM incident i JOIN report_period rp ON rp.id = i.report_period_id
            WHERE i.description ILIKE %s OR i.department ILIKE %s OR i.cause ILIKE %s
            LIMIT 20
            """,
            (like, like, like),
        )
        results.append({"source": "incident", "matches": cur.fetchall()})

        cur.execute(
            """
            SELECT rp.label, f.date, f.department, f.type, f.content, f.cause, f.resolution
            FROM feedback f JOIN report_period rp ON rp.id = f.report_period_id
            WHERE f.content ILIKE %s OR f.department ILIKE %s
            LIMIT 20
            """,
            (like, like),
        )
        results.append({"source": "feedback", "matches": cur.fetchall()})
    return results


TOOLS = [
    {
        "type": "function",
        "function": {
            "name": "query_metrics",
            "description": (
                "Chạy một câu SQL SELECT read-only trên các bảng số liệu định lượng "
                "(metric_value, metric, facility, report_period) để trả lời câu hỏi về số liệu, "
                "ví dụ số bệnh nhân ra viện, số ca phẫu thuật, doanh thu..."
            ),
            "parameters": {
                "type": "object",
                "properties": {
                    "sql": {"type": "string", "description": "Câu lệnh SELECT hợp lệ với Postgres."},
                },
                "required": ["sql"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "search_reports",
            "description": (
                "Tìm kiếm từ khóa trong nội dung tường thuật (narrative_section), sự cố y khoa (incident), "
                "và góp ý/khiếu nại người bệnh (feedback) để trả lời câu hỏi định tính, ví dụ 'có vấn đề gì "
                "nghiêm trọng không', 'tình hình ICU thế nào', 'có khiếu nại gì không'."
            ),
            "parameters": {
                "type": "object",
                "properties": {
                    "keyword": {"type": "string", "description": "Từ khóa tiếng Việt để tìm kiếm, vd 'ICU', 'hồi sức', 'sự cố', 'khiếu nại'."},
                },
                "required": ["keyword"],
            },
        },
    },
]


def ask(question: str, conn, groq_client, schema_context: str, verbose: bool = False) -> str:
    system_prompt = f"""Bạn là trợ lý AI Director cho Ban Giám đốc bệnh viện, trả lời dựa trên dữ liệu
báo cáo giao ban tuần đã được nạp vào Postgres. Luôn trả lời bằng tiếng Việt, ngắn gọn, rõ ràng,
và LUÔN trích dẫn tuần/report_period nguồn cho mỗi số liệu hoặc sự việc bạn nêu ra.
Dùng tool query_metrics cho câu hỏi định lượng (số liệu), dùng tool search_reports cho câu hỏi định tính
(sự cố, khiếu nại, tình hình một khoa/phòng). Nếu dữ liệu không có, hãy nói rõ là không có thay vì đoán.

{schema_context}
"""
    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": question},
    ]

    for _ in range(6):
        response = groq_client.chat.completions.create(
            model=MODEL,
            messages=messages,
            tools=TOOLS,
            tool_choice="auto",
            temperature=0.1,
        )
        msg = response.choices[0].message

        if not msg.tool_calls:
            return msg.content

        messages.append({"role": "assistant", "content": msg.content, "tool_calls": msg.tool_calls})

        for tool_call in msg.tool_calls:
            fn_name = tool_call.function.name
            args = json.loads(tool_call.function.arguments)
            if verbose:
                print(f"  [tool call] {fn_name}({args})", file=sys.stderr)

            if fn_name == "query_metrics":
                result = query_metrics(conn, args.get("sql", ""))
            elif fn_name == "search_reports":
                result = search_reports(conn, args.get("keyword", ""))
            else:
                result = {"error": f"Unknown tool {fn_name}"}

            messages.append({
                "role": "tool",
                "tool_call_id": tool_call.id,
                "content": json.dumps(result, ensure_ascii=False, default=str),
            })

    return "(Không lấy được câu trả lời cuối cùng sau nhiều lượt gọi tool.)"


def main():
    conn = get_conn()
    groq_client = Groq(api_key=GROQ_API_KEY)
    schema_context = build_schema_context(conn)

    verbose = "-v" in sys.argv
    args = [a for a in sys.argv[1:] if a != "-v"]

    if args:
        question = " ".join(args)
        print(ask(question, conn, groq_client, schema_context, verbose=verbose))
    else:
        print("AI Director Q&A (Ctrl+C để thoát)")
        while True:
            try:
                question = input("\n> ")
            except (EOFError, KeyboardInterrupt):
                break
            if not question.strip():
                continue
            print(ask(question, conn, groq_client, schema_context, verbose=verbose))

    conn.close()


if __name__ == "__main__":
    main()
