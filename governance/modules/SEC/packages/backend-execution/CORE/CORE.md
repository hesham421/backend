<!-- Source: PHASE:CORE -->

## PHASE CORE — Architectural Policies
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓ (re-passed v1.3)

CANONICAL ARCHITECTURE (backend layers): controller/ service/ mapper/ domain/ repository/ entity/ dto/ exception/ config/
Domain behavior placement: separate classes in domain/ (auth flows, token rotation, permission auto-generation, **Tier-1 module grants + Tier-2 derivation enforcement (RULE-SEC-013/014)** are non-trivial domain logic warranting dedicated domain services — e.g. AuthDomainService, PermissionGenerationDomainService, **AuthorizationGrantDomainService**).

PROJECT-STANDARD CONSTRAINTS:
  Entity base       : AuditableEntity on all **8** base tables (audit via AuditEntityListener) — SEC_USER_ACCOUNT, SEC_ROLE, **SEC_MODULE (v1.3)**, SEC_PERMISSION, SEC_PAGE, SEC_REFRESH_TOKEN, SEC_PASSWORD_RESET_TOKEN, SEC_ACCOUNT_ACTIVATION_TOKEN.
  Exception (declared): SEC_REFRESH_TOKEN / SEC_PASSWORD_RESET_TOKEN /
    SEC_ACCOUNT_ACTIVATION_TOKEN are session artifacts with their own lifecycle
    (expiresAt/usedFl/revokedFl) — still extend AuditableEntity here since db-script
    defines the four audit columns on them; treat as standard audited entities.
  Join tables SEC_USER_ROLE / SEC_ROLE_PERMISSION / **SEC_ROLE_MODULE (v1.3)**: composite PK, NO audit, NO surrogate id.
  ✗ orgUnitId never in any DTO. TenantAuditableEntity retired (no multi-tenancy).
  Error signaling   : LocalizedException — NotFoundException BANNED.
  Error catalog     : every ERR-ID registered 4× (ErrorCodes + messages.properties + i18n JSON + ErpErrorMapperService).
  Search contract   : SearchRequest extends BaseSearchContractRequest; ALLOWED_SORT_FIELDS per search.
  Deactivation      : isActiveFl=false (record preserved). RULE-SEC-012: no cascade to SOFT consumers.
  Security          : passwords & all tokens stored hashed only (RULE-SEC-004, DRV-005). JWT access + rotating refresh.
  Authorization (v1.3): **two-tier RBAC** — Tier-1 Role→Module (SEC_ROLE_MODULE) drives dashboard visibility + is a prerequisite (RULE-SEC-013); Tier-2 Role→Screen (SEC_ROLE_PERMISSION) is the real enforcement via CORE-9 PERM_<PAGE>_<TYPE>. Derivation (RULE-SEC-014, no orphan screen permission) enforced in the service layer (db-script v1.1: not a declarative DB constraint). No separate module-level runtime gate.
  Internal SSO (v1.3): SEC is the single platform auth authority; one internal JWT valid across all modules; auth-only (identity), **separate from the two-tier authorization**. No new entity/table (confirmation of existing design; srs-SEC A7). No external federation now.
  i18n / events     : CU library — SEC publishes CU ApplicationEvents (reset/activation requested); NOTIF listens. SEC never calls NOTIF directly (srs-SEC A7).

TYPE MAPPING (POSTGRESQL_16): BIGINT→Long · VARCHAR(N)→String · SMALLINT(_FL)→Boolean · TIMESTAMP→LocalDateTime.

MODULE-SPECIFIC NOTES:
  - CORE-9 ownership: SEC owns SEC_PAGE (screen registry) and SEC_PERMISSION. Permissions are auto-generated
    4-per-page (PERM_<PAGE_CODE>_<VIEW|CREATE|UPDATE|DELETE>) — RULE-SEC-011.
  - **(v1.3) Every SEC_PAGE belongs to a module (moduleFk NOT NULL → SEC_MODULE).** Granting a page's permissions to a role is valid only if the role is granted that page's module (RULE-SEC-014).
  - LOV values are runtime codes (no ENUM, no lookup table); label resolution via API-SEC-016 / CU i18n. **Module is a reference table, not a LOV.**
  - permissionType uses DB CHECK (VIEW/CREATE/UPDATE/DELETE) — fixed convention, not a runtime LOV.
  - No Workflow Engine (RULE-13 = OFF). Temporary account lock (lockedUntil) is NOT a lifecycle status.
─────────────────────────────────────────────────────────────────
