# sample-data/ (Placeholder)

This directory will hold synthetic sample data for the Mini HIS domain
(departments, beds, synthetic patients, admissions, staff, appointments)
and a small curated set of Knowledge Base documents (policies, SOPs,
sample reports), used to seed a running instance of this reference
implementation.

## Sprint 0 Status

**Empty by design.** No sample data is authored yet — this sprint is
documentation only. Sample data is targeted for Sprint 8 (demo scenarios),
though a minimal seed set may appear earlier alongside the Mini HIS
(Sprint 1) and Knowledge Base (Sprint 4) if needed for testing.

## Planned Contents (Future Sprints)

- `mini-his/` — synthetic department, bed, patient, admission, staff, and
  appointment records matching
  [../docs/10_DOMAIN_OVERVIEW.md](../docs/10_DOMAIN_OVERVIEW.md).
- `knowledge-base/` — curated policy/SOP/report documents used by RAG.

## Rule

All data here is synthetic. Per
[../docs/12_PROJECT_RULES.md](../docs/12_PROJECT_RULES.md) Rule 6, real
Protected Health Information (PHI) must never be introduced into this
directory.
