-- ============================================================================
-- V18: Grant SUPER_ADMIN role (and admin user) all system permissions.
--
-- Ensures the bootstrap SUPER_ADMIN role has 100% of all registered permissions
-- across all modules (SECURITY, ORGANIZATION, FINANCE, MASTERDATA, FILE, NOTIFICATION, etc.).
-- ============================================================================

-- 1. Ensure user 'admin' is assigned to SUPER_ADMIN role
INSERT INTO user_roles (user_id_fk, role_id_fk)
SELECT u.users_pk, r.roles_pk
FROM users u
CROSS JOIN roles r
WHERE u.username = 'admin'
  AND (r.role_code = 'SUPER_ADMIN' OR r.name = 'SUPER_ADMIN')
ON CONFLICT DO NOTHING;

-- 2. Grant all existing permissions to SUPER_ADMIN role
INSERT INTO role_permissions (role_id_fk, perm_id_fk)
SELECT r.roles_pk, p.permissions_pk
FROM roles r
CROSS JOIN permissions p
WHERE (r.role_code = 'SUPER_ADMIN' OR r.name = 'SUPER_ADMIN')
ON CONFLICT DO NOTHING;
