---
title: "AI Director — Tài liệu kỹ thuật"
subtitle: "Luồng hoạt động, công nghệ sử dụng, và hạ tầng triển khai"
date: "Cập nhật: 28/08/2026"
---

# AI Director — Tài liệu kỹ thuật

> Tài liệu này mô tả **service `hospital-ai/ai_director/`** đang chạy thật tại
> `https://ksbvapi.tuandv.id.vn/ai-director/` — không phải "AI Director" demo
> rule-based trong `hospital-backend`/`hospital-ui` (xem mục 1 để phân biệt).

---

## 1. Lưu ý quan trọng: có 2 tính năng cùng tên "AI Director"

Repo `hospital-ai-copilot` có **hai thứ khác nhau** đều tên là "AI Director", dễ nhầm:

| | **AI Director (demo rule-based)** | **AI Director (service thật — tài liệu này nói về cái này)** |
|---|---|---|
| Vị trí code | `hospital-backend/` (Java Quarkus) + `hospital-ui/` (React, trang `AiDirectorPage.tsx`) | `hospital-ai/ai_director/` (Python FastAPI) |
| Cách trả lời | Rule-based trên dữ liệu Mini HIS "hôm nay" (không dùng LLM) | Groq LLM, trả lời dựa trên báo cáo giao ban tuần đã nạp |
| Truy cập | Trong app quản trị `hospital-ui` (cần đăng nhập) | `https://ksbvapi.tuandv.id.vn/ai-director/` (public, không đăng nhập) |
| Trạng thái | Demo, ghi rõ "answers are computed from live data with business rules, not a language model — yet" | Prototype đang dùng thật, có upload/sửa/xoá báo cáo |

Toàn bộ phần còn lại của tài liệu chỉ nói về **service thật (bên phải)**.

---

## 2. Tổng quan chức năng

Service trả lời câu hỏi của Ban Giám đốc bệnh viện dựa trên dữ liệu các **báo cáo giao ban tuần**
(weekly briefing report), và cho phép tự upload báo cáo mới (`.pptx`), sửa, xoá báo cáo đã có.

Hai luồng chính:

1. **Hỏi đáp (Q&A)** — `GET /` (trang chat) → `POST /ask`
2. **Quản lý báo cáo** — `GET /manage` (trang quản lý) → `POST /reports/extract`,
   `POST /reports`, `GET/PUT/DELETE /reports/{id}`

---

## 3. Công nghệ sử dụng

| Thành phần | Công nghệ | Vai trò |
|---|---|---|
| Web framework | **FastAPI** (Python 3.12) | Expose các route HTTP, chạy bằng `uvicorn` |
| Process manager | **PM2** | Giữ service chạy nền, tên process `ksbv-ai`, tự restart khi crash |
| Reverse proxy | **nginx** | `ksbvapi.tuandv.id.vn` → `location /ai-director/` → `proxy_pass http://127.0.0.1:8041/` |
| LLM | **Groq API**, model `openai/gpt-oss-120b` | **Cả 2 việc**: (1) trả lời câu hỏi qua tool-calling, (2) trích xuất dữ liệu có cấu trúc từ file PowerPoint upload — xem chi tiết mục 5 |
| Đọc file PowerPoint | **python-pptx** | Đọc text + bảng biểu từ file `.pptx` upload lên |
| Database | **Postgres tự cài trên VPS** (không còn dùng Neon từ 28/08/2026) | Lưu toàn bộ dữ liệu báo cáo, truy cập qua **psycopg3** (`psycopg[binary]`) |
| Frontend | **HTML/CSS/JS thuần** (không framework, không build step) | 2 trang: `/` (chat) và `/manage` (quản lý), render trực tiếp từ chuỗi Python trong `app.py` |

Không dùng React/Vue, không dùng ORM, không dùng authentication — xem mục 8 (Giới hạn/rủi ro).

---

## 4. Cấu trúc file

```
hospital-ai/
|-- .env                    # DATABASE_URL, GROQ_API_KEY
|-- requirements.txt        # psycopg, groq, fastapi, uvicorn,
|                           # python-pptx, python-multipart...
|-- ingest/
|   `-- seed_data.py        # Script 1 lần: gõ tay 3 báo cáo mẫu
|                           # đầu tiên vào DB (không dùng cho
|                           # báo cáo mới)
`-- ai_director/
    |-- app.py              # FastAPI app: routes + 2 trang HTML
    |                       # (TEST_PAGE, MANAGE_PAGE) nhúng
    |                       # thẳng dạng string Python
    |-- qa.py               # Logic hỏi-đáp: build_schema_context()
    |                       # + ask() (Groq tool-calling loop)
    `-- reports.py          # Logic upload/sửa/xoá: đọc pptx,
                             # gọi Groq trích xuất, CRUD Postgres
