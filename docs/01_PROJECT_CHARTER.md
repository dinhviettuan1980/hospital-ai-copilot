# 01 — Project Charter

## 1. Purpose of This Document

This charter is the authoritative anchor for the Hospital AI Copilot reference
implementation. Every subsequent document, epic, and (eventually) line of code
must be traceable back to this charter. If a proposed feature cannot be
justified by this document, it does not belong in the project.

## 2. Project Name

**Hospital AI Copilot (Reference Implementation)**

## 3. Sponsor / Source Proposal

This project implements the architecture described in the **"AI Hospital
Copilot" strategy proposal** provided to the project team. That proposal
describes an AI-assisted decision-support system for hospital leadership,
built on top of a data warehouse, a knowledge base, retrieval-augmented
generation (RAG), and natural-language-to-SQL querying, orchestrated by an
"AI Director" agent.

This repository does not reproduce that proposal in full; it produces a
**minimal, working reference architecture** that demonstrates every major
component described in it, at a scale a single developer can build and a
reader can fully understand.

## 4. Problem Statement

Hospital directors and management teams are surrounded by operational data
(admissions, occupancy, staffing, throughput) and unstructured knowledge
(policies, SOPs, committee reports) but lack a unified, conversational way to
query both. Existing Hospital Information Systems (HIS) are transactional,
not analytical, and are not designed to answer natural-language strategic
questions such as *"Which department had the highest bed occupancy last
month, and what policy governs escalation when it happens?"*

The AI Hospital Copilot proposal addresses this by combining a data
warehouse, a knowledge base, and AI orchestration into a single "copilot"
experience for hospital leadership.

## 5. Project Objective

Deliver an **open-source, educational, enterprise-grade reference
implementation** that demonstrates how the AI Hospital Copilot architecture
fits together end-to-end — from a minimal operational data source, through a
data warehouse and knowledge base, to an AI orchestration layer that a
Hospital Director can converse with.

The reference implementation exists to teach and demonstrate architecture,
not to operate a real hospital.

## 6. Guiding Principles

These principles govern every decision made in this project. They are
restated in full in [12_PROJECT_RULES.md](12_PROJECT_RULES.md).

1. **Model the architecture, not the hospital.** The Mini HIS exists only to
   produce enough realistic data to exercise the warehouse, dashboard, and AI
   layers.
2. **Traceability.** Every feature must map to a capability named in the
   source proposal. No feature creep.
3. **Minimal domain.** Simulate only the hospital workflow needed to support
   the AI Copilot — not a full clinical or operational system.
4. **Architecture first.** No implementation begins until the architecture is
   documented and approved.
5. **Replaceability.** Every module is designed as an independently
   replaceable component, so a real hospital could swap in its own HIS, its
   own warehouse, or its own LLM provider without rearchitecting the system.

## 7. In-Scope Capabilities

See [03_SCOPE.md](03_SCOPE.md) for full detail. Summary:

- Mini HIS (minimal operational data source)
- Hospital Data Warehouse (analytical store)
- Realtime Dashboard
- Knowledge Base
- Retrieval-Augmented Generation (RAG)
- Text-to-SQL
- AI Director (orchestration + decision support)
- Administration, Authentication, Audit Log

## 8. Out-of-Scope

See [05_NON_GOALS.md](05_NON_GOALS.md). Summary: no full HIS, no pharmacy,
inventory, payroll, insurance, medical devices, PACS/radiology viewers, EMR
replacement, AI diagnosis, AI image recognition, IoT, robotics, billing, or
national health integration.

## 9. Primary Stakeholder / User

**Hospital Director** — the primary persona for whom the AI Director
experience is designed. See [07_PERSONAS.md](07_PERSONAS.md) for the full
stakeholder map.

## 10. Technology Envelope

Java/Quarkus + PostgreSQL (operational), React/TypeScript/Tailwind (frontend),
ClickHouse + Kafka (analytical data plane, with Iceberg/Trino/Debezium as
future extension points), Python + OpenAI-compatible APIs + Qdrant (AI
layer). Full detail in [09_TECH_STACK.md](09_TECH_STACK.md).

## 11. Current Sprint

**Sprint 0 — Foundation.** Documentation only. No source code is written in
this sprint. See [11_ROADMAP.md](11_ROADMAP.md).

## 12. Definition of Done for Sprint 0

- Complete repository structure created (folders only, per module).
- All 13 numbered governance documents present and internally consistent.
- High-level (C4-style) architecture documented at Context and Container
  level, with module responsibilities and future extension points.
- Backlog expressed as Epics only — no implementation tasks.
- ADR directory structured and ready, with process documented but no
  decisions recorded yet.
- Every document reviewed against the charter for scope creep.

## 13. Change Control

Any change that adds a capability not listed in Section 7, or that expands
the domain model beyond [10_DOMAIN_OVERVIEW.md](10_DOMAIN_OVERVIEW.md), must
be justified in writing and recorded as an ADR before it is accepted into the
backlog.
