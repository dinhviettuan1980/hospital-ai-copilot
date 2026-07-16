# 07 — Personas

## 1. Primary Persona

### Hospital Director

- **Role:** Senior executive accountable for overall hospital performance —
  operations, quality, capacity, and strategy.
- **Goals:** Understand what is happening across the hospital right now
  (occupancy, admissions, staffing pressure), and understand what policy or
  precedent applies to a given situation, without waiting on an analyst or
  digging through PDFs.
- **Primary Interaction:** The **AI Director** conversational interface,
  backed by the Realtime Dashboard for at-a-glance KPIs.
- **Representative Questions:**
  - "What is our current bed occupancy by department?"
  - "Which department had the highest average length of stay last quarter?"
  - "What is our escalation policy when ICU occupancy exceeds 90%?"
  - "Compare this month's admission rate to last month's and explain any
    policy that applies if the trend continues."
- **Success for This Persona:** Gets a fast, grounded, explainable answer —
  never a fabricated number, never an unsourced policy claim.

## 2. Secondary Personas

### Hospital Management Team

- **Role:** Department heads and operational managers who support the
  Director's decisions.
- **Goals:** Drill into the same KPIs at department level; understand
  trends relevant to their own area.
- **Primary Interaction:** Realtime Dashboard (department-filtered view),
  occasional AI Director queries scoped to their department.

### Hospital CIO

- **Role:** Owns technology strategy and vendor/integration decisions.
- **Goals:** Evaluate whether this architecture pattern (warehouse +
  knowledge base + RAG + Text-to-SQL + orchestration) is viable to adopt or
  adapt for the real hospital's systems.
- **Primary Interaction:** Architecture documentation
  ([08_HIGH_LEVEL_ARCHITECTURE.md](08_HIGH_LEVEL_ARCHITECTURE.md)),
  Administration module, Audit Log (governance and access control posture).
- **Success for This Persona:** Can identify exactly which module maps to
  which real system in their hospital, and what it would take to replace
  the Mini HIS with their real HIS via the CDC extension point.

### Technical Architects

- **Role:** Design and evaluate enterprise system architectures, potentially
  outside healthcare.
- **Goals:** Study a worked example of combining an operational system, an
  analytical warehouse, a knowledge/RAG layer, and LLM orchestration with
  clean module boundaries.
- **Primary Interaction:** All `docs/architecture/` and ADR content.

### AI Engineers

- **Role:** Build and evaluate the AI Director, RAG, and Text-to-SQL
  components.
- **Goals:** Understand the orchestration pattern — how the AI Director
  decides between RAG, Text-to-SQL, and combined responses — and how
  guardrails and audit logging are applied to LLM-driven components.
- **Primary Interaction:** `ai/`, `docs/api/`, Knowledge Base and Data
  Warehouse schemas (once implemented).

### Students Learning Hospital AI

- **Role:** Learners studying healthcare informatics, enterprise
  architecture, or applied AI.
- **Goals:** Learn, from a small and honest example, how these architectural
  pieces fit together in a real (if simplified) domain.
- **Primary Interaction:** The full `docs/` tree, read front-to-back, plus a
  running system in later sprints.

## 3. Personas Explicitly Not Designed For

To reinforce scope discipline, this project does **not** design for:

- **Clinicians** making point-of-care decisions about individual patients
  (see [05_NON_GOALS.md](05_NON_GOALS.md) — no clinical charting, no AI
  diagnosis).
- **Patients** or patient-facing users.
- **Billing/finance/insurance staff.**
- **Pharmacy or supply-chain staff.**

## 4. Persona-to-Module Traceability

| Persona | Primary Modules Used |
|---|---|
| Hospital Director | AI Director, Realtime Dashboard, Decision Support |
| Hospital Management Team | Realtime Dashboard, AI Director (scoped) |
| Hospital CIO | Administration, Audit Log, Architecture docs |
| Technical Architects | Architecture docs, ADRs |
| AI Engineers | AI Director, RAG, Text-to-SQL, Knowledge Base |
| Students | All modules and documentation |
