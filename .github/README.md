# .github/ (Placeholder)

This directory will hold GitHub-specific project configuration: CI/CD
workflows, issue templates, and pull request templates for the Hospital AI
Copilot reference implementation repository.

## Sprint 0 Status

**Empty by design.** No workflows are authored yet — this sprint is
documentation only. CI/CD is expected to come online incrementally as each
module is implemented (starting Sprint 1), with a consolidated pipeline
review in Sprint 8 (see [../docs/11_ROADMAP.md](../docs/11_ROADMAP.md)).

## Planned Contents (Future Sprints)

- `workflows/` — CI pipelines (build/test per service, once services
  exist).
- `ISSUE_TEMPLATE/` — templates aligned to this project's Epic structure
  ([../docs/backlog/EPICS.md](../docs/backlog/EPICS.md)).
- `pull_request_template.md` — a PR checklist enforcing
  [../docs/12_PROJECT_RULES.md](../docs/12_PROJECT_RULES.md) (traceability,
  scope discipline, ADR-when-needed).

## Rule

CI/CD introduced here must validate against this project's own governance
(traceability, scope, replaceability) — not just generic build/test
hygiene.
