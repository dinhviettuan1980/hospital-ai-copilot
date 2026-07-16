# 06 — Success Metrics

This project is a reference implementation, not a commercial product, so its
success metrics are architectural, educational, and demonstrative — not
business KPIs like "reduced readmission rate."

## 1. Sprint 0 (Documentation) Success Metrics

| Metric | Target |
|---|---|
| Governance documents present | All 13 numbered documents + README complete |
| Traceability | 100% of documented modules cite a capability from the source proposal |
| Scope leakage | 0 references to any item on the [Non-Goals](05_NON_GOALS.md) exclusion list |
| Architecture completeness | System Context + Container level both documented, with responsibilities and extension points for every module |
| Backlog discipline | Backlog contains Epics only — 0 implementation-level tasks |
| ADR readiness | ADR directory and template exist — 0 ADRs recorded (correct for this sprint) |
| Internal consistency | No contradictions between charter, scope, architecture, and domain docs (verified by self-review pass) |

## 2. Architecture Quality Metrics (Ongoing, All Sprints)

- **Replaceability:** Each module in
  [08_HIGH_LEVEL_ARCHITECTURE.md](08_HIGH_LEVEL_ARCHITECTURE.md) can be
  described in one paragraph explaining what would need to change to swap
  it for a real hospital's equivalent system — if that paragraph cannot be
  written, the module boundary is wrong.
- **Traceability:** Every Epic in [docs/backlog/EPICS.md](backlog/EPICS.md)
  maps 1:1 to a module named in [03_SCOPE.md](03_SCOPE.md).
- **Minimality:** The domain model in
  [10_DOMAIN_OVERVIEW.md](10_DOMAIN_OVERVIEW.md) contains only entities
  required to compute the KPIs and answer the example AI Director questions
  defined in that document — no speculative entities.

## 3. Educational / Reference-Value Metrics (Post-Implementation)

These apply once implementation sprints begin, and are recorded here so
later sprints do not lose sight of them:

- A new reader can explain the end-to-end data flow (Mini HIS → Warehouse →
  Dashboard / AI Director) after reading the architecture docs alone, before
  looking at code.
- The AI Director correctly routes a representative set of sample questions
  to RAG, Text-to-SQL, or both (routing-accuracy is a functional
  correctness metric, not a business metric).
- Every AI Director response used to inform a decision-support answer is
  traceable, via the Audit Log, to the underlying data or document sources
  it drew from.
- The system runs end-to-end on a single developer machine (via Docker
  Compose, defined in a later sprint) without requiring paid third-party
  infrastructure beyond an OpenAI-compatible API key.

## 4. Explicit Non-Metrics

The following are intentionally **not** tracked, because tracking them would
imply the project is trying to be a real hospital system:

- Patient outcome metrics (readmission, mortality, LOS reduction in a real
  clinical sense).
- Performance/scale benchmarks at real-hospital data volumes.
- Regulatory/compliance certification (HIPAA, GDPR, etc.) — the project may
  *illustrate* audit-log and access-control patterns relevant to compliance,
  but does not claim compliance.
