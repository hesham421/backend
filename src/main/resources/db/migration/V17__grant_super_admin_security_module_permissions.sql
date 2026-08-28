-- ============================================================================
-- V17: Grant SUPER_ADMIN the SECURITY-module permissions it never received.
--
-- Root cause: same stale-grant pattern as V16 (ORGANIZATION module).
-- V1__inital_schema.sql's SUPER_ADMIN (role pk 5) role_permissions rows are a
-- point-in-time snapshot; nothing re-syncs a role's grants when a new
-- permission/page is created afterward. SUPER_ADMIN is currently missing 118
-- SECURITY-module permissions, including PERM_USER_VIEW -- causing
-- GET /api/users and POST /api/users/search to return 403 FORBIDDEN even for
-- the bootstrap admin account.
--
-- Additive/idempotent (ON CONFLICT DO NOTHING), scoped to SECURITY only.
-- ============================================================================

INSERT INTO role_permissions (role_id_fk, perm_id_fk)
SELECT r.roles_pk, p.permissions_pk
FROM roles r
CROSS JOIN permissions p
JOIN sec_pages sp ON sp.sec_pages_pk = p.page_id_fk
WHERE r.name = 'SUPER_ADMIN'
  AND sp.module = 'SECURITY'
ON CONFLICT DO NOTHING;
