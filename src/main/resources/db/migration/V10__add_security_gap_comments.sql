-- ============================================================================
-- Reconciliation Migration — DATABASE_RECONCILIATION_REPORT.md Section 6
-- Source: governance/modules/SECURITY/gaps/db-script-SEC-gaps.md BLOCK 4
--         (COMMENTS) — the four DataScope/self-service-auth extension
--         tables only (SEC_USER_PROFILE, SEC_ROLE_BRANCH,
--         PASSWORD_RESET_TOKEN, ACCOUNT_ACTIVATION_TOKEN). The 7 pre-existing
--         AS-IS Security tables (USERS, ROLES, PERMISSIONS, SEC_PAGES,
--         REFRESH_TOKENS, USER_ROLES, ROLE_PERMISSIONS) are PERMANENT
--         EXCEPTION / out of scope for this document and are not touched.
--
-- FINDING (category GOVERNANCE_METADATA_ONLY): the gap doc defines 13
-- COMMENT ON TABLE/COLUMN statements (a documented subset of columns, not
-- all of them — e.g. no comment is specified for CREATED_BY/CREATED_AT/
-- UPDATED_BY/UPDATED_AT/IS_ACTIVE_FL on any of the 4 tables). V1 applied the
-- schema for these tables but not this metadata. This migration adds only
-- what governance specifies, verbatim.
-- ============================================================================

BEGIN;

COMMENT ON TABLE sec_user_profile IS 'User profile / branch assignment for DataScope — ENTITY-SEC-009';
COMMENT ON COLUMN sec_user_profile.user_id_fk IS 'PK and FK to USERS.USERS_PK — shared 1:1 primary key';
COMMENT ON COLUMN sec_user_profile.branch_id_fk IS 'FK to ORG_BRANCH.BRANCH_PK — XM-SEC-001';
COMMENT ON COLUMN sec_user_profile.preferred_lang IS 'Inferred VARCHAR(10) default pending OQ-004 resolution';
COMMENT ON COLUMN sec_user_profile.employee_id_fk IS 'Unconstrained — target HR module not yet governed, see OQ-005';

COMMENT ON TABLE sec_role_branch IS 'Role branch scope (DataScope) — ENTITY-SEC-010';
COMMENT ON COLUMN sec_role_branch.role_id_fk IS 'FK to ROLES.ROLES_PK — composite PK part 1';
COMMENT ON COLUMN sec_role_branch.branch_id_fk IS 'FK to ORG_BRANCH.BRANCH_PK — XM-SEC-002 — composite PK part 2';
COMMENT ON COLUMN sec_role_branch.data_access_level IS 'LOV-SEC-002 — MD_LOOKUP_DETAIL lookupKey DATA_ACCESS_LEVEL';

COMMENT ON TABLE password_reset_token IS 'Single-use password reset token — ENTITY-SEC-011';
COMMENT ON COLUMN password_reset_token.user_id_fk IS 'FK to USERS.USERS_PK';

COMMENT ON TABLE account_activation_token IS 'Single-use self-registration activation token — ENTITY-SEC-012';
COMMENT ON COLUMN account_activation_token.user_id_fk IS 'FK to USERS.USERS_PK';

COMMIT;
