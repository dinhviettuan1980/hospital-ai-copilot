# Sprint 0 Architecture Review — Hospital AI Copilot (Reference Implementation)

**Reviewer:** Architecture Review Board (independent audit)
**Reviewed Artifact:** Full repository as delivered at end of Sprint 0 (documentation only)
**Review Type:** Pre-Sprint 1 Gate Review

---

# Executive Summary

**Overall Score: 58 / 100**

**Recommendation: PASS WITH CHANGES**

The documentation set is well-organized, internally cross-linked, and mostly free of the domain-level feature creep it explicitly guards against (no pharmacy, billing, PACS, or clinical-AI leakage found anywhere). However, the audit found a **self-contradicting backlog rule**, a **service/infrastructure footprint that contradicts the project's own "single developer" charter constraint**, an **oversized Sprint 1**, and **incomplete domain and replaceability coverage** for modules that are equally in-scope but were not modeled with the same rigor as the Mini HIS. These are not cosmetic problems — they will surface as real friction the moment implementation starts, and one of them (the Epic/container mapping claim) is a documentation defect that is factually false as written, not a matter of opinion.

The foundation is salvageable and largely sound in its governance structure, but it is not yet safe to build on.

---

# Findings

## Critical Issues

**C1. The backlog's "1:1 Epic-to-container" rule is false, as written, by its own evidence.**
`docs/backlog/EPICS.md` states: *"Each Epic maps 1:1 to a container in 08_HIGH_LEVEL_ARCHITECTURE.md §2"*, and `docs/backlog/README.md` Rule 1 states: *"One Epic per container/module. No Epic spans multiple containers, and no container lacks an Epic."* The Epic Coverage Check table in the same file directly contradicts this: Epic 2 covers both *Ingestion/Streaming Backbone* and *Hospital Data Warehouse*; Epic 3 covers both *Dashboard Backend* and *Dashboard Frontend*; Epic 7 covers both *AI Director* and *Decision Support*. That is 3 of 10 Epics spanning multiple containers, against a 13-container architecture. This is the exact enforcement mechanism the project relies on to guarantee traceability (Principle 2), and it does not hold up under its own self-check. Either the rule is wrong (the granularity should be "Epic maps to one-or-more containers") or the coverage table is wrong. As written, the document lies to its own reader.

**C2. The technology footprint contradicts the charter's explicit "single developer" constraint.**
The Mission statement in `docs/01_PROJECT_CHARTER.md` promises a system "a single developer can build." The architecture that was actually produced requires standing up and operating: 5 separate Quarkus/JVM services (Mini HIS, Dashboard Backend API, Administration, Authentication, Audit Log), 3 separate Python services (RAG, Text-to-SQL, AI Director), 1 React/TypeScript frontend, plus Kafka, ClickHouse, PostgreSQL, and Qdrant as distinct infrastructure. That is **9 independently deployable application services and 4 distinct stateful data technologies** before Sprint 1 has even started. This is not "small enough for a single developer" — it is a small enterprise platform team's workload. No document in the repository acknowledges this tension or proposes consolidation (e.g., collapsing Administration + Authentication + Audit Log into a single "Platform" service, or unifying RAG + Text-to-SQL + AI Director into one Python process with three internal modules). Left unaddressed, this will either silently balloon the timeline or force an undocumented, unreviewed simplification mid-project — exactly what Principle 4 ("Architecture First") exists to prevent.

**C3. Sprint 1 is not "very small," and is optimistic given C2.**
`docs/11_ROADMAP.md` Sprint 1 bundles the *entire* Mini HIS domain (6 entities: Department, Bed, Patient, Admission, Staff Member, Appointment, full CRUD) **and** the Authentication service (login, session/token, RBAC) **and** "first ADRs recorded" into one sprint, for a stated single-developer team. Any one of these three items is a reasonable sprint on its own. Combined, and combined with the unresolved service-count problem in C2, Sprint 1 as scoped is not achievable in a single sprint without either silently descoping mid-flight or producing a shallow, unreviewed implementation of one of the two Epics.

