# 12 — Project Rules

These rules are binding for every contributor (human or AI) and every
future sprint. They restate and operationalize the principles from
[01_PROJECT_CHARTER.md](01_PROJECT_CHARTER.md) §6.

## Rule 1 — Model the Architecture, Not the Hospital

Every module exists to demonstrate an architectural pattern (operational →
analytical data flow, knowledge retrieval, NL-to-SQL, AI orchestration),
never to replicate real hospital operations in depth. When in doubt, choose
the shallower implementation that still proves the pattern.

**Enforcement:** Any pull request or design that adds depth to the Mini HIS
domain (new entities, new operational workflows) without a corresponding
KPI or AI Director question requiring it (see
[10_DOMAIN_OVERVIEW.md](10_DOMAIN_OVERVIEW.md) §8) must be rejected.

## Rule 2 — Traceability to the Proposal

Every feature, module, epic, and (later) task must be traceable to a
capability named in the source proposal, as captured in
[03_SCOPE.md](03_SCOPE.md). No feature creep.

**Enforcement:** New scope requires a citation + an ADR before it enters the
backlog (see [03_SCOPE.md](03_SCOPE.md) §6).

## Rule 3 — Minimal Domain

Simulate only enough hospital workflow to support the AI Copilot. The
domain model in [10_DOMAIN_OVERVIEW.md](10_DOMAIN_OVERVIEW.md) is the
ceiling, not a starting point to be casually extended.

## Rule 4 — Architecture First

No implementation code is written until the relevant architecture is
documented and (for Sprint 0) the whole-system architecture is approved.
Within later sprints, no module's implementation begins before its
container-level responsibilities and interfaces are already described in
[08_HIGH_LEVEL_ARCHITECTURE.md](08_HIGH_LEVEL_ARCHITECTURE.md).

## Rule 5 — Replaceability

Every module must be designed so a real hospital could replace it with
their own equivalent system without rearchitecting the rest. Concretely:

- Modules communicate through named, narrow interfaces (SQL over the
  warehouse, vector search over Qdrant, REST/HTTP, Kafka topics) — never
  through direct access to another module's internal storage.
- No module hardcodes assumptions about another module's internal
  implementation.

## Rule 6 — No Real Patient Data

All patient, staff, and operational data in this project is synthetic.
Real PHI must never be introduced, requested, or accepted into this
repository, its sample data, or its documentation.

## Rule 7 — No Clinical Decision-Making

The AI Director, Decision Support, RAG, and Text-to-SQL components operate
on operational/administrative data and institutional knowledge only. None
of them may be extended to provide diagnosis, triage, or individual-patient
treatment recommendations (see [05_NON_GOALS.md](05_NON_GOALS.md) §4).

## Rule 8 — Documentation Precedes Backlog; Backlog Precedes Tasks

Sprint 0 produces documentation and Epics only. Implementation-level tasks
are created only within an implementation sprint, and only for Epics that
already exist in [docs/backlog/EPICS.md](backlog/EPICS.md).

## Rule 9 — ADRs for Architectural Decisions

Any decision that changes a module boundary, a technology choice in
[09_TECH_STACK.md](09_TECH_STACK.md), or the domain model must be recorded
as an ADR in `docs/adr/` using the template in
[docs/adr/README.md](adr/README.md), before implementation.

## Rule 10 — Consistency Over Speed

If a new document or decision would contradict an existing governance
document, the contradiction must be resolved (by correcting the new
proposal or explicitly superseding the old document via ADR) before
proceeding. Silent inconsistency is not acceptable.

## Rule 11 — Educational Clarity Is a Feature

Where two designs are otherwise equivalent, prefer the one that is easier
for a reader unfamiliar with the hospital domain to understand. This
project's value is pedagogical as much as architectural.
