# 03 — Scope

## 1. Scope Philosophy

Scope is defined by one question: **"Is this capability named in the source
proposal?"** If yes, it is in scope, at minimal depth. If no, it is out of
scope, regardless of how useful or interesting it might be. See
[05_NON_GOALS.md](05_NON_GOALS.md) for the explicit exclusion list.

## 2. In-Scope Modules

| # | Module | Purpose | Depth in This Project |
|---|--------|---------|------------------------|
| 1 | **Mini HIS** | Minimal operational hospital system that produces source data | Just enough entities to support admissions, occupancy, and staffing signals (see [10_DOMAIN_OVERVIEW.md](10_DOMAIN_OVERVIEW.md)) |
| 2 | **Hospital Data Warehouse** | Analytical store for historical and aggregated hospital data | Star-schema-style facts/dimensions in ClickHouse, fed by Kafka |
| 3 | **Realtime Dashboard** | Visual, near-real-time KPI view for hospital leadership | A small, fixed set of KPIs tied directly to the domain model |
| 4 | **Knowledge Base** | Store of unstructured institutional knowledge (policies, SOPs, reports) | A small curated document set, not a full document management system |
| 5 | **RAG** | Retrieval-augmented answers grounded in the Knowledge Base | Single vector store (Qdrant), single retrieval pipeline |
| 6 | **Text-to-SQL** | Natural-language querying of the Data Warehouse | Constrained to the warehouse schema, with guardrails |
| 7 | **AI Director** | Orchestration layer that routes questions to RAG, Text-to-SQL, or both, and produces decision-support narratives | The "copilot" — the primary interface for the Hospital Director persona |
| 8 | **Decision Support** | Insight/alerting logic surfaced by the AI Director | Threshold- and trend-based, not predictive/clinical AI |
| 9 | **Administration** | User, role, and module configuration | Minimal admin surface — not a full IAM product |
| 10 | **Authentication** | Login and role-based access | Standard session/token auth, no SSO federation in this scope |
| 11 | **Audit Log** | Record of AI Director queries, data access, and admin actions | Append-only log, queryable, not a full SIEM |

## 3. Out-of-Scope

Full exclusion list and rationale: [05_NON_GOALS.md](05_NON_GOALS.md).
Summary: Full HIS, Pharmacy, Inventory, Payroll, Insurance, Medical Devices,
PACS Viewer, Radiology Viewer, EMR Replacement, AI Diagnosis, AI Image
Recognition, IoT, Robotics, Billing, National Health Integration, and
anything not named in the proposal.

## 4. Data Scope

- All patient, staff, and operational data is **synthetic**. No real
  Protected Health Information (PHI) is ever used or supported.
- The Mini HIS is the *only* system of record inside this reference
  implementation. There is no integration with real external hospital
  systems.
- The Knowledge Base contains a small set of representative, synthetic or
  publicly shareable policy/SOP-style documents.

## 5. Scope Boundary With the Proposal

Where the source proposal describes a capability at enterprise depth (e.g.,
"the data warehouse ingests from dozens of hospital systems via CDC"), this
project implements the **narrowest slice that still demonstrates the
architectural pattern** (e.g., "the data warehouse ingests from the one Mini
HIS via Kafka, with Debezium/CDC named as a future extension point"). This
narrowing is a depth reduction, never a scope addition or a different
pattern.

## 6. Scope Change Process

Any addition to this table requires:
1. A citation to the specific section of the source proposal that justifies
   it.
2. An ADR recorded in [docs/adr/](adr/) explaining the addition and its
   impact on module boundaries.
3. Confirmation that it does not violate any principle in
   [12_PROJECT_RULES.md](12_PROJECT_RULES.md).