## Major Issues

**M1. Decision Support is modeled as a Container but is explicitly not one.**
`docs/08_HIGH_LEVEL_ARCHITECTURE.md` §2 lists "Decision Support Logic" as container **#10** in a C4 **Container-level** table, with tech listed as *"Embedded within AI Director."* A component embedded inside another container's process is a **Component** (C4 Level 3), not a Container. Numbering it alongside 12 genuine containers misrepresents its deployability and, more importantly, silently contradicts Principle 5 (*every module is independently replaceable*) — Decision Support has no independent interface, no independent deployment, and therefore no independent replaceability, despite being catalogued as if it had all three.

**M2. "System Administrator" is a named actor with no persona definition.**
`docs/08_HIGH_LEVEL_ARCHITECTURE.md` §1.2 lists *"System Administrator (implied by Administration/Auth epics)"* as a first-class actor in the System Context, and `docs/backlog/EPICS.md` Epic 8 is explicitly written "for the System Administrator / CIO persona." `docs/07_PERSONAS.md` — the document whose entire purpose is to define every actor — has no System Administrator entry at all. A named actor exists in two documents and is undefined in the one document responsible for defining actors.

**M3. Audit Log Service's storage technology is left as an unresolved either/or inside "approved" Sprint 0 architecture.**
`docs/08_HIGH_LEVEL_ARCHITECTURE.md` §2 row 13 lists the Audit Log Service tech as *"Java, Quarkus, PostgreSQL (or ClickHouse for append-only query volume)."* Sprint 0's own exit criteria (`01_PROJECT_CHARTER.md` §12) requires the architecture to be approved before implementation begins. An unresolved technology fork inside a table presented as final output is not an approved decision — it is a deferred one wearing the clothes of a decision. It should either be resolved now or explicitly marked "TBD — resolve via ADR in Sprint 7" rather than presented as settled.

**M4. The `Patient` entity fails the domain model's own inclusion test.**
`docs/10_DOMAIN_OVERVIEW.md` §8 states new entities require justification by "an unmet KPI or unmet example question." Section §6 (5 KPIs) and §7 (4 example AI Director questions) never reference Patient or patient identity — every KPI and question is department/time-scoped (occupancy, admission rate, LOS, staffing ratio, throughput). `Patient`'s only justification given (§2) is *"needed to model admissions without becoming an EMR"* — a self-referential rationale, not a KPI-driven one. As modeled, `Patient` is a dimension with no consumer, which is precisely the kind of speculative entity Principle 3 (*Minimal Domain*) exists to prevent. Either add a KPI/question that requires patient-level linkage (e.g., readmission-adjacent, if that can be justified without drifting toward EMR/clinical territory), or collapse it into a non-entity attribute of Admission.

**M5. The Knowledge Base has zero conceptual domain modeling.**
`docs/10_DOMAIN_OVERVIEW.md` models the Mini HIS domain in detail (6 entities, relationships, warehouse mapping) but the Knowledge Base — an equally in-scope container (Epic 4) — is never modeled at all; the term appears exactly once, in a KPI-question example. No document anywhere describes what a Knowledge Base document *is* conceptually (title, category, policy vs. SOP vs. report, effective date, version). Sprint 4 implementers will have to invent this from nothing, violating "Architecture First" for that module specifically, even though every other module received this treatment.

**M6. Replaceability claims are not backed for most modules.**
Principle 5 claims *every* module is independently replaceable. `docs/09_TECH_STACK.md` §7 ("Replaceability Notes") substantiates this for exactly 4 of 13 containers: Mini HIS/PostgreSQL, ClickHouse, the LLM API, and Qdrant. Kafka, Authentication, Administration, Audit Log, the Dashboard services, RAG, Text-to-SQL, AI Director, and Decision Support have no stated replacement story anywhere in the repository. A principle that is asserted for "every module" but demonstrated for less than a third of them is not yet a verified property of the architecture — it is an aspiration.

