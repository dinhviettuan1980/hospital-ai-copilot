# Architecture Decision Records (ADR)

This directory records architecturally significant decisions made over the
life of the project. **No ADRs are recorded in Sprint 0.** This README
establishes the process and structure only, per the project brief's
instruction to prepare the ADR directory without writing ADRs yet.

## What Qualifies as an ADR

Per [12_PROJECT_RULES.md](../12_PROJECT_RULES.md) Rule 9, an ADR is required
for any decision that:

- Changes a module/container boundary defined in
  [08_HIGH_LEVEL_ARCHITECTURE.md](../08_HIGH_LEVEL_ARCHITECTURE.md).
- Changes or adds a technology choice beyond
  [09_TECH_STACK.md](../09_TECH_STACK.md).
- Changes the domain model in
  [10_DOMAIN_OVERVIEW.md](../10_DOMAIN_OVERVIEW.md).
- Introduces or removes scope relative to
  [03_SCOPE.md](../03_SCOPE.md) / [05_NON_GOALS.md](../05_NON_GOALS.md).

Implementation-detail choices (variable naming, internal code structure)
are **not** ADR-worthy.

## Numbering and Naming Convention

ADRs are numbered sequentially and named:

```
docs/adr/NNNN-short-kebab-case-title.md
```

Example (illustrative only — not yet created):
`docs/adr/0001-use-kafka-for-mini-his-ingestion.md`

## ADR Template

Every ADR must follow this structure:

```markdown
# NNNN — Title

## Status
Proposed | Accepted | Superseded by NNNN | Deprecated

## Context
What problem or decision point prompted this? Which proposal/document
section motivates it?

## Decision
The decision made, stated plainly.

## Consequences
What becomes easier or harder as a result? What module boundaries,
replaceability guarantees, or scope commitments are affected?

## Traceability
Which section of the source proposal, and which project document(s),
justify this decision?
```

## Lifecycle

1. A decision is proposed as a new ADR file with `Status: Proposed`.
2. It is reviewed against [12_PROJECT_RULES.md](../12_PROJECT_RULES.md) for
   scope and consistency.
3. Once accepted, `Status` is updated to `Accepted` and the ADR is
   cross-linked from the relevant governance document (e.g.,
   [08_HIGH_LEVEL_ARCHITECTURE.md](../08_HIGH_LEVEL_ARCHITECTURE.md) or
   [09_TECH_STACK.md](../09_TECH_STACK.md)).
4. If later superseded, the old ADR's status is updated to point to the new
   one — ADRs are never deleted, only superseded.

## Current State

| ADR | Title | Status |
|---|---|---|
| _(none yet)_ | — | Sprint 0 produces structure only |
