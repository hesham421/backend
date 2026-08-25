-- ============================================================================
-- src/main/resources/db/scripts/005_notification_service_account_seed.sql
-- ============================================================================
-- OBSOLETE as of the interface-injection migration (see
-- governance/project-artifacts/INTERFACE-VS-REST-AND-POM-STRUCTURE-RECOMMENDATION.md):
-- NotificationClient.java (the sole consumer of this account) has been deleted —
-- AuthEventListener now calls com.erp.notification.crossmodule.NotificationDispatchApi
-- directly, in-process, which needs no principal at all. This script is kept, not deleted,
-- because it may already have been run against a real database — a DBA can drop the
-- 'svc-notification' USERS row once confirmed unused elsewhere. Left below for reference/audit
-- trail only; do not run this script in any environment that doesn't already have it applied.
-- ============================================================================
--
-- Original purpose — XM-SEC-005 — service-to-service credential for erp-security ->
-- erp-notification cross-module calls (NotificationClient.java, called from
-- AuthEventListener's AFTER_COMMIT listener reacting to anonymous flows — self-registration,
-- forgot-password — which have no caller JWT to forward).
--
-- Seeds a dedicated USERS row ('svc-notification') that NotificationClient
-- resolves via UserAccountRepository and mints a real JWT for via
-- JwtService.generateAccess(...) — the exact same code path AuthService uses
-- for real logins. No SecurityConfig/JwtAuthenticationFilter changes needed:
-- /api/v1/notifications/send only requires @PreAuthorize("isAuthenticated()"),
-- no specific permission, so this account is intentionally assigned ZERO
-- roles (no USER_ROLES row) — it can authenticate but holds no authorities
-- beyond that, same minimal-privilege shape as any other service account.
--
-- PASSWORD is a BCrypt hash of a random, never-recorded value — this account
-- is never expected to authenticate via username/password login (only via
-- JwtService.generateAccess() called directly from NotificationClient), so
-- the password is intentionally unusable/unknown. It exists only to satisfy
-- USERS.PASSWORD NOT NULL.
--
-- Run manually by DBA (psql / pgAdmin), AFTER 001_security_schema_migration_
-- and_seed.sql. Safe to re-run (ON CONFLICT (USERNAME) DO NOTHING, same
-- idempotency convention as the 'admin' seed in 001).
-- ============================================================================

BEGIN;

INSERT INTO USERS (USERNAME, PASSWORD, ENABLED, CREATED_AT, CREATED_BY)
VALUES (
    'svc-notification',
    '$2a$10$Omd3Z2kic30U6npVRJAqCeePNzj3OtOPTLoWmZdqd3zlDcY1ujmWq', -- unusable: random password, never recorded
    1,
    now(),
    'SYSTEM'
)
ON CONFLICT (USERNAME) DO NOTHING;

COMMIT;

-- ============================================================================
-- VERIFICATION (read-only, run after COMMIT)
-- ============================================================================
-- SELECT users_pk, username, enabled FROM users WHERE username = 'svc-notification';
-- SELECT count(*) FROM user_roles ur JOIN users u ON u.users_pk = ur.user_id_fk
--   WHERE u.username = 'svc-notification';  -- expect 0 — intentionally roleless
