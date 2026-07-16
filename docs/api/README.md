# API Documentation (Placeholder)

This directory will hold API contracts (REST/HTTP endpoint definitions,
request/response schemas) for the service containers defined in
[../08_HIGH_LEVEL_ARCHITECTURE.md](../08_HIGH_LEVEL_ARCHITECTURE.md):
Dashboard Backend API, RAG Service, Text-to-SQL Service, AI Director,
Administration Service, Authentication Service, Audit Log Service.

## Sprint 0 Status

**Empty by design.** Per the project's current sprint scope, no API
contracts are authored yet — this sprint is documentation-only, and API
design is an implementation-sprint concern (see
[../11_ROADMAP.md](../11_ROADMAP.md)).

## Planned Contents (Future Sprints)

- One document per service container, defining its external HTTP contract.
- Shared conventions (error format, auth header expectations, pagination)
  in a single cross-cutting document.

## Rule

Any API documented here must correspond to a container already defined in
[../08_HIGH_LEVEL_ARCHITECTURE.md](../08_HIGH_LEVEL_ARCHITECTURE.md) and an
Epic in [../backlog/EPICS.md](../backlog/EPICS.md).
