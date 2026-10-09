Done. Four files written directly (no `[QUESTION]` raised; one ADR instead).

## Written

| File | Content |
|---|---|
| `governance-shared/analysis/modules/FIN/test_gen/backend-test-plan-fin.md` | `PHASE:TEST-PLAN-BE` split into `SUB:RULE-SCENARIOS` (24 TCs) + `SUB:API-SCENARIOS` (28) — **TC-FIN-001 … TC-FIN-052** |
| `.../frontend-test-plan-fin.md` | `PHASE:TEST-PLAN-FE` split into `SUB:UI-FLOWS` (35) + `SUB:INT-FLOW` (5) — **TC-FIN-053 … TC-FIN-092** |
| `.../test-execution-manifest-fin.md` | dependency order · RULE → CODE → TC · entity CRUD checklist (manifest is ON in the profile) |
| `governance-shared/analysis/decisions/FIN/ADR-FIN-009.md` | the three non-mechanical derivation choices (next free seq after ADR-FIN-008) |

`system-test-index-erp.md` was **not** written and no integration phase was emitted — scope is `module`, so §2 rule 3 applies and the existing platform file is untouched.

## Verified mechanically
- 92 TC marker pairs, all `START`/`END` matched, ids contiguous `001…092`, no duplicate, no restart.
- Every TC's `traces=` carries both its `AC-*` and its `REQ-*` (C10.1).
- Every `AC-FIN-001…046` is referenced by ≥1 backend TC — 46/46, zero ✗ (C10.2).
- Zero lines inside a TC record begin with an ID token, so no record silently truncates.
- TC counts are 1.13× (backend) and 1.00× (frontend) of their AC counts — under the ~2× guard.

## One thing worth your attention
FIN ships behaviour that **no AC, REQ or RULE states**: the `FIN-409-NOT-ACTIVE` run gate, the four deactivate endpoints, API-FIN-033, the keying-id 404s on the three reports, the dormant-year lineless entry, and RULE-FIN-017's two 400s. I wrote no TC for any of them — doing so would invent the acceptance criteria this engine claims to derive from, and the FIN artifacts warn twice not to read that gate back as spec. They are cited only as setup where an AC needs them (API-FIN-035 is the *only* way AC-FIN-021's inactive dimension value is reachable), and both plans carry an explicit "catalog rows no TC exercises" list. `API covered` therefore reads 26/37 on the backend track by design. Closing that is a P1 act — writing the missing ACs — not a test-generation one; ADR-FIN-009 records it.

`governance-shared/` is gitignored in this repo, so the files are on disk but untracked — the existing arrangement, which I left alone.