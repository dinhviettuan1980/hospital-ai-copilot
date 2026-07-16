# Backlog

This directory holds the project backlog. In Sprint 0, the backlog contains
**Epics only** — no implementation-level tasks or user stories. This is a
deliberate constraint (see [12_PROJECT_RULES.md](../12_PROJECT_RULES.md)
Rule 8): tasks are created only within an implementation sprint, and only
for Epics that already exist here.

## Contents

- [EPICS.md](EPICS.md) — the full Epic list, one per module/container.

## Epic Structure

Each Epic in [EPICS.md](EPICS.md) follows this structure:

- **Epic ID and Title**
- **Module / Container** it corresponds to (see
  [08_HIGH_LEVEL_ARCHITECTURE.md](../08_HIGH_LEVEL_ARCHITECTURE.md) §2)
- **Description** — what capability this Epic delivers
- **Traceability** — which section of [03_SCOPE.md](../03_SCOPE.md) it
  implements
- **Target Sprint** — reference to [11_ROADMAP.md](../11_ROADMAP.md)

## Rules Governing This Backlog

1. One Epic per container/module. No Epic spans multiple containers, and no
   container lacks an Epic.
2. Every Epic must cite the scope line item it implements.
3. No Epic may include work from the exclusion list in
   [05_NON_GOALS.md](../05_NON_GOALS.md).
4. Tasks are out of scope for this directory until the sprint in which an
   Epic is actively implemented begins.