## Minor Issues

**m1.** `docs/architecture/` is an empty directory whose README exists only to point back to `08_HIGH_LEVEL_ARCHITECTURE.md`. Housing the actual C4 content outside its dedicated folder, with the folder itself contributing nothing but a forward pointer, is structurally awkward and worth consolidating in a later pass.

**m2.** "Realtime Dashboard" is named throughout (charter, scope, epics, architecture) but "realtime" is never technically defined — no refresh interval, no push/poll/websocket mechanism is specified anywhere. This is a specific, testable technical claim currently left as marketing language.

**m3.** The project repeatedly describes itself as "open-source" (`02_VISION.md`, README) but ships no `LICENSE` file, no `CONTRIBUTING.md`; the README explicitly defers the license decision ("Not yet decided"). Calling a repository open-source before licensing it is premature and should be softened or resolved.

**m4.** `.claude/` is committed as a top-level, structurally-documented folder in a repository whose stated audience includes CIOs and technical architects evaluating it for adoption. Its README doesn't explain why AI-assistant tooling configuration is versioned alongside enterprise governance documentation. A one-line rationale (or moving it out of the tracked structural tree) would remove an obvious "why is this here" question from a real review.

**m5.** The ADR template in `docs/adr/README.md` is inline markdown rather than a separate `0000-template.md` file, which is the more common convention for ADR tooling and copy-paste workflows. Not wrong, just inconsistent with common practice.

**m6.** Several "Success Metrics" in `06_SUCCESS_METRICS.md` §3 (e.g., "a new reader can explain the end-to-end data flow... after reading the docs alone") have no defined validation method — no review checkpoint, no test subject, no pass/fail criterion. As written they are unfalsifiable aspirations, not metrics.

---

# Scope Audit

**FAIL**

Scope *content* is clean: an exhaustive cross-document grep for every item on the exclusion list (pharmacy, billing, PACS/radiology, EMR, AI diagnosis, image recognition, IoT, robotics, insurance, national health integration) found no leakage outside the documents whose explicit job is to name the exclusions. On content alone this would pass.

It fails on the audit's other explicit question: **"Is the scope too large for a reference implementation?"** The answer, evidenced in C2, is yes. Nine independently deployable services and four stateful data technologies is enterprise platform scope, not single-developer reference-implementation scope, regardless of how narrow each individual module's domain logic is. Scope discipline was applied to *domain breadth* (correctly) but not to *service/deployment topology* (not applied at all).

---

# Architecture Audit

**FAIL**

Layering (OLTP/OLAP separation, orchestration-over-monolith for the AI layer, LLM-agnostic contract) is genuinely well-reasoned and is the strongest part of this deliverable. It fails on rigor at the edges: Decision Support is modeled at the wrong C4 level and contradicts its own replaceability claim (M1), replaceability is asserted universally but substantiated for under a third of containers (M6), and one technology decision was left unresolved inside a table presented as final (M3). An architecture document that governs implementation should not contain internal category errors or open forks.

---

# Domain Audit

**FAIL**

The Mini HIS domain model itself is genuinely minimal and mostly well-justified — five of six entities trace cleanly to a KPI or example question, and the explicit non-goals (no clinical, billing, or HR depth) are respected. It fails because: (1) `Patient`, one of the six entities, does not pass the document's own inclusion test (M4); and (2) the domain modeling effort was applied unevenly — Mini HIS got a full entity/relationship/warehouse-mapping treatment while the Knowledge Base, an equally in-scope container, got none (M5). A domain audit has to evaluate coverage across all in-scope domains, not just the one that was modeled well.

---

# Repository Audit

**PASS**

