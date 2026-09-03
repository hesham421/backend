<!-- Source: PHASE:TEST-PLAN-BE / SUB:API-SCENARIOS -->

 <!-- TC:TC-BE-SEC-022:START -->
TC-BE-SEC-022 — API-SEC-001 Login happy | VALID → 200 tokens.
 <!-- TC:TC-BE-SEC-022:END -->
 <!-- TC:TC-BE-SEC-023:START -->
TC-BE-SEC-023 — API-SEC-002 Refresh happy | VALID → 200 rotated tokens.
 <!-- TC:TC-BE-SEC-023:END -->
 <!-- TC:TC-BE-SEC-024:START -->
TC-BE-SEC-024 — API-SEC-003 Logout happy | VALID → 204; refresh revoked.
 <!-- TC:TC-BE-SEC-024:END -->
 <!-- TC:TC-BE-SEC-025:START -->
TC-BE-SEC-025 — API-SEC-004 Forgot-password happy | VALID → 202 neutral; reset token created; CU event published.
 <!-- TC:TC-BE-SEC-025:END -->
 <!-- TC:TC-BE-SEC-026:START -->
TC-BE-SEC-026 — API-SEC-005 Reset-password happy | VALID → 200; new hash set.
 <!-- TC:TC-BE-SEC-026:END -->
 <!-- TC:TC-BE-SEC-027:START -->
TC-BE-SEC-027 — API-SEC-006 Activate happy | VALID → 200; status ACTIVE.
 <!-- TC:TC-BE-SEC-027:END -->
 <!-- TC:TC-BE-SEC-028:START -->
TC-BE-SEC-028 — API-SEC-007 Create user happy | VALID → 201; activation token issued.
 <!-- TC:TC-BE-SEC-028:END -->
 <!-- TC:TC-BE-SEC-029:START -->
TC-BE-SEC-029 — API-SEC-008 Search users happy + empty→200 (MANDATORY-J-7) | VALID/EDGE → 200 Page; empty filter → 200 [].
 <!-- TC:TC-BE-SEC-029:END -->
 <!-- TC:TC-BE-SEC-030:START -->
TC-BE-SEC-030 — API-SEC-009 Update user happy | VALID → 200; username unchanged.
 <!-- TC:TC-BE-SEC-030:END -->
 <!-- TC:TC-BE-SEC-031:START -->
TC-BE-SEC-031 — API-SEC-010 Deactivate user happy | VALID → 200/204; isActiveFl=0.
 <!-- TC:TC-BE-SEC-031:END -->
 <!-- TC:TC-BE-SEC-032:START -->
TC-BE-SEC-032 — API-SEC-011 Roles CRUD happy | VALID → create/read/update/search all 2xx.
 <!-- TC:TC-BE-SEC-032:END -->
 <!-- TC:TC-BE-SEC-033:START -->
TC-BE-SEC-033 — API-SEC-012 Assign role happy | VALID → 200; SEC_USER_ROLE row present.
 <!-- TC:TC-BE-SEC-033:END -->
 <!-- TC:TC-BE-SEC-034:START -->
TC-BE-SEC-034 — API-SEC-013 Pages CRUD happy | VALID → 201 + 4 permissions generated.
 <!-- TC:TC-BE-SEC-034:END -->
 <!-- TC:TC-BE-SEC-035:START -->
TC-BE-SEC-035 — API-SEC-014 List permissions happy | VALID → 200 Page.
 <!-- TC:TC-BE-SEC-035:END -->
 <!-- TC:TC-BE-SEC-036:START -->
TC-BE-SEC-036 — API-SEC-015 Grant/revoke permission happy | VALID → 200; SEC_ROLE_PERMISSION row present/removed.
 <!-- TC:TC-BE-SEC-036:END -->
 <!-- TC:TC-BE-SEC-037:START -->
TC-BE-SEC-037 — API-SEC-016 Lookups happy + permission enforcement (MANDATORY-J-5) + SQLi (MANDATORY-J-8)
 VALID → GET /security/lookups/SEC_USER_STATUS → 200 [{code,labelAr,labelEn}].
 ATTACK → user without VIEW on SCR-SEC-001 hitting API-SEC-008 → 403; username="x' OR '1'='1" stored literal.
 <!-- TC:TC-BE-SEC-037:END -->
 <!-- TC:TC-BE-SEC-042:START -->
TC-BE-SEC-042 — API-SEC-017 Assign module to role happy | VALID → 200; SEC_ROLE_MODULE row created; idempotent on repeat.
 <!-- TC:TC-BE-SEC-042:END -->
 <!-- TC:TC-BE-SEC-043:START -->
TC-BE-SEC-043 — API-SEC-018 Revoke module from role happy (no dependents) | VALID → 200/204; SEC_ROLE_MODULE row removed.
 <!-- TC:TC-BE-SEC-043:END -->
 <!-- TC:TC-BE-SEC-044:START -->
TC-BE-SEC-044 — API-SEC-019 Dashboard modules happy + empty→200 (MANDATORY-J-7) | VALID/EDGE → 200 [ModuleResponse]; user with no module grants → 200 [].
 <!-- TC:TC-BE-SEC-044:END -->
 <!-- TC:TC-BE-SEC-045:START -->
TC-BE-SEC-045 — API-SEC-020 Modules CRUD happy | VALID → create/read/update/search all 2xx; duplicate moduleCode → 409 ERR-0009 (RULE-SEC-010, already exercised at TC-BE-SEC-019).
 <!-- TC:TC-BE-SEC-045:END -->
