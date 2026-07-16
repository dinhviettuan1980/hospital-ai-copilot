# hospital-ai

This is a placeholder project. **No AI code is implemented in Sprint 1.**

## Why this directory exists now

The Sprint 1 goal is a working Mini HIS prototype: `hospital-backend`
(Department/Patient/Visit CRUD on Quarkus + PostgreSQL) and `hospital-ui`
(the React admin UI). Per the source architecture
(`docs/08_HIGH_LEVEL_ARCHITECTURE.md`), the AI layer — RAG, Text-to-SQL, and
the AI Director orchestrator — depends on the Hospital Data Warehouse and
Knowledge Base, neither of which exist yet. Building AI code against
nothing to ground it would produce an unreviewable, unrunnable stub, which
this project's rules treat as scope creep for the current sprint.

This directory exists now, alongside `hospital-backend` and `hospital-ui`,
so the three-project structure is visible from Sprint 1 onward instead of
appearing unannounced later.

## What will eventually live here

Per `docs/backlog/EPICS.md` (Epics 5, 6, 7) and
`docs/11_ROADMAP.md` (Sprints 4–6):

- `rag/` — Retrieval-Augmented Generation service (Python), answering
  questions grounded in the Knowledge Base via Qdrant.
- `text_to_sql/` — Natural-language-to-SQL service (Python), querying the
  Hospital Data Warehouse under guardrails.
- `ai_director/` — The orchestrator that routes a Hospital Director's
  question to RAG and/or Text-to-SQL and composes the final answer.

## Relationship to the top-level `ai/` folder

The repository also has a top-level `ai/` folder created in Sprint 0 as a
documentation-era placeholder for this same future work (see
`ai/README.md`). `hospital-ai/` is the actual project directory that will
be implemented; once implementation begins, `ai/` should be reconciled
into (or replaced by) `hospital-ai/` rather than maintained as a second,
parallel placeholder — that reconciliation is a Sprint 4 concern, not a
Sprint 1 one.

## Status

Not started. No dependencies, no code, no configuration.
