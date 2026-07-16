# hospital-backend

Quarkus + PostgreSQL API. Sprint 1: Department/Patient/Visit CRUD. Sprint 2:
Executive Dashboard, Command Center (rule-based alerts), a demo AI Director
(no LLM), and a Knowledge Center (document upload/search). Sprint D1:
Hospital Discovery — a survey/questionnaire module, independent of the
Mini HIS. See the root [README.md](../README.md) for full run instructions,
requirements, feature descriptions, and demo scripts.

## Quick start

```bash
./mvnw quarkus:dev
```

Requires a reachable PostgreSQL database, configured via environment
variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) or a local `.env` file
— see the root README for details. Schema is created/updated automatically
on startup, and demo data is seeded on first run: 5 departments (Emergency,
ICU, Cardiology, Surgery, Outpatient), beds per department, 50 patients,
200 visits, a handful of Knowledge Center documents, and the Hospital
Discovery catalog (20 sections, ~200 questions) plus 2 sample projects.

- API base: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/q/swagger-ui`
- Health: `http://localhost:8080/q/health`

## Tests

```bash
./mvnw test
```

Service-layer unit tests only (Mockito-based, no database required).

## Layout

```
src/main/java/com/hospital/
  entity/       # JPA entities: Department/Patient/Visit/Bed, DocumentCategory/KnowledgeDocument,
                #   DiscoveryProject/DiscoverySection/DiscoveryQuestion/DiscoveryAnswer/DiscoveryAttachment
  repository/   # Panache repositories (search/pagination/aggregation queries)
  dto/          # Request/response records
  mapper/       # Entity <-> DTO mapping
  service/      # Business logic, validation, transactions, Command Center rules, AiDirectorEngine,
                #   Discovery survey/progress/export/import
  controller/   # JAX-RS REST endpoints
  exception/    # Custom exceptions + JAX-RS exception mappers
  storage/      # LocalFileStorage — shared by Knowledge Center and Discovery attachment upload
  seed/         # Startup demo-data seeders (Mini HIS/Sprint2 seeder + Discovery catalog seeder)
```

## AI Director

`AiDirectorEngine` is an interface with one implementation today,
`RuleBasedAiDirectorEngine` — keyword matching over a fixed set of known
questions, answered via real aggregation queries (not an LLM). A future
implementation of the same interface (e.g. LLM + RAG + Text-to-SQL) can
replace it without changing `AiDirectorController` or the frontend.

## File storage

Both Knowledge Center and Hospital Discovery attachments share
`storage.LocalFileStorage` (bytes on the local filesystem, metadata in
PostgreSQL, no embeddings/vector search):

- Knowledge Center: `hospital.knowledge.storage-path` (default
  `./data/knowledge-uploads`) — `.pdf`/`.docx` only.
- Hospital Discovery: `hospital.discovery.storage-path` (default
  `./data/discovery-attachments`) — `.pdf`/`.docx`/`.xlsx`/`.png`/`.jpg`.

Both are gitignored; override via `KNOWLEDGE_STORAGE_PATH` /
`DISCOVERY_STORAGE_PATH`.

## Hospital Discovery notes

- The section/question catalog (`discovery_section`, `discovery_question`)
  is global, shared across every project, and seeded once — there is no
  write API for it yet (see the root README's Future Roadmap).
- `DiscoverySurveyService` and `DiscoveryExportService` bulk-load answers
  and attachments per project (one query each) rather than per question —
  with ~200 questions against a remote database, per-question queries were
  the difference between a 2-second page load and a 27-second one.
- Deleting a project cascades to its answers and attachments first
  (`discovery_answer`/`discovery_attachment` reference the project via a
  non-cascading FK) before the project row itself.
