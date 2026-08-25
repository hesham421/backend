-- ============================================================================
-- Security Module — DataScope extension + Self-Service Auth gap package
-- PLAN-SEC-002 / DBS-SEC-001 — Phase DATA+DOM (schema only, no seed users/roles)
-- Run manually by DBA (psql / pgAdmin). Not applied automatically — no Flyway
-- migration runner is wired up for erp-security. Safe to re-run (idempotent).
--
-- Source: governance-repo/modules/SECURITY/gaps/db-script-SEC-gaps.md
--         (FULL_DATABASE_SCRIPT, BLOCK 1-8). Reproduces that DDL verbatim in
--         terms of tables/columns/constraints/indexes/seed values, with one
--         deviation flagged in HANDOFF-PHASE-2-DATA-DOM.md: the source
--         document's Oracle-dialect `SEQ.NEXTVAL` seed syntax is translated
--         to Postgres `nextval('SEQ')`, matching 001_security_schema_
--         migration_and_seed.sql's existing SEC_PAGES_SEQ usage — this repo's
--         confirmed live DB_TARGET is Postgres (HANDOFF-PHASE-1-CORE.md),
--         not Oracle. No column/table/constraint name was changed.
--
-- ⚠ GOVERNANCE: Conflict #19 (master-registry.md Section 13) sign-off was
-- confirmed by the human operator in the Phase 1 (CORE) session — see
-- HANDOFF-PHASE-1-CORE.md. UK_USERS_EMAIL (BLOCK 5b) is included below on
-- that basis.
--
-- Prerequisites: USERS, ROLES, ORG_BRANCH, MD_MASTER_LOOKUP, MD_LOOKUP_DETAIL
-- must already exist in the target schema (all pre-existing / AS-IS).
-- ============================================================================

BEGIN;

-- ============================================================================
-- BLOCK 1: SEQUENCES
-- ============================================================================
CREATE SEQUENCE IF NOT EXISTS PASSWORD_RESET_TOKEN_SEQ
  START WITH 1
  INCREMENT BY 1
  NO CYCLE;

CREATE SEQUENCE IF NOT EXISTS ACCOUNT_ACTIVATION_TOKEN_SEQ
  START WITH 1
  INCREMENT BY 1
  NO CYCLE;

-- ============================================================================
-- BLOCK 2: PARENT TABLES
-- (none created in this delta — USERS, ROLES, ORG_BRANCH already exist)
-- ============================================================================

-- ============================================================================
-- BLOCK 3: CHILD TABLES
-- ============================================================================

-- ENTITY-SEC-009 — user profile / branch assignment (1:1 with USERS)
CREATE TABLE IF NOT EXISTS SEC_USER_PROFILE (
  USER_ID_FK        BIGINT          NOT NULL,
  BRANCH_ID_FK      BIGINT          NOT NULL,
  FULL_NAME_AR      VARCHAR(200),
  FULL_NAME_EN      VARCHAR(100),
  PREFERRED_LANG    VARCHAR(10),
  EMPLOYEE_ID_FK    BIGINT,
  IS_ACTIVE_FL      SMALLINT        DEFAULT 1 NOT NULL,
  CREATED_BY        VARCHAR(255)    NOT NULL,
  CREATED_AT        TIMESTAMP       NOT NULL,
  UPDATED_BY        VARCHAR(255),
  UPDATED_AT        TIMESTAMP
);

-- ENTITY-SEC-010 — role branch scope (DataScope)
CREATE TABLE IF NOT EXISTS SEC_ROLE_BRANCH (
  ROLE_ID_FK         BIGINT          NOT NULL,
  BRANCH_ID_FK       BIGINT          NOT NULL,
  DATA_ACCESS_LEVEL  VARCHAR(30)     NOT NULL,
  IS_ACTIVE_FL       SMALLINT        DEFAULT 1 NOT NULL,
  CREATED_BY         VARCHAR(255)    NOT NULL,
  CREATED_AT         TIMESTAMP       NOT NULL,
  UPDATED_BY         VARCHAR(255),
  UPDATED_AT         TIMESTAMP
);