```

---

## 5. Groq được dùng ở đâu — chi tiết

Có **2 nơi gọi Groq**, dùng chung 1 model (`openai/gpt-oss-120b`) nhưng cho 2 mục đích khác nhau:

### 5.1. `qa.py` — trả lời câu hỏi (`POST /ask`)

- Xây `schema_context` (danh sách report_period, facility, metric hiện có trong DB) rồi đưa vào system prompt.
- Gọi Groq với **2 tool** (function-calling):
  - `query_metrics(sql)` — chạy 1 câu `SELECT` read-only trên bảng `metric_value` cho câu hỏi định lượng (số bệnh nhân, doanh thu...).
  - `search_reports(keyword)` — tìm từ khoá trong `narrative_section`, `incident`, `feedback` cho câu hỏi định tính (sự cố, khiếu nại...).
- Groq tự quyết định gọi tool nào, đọc kết quả tool trả về, rồi tổng hợp câu trả lời cuối bằng tiếng Việt.
- Vòng lặp tối đa 6 lượt gọi tool trước khi buộc phải trả lời.

### 5.2. `reports.py` — trích xuất dữ liệu khi upload (`POST /reports/extract`)

- **Không dùng tool-calling** — dùng `response_format: json_object` để ép Groq trả JSON đúng schema.
- **2 loại prompt riêng biệt**, gọi tuần tự (không gộp 1 lần — xem mục 7 tại sao):
  1. **Metrics prompt** (`_build_metrics_prompt`) — chỉ nhận phần *bảng số liệu* của file, trả về `{"metrics": {...}}` khớp danh sách `metric_code` hợp lệ trong DB.
  2. **Narrative prompt** (`_NARRATIVE_PROMPT`) — chỉ nhận phần *văn bản tự do* (tiêu đề, sự cố, khiếu nại/khen, tường thuật khoa/phòng), trả về `{"label", "start_date", "end_date", "incidents", "feedback", "narrative_sections"}`.
- Cả 2 loại đều dùng `temperature=0.1` (giảm ngẫu nhiên) và `reasoning_effort="low"` (model `gpt-oss-120b` là reasoning model — nếu không giới hạn, phần suy luận ẩn sẽ ăn hết ngân sách token, trả về nội dung rỗng).
- Nếu file lớn (nhiều slide), mỗi loại prompt được **chia nhỏ theo ranh giới slide** thành nhiều lượt gọi Groq tuần tự, kết quả merge lại — xem mục 7.

---

## 6. Luồng hoạt động chi tiết

### 6.1. Luồng hỏi đáp (Q&A)

```
Người dùng mở https://ksbvapi.tuandv.id.vn/ai-director/
        |
        v
GET /             trả TEST_PAGE (HTML/JS nhúng trong app.py)
        |
        v   (JS gọi khi trang load)
GET /reports      liệt kê các tuần hiện có
        |         hiện ghi chú "Dữ liệu từ N tuần: ..."
        |
        v   (người dùng gõ câu hỏi, bấm Gửi)
POST /ask {question}
        |
        |-> build_schema_context(conn)
        |     [Postgres - luôn build MỚI mỗi lần hỏi, không cache,
        |      để thấy ngay báo cáo vừa upload]
        |
        v
ask(question, conn, groq_client, schema_context)   [qa.py]
        |
        v
Groq chat.completions.create(
    model = gpt-oss-120b,
    tools = [query_metrics, search_reports]
)
        |
        |-> Groq gọi tool query_metrics(sql)
        |     --> chạy SELECT trên metric_value --> trả rows
        |-> Groq gọi tool search_reports(kw)
        |     --> ILIKE trên narrative/incident/feedback --> rows
        |        (lặp tối đa 6 vòng)
        v
Groq tổng hợp câu trả lời cuối
(tiếng Việt, có trích dẫn tuần nguồn)
        |
        v
Trả JSON {"answer": "..."} về trình duyệt
```

### 6.2. Luồng upload báo cáo mới

```
Người dùng vào /manage, bấm "+ Upload báo cáo mới", chọn file .pptx
        |
        v
