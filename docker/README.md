# docker/ (Placeholder)

This directory will hold container/Compose definitions that let a single
developer run the full Hospital AI Copilot stack (Mini HIS, Kafka,
ClickHouse, Qdrant, all services, and the frontend) on one machine.

## Sprint 0 Status

**Empty by design.** No Dockerfiles or Compose files are authored yet —
this sprint is documentation only. Containerization is an implementation
concern targeted for Sprint 8 (see
[../docs/11_ROADMAP.md](../docs/11_ROADMAP.md)), though individual
services may gain their own Dockerfile as they are built in earlier
sprints.

## Planned Contents (Future Sprints)

- `docker-compose.yml` — full local stack definition.
- Per-service Dockerfiles, colocated with each service or referenced from
  here.

## Rule

The Compose topology must mirror the containers defined in
[../docs/08_HIGH_LEVEL_ARCHITECTURE.md](../docs/08_HIGH_LEVEL_ARCHITECTURE.md)
§2 exactly — no additional infrastructure services beyond what that
document names.
