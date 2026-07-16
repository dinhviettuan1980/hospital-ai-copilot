# 04 — Goals

Goals are organized by horizon. Sprint 0 goals must be fully satisfied before
any implementation sprint begins (Principle: *Architecture First*).

## 1. Sprint 0 Goals (Current)

- G1. Produce a complete, professional documentation set (this `docs/`
  directory) that fully describes the project's purpose, scope, personas,
  architecture, tech stack, domain, roadmap, rules, and glossary.
- G2. Produce a C4-style architecture description (System Context and
  Container level) with explicit module responsibilities and replacement
  boundaries.
- G3. Produce a backlog expressed only as Epics, each traceable to a
  named capability in the source proposal.
- G4. Establish the ADR process and directory structure, without recording
  any architectural decisions yet (decisions belong to the sprint in which
  they are made).
- G5. Create the full repository skeleton (folders + README placeholders)
  so future sprints have an unambiguous place to put each kind of artifact.
- G6. Self-review the full documentation set for internal consistency and
  scope discipline before declaring Sprint 0 complete.

## 2. Product Goals (Beyond Sprint 0, for context only)

These are not being built yet, but exist so later sprints have a fixed
target and don't drift:

- G7. A Hospital Director can log in, view a Realtime Dashboard of core
  hospital KPIs, and ask the AI Director natural-language questions.
- G8. The AI Director correctly distinguishes between questions that need
  structured data (Text-to-SQL over the warehouse), questions that need
  institutional knowledge (RAG over the Knowledge Base), and questions that
  need both.
- G9. Every AI Director interaction is captured in the Audit Log.
- G10. Every module (Mini HIS, Warehouse, Knowledge Base, RAG, Text-to-SQL,
  AI Director, Dashboard) can be individually disabled or replaced without
  breaking the others' contracts.

## 3. Educational Goals

- G11. A reader with enterprise architecture experience but no hospital
  domain background can understand the whole system from the documentation
  alone.
- G12. A reader can point to any implemented feature and trace it back to a
  specific line item in [03_SCOPE.md](03_SCOPE.md) and, ultimately, to the
  source proposal.
- G13. The project demonstrates, in miniature, real patterns used in
  enterprise hospital data platforms (CDC, lakehouse-ready warehousing,
  vector search, LLM orchestration, audit logging) without requiring
  enterprise-scale infrastructure to run.

## 4. Non-Goals Reminder

Goals are deliberately narrow. See [05_NON_GOALS.md](05_NON_GOALS.md) for
what this project explicitly will not attempt, and
[06_SUCCESS_METRICS.md](06_SUCCESS_METRICS.md) for how goal attainment is
measured.
