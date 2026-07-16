# Architecture Documentation

This directory holds the detailed architecture record for the Hospital AI
Copilot reference implementation. It expands on
[08_HIGH_LEVEL_ARCHITECTURE.md](../08_HIGH_LEVEL_ARCHITECTURE.md), which
remains the canonical System Context and Container-level description.

## Contents of This Sprint (Sprint 0)

This directory currently contains only this README. All architecture
content approved for Sprint 0 lives in
[../08_HIGH_LEVEL_ARCHITECTURE.md](../08_HIGH_LEVEL_ARCHITECTURE.md).

## C4 Levels Used in This Project

| Level | Name | Status |
|---|---|---|
| 1 | System Context | Done — see [08_HIGH_LEVEL_ARCHITECTURE.md](../08_HIGH_LEVEL_ARCHITECTURE.md) §1 |
| 2 | Container | Done — see [08_HIGH_LEVEL_ARCHITECTURE.md](../08_HIGH_LEVEL_ARCHITECTURE.md) §2 |
| 3 | Component | Deferred — produced per-container, per implementation sprint, once that container's code exists |
| 4 | Code | Not produced as a diagram; code itself is the artifact |

## Planned Contents (Future Sprints)

As implementation sprints proceed (see
[../11_ROADMAP.md](../11_ROADMAP.md)), this directory is expected to hold:

- `context.md` — expanded System Context detail, if it outgrows the summary
  document.
- `containers.md` — expanded Container-level detail per module.
- `components/<container-name>.md` — Component-level diagrams and
  descriptions, one per container, created only once that container is
  implemented.
- `data-flow.md` — sequence-style descriptions of key flows (e.g., "Director
  asks a combined RAG + Text-to-SQL question").
- `decisions.md` — an index into `docs/adr/` for architecture-relevant
  decisions.

## Rule

No component-level (C4 Level 3) content is added for a container before
that container has an approved container-level entry in
[08_HIGH_LEVEL_ARCHITECTURE.md](../08_HIGH_LEVEL_ARCHITECTURE.md) and an
Epic in [docs/backlog/EPICS.md](../backlog/EPICS.md). This preserves
Principle 4 (*Architecture First*) at every level of zoom, not just the top
level.
