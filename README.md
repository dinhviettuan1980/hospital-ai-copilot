# Hospital AI Copilot (Reference Implementation)

> An open-source, educational reference implementation of an AI Hospital
> Copilot architecture for Hospital Directors — combining a minimal
> operational system, a data warehouse, a knowledge base, RAG, Text-to-SQL,
> and an AI orchestration layer.

**This is not a Hospital Information System (HIS), not a full hospital
simulation, and not an ERP.** It models the *architecture* proposed in the
"AI Hospital Copilot" strategy document at a scale one developer can build
and one reader can fully understand. See
[docs/02_VISION.md](docs/02_VISION.md).

## Current Status

**Sprint D1 — Hospital Discovery.** Sprint 0 delivered documentation only
(see `docs/`). Sprint 1 delivered a working Mini HIS prototype (Department,
Patient, Visit CRUD). Sprint 2 turned that prototype into an executive demo
(**Executive Dashboard**, **Command Center**, a demo **AI Director** chat,
and a **Knowledge Center**). Sprint D1 adds a module that is deliberately
*not* about AI or the Mini HIS at all: **Hospital Discovery**, a digital
questionnaire that lets Solution Architects and Business Analysts survey a
hospital's people, process, and systems before any implementation begins —
see [Hospital Discovery Module](#hospital-discovery-module) below. Still no
Docker, Kafka, ClickHouse, Python, or LLM — see
[deviations from the original roadmap](#future-roadmap) below.

## Start Here

Read the documentation in numbered order — each document builds on the
last:

| # | Document | What It Answers |
|---|---|---|
| 01 | [Project Charter](docs/01_PROJECT_CHARTER.md) | Why does this project exist, and what governs it? |
| 02 | [Vision](docs/02_VISION.md) | What does success look like? |
| 03 | [Scope](docs/03_SCOPE.md) | What's in, precisely? |
| 04 | [Goals](docs/04_GOALS.md) | What are we trying to achieve, by horizon? |
| 05 | [Non-Goals](docs/05_NON_GOALS.md) | What's explicitly excluded, and why? |
| 06 | [Success Metrics](docs/06_SUCCESS_METRICS.md) | How do we know we succeeded? |
| 07 | [Personas](docs/07_PERSONAS.md) | Who is this for? |
| 08 | [High-Level Architecture](docs/08_HIGH_LEVEL_ARCHITECTURE.md) | How is the system structured (C4 Context + Container)? |
| 09 | [Tech Stack](docs/09_TECH_STACK.md) | What technology, and why? |
| 10 | [Domain Overview](docs/10_DOMAIN_OVERVIEW.md) | What's the minimal hospital domain model? |
| 11 | [Roadmap](docs/11_ROADMAP.md) | What gets built, and in what order? |
| 12 | [Project Rules](docs/12_PROJECT_RULES.md) | What rules govern every contribution? |
| 13 | [Glossary](docs/13_GLOSSARY.md) | What do the terms mean? |

## Sprint 2 Features

| Feature | What it is | Backend | Frontend |
|---|---|---|---|
| **Executive Dashboard** | 8 KPI cards a director actually cares about: Today's Patients, Today's Visits, Bed Occupancy, ICU Occupancy, Today's Surgeries, Emergency Cases, Average Waiting Time, Today's Revenue. | `GET /api/dashboard/executive-summary` | `DashboardPage` |
| **Command Center** | Simple, explainable threshold rules (no ML) over live data, surfaced as GREEN/YELLOW/RED alerts — e.g. "ICU nearly full," "Emergency waiting time too high," "Revenue lower than yesterday," "High patient volume." | `GET /api/command-center/status` | `CommandCenterPage` |
| **AI Director (demo)** | A ChatGPT-style interface. The backend has **no LLM** — `RuleBasedAiDirectorEngine` matches keywords to known questions and answers them with real aggregation queries over the same data the Dashboard uses, returning clean JSON (`answer`, `intent`, `data`). It implements the `AiDirectorEngine` interface, so a future LLM/RAG/Text-to-SQL-backed engine can be swapped in without touching the controller or the UI. | `POST /api/ai-director/ask` | `AiDirectorPage` |
| **Knowledge Center** | Categories + documents (PDF/DOCX upload, search by title, filter by category, download, delete). Metadata lives in PostgreSQL; files live on the local filesystem (`hospital.knowledge.storage-path`, default `hospital-backend/data/knowledge-uploads/`). No embeddings, no vector DB, no RAG — this sprint only prepares the future Knowledge Base. | `GET/POST /api/knowledge/categories`, `GET/POST /api/knowledge/documents`, `GET /api/knowledge/documents/{id}/download`, `DELETE /api/knowledge/documents/{id}` | `KnowledgeCenterPage` |

Demo data was extended accordingly: departments are now Emergency, ICU,
Cardiology, Surgery, and Outpatient (matching what the Executive
Dashboard/Command Center need); beds were added per department (ICU
seeded deliberately near capacity so the Command Center has something real
to flag); and every visit now carries a demo waiting time and charge.

## Hospital Discovery Module

### Purpose

Hospital Discovery is a digital replacement for the Word/Excel/paper
questionnaires a Solution Architect or Business Analyst would otherwise use
to survey a hospital before designing a solution. It is **completely
independent of the Mini HIS** (Department/Patient/Visit) — it is
pre-implementation requirement-gathering, not operational hospital data.
The goal is understanding a hospital, not software development; nothing in
this module is AI. What it collects is designed to later feed solution
architecture design, cost estimation, risk identification, roadmap
generation, and — eventually — the AI modules described elsewhere in this
README, via the JSON export below.

### Workflow

1. **Discovery Projects** (`/discovery/projects`) — one CRUD record per
   hospital survey: project name, hospital name, contact person/email/phone,
   survey date, status (`DRAFT` / `IN_PROGRESS` / `COMPLETED`), notes.
2. Opening a project goes to its **workspace**
   (`/discovery/projects/:id`) — a Notion/Jira-style layout: a sidebar of
   **sections** (20 of them: Hospital Overview, Organization, Business
   Process, IT Landscape, HIS, EMR, LIS, PACS, RIS, ERP, HRM, Data
   Warehouse, Dashboard, KPI, Integration, Infrastructure, Security, AI
   Readiness, Risks, Recommendations — each shows an answered/total badge),
   and a main panel of **question cards** for the selected section.
3. Each question card is independent — its own answer input (the type
   varies per question: Text, Number, Date, Yes/No, Single Choice, Multiple
   Choice, Rating, File Attachment, or URL), an optional comment, an
   optional risk level (Low/Medium/High), and — for evidence — file
   attachments (PDF/DOCX/XLSX/PNG/JPG). There is no single giant form: every
   card **auto-saves independently** ~600ms after you stop typing/selecting,
   with an inline "Saving.../Saved" indicator.
4. Progress (section-level and project-level) is computed automatically —
   answered questions ÷ total questions in the catalog — and shown as a
   progress bar everywhere a project appears.
5. **Export** (workspace header) downloads the complete survey as JSON.
   **Import** (`/discovery/projects`, "⬆ Import JSON") reads that same JSON
   and creates a **new** project from it — useful for migrating a survey
   between environments or duplicating one as a starting template. Import
   never overwrites an existing project.

### Database

Five tables, independent of every Mini HIS table:

| Table | Purpose |
|---|---|
| `discovery_project` | One row per hospital survey (the CRUD entity above). |
| `discovery_section` | Global, shared catalog of the 20 questionnaire sections — not owned by any one project. |
| `discovery_question` | Global, shared catalog of ~200 questions, each belonging to a section, with a configurable `answer_type` and (for choice questions) a JSON `options` column. Seeded automatically — **never hardcoded in the frontend**, which renders whatever the API returns. |
| `discovery_answer` | One row per (project, question) pair — the upsert target for auto-save. Holds the answer value, comment, and risk level. |
| `discovery_attachment` | Uploaded file metadata (project-scoped, optionally tied to a question); bytes live on the local filesystem (`hospital.discovery.storage-path`, default `hospital-backend/data/discovery-attachments/`), same pattern as the Knowledge Center. |

Questions and sections are read-only from the frontend in this sprint — the
catalog is seeded once and is intentionally not user-editable yet (see
[Future Roadmap](#future-roadmap)).

### Export Format

`GET /api/discovery/projects/{id}/export` returns a single JSON document
designed to be clean, complete, and extensible enough for a future AI
module to consume directly:

```json
{
  "exportVersion": 1,
  "exportedAt": "2026-07-16T09:26:24Z",
  "project": { "projectName": "...", "hospitalName": "...", "status": "IN_PROGRESS", "...": "..." },
  "progress": { "totalQuestions": 200, "answeredQuestions": 80, "percent": 40.0 },
  "sections": [
    {
      "code": "HIS", "name": "HIS", "description": "...", "displayOrder": 5,
      "questions": [
        {
          "code": "HIS-01", "title": "...", "answerType": "YES_NO", "options": [],
          "answer": { "value": "Yes", "comment": "...", "riskLevel": "MEDIUM" },
          "attachments": [{ "fileName": "vendor-contract.pdf", "contentType": "application/pdf", "fileSize": 48213 }]
        }
      ]
    }
  ]
}
```

This exact shape is also the **import** request body — export and import
are symmetric by design. Attachment entries in the export are metadata
only (filename/type/size); the file bytes themselves stay on the
filesystem and are not embedded in the JSON, so import restores answers
but not attachment files.

## Repository Structure

```
hospital-ai-copilot/
  docs/                   # All governance and architecture documentation
    architecture/         # Detailed/expanded architecture records (C4 levels)
    adr/                  # Architecture Decision Records (process ready, none recorded yet)
    backlog/              # Epics (implementation tasks come later, per Epic, per sprint)
    api/                  # API contracts (empty until implementation sprints)
    database/             # Concrete schema design (empty until implementation sprints)
    ui/                   # UI/UX design artifacts (empty until implementation sprints)
  hospital-backend/       # Sprint 1: Quarkus + PostgreSQL API (Department/Patient/Visit)
  hospital-ui/            # Sprint 1: React + TypeScript + Vite + Tailwind admin UI
  hospital-ai/            # Not started — placeholder for the future RAG/Text-to-SQL/AI Director project
  ai/                     # Sprint 0 placeholder for the same future AI layer (see hospital-ai/README.md)
  scripts/                # Developer/operator scripts (empty — Sprint 0)
  docker/                 # Container/Compose definitions (empty — Sprint 0)
  tools/                  # Internal project tooling (empty — Sprint 0)
  sample-data/            # Synthetic sample data (empty — Sprint 0)
  .claude/                # Claude Code project configuration (empty — Sprint 0)
  .github/                # CI/CD, issue/PR templates (empty — Sprint 0)
```

## Running Locally

### Requirements

- Java 21+ (JDK)
- Maven (or use the bundled `./mvnw` wrapper in `hospital-backend/`)
- Node.js 20+ and npm
- A running PostgreSQL instance (local or remote) — no Docker is used in
  this sprint

### 1. Configure the database connection

`hospital-backend` reads its datasource from environment variables (with
local defaults baked in), loaded automatically from a `.env` file in
`hospital-backend/` if present:

```
# hospital-backend/.env  (gitignored — never commit real credentials)
DB_USERNAME=your_postgres_user
DB_PASSWORD=your_postgres_password
DB_URL=jdbc:postgresql://host:5432/your_database
```

If no `.env` is provided, it falls back to
`jdbc:postgresql://localhost:5432/hospital_db` with user/password
`hospital`/`hospital`.

### 2. Start the backend

```bash
cd hospital-backend
./mvnw quarkus:dev
```

On first startup the backend creates its schema (`hibernate.orm` in
`update` mode) and seeds demo data automatically: 5 departments (Emergency,
ICU, Cardiology, Surgery, Outpatient), beds per department, 50 patients,
200 visits with realistic waiting times/charges, a handful of Knowledge
Center documents, and the Hospital Discovery catalog (20 sections, ~200
questions) plus 2 sample discovery projects with partial answers.
Subsequent restarts detect existing data and skip reseeding — if you need
to regenerate demo data (e.g. after upgrading from a database that predates
a given module), truncate the relevant tables and restart:
`department`, `patient`, `visit`, `bed` (Mini HIS/Executive Dashboard);
`document_category`, `knowledge_document` (Knowledge Center);
`discovery_project`, `discovery_section`, `discovery_question`,
`discovery_answer`, `discovery_attachment` (Hospital Discovery).

Uploaded Knowledge Center files are written to
`hospital.knowledge.storage-path` (default `./data/knowledge-uploads`,
gitignored); override with `KNOWLEDGE_STORAGE_PATH`. Hospital Discovery
attachments are written to `hospital.discovery.storage-path` (default
`./data/discovery-attachments`, gitignored); override with
`DISCOVERY_STORAGE_PATH`. Both are relative to `hospital-backend/`.

- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/q/swagger-ui
- Health check: http://localhost:8080/q/health

### 3. Start the frontend

```bash
cd hospital-ui
npm install
npm run dev
```

- App: http://localhost:5173

The frontend talks to the backend at `http://localhost:8080` by default;
override with `VITE_API_BASE_URL` if needed.

### Running the test suites

```bash
cd hospital-backend && ./mvnw test    # service-layer unit tests (Mockito, no DB required)
cd hospital-ui && npm test            # component tests (Vitest + Testing Library)
```

### Screenshots

_Placeholder — to be added once the UI has been visually reviewed in a
browser. Nothing below has been captured yet._

- Executive Dashboard: `docs/screenshots/dashboard.png`
- Command Center: `docs/screenshots/command-center.png`
- AI Director: `docs/screenshots/ai-director.png`
- Knowledge Center: `docs/screenshots/knowledge-center.png`
- Departments: `docs/screenshots/departments.png`
- Patients: `docs/screenshots/patients.png`
- Visits: `docs/screenshots/visits.png`
- Discovery Dashboard: `docs/screenshots/discovery-dashboard.png`
- Discovery Projects: `docs/screenshots/discovery-projects.png`
- Discovery Workspace (sections + question cards): `docs/screenshots/discovery-workspace.png`

### Sprint 2 Demo Script

A suggested walkthrough for a hospital-executive audience, front to back:

1. **Open Dashboard** — "Here's what's happening in your hospital right
   now, live from the database: today's patients and visits, bed and ICU
   occupancy, surgeries, emergency cases, average wait time, today's
   revenue."
2. **Open Command Center** — "The system watches these numbers continuously
   and flags what needs attention." (The seed data deliberately puts ICU
   occupancy near capacity, so this screen should show a real RED alert out
   of the box — point at it.)
3. **Ask AI Director** — Type or click one of the example questions (e.g.
   *"How many ICU beds are available?"* or *"Show today's hospital
   summary"*). "This isn't a canned script — it's querying the same live
   data as the dashboard. Today it's a rule engine; the interface is built
   so a real LLM can be dropped in later without changing anything you're
   looking at."
4. **Open Knowledge Center** — "Your policies and SOPs, searchable by title
   or category." Upload a real PDF live if possible — "and that's
   immediately searchable and downloadable."
5. **Explain future roadmap** — Point to the [Future Roadmap](#future-roadmap)
   section: the Knowledge Center is intentionally metadata-only today
   (files on disk, PostgreSQL for search) because the next phase adds real
   semantic search (RAG) over these same documents, and the AI Director's
   rule engine is designed to be replaced by an LLM + Text-to-SQL without
   any UI rework — this demo is the shape of the real product, not a mockup
   of it.

## The Five Governing Principles

1. **Model the architecture, not the hospital.**
2. **Traceability** — every feature maps back to the source proposal.
3. **Minimal domain** — simulate only enough to support the AI Copilot.
4. **Architecture first** — no implementation before design is approved.
5. **Replaceability** — every module can be swapped for a real system.

Full detail: [docs/12_PROJECT_RULES.md](docs/12_PROJECT_RULES.md).

## In Scope / Out of Scope (Summary)

**In scope:** Mini HIS, Hospital Data Warehouse, Realtime Dashboard,
Knowledge Base, RAG, Text-to-SQL, AI Director, Administration,
Authentication, Audit Log.

**Out of scope:** Full HIS, Pharmacy, Inventory, Payroll, Insurance,
Medical Devices, PACS/Radiology Viewers, EMR replacement, AI Diagnosis, AI
Image Recognition, IoT, Robotics, Billing, National Health Integration —
and anything else not named in the source proposal.

Full detail: [docs/03_SCOPE.md](docs/03_SCOPE.md) and
[docs/05_NON_GOALS.md](docs/05_NON_GOALS.md).

## Technology Envelope

Java/Quarkus/PostgreSQL (operational) · React/TypeScript/Tailwind
(frontend) · ClickHouse/Kafka (analytical, with Iceberg/Trino/Debezium as
future extension points) · Python/OpenAI-compatible APIs/Qdrant (AI layer).

Full detail: [docs/09_TECH_STACK.md](docs/09_TECH_STACK.md).

## Future Roadmap

**Deviation from the original Sprint 0 plan:** [docs/11_ROADMAP.md](docs/11_ROADMAP.md)
originally scheduled Sprint 2 as "Kafka ingestion + ClickHouse Hospital
Data Warehouse." Instead, Sprint 2 was redirected to build an executive
demo (Dashboard, Command Center, AI Director, Knowledge Center) directly on
`hospital-backend`/PostgreSQL, with no LLM, no warehouse, and no streaming
infrastructure — optimizing for "can a director see the vision in one
sitting" over "is the data platform built." This is a deliberate,
explicitly-scoped detour, not a silent architecture change: every Sprint 2
feature still talks to PostgreSQL directly, and nothing about the Sprint 0
architecture ([docs/08_HIGH_LEVEL_ARCHITECTURE.md](docs/08_HIGH_LEVEL_ARCHITECTURE.md))
was invalidated by it — the warehouse, Kafka, and the AI layer described
there are simply still ahead of us, not replaced.

What's still ahead, per [docs/11_ROADMAP.md](docs/11_ROADMAP.md) and
[docs/backlog/EPICS.md](docs/backlog/EPICS.md) (renumbering likely once
Sprint 2's detour is reconciled back into the plan):

- **Data Warehouse** — Kafka ingestion + ClickHouse, so the Executive
  Dashboard and Command Center read from a real analytical store instead of
  direct PostgreSQL aggregation.
- **RAG** — real semantic search (`hospital-ai/rag/`, Qdrant) over the
  Knowledge Center documents this sprint only stored as metadata + files.
- **Text-to-SQL** (`hospital-ai/text_to_sql/`) against the warehouse.
- **A real AI Director** (`hospital-ai/ai_director/`) — replacing
  `RuleBasedAiDirectorEngine` with an LLM/RAG/Text-to-SQL-backed
  implementation of the same `AiDirectorEngine` interface, so the chat UI
  built in this sprint doesn't need to change.
- **Administration + Authentication** — there is still no login on this
  application; anyone who can reach the API can use it.
- **Audit Log** — every AI Director question and Knowledge Center action
  should be traceable.
- **Docker Compose** for a single-command local environment, plus the
  screenshots and demo recordings this README references but doesn't have
  yet.

Nothing above is built yet.

**Hospital Discovery is not part of that detour** — it isn't a stand-in for
a later AI-Copilot Epic the way Sprint 2's demo screens are; it's a
genuinely new, permanent capability (per its own spec: "reusable... not
only Hanoi Heart Hospital"), and its export JSON is explicitly designed as
future input to the AI modules above, not something they replace. What's
still missing from Hospital Discovery specifically:

- **A question/section catalog admin UI.** Sections and questions are
  fully modeled in the database and served dynamically (never hardcoded in
  React), and the backend already has everything needed to manage them
  (`DiscoverySectionRepository`, `DiscoveryQuestionRepository`), but there
  is no frontend screen to create/edit/reorder them yet — the catalog is
  seeded once and is read-only from the UI this sprint.
- **Attachment restoration on import.** Export includes attachment
  *metadata* only (filenames/sizes), not file bytes, so importing a survey
  restores answers but not the files themselves.
- **No login**, same gap as the rest of the app — any discovery project is
  visible/editable by anyone who can reach the API.

## Contributing

This project is governed by strict scope and process rules — read
[docs/12_PROJECT_RULES.md](docs/12_PROJECT_RULES.md) before proposing any
change. Every new capability must cite the section of
[docs/03_SCOPE.md](docs/03_SCOPE.md) it implements, and any architecturally
significant decision must be recorded as an ADR in
[docs/adr/](docs/adr/README.md).

## License

Not yet decided.
