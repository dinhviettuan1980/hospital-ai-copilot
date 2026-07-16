# 05 — Non-Goals

Non-goals are as important as goals. Every item below has been explicitly
excluded from this project. If any future request reintroduces one of these,
it must be rejected unless the source proposal itself is revised.

## 1. This Is Not a Full Hospital Information System

The Mini HIS module exists solely to produce enough realistic operational
data to exercise the warehouse, dashboard, and AI layers. It is not, and
will never become, a general-purpose HIS.

## 2. Explicitly Excluded Domains

The following hospital/enterprise domains are **out of scope**, in full,
regardless of how natural an addition they might seem:

- Pharmacy management
- Inventory / supply chain management
- Payroll
- Insurance and claims processing
- Medical device integration
- PACS Viewer (Picture Archiving and Communication System)
- Radiology Viewer
- EMR (Electronic Medical Record) replacement or clinical charting
- AI Diagnosis (clinical decision-making about individual patients)
- AI Image Recognition (radiology, pathology, etc.)
- IoT device integration
- Robotics / automation
- Billing systems
- National Health Integration (e.g., national health information exchanges)

## 3. Why These Are Excluded

- **Not named in the proposal.** The source "AI Hospital Copilot" proposal
  describes a director-facing analytics and knowledge copilot, not a
  clinical or operational system of record for these domains.
- **Scope discipline.** Each of these domains is, on its own, a
  multi-year enterprise product. Including any of them would turn this
  reference implementation into an attempted full HIS/EHR, which directly
  violates Principle 1 (*Model the architecture, not the hospital*).
- **Risk.** Several of these domains (EMR, AI Diagnosis, medical devices)
  carry direct patient-safety and regulatory implications that are entirely
  inappropriate for an educational reference implementation.

## 4. Non-Goals for the AI Layer Specifically

- The AI Director does **not** make clinical recommendations about
  individual patients.
- The AI Director does **not** perform diagnosis, triage, or treatment
  suggestions.
- RAG and Text-to-SQL operate only over the Knowledge Base and Data
  Warehouse defined in this project — not over arbitrary external data
  sources.
- No image, waveform, or signal data (radiology, ECG, monitoring feeds) is
  ingested or interpreted anywhere in this system.

## 5. Non-Goals for Sprint 0 Specifically

- No source code (Java, React, SQL, Docker, API contracts) is produced in
  this sprint.
- No ADRs are authored yet — only the ADR process and directory structure.
- No implementation-level backlog items (tasks, stories) are created — only
  Epics.

## 6. Boundary Cases

Some capabilities sound related to excluded domains but are explicitly
retained because they support the in-scope AI Director experience:

| Sounds Like | Actually Is | Status |
|---|---|---|
| "Inventory" | Bed availability count used for occupancy KPIs | In scope, as a Mini HIS attribute, not an inventory system |
| "Billing" | Admission/discharge records used for LOS metrics | In scope, as operational data, not a billing/claims system |
| "EMR" | Minimal encounter/admission record | In scope at Mini HIS depth only, never clinical charting |

When in doubt, the test in [03_SCOPE.md](03_SCOPE.md) §6 applies: cite the
proposal, or leave it out.
