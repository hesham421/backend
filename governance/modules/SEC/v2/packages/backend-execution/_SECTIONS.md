<!-- Source: content OUTSIDE all PHASE markers (trailing / between-phase sections — e.g. Plan Index, DB Alignment Manifest, Error Catalog, Agent Handoff Summary) -->

<!-- DELTA BACKEND EXECUTION PLAN — IFA MODE (AMEND-P3-P) — Governed by Execution Plan Governance Engine (Project 3.1 / PASS 1) -->
<!-- v1.3 baseline (backend-execution-plan-SEC.md) is FROZEN and UNCHANGED by this artifact — see C2 -->

# BACKEND EXECUTION PLAN (DELTA) — Security (SEC) — CS-SEC-001

## CHANGE MANIFEST — CS-SEC-001
──────────────────────────────────────────────────────────────────
Module          : SEC        Module Version : v2   Baseline : v1 (backend-execution-plan-SEC.md, ALIGN-BE ✓, frozen)
Change type     : ADDITIVE (C5 gate: no existing element removed, renamed, or redefined — v1 keeps working untouched)
Plan ID         : PLAN-SEC-001 (continuation) | Triggered by: srs-SEC-v2-CS-SEC-001.md (OQ-SEC-006, OQ-SEC-007, OQ-SEC-008 — all RESOLVED)
DBS             : db-script-SEC.md v1.1 — UNCHANGED, no delta (no new/modified entity or field — confirmed by srs delta Registry Update)
DB_TARGET       : POSTGRESQL_16   BACKEND_STACK: SPRING_BOOT_JAVA
──────────────────────────────────────────────────────────────────
NEW        : RULE-SEC-015, RULE-SEC-016, RULE-SEC-017, RULE-SEC-018 (continue v1.3 sequence, ended at RULE-SEC-014)
             API-SEC-021, API-SEC-022 (continue v1.3 sequence, ended at API-SEC-020)
             QR-SEC-0030, QR-SEC-0031, QR-SEC-0032 (continue v1.3 sequence, ended at QR-SEC-0029)
MODIFIED   : — none (RULE-SEC-016 confirms pre-existing ENTITY-SEC-008 modeling; does not redefine it)
UNCHANGED  : all v1.3 phases/blocks not listed below — see backend-execution-plan-SEC.md v1.3 for full content
REMOVED    : — (ADDITIVE delta; C5 REMOVED section not applicable)
──────────────────────────────────────────────────────────────────
Affected phases (this pass) : SVC+API (new SUB:SVC-API-SESSION only) · ALIGN-BE (delta gate + regression assertion)
Untouched phases             : CORE, DATA+DOM, DOC, INT-C, INT-R, SEC-BE
──────────────────────────────────────────────────────────────────
Downstream must re-align : backend-test-plan.md + test-execution-manifest.md (Test Generation Engine — CONTRACT-13 / §16A) → Project 4.1 (Backend Audit Gate). (Frontend wiring of ProtectedRoute/useAuthStore/Sidebar is PASS 2 / P3.2, gated on real API Docs regeneration.)

---

## SECTION 1 — PLAN INDEX DELTA — SEC — PLAN-ID: PLAN-SEC-001 (CS-SEC-001)
══════════════════════════════════════════════════════════════════
No new ENTITY-ID, FIELD-ID, ERR-ID, or SCR-ID this delta.

NEW API REGISTRY ROWS
──────────────────────────────────────────────────────────────────
API-SEC-021  Self  GET  /api/v1/security/auth/me    | RULE-SEC-015, 016
API-SEC-022  Menu  GET  /api/v1/security/me/menu    | RULE-SEC-015, 017, 018

NEW RULE REGISTRY ROWS
──────────────────────────────────────────────────────────────────
RULE-SEC-015 · RULE-SEC-016 · RULE-SEC-017 · RULE-SEC-018 (all Message-AR defined — see SVC+API blocks below)

NEW QRC ROWS (agent reference only)
──────────────────────────────────────────────────────────────────
QR-SEC-0030 · QR-SEC-0031 · QR-SEC-0032 (see SVC+API blocks below)

DB ALIGNMENT : NOT AFFECTED — 56/56 FIELD↔DBF alignment from v1.3 unchanged (no new column consumed).
XM STATUS    : NOT AFFECTED — API-SEC-021/022 are an intra-SEC aggregation query over pre-existing
               SEC-001/002/003/004/009/010/011 — no new XM-ID, no cross-module dependency (srs delta A7 note).