POST /reports/extract (multipart file)
        |
        |-> extract_pptx_streams(file_bytes)   [python-pptx]
        |     Duyệt từng slide, từng shape:
        |       - shape có bảng (table)?
        |            cell chứa từ khoá định tính
        |            ("góp ý","khen","sự cố","khiếu nại",...)?
        |              CÓ    -> gộp vào narrative_text
        |                       (bảng khen/góp ý dạng PowerPoint
        |                        table vẫn vào luồng định tính,
        |                        không bị bỏ sót)
        |              KHÔNG -> gộp vào table_text (số liệu)
        |       - shape là text tự do? -> gộp vào narrative_text
        |
        v
extract_report_data(table_text, narrative_text, groq, conn)
        |
        |-> Chia table_text thành chunk <= 9000 ký tự
        |   theo ranh giới slide. Với mỗi chunk:
        |     _chat_json(metrics_prompt, chunk, max_tokens=2200)
        |       --> Groq
        |     merge vào dict `metrics` (chunk sau không ghi đè
        |     chunk trước nếu chunk trước đã có giá trị)
        |     sleep 8s trước chunk kế tiếp (né rate limit/phút)
        |
        |-> Chia narrative_text thành chunk tương tự. Với mỗi chunk:
        |     _chat_json(narrative_prompt, chunk, max_tokens=2400)
        |       --> Groq
        |     gộp (extend) incidents/feedback/narrative_sections;
        |     lấy label/start_date/end_date từ chunk đầu có giá trị
        |
        v
Trả JSON "draft" về trình duyệt - CHƯA GHI VÀO DATABASE
        |  (kèm cờ overlaps: có tuần nào trong DB trùng ngày không,
        |   cờ truncated: có bị cắt bớt do quá nhiều slide không)
        v
Trình duyệt render form review: bảng số liệu (32 chỉ số,
nhóm theo category), danh sách sự cố / khiếu nại-khen /
tường thuật - TẤT CẢ ĐỀU SỬA ĐƯỢC TRỰC TIẾP
        |
        v   (người dùng kiểm tra, sửa số liệu sai, bấm "Lưu báo cáo")
POST /reports {label, start_date, end_date, metrics,
               incidents, feedback, narrative_sections}
        |
        |-> Kiểm tra overlap ngày với báo cáo đã có
        |     (nếu có và chưa force=true -> trả 409,
        |      JS hiện confirm() hỏi "vẫn lưu?")
        |
        v
reports_svc.create_report(conn, data, source_file)
        |
        |-> INSERT report_period (label, start/end_date, source_file)
        |-> INSERT metric_value  x N (join facility_id + metric_id)
        |-> INSERT incident      x N
        |-> INSERT feedback      x N
        |-> INSERT narrative_section x N
        |     (tất cả trong 1 transaction - lỗi bước nào cũng
        |      rollback toàn bộ)
        v
commit() -> trả {"id": report_id}
         -> JS load lại danh sách, quay về màn hình list
