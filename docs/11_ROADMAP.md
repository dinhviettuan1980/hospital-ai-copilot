# 11 — Roadmap

This roadmap is sequenced so that every sprint produces a runnable increment
that later sprints depend on. No implementation sprint begins until the
prior sprint's documentation/architecture obligations are satisfied
(Principle 4: *Architecture First*).

## Sprint 0 — Foundation (Current)

- **Goal:** Documentation-only foundation. No source code.
- **Deliverables:** Full repository skeleton; 13 governance documents +
  README; C4 Context/Container architecture; Epic-only backlog; ADR
  structure (no ADRs yet).
- **Exit Criteria:** See [01_PROJECT_CHARTER.md](01_PROJECT_CHARTER.md) §12.

## Sprint 1 — Mini HIS + Authentication (Planned)

- Stand up the Mini HIS domain model ([10_DOMAIN_OVERVIEW.md](10_DOMAIN_OVERVIEW.md))
  on Quarkus + PostgreSQL.
- Stand up Authentication (login, session/token, basic RBAC) since every
  later sprint's UI depends on it.
- First ADRs recorded (e.g., domain schema decisions, auth token strategy).

## Sprint 2 — Hospital Data Warehouse + Ingestion (Planned)

- Stand up Kafka as the ingestion backbone from the Mini HIS.
- Stand up ClickHouse with the fact/dimension model derived from
  [10_DOMAIN_OVERVIEW.md](10_DOMAIN_OVERVIEW.md) §5.
- Validate end-to-end: a change in the Mini HIS is observable in the
  warehouse.

## Sprint 3 — Realtime Dashboard (Planned)

- Dashboard backend API (Quarkus) serving KPI queries from the warehouse.
- Dashboard frontend (React/TypeScript/Tailwind) rendering the KPIs listed
  in [10_DOMAIN_OVERVIEW.md](10_DOMAIN_OVERVIEW.md) §6.

## Sprint 4 — Knowledge Base + RAG (Planned)

- Curate the initial Knowledge Base document set (policies, SOPs, sample
  reports).
- Stand up Qdrant and the embedding/indexing pipeline.
- Stand up the RAG service and validate grounded, sourced answers.

## Sprint 5 — Text-to-SQL (Planned)

- Stand up the Text-to-SQL service against the warehouse schema, with
  guardrails (read-only, schema-constrained, query validation).
- Validate against the example questions in
  [10_DOMAIN_OVERVIEW.md](10_DOMAIN_OVERVIEW.md) §7.

## Sprint 6 — AI Director + Decision Support (Planned)

- Build the orchestration layer: intent classification, routing to RAG
  and/or Text-to-SQL, response composition.
- Implement Decision Support threshold/trend logic and wire it into AI
  Director responses.
- Wire every AI Director interaction into the Audit Log.

## Sprint 7 — Administration + Audit Hardening (Planned)

- Build the Administration module (user/role/module-config management).
- Harden Audit Log coverage across all modules; add query/reporting
  surface for the CIO/Administrator persona.

## Sprint 8 — Polish, Docs, and Demo Scenarios (Planned)

- End-to-end demo scripts and sample data (`sample-data/`, `scripts/`).
- Docker Compose environment for single-machine, single-developer runs.
- Final documentation pass linking implementation back to every governance
  document.

## Roadmap Principles

- Each sprint's scope is drawn directly from the Epics in
  [docs/backlog/EPICS.md](backlog/EPICS.md) — no sprint introduces a
  capability not already named there.
- Sprints are sequenced by **data dependency**, not by feature glamour: the
  AI layer (Sprints 4–6) cannot be meaningfully built or demoed before the
  data plane (Sprints 1–3) exists.
- Future extension points from
  [08_HIGH_LEVEL_ARCHITECTURE.md](08_HIGH_LEVEL_ARCHITECTURE.md) §6
  (Iceberg, Trino, Debezium, real HIS integration, SSO) are explicitly
  **not** scheduled in any sprint above — they remain documented
  possibilities, not commitments.
