# Database Documentation (Placeholder)

This directory will hold concrete schema design for both the operational
plane (PostgreSQL, Mini HIS + Administration + Authentication) and the
analytical plane (ClickHouse, Hospital Data Warehouse), derived from the
conceptual model in
[../10_DOMAIN_OVERVIEW.md](../10_DOMAIN_OVERVIEW.md).

## Sprint 0 Status

**Empty by design.** No table DDL, entity-relationship diagrams, or index
strategy are authored yet. Sprint 0 defines only the conceptual domain
model; concrete schema is an implementation-sprint concern (see
[../11_ROADMAP.md](../11_ROADMAP.md), Sprints 1–2).

## Planned Contents (Future Sprints)

- `operational-schema.md` — PostgreSQL schema for Mini HIS, Administration,
  Authentication.
- `warehouse-schema.md` — ClickHouse fact/dimension schema for the Hospital
  Data Warehouse.
- `data-dictionary.md` — field-level definitions cross-referenced to
  [../10_DOMAIN_OVERVIEW.md](../10_DOMAIN_OVERVIEW.md).

## Rule

No table or column may be added here unless it maps to an entity or
relationship already defined in
[../10_DOMAIN_OVERVIEW.md](../10_DOMAIN_OVERVIEW.md).
