# tools/ (Placeholder)

This directory will hold internal developer tooling that supports building,
validating, or exploring this project (e.g., documentation link checkers,
architecture-to-backlog traceability checkers), distinct from the
end-user-facing `scripts/` directory.

## Sprint 0 Status

**Empty by design.** No tooling is authored yet — this sprint is
documentation only. See [../docs/11_ROADMAP.md](../docs/11_ROADMAP.md).

## Planned Contents (Future Sprints)

- A traceability checker validating that every Epic in
  [../docs/backlog/EPICS.md](../docs/backlog/EPICS.md) maps to a container
  in [../docs/08_HIGH_LEVEL_ARCHITECTURE.md](../docs/08_HIGH_LEVEL_ARCHITECTURE.md),
  and vice versa (operationalizing [12_PROJECT_RULES.md](../docs/12_PROJECT_RULES.md)
  Rule 2).
- Documentation link/consistency checks across `docs/`.

## Rule

Tooling here supports the project's own governance and quality, not
hospital-domain functionality — it is never a substitute for an in-scope
module.