```

### 6.3. Luồng sửa / xoá báo cáo đã có

- **Sửa**: `GET /reports/{id}` (đọc từ 5 bảng, dựng lại đúng shape draft) → hiện lên form giống hệt
  màn hình review upload → `PUT /reports/{id}` → `update_report()`: update `report_period`,
  **xoá hết** `metric_value`/`incident`/`feedback`/`narrative_section` cũ của tuần đó rồi **insert lại
  toàn bộ** từ dữ liệu mới (không diff từng dòng) — cũng trong 1 transaction.
- **Xoá**: `DELETE /reports/{id}` → xoá 4 bảng con trước, rồi xoá `report_period` (không có
  `ON DELETE CASCADE` ở schema nên phải xoá tay theo đúng thứ tự).

---

## 7. Vì sao phải "chunk" và "sleep" — giới hạn thật của Groq

Đây là phần quan trọng nhất để hiểu tại sao code không đơn giản là "gửi cả file cho Groq 1 lần":

- Tài khoản Groq của tổ chức này, ở gói **on-demand (miễn phí)**, giới hạn model `openai/gpt-oss-120b`:
  - **8.000 token/phút** (TPM) — tính trên **1 request đơn lẻ** (prompt + `max_tokens` dự phòng cho
    completion), không phải tổng dồn.
  - **200.000 token/ngày** (TPD) — dùng chung cho cả tính năng hỏi-đáp lẫn trích xuất báo cáo.
- Một báo cáo tuần thật (~30-50 slide) đọc hết bằng `python-pptx` dễ vượt 8.000 token nếu gửi
  nguyên khối 1 lần → Groq trả lỗi `413 rate_limit_exceeded` ngay lập tức (không phải lỗi tạm thời,
  gửi lại y hệt sẽ luôn lỗi).
- Cách xử lý:
  1. **Tách 2 luồng riêng** (bảng số liệu / văn bản định tính) thay vì gộp 1 prompt — giảm ~40-50%
     kích thước mỗi request.
  2. **Chia nhỏ theo ranh giới slide** (`_chunk_by_slide`, tối đa 9.000 ký tự/chunk, trần 6 chunk/luồng)
     nếu vẫn còn dài, thay vì cắt cụt (tránh mất dữ liệu ở cuối file).
  3. **`reasoning_effort="low"`** — `gpt-oss-120b` là model có suy luận ẩn (chain-of-thought), phần
     suy luận này cũng tính vào `max_tokens`; nếu để mặc định, suy luận ăn hết ngân sách và trả về
     nội dung rỗng (`content == ""`).
  4. **`time.sleep(8)`** giữa các chunk cùng loại — bucket token/phút của Groq hồi phục dần theo thời
     gian thực (~133 token/giây), nghỉ giữa các lượt gọi giúp chunk sau không bị dính rate limit do
     chunk trước vừa dùng gần hết hạn mức.
  5. Lỗi `429` (hết hạn mức tạm thời) được **retry 1 lần** sau khi chờ; lỗi `413` (bản thân request
     quá lớn) thì **fail ngay**, không retry vì gửi lại y hệt sẽ luôn lỗi.
- Hệ quả thực tế: trích xuất 1 báo cáo lớn có thể mất **1-3 phút** (nhiều lượt gọi Groq tuần tự +
  các khoảng nghỉ) — UI đã có animation quay + đếm giây để người dùng biết trang không bị treo.

---

## 8. Độ chính xác AI — vì sao luôn có bước review trước khi lưu

- `POST /reports/extract` **không bao giờ ghi vào Postgres** — chỉ trả JSON "draft" để người dùng xem
  và sửa. Việc ghi DB chỉ xảy ra khi người dùng chủ động bấm "Lưu báo cáo" (`POST/PUT /reports`).
- Lý do bắt buộc phải có bước review (phát hiện được trong quá trình test bằng file thật):
  - Một số báo cáo dùng bảng so sánh 3 cột (**trung bình tháng trước | tuần này | chênh lệch**) thay
    vì cột CS1/CS2/TOTAL như 3 báo cáo mẫu đầu tiên — ban đầu AI hay lấy nhầm cột trung bình tháng
    trước thay vì cột tuần báo cáo. Đã sửa bằng cách thêm ví dụ cụ thể vào prompt, nhưng không đảm bảo
    đúng 100% với mọi định dạng bảng biểu tương lai.
  - Nội dung khen/góp ý đôi khi nằm trong **bảng PowerPoint** thay vì văn bản tự do — nếu không phân
    loại đúng, phần này bị bỏ sót hoàn toàn (đã sửa bằng keyword-matching, xem mục 6.2).
  - Prompt luôn dặn model để `null`/bỏ qua nếu không chắc chắn thay vì tự bịa số, nhưng đây là chỉ dẫn
    (instruction), không phải ràng buộc cứng (constraint) — không thể đảm bảo tuyệt đối.
- Vì dữ liệu dùng để báo cáo cho Ban Giám đốc, **không nên tắt bước review** kể cả khi AI đã "nhìn có
  vẻ đúng".

---

## 9. Schema Database (Postgres)

```
report_period(id, label, start_date, end_date, source_file)

facility(id, code, name)
    -- code in {CS1, CS2, TOTAL}

metric(id, code, category, name, unit)
    -- danh mục ~32 chỉ số cố định

metric_value(id, report_period_id, facility_id, metric_id,
             value, note)

incident(id, report_period_id, department, incident_date,
         description, cause, corrective_action, resolved,
         severity)

feedback(id, report_period_id, date, department, type,
         content, cause, resolution)
    -- type in {complaint, praise}

