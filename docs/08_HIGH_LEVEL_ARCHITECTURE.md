# 08 — High-Level Architecture

This document describes the architecture using **C4 thinking** (Context and
Container levels only). No implementation-level or component/code diagrams
are produced in Sprint 0 — those belong in `docs/architecture/` once
implementation begins, and are explicitly deferred (see
[docs/architecture/README.md](architecture/README.md)).

## 1. C4 Level 1 — System Context

### 1.1 The System

**Hospital AI Copilot** — a single logical system, as seen from the outside,
that lets a Hospital Director and their management team interact with
hospital operational data and institutional knowledge through a
conversational AI layer and a dashboard.

### 1.2 Actors (People)

| Actor | Relationship to the System |
|---|---|
| Hospital Director (primary) | Converses with the AI Director; views the Dashboard |
| Hospital Management Team | Views department-scoped Dashboard; occasional AI Director use |
| Hospital CIO | Reviews Administration, Audit Log, and architecture |
| Technical Architects / AI Engineers | Study and extend the reference architecture |
| Students | Learn from documentation and running system |
| System Administrator (implied by Administration/Auth epics) | Manages users, roles, and module configuration |

### 1.3 External / Adjacent Systems

Because this is a reference implementation, most "external systems" are
**simulated inside the system boundary** rather than truly external:

| System | Nature | Role |
|---|---|---|
| Mini HIS | Simulated, inside the boundary | Acts as the system of record for operational hospital data — stands in for a real hospital's HIS |
| OpenAI-compatible LLM API | Truly external | Provides the language model capability behind RAG, Text-to-SQL, and AI Director reasoning |
| (Future) Real Hospital HIS | Not present in this project | Named as the real-world system the Mini HIS stands in for; see §6 Extension Points |

### 1.4 Context Diagram (Descriptive)

```
                        +-----------------------------+
                        |     Hospital Director        |
                        |  (+ Management Team, CIO,    |
                        |   Architects, AI Engineers,   |
                        |   Students)                   |
                        +---------------+---------------+
                                        |
                                        | asks questions, views KPIs,
                                        | administers the system
                                        v
                        +-----------------------------------------+
                        |          Hospital AI Copilot             |
                        |  (Dashboard, AI Director, Warehouse,     |
                        |   Knowledge Base, Mini HIS, Admin, Audit)|
                        +---------------+---------------------------+
                                        |
                                        | LLM completions / embeddings
                                        v
                        +-----------------------------+
                        |  OpenAI-compatible LLM API    |
                        |   (external, swappable)       |
                        +-----------------------------+
```

## 2. C4 Level 2 — Containers

Each container below is a deployable/runnable unit with a single clear
responsibility. Containers correspond 1:1 to the Epics in
[docs/backlog/EPICS.md](backlog/EPICS.md).

| # | Container | Tech | Responsibility |
|---|---|---|---|
| 1 | **Mini HIS** | Java, Quarkus, PostgreSQL | Owns the minimal operational domain (departments, beds, patients, admissions, staff, appointments). System of record for source data. See [10_DOMAIN_OVERVIEW.md](10_DOMAIN_OVERVIEW.md). |
| 2 | **Ingestion / Streaming Backbone** | Kafka (Debezium as future CDC extension point) | Carries change events from the Mini HIS to the Data Warehouse. Decouples operational and analytical planes. |
| 3 | **Hospital Data Warehouse** | ClickHouse | Analytical store of fact/dimension data derived from the Mini HIS. Powers the Dashboard and Text-to-SQL. |
| 4 | **Realtime Dashboard (Backend API)** | Java, Quarkus | Serves aggregated KPI queries from the Warehouse to the frontend. |
| 5 | **Realtime Dashboard (Frontend)** | React, TypeScript, Tailwind | Visualizes KPIs for the Hospital Director and Management Team. |
| 6 | **Knowledge Base Store** | Document store + Qdrant (vector index) | Holds curated institutional documents (policies, SOPs, reports) and their embeddings. |
| 7 | **RAG Service** | Python, OpenAI-compatible API, Qdrant client | Answers natural-language questions grounded in the Knowledge Base via retrieval + generation. |
| 8 | **Text-to-SQL Service** | Python, OpenAI-compatible API | Translates natural-language questions into constrained SQL against the Data Warehouse, executes, and returns structured results. |
| 9 | **AI Director** | Python, OpenAI-compatible API | Orchestrates the Hospital Director's conversation: classifies intent, routes to RAG and/or Text-to-SQL, applies Decision Support logic, composes the final answer. |
| 10 | **Decision Support Logic** | Embedded within AI Director | Threshold/trend rules over Warehouse data that the AI Director can cite when composing recommendations. |
| 11 | **Administration Service** | Java, Quarkus, PostgreSQL | Manages users, roles, and module configuration. |
| 12 | **Authentication Service** | Java, Quarkus | Handles login, session/token issuance, role-based access control, shared by all containers. |
| 13 | **Audit Log Service** | Java, Quarkus, PostgreSQL (or ClickHouse for append-only query volume) | Records AI Director queries, data access, and administrative actions. Queryable by the CIO/Administrator. |

