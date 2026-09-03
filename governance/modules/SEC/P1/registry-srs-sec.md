# REGISTRY EXTRACT — registry-srs-SEC
══════════════════════════════════════════════════════════════════
Module          : Security (SEC)
Source artifact : srs-SEC.md (v1.3)
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4.1/P4.2 audit
══════════════════════════════════════════════════════════════════

## HEADER
Module name : Security (السيكيوريتي)
Module Prefix : SEC
OQ count : 1 (OQ-SEC-001 — RESOLVED)

## ENTITIES (PART A — A3)
| ENTITY-ID | Entity Name | Type |
|---|---|---|
| ENTITY-SEC-001 | UserAccount | SHARED |
| ENTITY-SEC-002 | Role | PRIVATE |
| ENTITY-SEC-003 | Permission | PRIVATE |
| ENTITY-SEC-004 | Page (Screen Registry) | PRIVATE |
| ENTITY-SEC-005 | RefreshToken | PRIVATE (internal) |
| ENTITY-SEC-006 | PasswordResetToken | PRIVATE (internal) |
| ENTITY-SEC-007 | AccountActivationToken | PRIVATE (internal) |
| ENTITY-SEC-008 | UserRole (join) | INTERNAL/JOIN |
| ENTITY-SEC-009 | RolePermission (join) | INTERNAL/JOIN |
| ENTITY-SEC-010 | Module (Registry) | PRIVATE (Reference/Config) |
| ENTITY-SEC-011 | RoleModule (join) | INTERNAL/JOIN |

## RULES (PART A — A4)
| RULE-ID | Short Title | Test-Hint |
|---|---|---|
| RULE-SEC-001 | Unique username | — |
| RULE-SEC-002 | Required core account fields | — |
| RULE-SEC-003 | Password complexity enforced | — |
| RULE-SEC-004 | Store password hashed only | — |
| RULE-SEC-005 | Lock after failed logins | — |
| RULE-SEC-006 | Rotate refresh token | — |
| RULE-SEC-007 | Single active reset token | — |
| RULE-SEC-008 | Single active activation token | — |
| RULE-SEC-009 | Block login on non-active account | — |
| RULE-SEC-010 | Unique role/permission/page/module codes | — |
| RULE-SEC-011 | Auto-generate 4 permissions per page | — |
| RULE-SEC-012 | Deactivation allowed, no cascade | — |
| RULE-SEC-013 | Module grant = dashboard filter + prerequisite | — |
| RULE-SEC-014 | No orphan screen permission | — |

## LOVs (PART A — A5)
| LOV-ID | LOV Name |
|---|---|
| LOV-SEC-001 | preferredLang (SEC_PREFERRED_LANG) |
| LOV-SEC-002 | userStatus (SEC_USER_STATUS) |

## LIFECYCLE STATES (PART A — A6)
UserAccount: PENDING_ACTIVATION → ACTIVE ⇄ INACTIVE (RULE-SEC-012 permits reactivation)

## DEPENDENCIES (PART A — A7)
| Type | Target ENTITY-ID | Target Module | XM candidate |
|---|---|---|---|
| — | (none) | — | — |
Note: SEC consumes no other module's entity (SEC is the identity owner). SEC USES the CU library
(code dependency, not a HARD-FK/SOFT-READ/SHARED-CONSUME governed dependency type).

## SCREENS (PART B)
| SCR-ID | page_code | Screen Name | Pattern |
|---|---|---|---|
| SCR-SEC-001 | SEC_USERS | User Management | PATTERN-2 (SIDE_DRAWER) |
| SCR-SEC-002 | SEC_ROLES | Roles · Modules · Permissions | PATTERN-2 (SIDE_DRAWER) |
| SCR-SEC-003 | SEC_PAGE_REGISTRY | Page/Screen Registry | PATTERN-2 (SIDE_DRAWER) |
| SCR-SEC-004 | SEC_MODULES | Module Registry | PATTERN-2 (SIDE_DRAWER) |
Public (pre-auth, no SEC_PAGE row): Login, Forgot/Reset Password, Activate.

## APIs (PART B — B5)
| API-ID | Method | Endpoint | Owning SCR-ID |
|---|---|---|---|
| API-SEC-001 | POST | /api/v1/security/auth/login | — (public) |
| API-SEC-002 | POST | /api/v1/security/auth/refresh | — (public) |
| API-SEC-003 | POST | /api/v1/security/auth/logout | — (public) |
| API-SEC-004 | POST | /api/v1/security/auth/forgot-password | — (public) |
| API-SEC-005 | POST | /api/v1/security/auth/reset-password | — (public) |
| API-SEC-006 | POST | /api/v1/security/auth/activate | — (public) |
| API-SEC-007 | POST | /api/v1/security/users | SCR-SEC-001 |
| API-SEC-008 | GET | /api/v1/security/users | SCR-SEC-001 |
| API-SEC-009 | PUT | /api/v1/security/users/{id} | SCR-SEC-001 |
| API-SEC-010 | DELETE | /api/v1/security/users/{id} | SCR-SEC-001 |
| API-SEC-011 | POST/GET/PUT/DELETE | /api/v1/security/roles | SCR-SEC-002 |
| API-SEC-012 | POST | /api/v1/security/users/{id}/roles | SCR-SEC-001 |
| API-SEC-013 | POST/GET/PUT/DELETE | /api/v1/security/pages | SCR-SEC-003 |
| API-SEC-014 | GET | /api/v1/security/permissions | SCR-SEC-002 |
| API-SEC-015 | POST/DELETE | /api/v1/security/roles/{id}/permissions | SCR-SEC-002 |
| API-SEC-016 | GET | /api/v1/security/lookups/{lookupKey} | (cross-screen) |
| API-SEC-017 | POST | /api/v1/security/roles/{id}/modules | SCR-SEC-002 |
| API-SEC-018 | DELETE | /api/v1/security/roles/{id}/modules/{moduleId} | SCR-SEC-002 |
| API-SEC-019 | GET | /api/v1/security/me/modules | — (self-scoped) |
| API-SEC-020 | POST/GET/PUT/DELETE | /api/v1/security/modules | SCR-SEC-004 |

## PERMISSIONS (Permissions Summary)
| PERM Name | Linked SCR-ID(s) |
|---|---|
| PERM_SEC_USERS_{VIEW,CREATE,UPDATE,DELETE} | SCR-SEC-001 |
| PERM_SEC_ROLES_{VIEW,CREATE,UPDATE,DELETE} | SCR-SEC-002 |
| PERM_SEC_PAGE_REGISTRY_{VIEW,CREATE,UPDATE,DELETE} | SCR-SEC-003 |
| PERM_SEC_MODULES_{VIEW,CREATE,UPDATE,DELETE} | SCR-SEC-004 |
All granted to SYS_ADMIN per srs-SEC Permissions Summary.

## OQ LOG STATUS
| OQ-ID | Status | One-line topic | Escalation |
|---|---|---|---|
| OQ-SEC-001 | RESOLVED | Deactivation impact of SHARED UserAccount on SOFT consumers | XM-ESC-SEC |

---
*End of registry-srs-SEC.md*
