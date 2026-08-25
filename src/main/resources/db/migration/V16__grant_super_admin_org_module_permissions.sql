-- ============================================================================
-- V16: Grant SUPER_ADMIN the ORGANIZATION-module permissions it never
-- received.
--
-- Root cause: V1__inital_schema.sql's SUPER_ADMIN (role pk 5) role_permissions
-- rows are a point-in-time snapshot taken before most ORGANIZATION module
-- pages/permissions existed (created later at runtime via POST /api/pages,
-- per their created_at timestamps). Nothing re-syncs a role's grants when a
-- new permission is created afterward, so SUPER_ADMIN was left with only
-- PERM_BRANCH_VIEW out of the ORGANIZATION module's 28 permissions -- the
-- bootstrap admin account could not create/update/delete/view Legal
-- Entities, Regions, Departments, Cost Centers, Profit Centers, or Location
-- Sites, and could only VIEW (not manage) Branches.
--
-- Additive/idempotent (ON CONFLICT DO NOTHING), scoped to ORGANIZATION only.
-- SECURITY (90 missing) and FINANCE (4 missing) show the same stale-grant
-- pattern and are intentionally left untouched here -- see the accompanying
-- fix report's follow-up note.
-- ============================================================================

INSERT INTO role_permissions (role_id_fk, perm_id_fk)
SELECT r.roles_pk, p.permissions_pk
FROM roles r
CROSS JOIN permissions p
JOIN sec_pages sp ON sp.sec_pages_pk = p.page_id_fk
WHERE r.name = 'SUPER_ADMIN'
  AND sp.module = 'ORGANIZATION'
ON CONFLICT DO NOTHING;