### 2.1 Container Diagram (Descriptive)

```
 [Dashboard Frontend] --HTTP--> [Dashboard Backend API] --SQL--> [Data Warehouse]
                                          ^                              ^
                                          |                              |
 [Hospital Director] --chat--> [AI Director] --routes to--> [RAG Service] --vector search--> [Knowledge Base / Qdrant]
                                          |
                                          +--routes to--> [Text-to-SQL Service] --SQL--> [Data Warehouse]
                                          |
                                          +--applies--> [Decision Support Logic]
                                          |
                                          +--writes--> [Audit Log Service]

 [Mini HIS] --change events--> [Kafka] --sink--> [Data Warehouse]

 [Authentication Service] --secures--> (all Director/Dashboard/Admin entry points)
 [Administration Service] --configures--> (users, roles, module toggles)
```

## 3. Module Responsibilities (Summary Table)

| Module | Single Responsibility | Does NOT Do |
|---|---|---|
| Mini HIS | Own and serve minimal operational hospital data | Clinical charting, billing, pharmacy, devices |
| Data Warehouse | Store analytical facts/dimensions for querying | Serve transactional writes |
| Realtime Dashboard | Visualize KPIs | Author policy, run AI reasoning |
| Knowledge Base | Store and index institutional documents | Store operational/transactional data |
| RAG | Ground natural-language answers in documents | Query structured warehouse data |
| Text-to-SQL | Translate NL to SQL against the warehouse | Answer document/policy questions |
| AI Director | Orchestrate intent routing and compose answers | Directly own data storage |
| Decision Support | Surface threshold/trend-based insights | Make clinical/patient-level decisions |
| Administration | Manage users, roles, module config | Manage clinical or operational workflows |
| Authentication | AuthN/AuthZ for all modules | Manage business data |
| Audit Log | Immutable record of sensitive actions | Alerting/SIEM-grade analysis |

## 4. Cross-Cutting Concerns

- **Authentication & Authorization** apply uniformly to the Dashboard,
  Administration, and AI Director entry points via the Authentication
  Service.
- **Audit Logging** is invoked by the AI Director (every query and its
  routing decision), the Administration Service (every admin action), and
  the Dashboard backend (data access, if configured) — never bypassed.
- **Replaceability** (Principle 5): every container above communicates with
  its neighbors through a narrow, named interface (SQL over the warehouse,
  vector search over Qdrant, REST/HTTP between services, Kafka topics for
  ingestion). No container reaches into another's internal storage.

## 5. Architectural Style

- **Operational/Analytical separation:** the Mini HIS (OLTP, PostgreSQL) is
  cleanly separated from the Data Warehouse (OLAP, ClickHouse), connected
  only via an event stream (Kafka). This mirrors real hospital data
  platform patterns without requiring real hospital-scale infrastructure.
- **Service orchestration over monolith:** the AI Director is a thin
  orchestrator, not a monolithic AI service — it delegates to RAG and
  Text-to-SQL as independent services, which keeps each one independently
  testable, replaceable, and understandable.
- **LLM-agnostic:** all AI components speak to the LLM only via an
  OpenAI-compatible API contract, so the underlying model/provider is
  swappable.

## 6. Future Extension Points

These are explicitly **not built** in this project, but the architecture is
deliberately shaped to accommodate them later without redesign:

| Extension Point | Where It Plugs In | Why It's Deferred |
|---|---|---|
| **Debezium CDC** | Replaces/augments the Kafka producer side of the Ingestion Backbone, capturing changes directly from PostgreSQL | Not required at reference-implementation data volumes; simple producer suffices |
| **Apache Iceberg** | Sits alongside/beneath ClickHouse as a table format for long-term, lakehouse-style storage of warehouse data | Adds operational complexity not needed to demonstrate the pattern |
| **Trino** | Federated query layer over Iceberg + ClickHouse + other future sources | Only valuable once there is more than one analytical source to federate |
| **Real Hospital HIS Integration** | Replaces the Mini HIS container behind the same Kafka/ingestion contract | Out of scope by design (Principle 1); the contract is what matters, not a real integration |
| **Additional AI Director Skills** | New routable capabilities alongside RAG and Text-to-SQL | Kept minimal to two clearly demonstrable reasoning paths for this reference implementation |
| **SSO / Federated Identity** | Extends the Authentication Service | Basic auth sufficient to demonstrate RBAC pattern |

## 7. Traceability

Every container in §2 corresponds to an item in
[03_SCOPE.md](03_SCOPE.md) §2 and an Epic in
[docs/backlog/EPICS.md](backlog/EPICS.md). No container exists without both.