-- ENTITY-SEC-011 — password reset token
CREATE TABLE IF NOT EXISTS PASSWORD_RESET_TOKEN (
  TOKEN_PK      BIGINT        NOT NULL,
  TOKEN         VARCHAR(64)   NOT NULL,
  USER_ID_FK    BIGINT        NOT NULL,
  CREATED_AT    TIMESTAMP     NOT NULL,
  EXPIRES_AT    TIMESTAMP     NOT NULL,
  USED_FL       SMALLINT      DEFAULT 0 NOT NULL
);

-- ENTITY-SEC-012 — account activation token
CREATE TABLE IF NOT EXISTS ACCOUNT_ACTIVATION_TOKEN (
  TOKEN_PK      BIGINT        NOT NULL,
  TOKEN         VARCHAR(64)   NOT NULL,
  USER_ID_FK    BIGINT        NOT NULL,
  CREATED_AT    TIMESTAMP     NOT NULL,
  EXPIRES_AT    TIMESTAMP     NOT NULL,
  USED_FL       SMALLINT      DEFAULT 0 NOT NULL
);

-- ============================================================================
-- BLOCK 4: COMMENTS
-- ============================================================================
COMMENT ON TABLE SEC_USER_PROFILE IS 'User profile / branch assignment for DataScope — ENTITY-SEC-009';
COMMENT ON COLUMN SEC_USER_PROFILE.USER_ID_FK IS 'PK and FK to USERS.USERS_PK — shared 1:1 primary key';
COMMENT ON COLUMN SEC_USER_PROFILE.BRANCH_ID_FK IS 'FK to ORG_BRANCH.BRANCH_PK — XM-SEC-001';
COMMENT ON COLUMN SEC_USER_PROFILE.PREFERRED_LANG IS 'Inferred VARCHAR(10) default pending OQ-004 resolution';
COMMENT ON COLUMN SEC_USER_PROFILE.EMPLOYEE_ID_FK IS 'Unconstrained — target HR module not yet governed, see OQ-005';

COMMENT ON TABLE SEC_ROLE_BRANCH IS 'Role branch scope (DataScope) — ENTITY-SEC-010';
COMMENT ON COLUMN SEC_ROLE_BRANCH.ROLE_ID_FK IS 'FK to ROLES.ROLES_PK — composite PK part 1';
COMMENT ON COLUMN SEC_ROLE_BRANCH.BRANCH_ID_FK IS 'FK to ORG_BRANCH.BRANCH_PK — XM-SEC-002 — composite PK part 2';
COMMENT ON COLUMN SEC_ROLE_BRANCH.DATA_ACCESS_LEVEL IS 'LOV-SEC-002 — MD_LOOKUP_DETAIL lookupKey DATA_ACCESS_LEVEL';

COMMENT ON TABLE PASSWORD_RESET_TOKEN IS 'Single-use password reset token — ENTITY-SEC-011';
COMMENT ON COLUMN PASSWORD_RESET_TOKEN.USER_ID_FK IS 'FK to USERS.USERS_PK';

COMMENT ON TABLE ACCOUNT_ACTIVATION_TOKEN IS 'Single-use self-registration activation token — ENTITY-SEC-012';
COMMENT ON COLUMN ACCOUNT_ACTIVATION_TOKEN.USER_ID_FK IS 'FK to USERS.USERS_PK';

-- ============================================================================
-- BLOCK 5: CONSTRAINTS
-- ============================================================================

