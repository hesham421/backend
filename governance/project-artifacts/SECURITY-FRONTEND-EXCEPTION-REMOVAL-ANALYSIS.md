# SECURITY Frontend Exception — Removal Analysis & Dry-Run Plan

**Status: DRAFT — not yet approved.** This document exists to satisfy the
confirmation process CLAUDE.md's STRUCTURAL LAW section requires before its
own Ownership table changes: *"evidence-based analysis, a dry-run plan, and
explicit human confirmation... Silent structural drift is treated as a
governance violation, not a convenience."* Nothing below has been executed.

Requested change: remove SECURITY's PERMANENT EXCEPTION row so it can get a
normal `frontend/governance/modules/SECURITY/` footprint like any other
module (frontend execution plan, Playwright test track, mockups, etc.).

---

## 1. Current state (verified against this checkout, 2026-08-26)

**Backend side** — `backend/governance/modules/SECURITY/`:
```
api-docs/endpoints/   authentication.md, menu-management.md,
                      page-management.md, permission-management.md,
                      role-access-control.md,
                      security-datascope-role-branches.md,
                      security-datascope-user-profiles.md,
                      user-management.md
p0/                   business-policies-SECURITY.md, module-registry-SECURITY.md
p0.5/                 prd-SECURITY-2.md
p1/                   srs-SECURITY.md
reports/              TEST-REPORT-SECURITY-2026-07-11.md,
                      TEST-REPORT-SECURITY-2026-07-17-FIXES.md
test api/             security_api_test_report.html,
                      security_problems_report.md,
                      test_security_apis-2.py
```

Notably **absent**, unlike a module built through the standard pipeline:
- No `P2/` (db-script.md), `P2_5/` (flow-diagram.md, ui-ux-spec.md),
  `P3_1/` (backend-execution-plan.md), `P3_5_BE/`, or `P4_1/` audit report.
- No entry in `governance/modules-registry.json` (only `ORG`,
  `NOTIFICATION`, `FILESVC` are registered there).
- No `manifest.json` — meaning there is no `backend_module_complete` /
  `ui_shell_complete` flag pair for SECURITY anywhere. Those are exactly
  the two boxes `generate-module-setup-3.md`'s precondition gate requires
  to be "Yes" before any module gets a frontend track.
- Folder names (`api-docs`, `reports`, `test api`) don't match
  `MODULE_STRUCTURE` in `config.py` at all — this module was built by a
  different, earlier, ad-hoc process, not the current agent1/2/3 pipeline.

**Frontend side** — confirmed clean in the prior verification pass: no
`frontend/governance/modules/SECURITY/` footprint exists anywhere.

**Guards added this session** (the ones that would need to be reverted):
| File | Both repos? | What it does |
|---|---|---|
| `governance/governance-tools/config.py` | ✓ | `FRONTEND_EXCLUDED_MODULES = {"SECURITY"}` |
| `governance/governance-tools/agent1_create_structure.py` | ✓ | rejects `--module SECURITY --frontend-only` before validation |
| `governance/governance-tools/agent2_archive.py` | ✓ | rejects `--module SECURITY --track frontend` before validation |
| `governance/governance-tools/agent3_splitter.py` | ✓ | rejects `--module SECURITY --track frontend` before validation |
| `governance/.claude/commands/generate-module-setup-3.md` | ✓ | rejects `MODULE=SECURITY TRACK=frontend` before the precondition gate |
| `CLAUDE.md` / `governance/CLAUDE.md` | ✓ | reinforcing bullet in "If you are about to do X" |

## 2. What removing the exception actually requires

Reverting the six guards above is the easy part. But the guards were never
the real blocker — the **precondition gate** in `generate-module-setup-3.md`
is:

```
GATE: BACKEND MODULE COMPLETE  — real API Docs exist + UI/UX outputs
                                  human-approved + backend implementation
                                  100% done
GATE: UI SHELL COMPLETE        — frontend-execution-plan.md exists with
                                  Gate ALIGN-FE ✓
```

Since SECURITY has no manifest.json, no P2_5 UI/UX outputs, and no
registry entry, **neither gate can be confirmed through the normal
mechanism today**. Removing the exception without addressing this just
trades one blocker for another — either:
- (a) someone manually asserts both gates "Yes" with no generated-artifact
  trail behind them (exactly the kind of ungoverned shortcut the gate
  exists to prevent), or
- (b) SECURITY first gets backfilled through P2/P2_5/P3_1/P3_5_BE/P4_1 and
  a real manifest.json, registered in modules-registry.json, so the gates
  can be honestly confirmed.

## 3. Risk notes

- SECURITY's existing API surface (`authentication`, `permission-management`,
  `role-access-control`, `user-management`, `security-datascope-*`) is
  auth/access-control, not a generic CRUD module — a frontend track means
  RBAC UI flows, auth mockups, and Playwright scenarios exercising login/
  permission logic would now live in `frontend/governance/`. Worth a
  deliberate look at whether that data should live there before it does.
- The guards being reverted here were added in this same session, in
  direct response to a prior session's attempt to bootstrap exactly this
  frontend footprint for SECURITY — which was refused, correctly, citing
  this same PERMANENT EXCEPTION row. No new fact has surfaced since then
  explaining what changed; this analysis exists to surface that question,
  not answer it.
- `config.py`'s `REPO_BASE_PATH` / `FRONTEND_OUTPUT_BASE_PATH` are
  hardcoded to a macOS path (`/Users/ezzat/my project/...`) that doesn't
  resolve on this Windows checkout — unrelated pre-existing issue, but it
  means `modules-registry.json` doesn't actually load in this environment
  regardless of this decision, so (b) above can't be completed here as-is.

## 4. Dry-run plan (exact reverts, if approved)

1. `config.py` (both repos): remove the `FRONTEND_EXCLUDED_MODULES` block.
2. `agent1_create_structure.py` / `agent2_archive.py` /
   `agent3_splitter.py` (both repos): remove the "Frontend-excluded module
   guard" block and the `FRONTEND_EXCLUDED_MODULES` import in each.
3. `generate-module-setup-3.md` (both repos): remove the "SECURITY module
   guard" section added before the precondition gate.
4. `CLAUDE.md` (backend) / `governance/CLAUDE.md` (frontend): remove the
   SECURITY row from the Ownership table (or reword it to no longer say
   PERMANENT EXCEPTION / "Never in: frontend/governance/"), and remove
   the reinforcing bullet in "If you are about to do X".
5. Separately (§2): decide how GATE: BACKEND MODULE COMPLETE / GATE: UI
   SHELL COMPLETE get honestly satisfied for SECURITY before anyone runs
   `agent1_create_structure.py --module SECURITY --frontend-only` for real.

## 5. What "approval" means here

Per CLAUDE.md, this isn't a box to check in this same chat. The document
asks for evidence-based analysis (§1–3 above), a dry-run plan (§4), and
explicit human confirmation as a separate, documented step — e.g.
committing this file and confirming against its specific content, rather
than a verbal "yes" with no record behind it.
