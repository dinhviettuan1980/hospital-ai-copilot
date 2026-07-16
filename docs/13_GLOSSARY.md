# 13 — Glossary

| Term | Definition |
|---|---|
| **ADR** | Architecture Decision Record — a short document capturing a single architectural decision, its context, and its consequences. See [docs/adr/](adr/). |
| **AI Director** | The orchestration layer/agent that is the Hospital Director's primary conversational interface; routes questions to RAG and/or Text-to-SQL and composes decision-support answers. |
| **Admission / Encounter** | A patient's stay in the Mini HIS domain model: admit time, discharge time, Department, Bed. |
| **Audit Log** | The append-only record of AI Director queries, data access, and administrative actions. |
| **C4 Model** | A modeling approach for software architecture at four levels of zoom: Context, Container, Component, Code. This project uses only Context and Container in Sprint 0. |
| **CDC (Change Data Capture)** | A technique for streaming database changes as events (e.g., via Debezium). Named as a future extension point, not implemented in this project. |
| **Container (C4 sense)** | A separately deployable/runnable unit (e.g., a service or datastore) in the C4 model — not to be confused with Docker containers, though they often correspond. |
| **Decision Support** | Threshold- and trend-based insight logic surfaced by the AI Director; explicitly not clinical/predictive AI. |
| **Epic** | A large, traceable unit of backlog scope corresponding to one module/capability; Sprint 0 produces Epics only, no tasks. |
| **HIS (Hospital Information System)** | A real-world hospital's operational system of record; this project's Mini HIS is a minimal stand-in, not a full HIS. |
| **Knowledge Base** | The curated set of institutional documents (policies, SOPs, reports) and their vector index, used by RAG. |
| **LOS (Length of Stay)** | The duration of a patient admission, from admit to discharge; a core KPI. |
| **Mini HIS** | The minimal operational hospital system built for this project; the sole system of record inside the reference implementation. |
| **Module** | Used interchangeably with "Container" in this documentation set to describe a major, independently replaceable system part. |
| **OLAP** | Online Analytical Processing — the query pattern served by the Data Warehouse (ClickHouse). |
| **OLTP** | Online Transaction Processing — the query pattern served by the Mini HIS (PostgreSQL). |
| **OpenAI-compatible API** | An LLM API contract (completions/embeddings) that multiple providers implement, allowing the underlying model provider to be swapped without changing calling code. |
| **PHI (Protected Health Information)** | Real, identifiable patient health data. Never used in this project; all data is synthetic. |
| **RAG (Retrieval-Augmented Generation)** | A pattern where an LLM's answer is grounded by retrieving relevant documents (from the Knowledge Base via Qdrant) before generating a response. |
| **Reference Implementation** | A deliberately minimal, educational build that demonstrates an architecture pattern — not a production system. |
| **Replaceability** | The design property (Principle 5) that any module can be swapped for a real hospital's equivalent system without rearchitecting the rest. |
| **Text-to-SQL** | A pattern where an LLM translates a natural-language question into SQL, executed against the Data Warehouse under guardrails. |
| **Traceability** | The requirement (Principle 2) that every feature maps back to a capability named in the source proposal. |
| **Warehouse (Hospital Data Warehouse)** | The analytical datastore (ClickHouse) holding fact/dimension data derived from the Mini HIS, used by the Dashboard and Text-to-SQL. |
