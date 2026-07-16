# Epics

Epics only. No implementation tasks. Each Epic maps 1:1 to a container in
[08_HIGH_LEVEL_ARCHITECTURE.md](../08_HIGH_LEVEL_ARCHITECTURE.md) §2 and a
line item in [03_SCOPE.md](../03_SCOPE.md) §2.

---

### Epic 1 — Mini HIS

**Module:** Mini HIS (Java, Quarkus, PostgreSQL)
**Description:** Deliver the minimal operational hospital domain
(Department, Bed, Patient, Admission, Staff Member, Appointment) as the
system of record that feeds the rest of the platform.
**Traceability:** [03_SCOPE.md](../03_SCOPE.md) §2 row 1;
[10_DOMAIN_OVERVIEW.md](../10_DOMAIN_OVERVIEW.md)
**Target Sprint:** Sprint 1

---

### Epic 2 — Hospital Data Warehouse

**Module:** Data Warehouse (ClickHouse) + Ingestion Backbone (Kafka)
**Description:** Deliver the analytical store and the streaming pipeline
that carries operational changes from the Mini HIS into fact/dimension
tables usable by the Dashboard and Text-to-SQL.
**Traceability:** [03_SCOPE.md](../03_SCOPE.md) §2 row 2
**Target Sprint:** Sprint 2

---

### Epic 3 — Realtime Dashboard

**Module:** Dashboard Backend API (Quarkus) + Dashboard Frontend
(React/TypeScript/Tailwind)
**Description:** Deliver a visual, near-real-time view of the core hospital
KPIs defined in [10_DOMAIN_OVERVIEW.md](../10_DOMAIN_OVERVIEW.md) §6, for
the Hospital Director and Management Team personas.
**Traceability:** [03_SCOPE.md](../03_SCOPE.md) §2 row 3
**Target Sprint:** Sprint 3

---

### Epic 4 — Knowledge Base

**Module:** Knowledge Base Store (document store + Qdrant)
**Description:** Curate and index a small set of institutional documents
(policies, SOPs, reports) so they are retrievable by the RAG service.
**Traceability:** [03_SCOPE.md](../03_SCOPE.md) §2 row 4
**Target Sprint:** Sprint 4

---

### Epic 5 — Retrieval-Augmented Generation (RAG)

**Module:** RAG Service (Python, OpenAI-compatible API, Qdrant client)
**Description:** Deliver grounded, sourced natural-language answers to
questions about institutional knowledge, retrieving from the Knowledge Base
before generating a response.
**Traceability:** [03_SCOPE.md](../03_SCOPE.md) §2 row 5
**Target Sprint:** Sprint 4

---

### Epic 6 — Text-to-SQL

**Module:** Text-to-SQL Service (Python, OpenAI-compatible API)
**Description:** Translate natural-language questions into constrained,
guarded SQL against the Data Warehouse and return structured results.
**Traceability:** [03_SCOPE.md](../03_SCOPE.md) §2 row 6
**Target Sprint:** Sprint 5

---

### Epic 7 — AI Director

**Module:** AI Director (Python, OpenAI-compatible API) + embedded Decision
Support Logic
**Description:** Deliver the orchestration layer that is the Hospital
Director's primary conversational interface — classifying intent, routing
to RAG and/or Text-to-SQL, applying Decision Support threshold/trend logic,
and composing a single grounded answer.
**Traceability:** [03_SCOPE.md](../03_SCOPE.md) §2 rows 7–8
**Target Sprint:** Sprint 6

---

### Epic 8 — Administration

**Module:** Administration Service (Java, Quarkus, PostgreSQL)
**Description:** Deliver user, role, and module configuration management
for the System Administrator / CIO persona.
**Traceability:** [03_SCOPE.md](../03_SCOPE.md) §2 row 9
**Target Sprint:** Sprint 7

---

### Epic 9 — Authentication

**Module:** Authentication Service (Java, Quarkus)
**Description:** Deliver login, session/token issuance, and role-based
access control shared across all Director-, Dashboard-, and
Administration-facing entry points.
**Traceability:** [03_SCOPE.md](../03_SCOPE.md) §2 row 10
**Target Sprint:** Sprint 1 (foundational; required by nearly every other
Epic)

---

### Epic 10 — Audit Log

**Module:** Audit Log Service (Java, Quarkus, PostgreSQL/ClickHouse)
**Description:** Deliver an immutable, queryable record of AI Director
queries, data access, and administrative actions.
**Traceability:** [03_SCOPE.md](../03_SCOPE.md) §2 row 11
**Target Sprint:** Sprint 6 (wired into AI Director), hardened in Sprint 7

---

## Epic Coverage Check

| Container (from architecture doc) | Covered By |
|---|---|
| Mini HIS | Epic 1 |
| Ingestion / Streaming Backbone | Epic 2 |
| Hospital Data Warehouse | Epic 2 |
| Dashboard Backend + Frontend | Epic 3 |
| Knowledge Base Store | Epic 4 |
| RAG Service | Epic 5 |
| Text-to-SQL Service | Epic 6 |
| AI Director + Decision Support | Epic 7 |
| Administration Service | Epic 8 |
| Authentication Service | Epic 9 |
| Audit Log Service | Epic 10 |

Every container has exactly one Epic. No Epic exists without a container.