-- 5a: Primary Keys
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'PK_SEC_USER_PROFILE') THEN
        ALTER TABLE SEC_USER_PROFILE ADD CONSTRAINT PK_SEC_USER_PROFILE PRIMARY KEY (USER_ID_FK);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'PK_SEC_ROLE_BRANCH') THEN
        ALTER TABLE SEC_ROLE_BRANCH ADD CONSTRAINT PK_SEC_ROLE_BRANCH PRIMARY KEY (ROLE_ID_FK, BRANCH_ID_FK);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'PK_PASSWORD_RESET_TOKEN') THEN
        ALTER TABLE PASSWORD_RESET_TOKEN ADD CONSTRAINT PK_PASSWORD_RESET_TOKEN PRIMARY KEY (TOKEN_PK);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'PK_ACCOUNT_ACTIVATION_TOKEN') THEN
        ALTER TABLE ACCOUNT_ACTIVATION_TOKEN ADD CONSTRAINT PK_ACCOUNT_ACTIVATION_TOKEN PRIMARY KEY (TOKEN_PK);
    END IF;
END $$;

-- 5b: Unique Constraints
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'UK_PASSWORD_RESET_TOKEN_TOKEN') THEN
        ALTER TABLE PASSWORD_RESET_TOKEN ADD CONSTRAINT UK_PASSWORD_RESET_TOKEN_TOKEN UNIQUE (TOKEN);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'UK_ACCOUNT_ACTIVATION_TOKEN_TOKEN') THEN
        ALTER TABLE ACCOUNT_ACTIVATION_TOKEN ADD CONSTRAINT UK_ACCOUNT_ACTIVATION_TOKEN_TOKEN UNIQUE (TOKEN);
    END IF;

    -- ⚠ GOVERNANCE EXCEPTION — ALTER on pre-existing AS-IS table (USERS).
    -- Conflict #19 sign-off confirmed in Phase 1 (HANDOFF-PHASE-1-CORE.md) — RULE-SEC-041 / AQ-008.
    --
    -- DEVIATION from db-script-SEC-gaps.md — flagged in HANDOFF-PHASE-2-DATA-DOM.md,
    -- confirmed with the operator before applying: FIELD-SEC-0032 (Section 1.4,
    -- String(150)) and BLOCK 5b both assume USERS.EMAIL already exists — the live
    -- schema has no such column, and the governed script never ADDs it, only
    -- ALTERs a unique constraint onto it. The ADD COLUMN below (nullable — the
    -- existing bootstrap admin row has no email value to backfill) is added here
    -- so BLOCK 5b's constraint is satisfiable; not a new business column beyond
    -- what FIELD-SEC-0032 already specifies.
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'email') THEN
        ALTER TABLE USERS ADD COLUMN EMAIL VARCHAR(150);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'UK_USERS_EMAIL') THEN
        ALTER TABLE USERS ADD CONSTRAINT UK_USERS_EMAIL UNIQUE (EMAIL);
    END IF;
END $$;

-- 5c: Check Constraints
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'CHK_PASSWORD_RESET_TOKEN_USED_FL') THEN
        ALTER TABLE PASSWORD_RESET_TOKEN ADD CONSTRAINT CHK_PASSWORD_RESET_TOKEN_USED_FL CHECK (USED_FL IN (0,1));
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'CHK_ACCOUNT_ACTIVATION_TOKEN_USED_FL') THEN
        ALTER TABLE ACCOUNT_ACTIVATION_TOKEN ADD CONSTRAINT CHK_ACCOUNT_ACTIVATION_TOKEN_USED_FL CHECK (USED_FL IN (0,1));
    END IF;
END $$;

