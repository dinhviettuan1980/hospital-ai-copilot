# ai/ (Placeholder)

This directory will hold the Python-based AI layer services: RAG,
Text-to-SQL, and the AI Director orchestrator, as defined in
[../docs/08_HIGH_LEVEL_ARCHITECTURE.md](../docs/08_HIGH_LEVEL_ARCHITECTURE.md)
§2 (containers 7–10).

## Sprint 0 Status

**Empty by design.** This is Sprint 0 — documentation only. No Python
source code, prompts, or service implementations are created yet (see
[../docs/11_ROADMAP.md](../docs/11_ROADMAP.md), Sprints 4–6).

## Planned Structure (Future Sprints)

```
ai/
  rag/              # RAG service: retrieval + generation over the Knowledge Base
  text_to_sql/       # Text-to-SQL service: NL -> SQL against the Data Warehouse
  ai_director/       # Orchestrator: intent routing, decision support, response composition
```

## Rule

No code is added here until the corresponding Epic
([../docs/backlog/EPICS.md](../docs/backlog/EPICS.md) Epics 5, 6, 7) reaches
its target sprint, and the relevant container's responsibilities are
documented in
[../docs/08_HIGH_LEVEL_ARCHITECTURE.md](../docs/08_HIGH_LEVEL_ARCHITECTURE.md).
