# scripts/ (Placeholder)

This directory will hold developer/operator scripts (e.g., environment
bootstrap, data seeding, demo scenario runners) supporting the modules
defined in
[../docs/08_HIGH_LEVEL_ARCHITECTURE.md](../docs/08_HIGH_LEVEL_ARCHITECTURE.md).

## Sprint 0 Status

**Empty by design.** No scripts are authored yet — this sprint is
documentation only. See [../docs/11_ROADMAP.md](../docs/11_ROADMAP.md)
(scripts arrive alongside their corresponding module's implementation
sprint, with a dedicated demo-scenario pass in Sprint 8).

## Planned Contents (Future Sprints)

- Environment bootstrap scripts (start dependent services).
- Sample data seeding scripts (loads [../sample-data/](../sample-data/)
  into the Mini HIS).
- Demo scenario runners exercising the example AI Director questions in
  [../docs/10_DOMAIN_OVERVIEW.md](../docs/10_DOMAIN_OVERVIEW.md) §7.

## Rule

Every script must exist to support a capability already defined in this
project's documentation — no scripts for out-of-scope domains (see
[../docs/05_NON_GOALS.md](../docs/05_NON_GOALS.md)).