-- 5d: Foreign Keys — Intra-module (to pre-existing Security tables)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'FK_SEC_USER_PROFILE_USER') THEN
        ALTER TABLE SEC_USER_PROFILE ADD CONSTRAINT FK_SEC_USER_PROFILE_USER
          FOREIGN KEY (USER_ID_FK) REFERENCES USERS (USERS_PK);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'FK_SEC_ROLE_BRANCH_ROLE') THEN
        ALTER TABLE SEC_ROLE_BRANCH ADD CONSTRAINT FK_SEC_ROLE_BRANCH_ROLE
          FOREIGN KEY (ROLE_ID_FK) REFERENCES ROLES (ROLES_PK);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'FK_PASSWORD_RESET_TOKEN_USER') THEN
        ALTER TABLE PASSWORD_RESET_TOKEN ADD CONSTRAINT FK_PASSWORD_RESET_TOKEN_USER
          FOREIGN KEY (USER_ID_FK) REFERENCES USERS (USERS_PK);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'FK_ACCOUNT_ACTIVATION_TOKEN_USER') THEN
        ALTER TABLE ACCOUNT_ACTIVATION_TOKEN ADD CONSTRAINT FK_ACCOUNT_ACTIVATION_TOKEN_USER
          FOREIGN KEY (USER_ID_FK) REFERENCES USERS (USERS_PK);
    END IF;
END $$;

-- 5e: Foreign Keys — Cross-module (XM-SEC-001, XM-SEC-002 — both READY, target GOVERNED)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'FK_SEC_USER_PROFILE_BRANCH') THEN
        ALTER TABLE SEC_USER_PROFILE ADD CONSTRAINT FK_SEC_USER_PROFILE_BRANCH
          FOREIGN KEY (BRANCH_ID_FK) REFERENCES ORG_BRANCH (BRANCH_PK);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'FK_SEC_ROLE_BRANCH_BRANCH') THEN
        ALTER TABLE SEC_ROLE_BRANCH ADD CONSTRAINT FK_SEC_ROLE_BRANCH_BRANCH
          FOREIGN KEY (BRANCH_ID_FK) REFERENCES ORG_BRANCH (BRANCH_PK);
    END IF;
END $$;

-- ============================================================================
-- BLOCK 6: TRIGGERS
-- (none — no auto-PK triggers; audit fields populated by AuditEntityListener at
--  the application layer, consistent with existing Security tables. No
--  SRS-mandated audit trigger.)
-- ============================================================================

-- ============================================================================
-- BLOCK 7: INDEXES
-- ============================================================================
CREATE INDEX IF NOT EXISTS IDX_SEC_USER_PROFILE_BRANCH ON SEC_USER_PROFILE (BRANCH_ID_FK);
CREATE INDEX IF NOT EXISTS IDX_SEC_USER_PROFILE_EMPLOYEE ON SEC_USER_PROFILE (EMPLOYEE_ID_FK);

CREATE INDEX IF NOT EXISTS IDX_SEC_ROLE_BRANCH_BRANCH ON SEC_ROLE_BRANCH (BRANCH_ID_FK);

CREATE INDEX IF NOT EXISTS IDX_PASSWORD_RESET_TOKEN_USER ON PASSWORD_RESET_TOKEN (USER_ID_FK);
CREATE INDEX IF NOT EXISTS IDX_PASSWORD_RESET_TOKEN_EXPIRES ON PASSWORD_RESET_TOKEN (EXPIRES_AT);

CREATE INDEX IF NOT EXISTS IDX_ACCT_ACTIVATION_TOKEN_USER ON ACCOUNT_ACTIVATION_TOKEN (USER_ID_FK);
CREATE INDEX IF NOT EXISTS IDX_ACCT_ACTIVATION_TOKEN_EXPIRES ON ACCOUNT_ACTIVATION_TOKEN (EXPIRES_AT);

-- Note: UK_USERS_EMAIL above already provides an index on USERS.EMAIL — no
-- separate CREATE INDEX needed (Postgres auto-indexes UNIQUE constraints).

