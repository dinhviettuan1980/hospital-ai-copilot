# 10 — Domain Overview

## 1. Purpose

This document defines the **minimal domain model** for the Mini HIS —
just enough entities to produce realistic operational data that can flow
into the Data Warehouse, drive the Realtime Dashboard, and give the AI
Director something meaningful to reason about. Per Principle 3 (*Minimal
Domain*), no entity is added unless it is required to support one of the
example KPIs or example AI Director questions below.

This is a conceptual/logical model. No table schemas, primary keys, or SQL
DDL are defined here — that belongs to `docs/database/` in a later sprint.

## 2. Core Entities

| Entity | Description | Why It Exists |
|---|---|---|
| **Department** | A hospital unit (e.g., Emergency, ICU, Cardiology, General Ward) | Dimension for nearly every KPI; scoping unit for Management Team persona |
| **Bed** | A bed belonging to a Department, with a status (available/occupied) | Basis for occupancy KPIs |
| **Patient (synthetic)** | A minimal, non-identifying patient reference (no real PHI) | Needed to model admissions without becoming an EMR |
| **Admission / Encounter** | A patient's stay: admit time, discharge time (nullable), Department, Bed | Basis for occupancy, length-of-stay (LOS), and admission-rate KPIs |
| **Staff Member** | A minimal staff reference: role (e.g., nurse, physician), Department | Basis for staffing-ratio KPIs |
| **Appointment** | A scheduled, non-admission encounter (e.g., outpatient visit) tied to a Department | Basis for throughput/scheduling KPIs; distinguishes scheduled vs. unscheduled load |

## 3. Explicit Domain Boundaries

- **Patient** records carry only what is needed to compute aggregate KPIs
  (e.g., a synthetic identifier, admission linkage). No clinical, billing,
  or identifying detail is modeled — this is not an EMR (see
  [05_NON_GOALS.md](05_NON_GOALS.md)).
- **Staff Member** records carry only role and department — no payroll,
  scheduling-shift detail, or HR data.
- **Bed** is a simple capacity unit — no equipment, device, or maintenance
  tracking.
- There is no Pharmacy, Inventory, Billing, or Insurance entity anywhere in
  this model, by design.

## 4. Relationships (Conceptual)

```
Department 1---* Bed
Department 1---* StaffMember
Department 1---* Appointment
Bed 1---* Admission (over time, one bed hosts many admissions)
Patient 1---* Admission
Admission *---1 Department (denormalized for reporting convenience)
```

## 5. Data Warehouse Mapping (Conceptual, Not Schema)

The Data Warehouse (see [08_HIGH_LEVEL_ARCHITECTURE.md](08_HIGH_LEVEL_ARCHITECTURE.md))
consumes change events from these entities and organizes them, conceptually,
into:

- **Dimensions:** Department, Bed, Patient (synthetic), Staff Member.
- **Facts:** Admission events (admit/discharge), Appointment events, Bed
  status changes.

Concrete fact/dimension table design is deferred to `docs/database/` in a
later sprint.

## 6. Example KPIs Supported by This Model

These are the KPIs the Realtime Dashboard is expected to show, and the
reason every entity above exists:

1. **Bed Occupancy Rate** (overall and by Department) — from Bed + Admission.
2. **Admission Rate** (admissions per day/week, overall and by Department) —
   from Admission.
3. **Average Length of Stay (LOS)** (overall and by Department) — from
   Admission admit/discharge timestamps.
4. **Staffing Ratio** (staff per occupied bed, by Department) — from Staff
   Member + Bed/Admission.
5. **Appointment Throughput** (scheduled visits per Department per period) —
   from Appointment.

## 7. Example AI Director Questions Supported by This Model

- "What is our current bed occupancy in the ICU?" → Text-to-SQL over
  Bed/Admission facts.
- "Which department has the highest average length of stay this month?" →
  Text-to-SQL over Admission facts.
- "What is our policy when a department's staffing ratio falls below
  target?" → RAG over the Knowledge Base.
- "Admissions in the Emergency department are trending up — what does
  policy say we should do, and how far are we from that threshold today?"
  → Combined Text-to-SQL (current trend) + RAG (policy) + Decision Support
  (threshold comparison).

## 8. Domain Change Control

Any new entity proposed for the Mini HIS must be justified by an unmet KPI
or unmet example question above. If it cannot be tied to one, it is scope
creep and must not be added (see [12_PROJECT_RULES.md](12_PROJECT_RULES.md)).
