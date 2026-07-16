# 09 — Technology Stack

This document lists the technology envelope for the project. No versions or
implementation configuration are pinned in Sprint 0 — that is an
implementation-sprint concern. This is a **decision boundary**, not a setup
guide.

## 1. Backend (Operational Plane)

| Technology | Used For |
|---|---|
| **Java** | Primary backend language for Mini HIS, Dashboard API, Administration, Authentication, Audit Log |
| **Quarkus** | Backend application framework — chosen for fast startup and container-native fit, appropriate for a multi-service reference implementation |
| **PostgreSQL** | Operational (OLTP) datastore for the Mini HIS, Administration, and Authentication modules |

## 2. Frontend

| Technology | Used For |
|---|---|
| **React** | Dashboard and AI Director chat UI |
| **TypeScript** | Type safety across all frontend code |
| **Tailwind CSS** | Styling system for the Dashboard and chat UI |

## 3. Data Plane (Analytical)

| Technology | Used For |
|---|---|
| **ClickHouse** | Hospital Data Warehouse — columnar analytical store for facts/dimensions, and the query target for Text-to-SQL |
| **Kafka** | Streaming backbone carrying change events from the Mini HIS to the Data Warehouse |

### 3.1 Future Data Plane Support (Not Built in This Project)

| Technology | Future Role |
|---|---|
| **Apache Iceberg** | Table format for lakehouse-style long-term storage |
| **Trino** | Federated SQL query engine across multiple analytical sources |
| **Debezium** | Change Data Capture directly from PostgreSQL, replacing a hand-rolled producer |

## 4. AI Layer

| Technology | Used For |
|---|---|
| **Python** | Language for RAG, Text-to-SQL, and AI Director services |
| **OpenAI-compatible APIs** | LLM completions and embeddings — the AI layer targets this contract so any compatible provider (hosted or self-hosted) can be substituted |
| **Qdrant** | Vector database backing the Knowledge Base for RAG retrieval |

## 5. Why This Stack

- **Java/Quarkus + PostgreSQL** for the operational plane reflects how real
  hospital operational systems are commonly built in enterprise settings —
  the reference implementation should feel familiar to a hospital's
  existing engineering team, not exotic.
- **React/TypeScript/Tailwind** is a mainstream, well-understood frontend
  stack, keeping the Dashboard approachable to a broad audience of students
  and engineers.
- **ClickHouse + Kafka** demonstrates a realistic, lightweight
  operational-to-analytical data pipeline pattern without requiring a full
  lakehouse stack — while leaving Iceberg/Trino/Debezium as clearly named
  extension points for anyone who wants to scale the pattern up.
- **Python + OpenAI-compatible APIs + Qdrant** is the dominant, most legible
  stack for RAG and LLM orchestration work today, maximizing the
  educational value of the AI layer for AI engineers and students.

## 6. Stack Boundaries (What This Section Does NOT Decide)

- Specific library versions, ORMs, build tools, or package managers.
- Specific OpenAI-compatible provider (e.g., OpenAI itself vs. a
  self-hosted alternative) — this is a runtime configuration choice, not an
  architectural one.
- Deployment/orchestration technology (e.g., specific container runtime,
  Kubernetes vs. Docker Compose) — deferred to `docker/` and the
  implementation sprints.
- Testing frameworks and CI tooling — deferred to `.github/` and
  implementation sprints.

## 7. Replaceability Notes

Per Principle 5, every technology choice above sits behind a module
boundary defined in
[08_HIGH_LEVEL_ARCHITECTURE.md](08_HIGH_LEVEL_ARCHITECTURE.md):

- PostgreSQL (Mini HIS) can be replaced by a real HIS's own database without
  affecting the Data Warehouse, as long as the ingestion contract
  (Kafka topics / future Debezium CDC) is preserved.
- ClickHouse can be replaced by another OLAP engine without affecting the
  Dashboard or Text-to-SQL contracts, as long as SQL semantics are
  preserved.
- The OpenAI-compatible API contract means the underlying LLM provider can
  be swapped without touching RAG, Text-to-SQL, or AI Director logic.
- Qdrant can be replaced by another vector database without affecting the
  RAG service's external contract.