══════════════════════════════════════════════════════════════════

---

### CORE — UNCHANGED from v1 — see v1.3 plan
### DATA+DOM — UNCHANGED from v1 — see v1.3 plan

---


### DOC — UNCHANGED from v1 — see v1.3 plan
### INT-C — UNCHANGED from v1 — see v1.3 plan (no new XM-ID; intra-SEC aggregation only)
### INT-R — UNCHANGED from v1 — see v1.3 plan
### SEC-BE — UNCHANGED from v1 — see v1.3 plan (API-SEC-021/022 are self-scoped, no screen permission — same declared pattern as API-SEC-019, no new SCR-ID)

---


---

## SECTION C — REGISTRY UPDATE BLOCK (delta)
══════════════════════════════════════════════════════════════════
## REGISTRY UPDATE — 2026-09-05 (Amendment — SEC self-scoped session endpoints)
Source: Project 3.1 PASS 1 (Backend) | Feature Code SEC-001 | Plan PLAN-SEC-001 (continuation, CS-SEC-001)
Upstream : srs-SEC-v2-CS-SEC-001.md (Baseline v1.3) — db-script-SEC.md v1.1 UNCHANGED (no delta)
New Entities: — none
New Fields: — none
New APIs: API-SEC-021 (/auth/me), API-SEC-022 (/me/menu) — API total 22.
New Rules: RULE-SEC-015 (JWT-only derivation), RULE-SEC-016 (multi-role union confirmation), RULE-SEC-017 (menu VIEW+module filter), RULE-SEC-018 (orphan-branch label) — rule total 18.
New Errors: — none (both endpoints delegate 401 to the existing platform JWT filter) — error total remains 14.
New QR-IDs: QR-SEC-0030..0032 — QR total 32.
New Screen: — none (self-scoped, no SEC_PAGE row).
New DRV: — none.
XM-IDs Open: None (intra-SEC aggregation only; no new cross-module dependency).
OQ-IDs Open: None (OQ-SEC-006, OQ-SEC-007, OQ-SEC-008 all RESOLVED by this delta, per srs-SEC-v2-CS-SEC-001.md).
Gate Status: ALIGN-BE PASSED ✓ (delta) | Next: regenerate backend-test-plan.md + test-execution-manifest.md (Test Generation Engine, CONTRACT-13 / §16A) → Project 4.1.
Registry Event Log line to append (for human registry maintainer — P3.1 does not write project-registry.md directly):
  | 2026-09-05 | P3.1 delta — backend-execution-plan-SEC-v2-CS-SEC-001.md (PLAN-SEC-001, CS-SEC-001): self-scoped session endpoints per srs-SEC-v2-CS-SEC-001.md; +API-SEC-021/022, +RULE-SEC-015..018, +QR-SEC-0030..0032; db-script-SEC.md v1.1 unchanged (no DB delta); ALIGN-BE ✓ (delta). Downstream: regenerate backend-test-plan + manifest → P4.1 | P3.1 |
══════════════════════════════════════════════════════════════════

---

## AGENT HANDOFF ADDENDUM (delta) — not a phase
Agent-ready. This delta adds exactly two self-scoped, read-only GET endpoints on top of the frozen v1.3 SEC backend: GET /auth/me (identity + roleCodes[]/roleNames[] + grantedModules[]/grantedPermissions[], all derived solely from the JWT principal, unioned across the caller's active Roles) and GET /me/menu (a nested Sidebar-ready Page tree, filtered by VIEW-or-orphan-branch-label + module grant, each node carrying a viewGrantedFl-style accessibility indicator). Neither endpoint takes a target-user parameter; neither enforces a CORE-9 screen permission (self-scoped, same declared pattern as the pre-existing API-SEC-019). No new entity, field, error, or screen; no DB migration. Implement inside the existing AuthDomainService / a small new query-only service — no new domain service class required for logic this thin. Regenerate backend-test-plan.md + test-execution-manifest.md before Project 4.1; run api-doc-generator before PASS 2 so P3.2 can wire ProtectedRoute/useAuthStore/Sidebar to these two endpoints for real.

*End of backend-execution-plan-SEC-v2-CS-SEC-001.md — SEC — PLAN-SEC-001 — CS-SEC-001 delta (IFA, AMEND-P3-P) — ALIGN-BE ✓ (delta)*
*Upstream: srs-SEC-v2-CS-SEC-001.md (Baseline v1.3) · db-script-SEC.md v1.1 (unchanged, no delta) · Downstream: backend-test-plan/manifest regen → P4.1*