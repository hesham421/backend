# test-execution-manifest.md — Security (SEC) — PLAN-ID: PLAN-SEC-001
══════════════════════════════════════════════════════════════════
Derived view (CONTRACT-13) — no new RULE/ERR/TC. Consumed by Project 5.
Source: backend-execution-plan-SEC.md (v1.3, ALIGN-BE ✓) + backend-test-plan-SEC.md (v2, regenerated) | DB_TARGET: POSTGRESQL_16
Regenerated 2026-09-03 — prior version archived to [BACKUP]/SEC__test-execution-manifest-SEC.md__2026-09-03.md
══════════════════════════════════════════════════════════════════

SECTION: DEPENDENCY ORDER (topological — intra-module FKs)
 1. SEC_USER_ACCOUNT (root identity; no parent)
 2. SEC_ROLE (independent)
 3. SEC_MODULE (independent) ⟵ v1.3 — must precede SEC_PAGE
 4. SEC_PAGE (self-FK parentPage; FK moduleFk → SEC_MODULE NOT NULL ⟵ v1.3; seed roots first)
 5. SEC_PERMISSION (FK → SEC_PAGE) ← generated after its page (RULE-SEC-011)
 6. SEC_ROLE_MODULE (FK → SEC_ROLE, SEC_MODULE) ⟵ v1.3 — Tier-1 grant; seed BEFORE Tier-2 grants (RULE-SEC-014)
 7. SEC_USER_ROLE (FK → SEC_USER_ACCOUNT, SEC_ROLE)
 8. SEC_ROLE_PERMISSION (FK → SEC_ROLE, SEC_PERMISSION) — Tier-2; requires the role's SEC_ROLE_MODULE row to exist first (RULE-SEC-014)
 9. SEC_REFRESH_TOKEN / SEC_PASSWORD_RESET_TOKEN / SEC_ACCOUNT_ACTIVATION_TOKEN (FK → SEC_USER_ACCOUNT)
 (No cross-module dependency — CU is a library; UserAccount consumed SOFT-READ by FILE/NOTIF on their side.)

SECTION: RULE→ERR→TC TRIPLES
 RULE-ID │ ERR-ID │ TC-BE-ID │ HTTP
 ─────────────┼──────────┼────────────────┼──────
 RULE-SEC-001 │ ERR-0001 │ TC-BE-SEC-002 │ 409
 RULE-SEC-001 │ ERR-0010 │ TC-BE-SEC-002 │ 409 (email variant)
 RULE-SEC-002 │ ERR-0002 │ TC-BE-SEC-004 │ 400
 RULE-SEC-003 │ ERR-0003 │ TC-BE-SEC-006 │ 422
 RULE-SEC-005 │ ERR-0004 │ TC-BE-SEC-009 │ 423
 RULE-SEC-006 │ ERR-0005 │ TC-BE-SEC-011 │ 401
 RULE-SEC-007 │ ERR-0006 │ TC-BE-SEC-013 │ 400
 RULE-SEC-008 │ ERR-0007 │ TC-BE-SEC-015 │ 400
 RULE-SEC-009 │ ERR-0008 │ TC-BE-SEC-017 │ 403
 RULE-SEC-010 │ ERR-0009 │ TC-BE-SEC-019 │ 409
 RULE-SEC-014 │ ERR-0013 │ TC-BE-SEC-040 │ 422 (grant without module) ⟵ v1.3
 RULE-SEC-014 │ ERR-0014 │ TC-BE-SEC-041 │ 409 (revoke with dependents) ⟵ v1.3
 (PLATFORM) │ ERR-0011 │ TC-BE-SEC-037 │ 401
 (PLATFORM) │ ERR-0012 │ TC-BE-SEC-037 │ 404
 (RULE-SEC-004/011/012 — internal/permissive, no ERR: TC-BE-SEC-007/020/021 assert behavior)
 (RULE-SEC-013 — display filter, no ERR: TC-BE-SEC-038 asserts behavior) ⟵ v1.3

SECTION: ENTITY CRUD CHECKLIST
 Entity │ Create │ Search │ Update │ Activate │ Deactivate │ GetById │ Delete
 ───────────────────────────┼────────┼────────┼────────┼──────────┼────────────┼─────────┼───────
 UserAccount │ ✓ │ ✓ │ ✓ │ ✓ │ ✓ │ ✓ │ —
 Role │ ✓ │ ✓ │ ✓ │ (flag) │ (flag) │ ✓ │ —
 Page │ ✓ │ ✓ │ ✓ │ (flag) │ (flag) │ ✓ │ —
 Permission │ auto │ ✓ │ — │ — │ — │ ✓ │ —
 Module ⟵ v1.3 │ ✓ │ ✓ │ ✓ │ (flag) │ (flag) │ ✓ │ —
 UserRole (join) │ assign│ — │ — │ — │ remove │ — │ —
 RolePermission (join) │ grant │ — │ — │ — │ revoke │ — │ —
 RoleModule (join) ⟵ v1.3 │ grant │ — │ — │ — │ revoke (blocked if dependents, ERR-0014) │ — │ —
 Refresh/Reset/Activation │ issue │ — │ rotate/consume │ — │ revoke │ — │ —
══════════════════════════════════════════════════════════════════
Regeneration rule: regenerate this manifest if either source plan is amended, before handing to Project 5.
══════════════════════════════════════════════════════════════════

*End of test-execution-manifest.md — SEC — PLAN-SEC-001 (v2 — regenerated for v1.3 two-tier RBAC/SSO amendment)*