-- ============================================================================
-- BLOCK 8: LOOKUP SEED DATA
-- (MD_MASTER_LOOKUP / MD_LOOKUP_DETAIL are shared system tables, never
--  recreated here — seed INSERTs only. Source document used Oracle-dialect
--  `SEQ.NEXTVAL`; translated to Postgres `nextval('SEQ')` here — see header.
--  DEVIATION: db-script-SEC-gaps.md's INSERT lists omit CREATED_AT/CREATED_BY,
--  which are NOT NULL on both tables (AuditableEntity) — added here, following
--  001_security_schema_migration_and_seed.sql's existing SYSTEM-seed convention,
--  so the seed is actually insertable; no LOV-SEC-002 business column changed.)
-- ============================================================================
INSERT INTO MD_MASTER_LOOKUP
  (ID_PK, LOOKUP_KEY, LOOKUP_NAME, LOOKUP_NAME_EN, IS_ACTIVE, CREATED_AT, CREATED_BY)
VALUES
  (nextval('MD_MASTER_LOOKUP_SEQ'), 'DATA_ACCESS_LEVEL', 'مستوى نطاق البيانات', 'Data Access Level', 1, TIMESTAMP '2026-07-09 00:00:00', 'SYSTEM')
ON CONFLICT (LOOKUP_KEY) DO NOTHING;

INSERT INTO MD_LOOKUP_DETAIL
  (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_AT, CREATED_BY)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), ml.ID_PK, v.code, v.name_ar, v.name_en, v.sort_order, 1, TIMESTAMP '2026-07-09 00:00:00', 'SYSTEM'
FROM (VALUES
    ('BRANCH_ONLY', 'الفرع فقط', 'Branch Only', 1),
    ('BRANCH_AND_CHILDREN', 'الفرع وفروعه التابعة', 'Branch and Children', 2),
    ('ALL', 'كل الفروع', 'All', 3)
) AS v(code, name_ar, name_en, sort_order)
JOIN MD_MASTER_LOOKUP ml ON ml.LOOKUP_KEY = 'DATA_ACCESS_LEVEL'
ON CONFLICT (MASTER_LOOKUP_ID_FK, CODE) DO NOTHING;

COMMIT;

-- ============================================================================
-- BLOCK 9: VIEWS
-- (none required by SRS for this delta)
-- ============================================================================

-- ============================================================================
-- BLOCK 10: FUNCTIONS AND PROCEDURES
-- (none required by SRS for this delta)
-- ============================================================================

-- ============================================================================
-- BLOCK 11: DEFERRED FK PATCH BLOCKS
-- (No DEFERRED HARD-FK — both XM-SEC-001 and XM-SEC-002 are READY and applied
--  live in BLOCK 5e above. EMPLOYEE_ID_FK is intentionally left unconstrained —
--  no XM-ID exists for an ungoverned HR module. Uncomment and assign an XM-ID
--  once HR is governed — see OQ-005.)
-- ============================================================================
-- -- FUTURE — pending HR module governance (OQ-005, no XM-ID yet)
-- -- ALTER TABLE SEC_USER_PROFILE
-- --   ADD CONSTRAINT FK_SEC_USER_PROFILE_EMPLOYEE
-- --     FOREIGN KEY (EMPLOYEE_ID_FK) REFERENCES HR_EMPLOYEE (EMPLOYEE_PK);

-- ============================================================================
-- VERIFICATION (read-only, run after COMMIT)
-- ============================================================================
SELECT 'sec_user_profile' AS table_name, COUNT(*) AS row_count FROM SEC_USER_PROFILE
UNION ALL
SELECT 'sec_role_branch', COUNT(*) FROM SEC_ROLE_BRANCH
UNION ALL
SELECT 'password_reset_token', COUNT(*) FROM PASSWORD_RESET_TOKEN
UNION ALL
SELECT 'account_activation_token', COUNT(*) FROM ACCOUNT_ACTIVATION_TOKEN
UNION ALL
SELECT 'data_access_level_lookup_values', COUNT(*) FROM MD_LOOKUP_DETAIL ld
  JOIN MD_MASTER_LOOKUP ml ON ml.ID_PK = ld.MASTER_LOOKUP_ID_FK
  WHERE ml.LOOKUP_KEY = 'DATA_ACCESS_LEVEL';