Folder structure matches the requested layout exactly, naming conventions (`NN_TITLE.md`) are consistent and correctly ordered, every placeholder directory has a README stating its Sprint 0 status and forward rule, and internal relative links were verified to resolve (including directory-style links). The only issues found (m1, m3, m4, m5) are minor and do not compromise navigability or professionalism. This is the one audit area where execution matched intent without a structural defect.

---

# Roadmap Audit

**FAIL**

The sprint sequencing logic (data-dependency-ordered: operational → analytical → dashboard → AI layer) is sound and correctly defers Iceberg/Trino/Debezium/SSO as documented, unscheduled extension points. It fails on feasibility: Sprint 1 bundles two Epics plus first-ADR work for a single developer (C3), and that infeasibility is compounded by the unresolved service-count problem from the architecture itself (C2). A roadmap cannot be judged realistic in isolation from the architecture it is sequencing — a "small" Sprint 1 that sits on top of a 9-service architecture isn't actually small.

---

# Top 10 Recommendations

1. **Resolve the Epic/container mapping contradiction (C1).** Either change the rule in `EPICS.md` and `backlog/README.md` to "each Epic covers one or more containers, and every container maps to exactly one Epic," or split Epics 2, 3, and 7 so the 1:1 claim becomes literally true. Pick one and make the documents agree with each other.
2. **Confront the single-developer vs. 9-service contradiction (C2) head-on, in an ADR.** Either explicitly revise the charter's "single developer" framing to acknowledge a small-team or extended-timeline reality, or consolidate services (e.g., merge Administration + Authentication + Audit Log into one Platform service; merge RAG + Text-to-SQL + AI Director into one Python application with internal module boundaries instead of three network hops). Do this before Sprint 1, not during it.
3. **Re-scope Sprint 1 (C3).** Split it: Authentication alone (it is a hard dependency for everything else), or a minimal 2–3 entity slice of the Mini HIS, but not the full 6-entity domain plus Authentication plus first ADRs in one sprint.
4. **Re-classify Decision Support (M1).** Either give it a real container boundary (its own service/library with a defined interface) consistent with Principle 5, or explicitly demote it out of the Container-level table into a documented Component of the AI Director, and stop counting it as one of "13 containers."
5. **Add "System Administrator" to `07_PERSONAS.md` (M2)**, or remove the actor from the architecture doc and Epic 8 description if it isn't actually meant to be a distinct persona from the CIO.
6. **Decide the Audit Log storage technology, or explicitly mark it TBD-pending-ADR (M3).** Do not leave an "or" inside a table that Sprint 0's own exit criteria call "approved architecture."
7. **Resolve the `Patient` entity (M4).** Either cite a real KPI/question that needs patient-level linkage, or remove it as a standalone entity and fold patient reference into Admission.
8. **Add minimal Knowledge Base domain modeling to `10_DOMAIN_OVERVIEW.md` or a sibling document (M5)**, at the same level of rigor given to the Mini HIS, before Sprint 4 needs it.
9. **Complete the Replaceability Notes in `09_TECH_STACK.md` §7 (M6)**, covering all 13 containers, or soften Principle 5's "every module" language to match what has actually been demonstrated.
10. **Add a LICENSE file or stop describing the project as "open-source" until one exists (m3)**; address the `.claude/` rationale (m4) while you're in that area.

---

# Final Decision

**NO**

Sprint 1 cannot begin yet. The blocking items are C1, C2, and C3: a self-contradicting backlog rule, an architecture whose service footprint contradicts the project's own single-developer constraint, and a Sprint 1 scope that is not small under either the roadmap's own standard or the architecture that underlies it. None of these require new architecture or new features to fix — they require resolving contradictions already present in the existing documents (per Recommendations 1–3) and recording the resulting decisions as ADRs, consistent with Rule 9 in `12_PROJECT_RULES.md`. Once C1–C3 are resolved and M1–M3 (the items that directly touch Sprint 1's dependencies: Decision Support's boundary, the Administrator persona, and the Audit Log tech decision) are addressed, this repository is in good enough shape to gate Sprint 1 start.