narrative_section(id, report_period_id, section_name, content)
```

- `facility`/`metric` là bảng danh mục cố định, được seed 1 lần (`ingest/seed_data.py`), **không** tạo
  thêm khi upload báo cáo mới — báo cáo mới chỉ tham chiếu tới `metric_code` đã có sẵn.
- Không có foreign key `ON DELETE CASCADE` — mọi xoá/sửa phải tự xoá bảng con trước (xem `_delete_children`
  trong `reports.py`).
- 3 báo cáo đầu tiên (20/04/2022, 09/07/2025, 03/06/2026) được **gõ tay** từ file `.ppt` gốc vào
  `seed_data.py` — không đi qua pipeline AI. Từ báo cáo thứ 4 trở đi mới dùng pipeline upload tự động
  mô tả ở mục 6.2.

---

## 10. Hạ tầng & triển khai

| | Giá trị |
|---|---|
| Server | Galaxy Cloud VPS `103.163.216.32` (user `pc1`) |
| Process | PM2 process tên `ksbv-ai` — `uvicorn ai_director.app:app --host 127.0.0.1 --port 8041` |
| Domain | `ksbvapi.tuandv.id.vn` (nginx, SSL Let's Encrypt) |
| nginx routing | `/` → cổng `8040` (Java Quarkus `ksbvapi`, không liên quan) · `/ai-director/` → `proxy_pass http://127.0.0.1:8041/` (bỏ tiền tố path) |
| nginx config đặc biệt cho `/ai-director/` | `client_max_body_size 30m;` (mặc định 1MB không đủ cho file .pptx) · `proxy_read/send/connect_timeout 280s;` (mặc định 60s không đủ cho trích xuất nhiều chunk) |
| Database | Postgres 16 tự cài trên cùng VPS (database `ksbvai`, user `ksbvai_user`, bind `127.0.0.1:5432`) — trước 28/08/2026 dùng Neon, đã migrate về local để đồng bộ với các app khác trong hạ tầng. Connection string trong `.env` (`DATABASE_URL`) |
| Deploy | `git push` → `ssh pc1@103.163.216.32 'bash ~/deploy-ksbv-ai.sh'` → script: `git pull` + `pip install -r requirements.txt` + `pm2 restart ksbv-ai` |
| CI/CD tự động | **Không** — service này nằm ngoài cron auto-deploy 2 phút áp dụng cho các app khác trong hạ tầng, phải deploy tay bằng script trên |

---

## 11. Giới hạn & rủi ro đã biết

- **Không có authentication** trên bất kỳ route nào (`/ask`, `/reports/*`, `/manage`) — ai có URL đều
  gọi được, kể cả xoá/sửa báo cáo. Domain không public link ở đâu ngoài nội bộ, nhưng đây vẫn là
  thiếu sót cần lưu ý nếu mở rộng phạm vi sử dụng.
- **Không rate-limit ở tầng ứng dụng** — nếu nhiều người dùng upload cùng lúc, hạn mức Groq
  (8.000 TPM / 200.000 TPD) dùng chung sẽ cạn rất nhanh và tất cả cùng gặp lỗi.
- **Không versioning/audit log** cho sửa/xoá báo cáo — `UPDATE`/`DELETE` ghi đè trực tiếp, không giữ
  lịch sử bản cũ.
- **1 kết nối Postgres dùng chung** (biến global `_conn` trong `app.py`) cho toàn bộ request, tự
  reconnect nếu kết nối bị đóng do idle — mô hình đơn giản, phù hợp lưu lượng thấp hiện tại nhưng
  không scale tốt nếu có nhiều request đồng thời.
- **Gói Groq miễn phí giới hạn thấp** (mục 7) — nếu tần suất sử dụng tăng, nên cân nhắc nâng cấp lên
  Dev Tier trên Groq Console (quyết định thuộc về người quản lý tài khoản, không tự động hoá được).

---

## 12. Tóm tắt nhanh — "Groq dùng ở đâu?"

| Route | File | Việc Groq làm | Số lượt gọi Groq |
|---|---|---|---|
| `POST /ask` | `qa.py::ask()` | Trả lời câu hỏi, tự gọi tool SQL/keyword-search | 1 request ban đầu + tối đa 6 vòng tool-call |
| `POST /reports/extract` | `reports.py::extract_report_data()` | Trích xuất số liệu (metrics prompt) | 1 lượt/chunk bảng số liệu (thường 1, tối đa 6) |
| `POST /reports/extract` | `reports.py::extract_report_data()` | Trích xuất định tính (narrative prompt) | 1 lượt/chunk văn bản (thường 1, tối đa 6) |

Model dùng ở cả 3 trường hợp trên: **`openai/gpt-oss-120b`** qua Groq API (biến `MODEL` trong `qa.py`
và `reports.py`), key cấu hình trong `hospital-ai/.env` (`GROQ_API_KEY`).
