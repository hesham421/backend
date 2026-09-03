<!-- DB Script — Governed by Database Governance Engine (Project 2 / MODE 1.5) -->

# DB SCRIPT — Security (SEC)

## 1. DB SCRIPT HEADER
```
DBS-ID          : DBS-SEC-001
Module          : Security (SEC)
Script Version  : 1.1 — CONTINUATION (updated from v1.0)
SRS Feature Code: SEC-001  (srs-SEC.md v1.3 — two-tier RBAC + internal SSO)
Direct Upstream : srs-SEC.md v1.3  (prd-SEC v2 / domain-profile-ERP.md v2)
Platform        : Foundation (Domain: ERP)
DB_TARGET       : POSTGRESQL_16   (Architect-confirmed 2026-09-02; recorded in master-registry event log)
Date            : 2026-09-02
Status          : GATE PASSED (re-passed after v1.1 amendment)
Open Questions  : None
Tables          : 11 (8 base + 3 join)      [v1.0 = 9; +SEC_MODULE, +SEC_ROLE_MODULE]
XM Dependencies : None outbound. SEC OWNS the SHARED entity UserAccount; Tier-1 is intra-SEC (no new XM).
Lookup Tables   : None — LOVs runtime-loaded codes per SRS A5. Module is a REFERENCE entity, not a LOV.
```

> **UPSTREAM CHANGE — SEC two-tier RBAC + internal SSO (domain-profile-ERP.md v2)**
> - **Triggered by:** srs-SEC.md **v1.3** (ENTITY-SEC-010 Module, ENTITY-SEC-011 RoleModule, `moduleFk` on ENTITY-SEC-004 Page, RULE-SEC-013/014, API-SEC-017..020).
> - **Amended here (db-script v1.0 -> v1.1):** +table `SEC_MODULE`; +join table `SEC_ROLE_MODULE`; +column `SEC_PAGE.MODULE_FK` (FK->SEC_MODULE, NOT NULL); +sequence `SEQ_SEC_MODULE`; +PK/UNIQUE/CHECK/FK constraints; +indexes. New DBF-IDs **DBF-0049..DBF-0056**. **All prior IDs (DBF-0001..0048, tables, constraints) preserved verbatim.**
> - **Downstream must re-align:** **P2.5** (ui-ux-spec-SEC / flow-diagram-SEC) then **P3.1** (backend-execution-plan-SEC).

> **Governed design notes (SRS-governs-DB, Layer 1 > Layer 2):**
> - **Tier-1 modeling** follows srs v1.3 **DRV**: an explicit `SEC_MODULE` reference table + `SEC_ROLE_MODULE` join + `SEC_PAGE.MODULE_FK` — *not* the SEC_PAGE-hierarchy alternative. Invented nothing beyond srs v1.3.
> - **RULE-SEC-013** (Tier-1 grant = dashboard DISPLAY FILTER + prerequisite): realized structurally by the `SEC_ROLE_MODULE` grant table; the dashboard set is read via API-SEC-019. Per SRS there is **no separate module-level runtime gate**, so no extra DB gate is added.
> - **RULE-SEC-014** (no orphan screen permission): a cross-table conditional invariant — a `SEC_ROLE_PERMISSION` row is valid only if the role also holds the `SEC_ROLE_MODULE` grant for the module of that permission's page (`SEC_PERMISSION -> SEC_PAGE.MODULE_FK`). **Not expressible as a declarative PostgreSQL constraint** without a trigger/assertion; consistent with medium-complexity / no-over-engineering / no-workflow governance, it is **enforced at the application/service layer** (P3.1) and surfaced in the UI picker (P2.5). The DB supplies the structural backbone: `SEC_PAGE.MODULE_FK NOT NULL` + `SEC_ROLE_MODULE`.
> - **Internal SSO** (US-SEC-011): confirmation of the existing single-JWT authority design — **no new table/column** (auth-only, separate from the two authorization tiers).
> - Join tables (`SEC_USER_ROLE`, `SEC_ROLE_PERMISSION`, `SEC_ROLE_MODULE`) use composite PKs with no surrogate ID and no audit columns. Standard audit columns on base tables carry no DBF-IDs.

## 2. DB FIELD TRACEABILITY MATRIX — Security — DBS-ID: DBS-SEC-001
```
DBF-ID    | Table Name                    | Column Name         | DB Type       | SRS Source
DBF-0001  | SEC_USER_ACCOUNT              | ID                  | BIGINT        | ENTITY-SEC-001.userAccountPk
DBF-0002  | SEC_USER_ACCOUNT              | USERNAME            | VARCHAR(100)  | ENTITY-SEC-001.username
DBF-0003  | SEC_USER_ACCOUNT              | PASSWORD_HASH       | VARCHAR(255)  | ENTITY-SEC-001.passwordHash
DBF-0004  | SEC_USER_ACCOUNT              | EMAIL               | VARCHAR(255)  | ENTITY-SEC-001.email
DBF-0005  | SEC_USER_ACCOUNT              | PHONE               | VARCHAR(30)   | ENTITY-SEC-001.phone
DBF-0006  | SEC_USER_ACCOUNT              | FULL_NAME           | VARCHAR(200)  | ENTITY-SEC-001.fullName
DBF-0007  | SEC_USER_ACCOUNT              | PREFERRED_LANG_ID   | VARCHAR(10)   | ENTITY-SEC-001.preferredLangId (LOV-SEC-001)
DBF-0008  | SEC_USER_ACCOUNT              | USER_STATUS_ID      | VARCHAR(50)   | ENTITY-SEC-001.userStatusId (LOV-SEC-002)
DBF-0009  | SEC_USER_ACCOUNT              | FAILED_LOGIN_COUNT  | SMALLINT      | ENTITY-SEC-001.failedLoginCount
DBF-0010  | SEC_USER_ACCOUNT              | LOCKED_UNTIL        | TIMESTAMP     | ENTITY-SEC-001.lockedUntil
DBF-0011  | SEC_USER_ACCOUNT              | IS_ACTIVE_FL        | SMALLINT      | ENTITY-SEC-001.isActiveFl
DBF-0012  | SEC_ROLE                      | ID                  | BIGINT        | ENTITY-SEC-002.rolePk
DBF-0013  | SEC_ROLE                      | ROLE_CODE           | VARCHAR(50)   | ENTITY-SEC-002.roleCode
DBF-0014  | SEC_ROLE                      | NAME_AR             | VARCHAR(200)  | ENTITY-SEC-002.nameAr
DBF-0015  | SEC_ROLE                      | NAME_EN             | VARCHAR(100)  | ENTITY-SEC-002.nameEn
DBF-0016  | SEC_ROLE                      | IS_ACTIVE_FL        | SMALLINT      | ENTITY-SEC-002.isActiveFl
DBF-0017  | SEC_PERMISSION               | ID                  | BIGINT        | ENTITY-SEC-003.permissionPk
DBF-0018  | SEC_PERMISSION               | PERMISSION_CODE     | VARCHAR(150)  | ENTITY-SEC-003.permissionCode
DBF-0019  | SEC_PERMISSION               | PERMISSION_TYPE     | VARCHAR(20)   | ENTITY-SEC-003.permissionType (CORE-9 convention)
DBF-0020  | SEC_PERMISSION               | NAME_AR             | VARCHAR(200)  | ENTITY-SEC-003.nameAr
DBF-0021  | SEC_PERMISSION               | NAME_EN             | VARCHAR(100)  | ENTITY-SEC-003.nameEn
DBF-0022  | SEC_PERMISSION               | IS_ACTIVE_FL        | SMALLINT      | ENTITY-SEC-003.isActiveFl
DBF-0023  | SEC_PERMISSION               | PAGE_FK             | BIGINT        | ENTITY-SEC-003.pageFk -> ENTITY-SEC-004
DBF-0024  | SEC_PAGE                     | ID                  | BIGINT        | ENTITY-SEC-004.pagePk
DBF-0025  | SEC_PAGE                     | PAGE_CODE           | VARCHAR(100)  | ENTITY-SEC-004.pageCode
DBF-0026  | SEC_PAGE                     | NAME_AR             | VARCHAR(200)  | ENTITY-SEC-004.nameAr
DBF-0027  | SEC_PAGE                     | NAME_EN             | VARCHAR(100)  | ENTITY-SEC-004.nameEn
DBF-0028  | SEC_PAGE                     | IS_ACTIVE_FL        | SMALLINT      | ENTITY-SEC-004.isActiveFl
DBF-0029  | SEC_PAGE                     | PARENT_PAGE_FK      | BIGINT        | ENTITY-SEC-004.parentPageFk -> ENTITY-SEC-004 (self)
DBF-0030  | SEC_REFRESH_TOKEN            | ID                  | BIGINT        | ENTITY-SEC-005.refreshTokenPk
DBF-0031  | SEC_REFRESH_TOKEN            | TOKEN               | VARCHAR(255)  | ENTITY-SEC-005.token
DBF-0032  | SEC_REFRESH_TOKEN            | EXPIRES_AT          | TIMESTAMP     | ENTITY-SEC-005.expiresAt
DBF-0033  | SEC_REFRESH_TOKEN            | REVOKED_FL          | SMALLINT      | ENTITY-SEC-005.revokedFl
DBF-0034  | SEC_REFRESH_TOKEN            | USER_ACCOUNT_FK     | BIGINT        | ENTITY-SEC-005.userAccountFk -> ENTITY-SEC-001
DBF-0035  | SEC_PASSWORD_RESET_TOKEN     | ID                  | BIGINT        | ENTITY-SEC-006.passwordResetTokenPk
DBF-0036  | SEC_PASSWORD_RESET_TOKEN     | TOKEN               | VARCHAR(255)  | ENTITY-SEC-006.token
DBF-0037  | SEC_PASSWORD_RESET_TOKEN     | EXPIRES_AT          | TIMESTAMP     | ENTITY-SEC-006.expiresAt
DBF-0038  | SEC_PASSWORD_RESET_TOKEN     | USED_FL             | SMALLINT      | ENTITY-SEC-006.usedFl
DBF-0039  | SEC_PASSWORD_RESET_TOKEN     | USER_ACCOUNT_FK     | BIGINT        | ENTITY-SEC-006.userAccountFk -> ENTITY-SEC-001
DBF-0040  | SEC_ACCOUNT_ACTIVATION_TOKEN | ID                  | BIGINT        | ENTITY-SEC-007.accountActivationTokenPk
DBF-0041  | SEC_ACCOUNT_ACTIVATION_TOKEN | TOKEN               | VARCHAR(255)  | ENTITY-SEC-007.token
DBF-0042  | SEC_ACCOUNT_ACTIVATION_TOKEN | EXPIRES_AT          | TIMESTAMP     | ENTITY-SEC-007.expiresAt
DBF-0043  | SEC_ACCOUNT_ACTIVATION_TOKEN | USED_FL             | SMALLINT      | ENTITY-SEC-007.usedFl
DBF-0044  | SEC_ACCOUNT_ACTIVATION_TOKEN | USER_ACCOUNT_FK     | BIGINT        | ENTITY-SEC-007.userAccountFk -> ENTITY-SEC-001
DBF-0045  | SEC_USER_ROLE                | USER_ACCOUNT_FK     | BIGINT        | ENTITY-SEC-008.userAccountFk -> ENTITY-SEC-001
DBF-0046  | SEC_USER_ROLE                | ROLE_FK             | BIGINT        | ENTITY-SEC-008.roleFk -> ENTITY-SEC-002
DBF-0047  | SEC_ROLE_PERMISSION          | ROLE_FK             | BIGINT        | ENTITY-SEC-009.roleFk -> ENTITY-SEC-002
DBF-0048  | SEC_ROLE_PERMISSION          | PERMISSION_FK       | BIGINT        | ENTITY-SEC-009.permissionFk -> ENTITY-SEC-003
---- v1.3 amendment (two-tier RBAC) — new IDs appended, none reordered ----
DBF-0049  | SEC_PAGE                     | MODULE_FK           | BIGINT        | ENTITY-SEC-004.moduleFk -> ENTITY-SEC-010 (added v1.3)
DBF-0050  | SEC_MODULE                   | ID                  | BIGINT        | ENTITY-SEC-010.modulePk
DBF-0051  | SEC_MODULE                   | MODULE_CODE         | VARCHAR(50)   | ENTITY-SEC-010.moduleCode
DBF-0052  | SEC_MODULE                   | NAME_AR             | VARCHAR(200)  | ENTITY-SEC-010.nameAr
DBF-0053  | SEC_MODULE                   | NAME_EN             | VARCHAR(100)  | ENTITY-SEC-010.nameEn
DBF-0054  | SEC_MODULE                   | IS_ACTIVE_FL        | SMALLINT      | ENTITY-SEC-010.isActiveFl
DBF-0055  | SEC_ROLE_MODULE              | ROLE_FK             | BIGINT        | ENTITY-SEC-011.roleFk -> ENTITY-SEC-002
DBF-0056  | SEC_ROLE_MODULE              | MODULE_FK           | BIGINT        | ENTITY-SEC-011.moduleFk -> ENTITY-SEC-010
Total: 56 DBF-IDs across 11 tables  (v1.0: 48/9 -> v1.1: +8 DBF-IDs, +2 tables, +1 column)
```

## 3. CROSS-MODULE DEPENDENCY REGISTER (XM REGISTER) — Security — DBS-ID: DBS-SEC-001
```
XM-ID   | Type | This Table | FK/Ref Column | Target Table | Target Module | Status
(none)  — SEC has no OUTBOUND cross-module dependencies. Tier-1 (Module/RoleModule) is INTRA-SEC.
```
> SEC owns the SHARED entity `UserAccount` (SEC_USER_ACCOUNT). Inbound SOFT-READ consumers register XM-FILE-001 / XM-NOTIF-001 in their own scripts. The v1.3 Tier-1 additions introduce **no** new cross-module dependency.

## 4. FULL_DATABASE_SCRIPT
```sql
-- ============================================================
-- FULL DATABASE SCRIPT — Security (SEC) — DBS-SEC-001 (v1.1)
-- Target: POSTGRESQL_16   |   Execute in psql / pgAdmin
-- v1.1 additions marked "-- [v1.3]" (two-tier RBAC: Module + RoleModule + Page.MODULE_FK)
-- ============================================================

-- ============================================================
-- BLOCK 1: SEQUENCES
-- ============================================================
CREATE SEQUENCE SEQ_SEC_USER_ACCOUNT             START WITH 1 INCREMENT BY 1 NO CACHE NO CYCLE;
CREATE SEQUENCE SEQ_SEC_ROLE                     START WITH 1 INCREMENT BY 1 NO CACHE NO CYCLE;
CREATE SEQUENCE SEQ_SEC_PERMISSION               START WITH 1 INCREMENT BY 1 NO CACHE NO CYCLE;
CREATE SEQUENCE SEQ_SEC_PAGE                     START WITH 1 INCREMENT BY 1 NO CACHE NO CYCLE;
CREATE SEQUENCE SEQ_SEC_REFRESH_TOKEN            START WITH 1 INCREMENT BY 1 NO CACHE NO CYCLE;
CREATE SEQUENCE SEQ_SEC_PASSWORD_RESET_TOKEN     START WITH 1 INCREMENT BY 1 NO CACHE NO CYCLE;
CREATE SEQUENCE SEQ_SEC_ACCOUNT_ACTIVATION_TOKEN START WITH 1 INCREMENT BY 1 NO CACHE NO CYCLE;
CREATE SEQUENCE SEQ_SEC_MODULE                   START WITH 1 INCREMENT BY 1 NO CACHE NO CYCLE;  -- [v1.3]

-- ============================================================
-- BLOCK 2: PARENT TABLES (no intra-module FK dependencies)
-- ============================================================
CREATE TABLE SEC_USER_ACCOUNT (
  ID                 BIGINT        NOT NULL,
  USERNAME           VARCHAR(100)  NOT NULL,
  PASSWORD_HASH      VARCHAR(255)  NOT NULL,
  EMAIL              VARCHAR(255)  NOT NULL,
  PHONE              VARCHAR(30),
  FULL_NAME          VARCHAR(200)  NOT NULL,
  PREFERRED_LANG_ID  VARCHAR(10)   NOT NULL,
  USER_STATUS_ID     VARCHAR(50)   NOT NULL,
  FAILED_LOGIN_COUNT SMALLINT      DEFAULT 0 NOT NULL,
  LOCKED_UNTIL       TIMESTAMP,
  IS_ACTIVE_FL       SMALLINT      DEFAULT 1 NOT NULL,
  CREATED_BY         VARCHAR(255),
  CREATED_AT         TIMESTAMP,
  UPDATED_BY         VARCHAR(255),
  UPDATED_AT         TIMESTAMP
);

CREATE TABLE SEC_ROLE (
  ID           BIGINT        NOT NULL,
  ROLE_CODE    VARCHAR(50)   NOT NULL,
  NAME_AR      VARCHAR(200)  NOT NULL,
  NAME_EN      VARCHAR(100)  NOT NULL,
  IS_ACTIVE_FL SMALLINT      DEFAULT 1 NOT NULL,
  CREATED_BY   VARCHAR(255),
  CREATED_AT   TIMESTAMP,
  UPDATED_BY   VARCHAR(255),
  UPDATED_AT   TIMESTAMP
);

-- [v1.3] Module registry (Tier-1 grantable unit + dashboard display unit)
CREATE TABLE SEC_MODULE (
  ID           BIGINT        NOT NULL,
  MODULE_CODE  VARCHAR(50)   NOT NULL,
  NAME_AR      VARCHAR(200)  NOT NULL,
  NAME_EN      VARCHAR(100)  NOT NULL,
  IS_ACTIVE_FL SMALLINT      DEFAULT 1 NOT NULL,
  CREATED_BY   VARCHAR(255),
  CREATED_AT   TIMESTAMP,
  UPDATED_BY   VARCHAR(255),
  UPDATED_AT   TIMESTAMP
);

CREATE TABLE SEC_PAGE (
  ID             BIGINT        NOT NULL,
  PAGE_CODE      VARCHAR(100)  NOT NULL,
  NAME_AR        VARCHAR(200)  NOT NULL,
  NAME_EN        VARCHAR(100)  NOT NULL,
  MODULE_FK      BIGINT        NOT NULL,   -- [v1.3] owning module -> SEC_MODULE (enables RULE-SEC-014 derivation)
  PARENT_PAGE_FK BIGINT,
  IS_ACTIVE_FL   SMALLINT      DEFAULT 1 NOT NULL,
  CREATED_BY     VARCHAR(255),
  CREATED_AT     TIMESTAMP,
  UPDATED_BY     VARCHAR(255),
  UPDATED_AT     TIMESTAMP
);

-- ============================================================
-- BLOCK 3: CHILD TABLES (intra-module FK dependencies)
-- ============================================================
CREATE TABLE SEC_PERMISSION (
  ID              BIGINT        NOT NULL,
  PERMISSION_CODE VARCHAR(150)  NOT NULL,
  PERMISSION_TYPE VARCHAR(20)   NOT NULL,
  NAME_AR         VARCHAR(200)  NOT NULL,
  NAME_EN         VARCHAR(100)  NOT NULL,
  IS_ACTIVE_FL    SMALLINT      DEFAULT 1 NOT NULL,
  PAGE_FK         BIGINT        NOT NULL,
  CREATED_BY      VARCHAR(255),
  CREATED_AT      TIMESTAMP,
  UPDATED_BY      VARCHAR(255),
  UPDATED_AT      TIMESTAMP
);

CREATE TABLE SEC_REFRESH_TOKEN (
  ID              BIGINT        NOT NULL,
  TOKEN           VARCHAR(255)  NOT NULL,
  EXPIRES_AT      TIMESTAMP     NOT NULL,
  REVOKED_FL      SMALLINT      DEFAULT 0 NOT NULL,
  USER_ACCOUNT_FK BIGINT        NOT NULL,
  CREATED_BY      VARCHAR(255),
  CREATED_AT      TIMESTAMP,
  UPDATED_BY      VARCHAR(255),
  UPDATED_AT      TIMESTAMP
);

CREATE TABLE SEC_PASSWORD_RESET_TOKEN (
  ID              BIGINT        NOT NULL,
  TOKEN           VARCHAR(255)  NOT NULL,
  EXPIRES_AT      TIMESTAMP     NOT NULL,
  USED_FL         SMALLINT      DEFAULT 0 NOT NULL,
  USER_ACCOUNT_FK BIGINT        NOT NULL,
  CREATED_BY      VARCHAR(255),
  CREATED_AT      TIMESTAMP,
  UPDATED_BY      VARCHAR(255),
  UPDATED_AT      TIMESTAMP
);

CREATE TABLE SEC_ACCOUNT_ACTIVATION_TOKEN (
  ID              BIGINT        NOT NULL,
  TOKEN           VARCHAR(255)  NOT NULL,
  EXPIRES_AT      TIMESTAMP     NOT NULL,
  USED_FL         SMALLINT      DEFAULT 0 NOT NULL,
  USER_ACCOUNT_FK BIGINT        NOT NULL,
  CREATED_BY      VARCHAR(255),
  CREATED_AT      TIMESTAMP,
  UPDATED_BY      VARCHAR(255),
  UPDATED_AT      TIMESTAMP
);

CREATE TABLE SEC_USER_ROLE (
  USER_ACCOUNT_FK BIGINT NOT NULL,
  ROLE_FK         BIGINT NOT NULL
);

CREATE TABLE SEC_ROLE_PERMISSION (
  ROLE_FK       BIGINT NOT NULL,
  PERMISSION_FK BIGINT NOT NULL
);

-- [v1.3] Tier-1 grant: role x module
CREATE TABLE SEC_ROLE_MODULE (
  ROLE_FK   BIGINT NOT NULL,
  MODULE_FK BIGINT NOT NULL
);

-- ============================================================
-- BLOCK 4: COMMENTS
-- ============================================================
COMMENT ON TABLE SEC_USER_ACCOUNT IS 'SHARED identity entity (ENTITY-SEC-001). Consumed SOFT-READ by FILE/NOTIF. Deactivation: no cascade, history retained (RULE-SEC-012).';
COMMENT ON COLUMN SEC_USER_ACCOUNT.PREFERRED_LANG_ID IS 'Language code (LOV-SEC-001); runtime-loaded, no lookup table.';
COMMENT ON COLUMN SEC_USER_ACCOUNT.USER_STATUS_ID IS 'Account lifecycle code (LOV-SEC-002): PENDING_ACTIVATION/ACTIVE/INACTIVE; runtime-loaded.';
COMMENT ON COLUMN SEC_USER_ACCOUNT.FAILED_LOGIN_COUNT IS 'Failed-login counter for temporary lock (RULE-SEC-005).';
COMMENT ON COLUMN SEC_USER_ACCOUNT.LOCKED_UNTIL IS 'Temporary lock expiry (not a lifecycle status).';
COMMENT ON COLUMN SEC_USER_ACCOUNT.IS_ACTIVE_FL IS 'Active flag: 1=active, 0=inactive.';
COMMENT ON TABLE SEC_ROLE IS 'RBAC role (ENTITY-SEC-002). Tier-1 modules granted via SEC_ROLE_MODULE; Tier-2 screen permissions via SEC_ROLE_PERMISSION.';
COMMENT ON TABLE SEC_MODULE IS '[v1.3] Module registry (ENTITY-SEC-010). Tier-1 grantable unit + dashboard display unit; owns pages via SEC_PAGE.MODULE_FK.';
COMMENT ON COLUMN SEC_MODULE.MODULE_CODE IS 'Unique module code (e.g. SEC/FILE/NOTIF).';
COMMENT ON COLUMN SEC_MODULE.IS_ACTIVE_FL IS 'Active flag: 1=active, 0=inactive.';
COMMENT ON TABLE SEC_PERMISSION IS 'Permission auto-generated per Page (ENTITY-SEC-003 / CORE-9). Tier-2 enforcement.';
COMMENT ON COLUMN SEC_PERMISSION.PERMISSION_TYPE IS 'CORE-9 code convention: VIEW/CREATE/UPDATE/DELETE (not a runtime LOV).';
COMMENT ON COLUMN SEC_PERMISSION.PAGE_FK IS 'FK to SEC_PAGE.';
COMMENT ON TABLE SEC_PAGE IS 'Screen/Page registry — CORE-9 owner (ENTITY-SEC-004). Each page belongs to a module (MODULE_FK).';
COMMENT ON COLUMN SEC_PAGE.MODULE_FK IS '[v1.3] Owning module (FK -> SEC_MODULE, NOT NULL). Basis of RULE-SEC-014 derivation (no orphan screen permission).';
COMMENT ON COLUMN SEC_PAGE.PARENT_PAGE_FK IS 'Self-FK to SEC_PAGE for navigation hierarchy within the module.';
COMMENT ON TABLE SEC_REFRESH_TOKEN IS 'JWT refresh token; rotated on refresh (ENTITY-SEC-005 / RULE-SEC-006).';
COMMENT ON TABLE SEC_PASSWORD_RESET_TOKEN IS 'Single-use password reset token (ENTITY-SEC-006 / RULE-SEC-007).';
COMMENT ON TABLE SEC_ACCOUNT_ACTIVATION_TOKEN IS 'Single-use account activation token (ENTITY-SEC-007 / RULE-SEC-008).';
COMMENT ON TABLE SEC_USER_ROLE IS 'Join: user-role (ENTITY-SEC-008); composite PK, no surrogate id/audit.';
COMMENT ON TABLE SEC_ROLE_PERMISSION IS 'Join: role-permission, Tier-2 (ENTITY-SEC-009); composite PK. Subject to RULE-SEC-014 (enforced app-layer).';
COMMENT ON TABLE SEC_ROLE_MODULE IS '[v1.3] Join: role-module, Tier-1 grant (ENTITY-SEC-011); composite PK, no surrogate id/audit. Drives dashboard filter + screen-permission prerequisite (RULE-SEC-013).';

-- ============================================================
-- BLOCK 5: CONSTRAINTS
-- ============================================================
-- 5a. PRIMARY KEYS
ALTER TABLE SEC_USER_ACCOUNT             ADD CONSTRAINT PK_SEC_USER_ACCOUNT             PRIMARY KEY (ID);
ALTER TABLE SEC_ROLE                     ADD CONSTRAINT PK_SEC_ROLE                     PRIMARY KEY (ID);
ALTER TABLE SEC_MODULE                   ADD CONSTRAINT PK_SEC_MODULE                   PRIMARY KEY (ID);              -- [v1.3]
ALTER TABLE SEC_PERMISSION               ADD CONSTRAINT PK_SEC_PERMISSION               PRIMARY KEY (ID);
ALTER TABLE SEC_PAGE                     ADD CONSTRAINT PK_SEC_PAGE                     PRIMARY KEY (ID);
ALTER TABLE SEC_REFRESH_TOKEN            ADD CONSTRAINT PK_SEC_REFRESH_TOKEN            PRIMARY KEY (ID);
ALTER TABLE SEC_PASSWORD_RESET_TOKEN     ADD CONSTRAINT PK_SEC_PASSWORD_RESET_TOKEN     PRIMARY KEY (ID);
ALTER TABLE SEC_ACCOUNT_ACTIVATION_TOKEN ADD CONSTRAINT PK_SEC_ACCOUNT_ACTIVATION_TOKEN PRIMARY KEY (ID);
ALTER TABLE SEC_USER_ROLE                ADD CONSTRAINT PK_SEC_USER_ROLE                PRIMARY KEY (USER_ACCOUNT_FK, ROLE_FK);
ALTER TABLE SEC_ROLE_PERMISSION          ADD CONSTRAINT PK_SEC_ROLE_PERMISSION          PRIMARY KEY (ROLE_FK, PERMISSION_FK);
ALTER TABLE SEC_ROLE_MODULE              ADD CONSTRAINT PK_SEC_ROLE_MODULE              PRIMARY KEY (ROLE_FK, MODULE_FK);  -- [v1.3]

-- 5b. UNIQUE CONSTRAINTS  (RULE-SEC-001, RULE-SEC-010)
ALTER TABLE SEC_USER_ACCOUNT             ADD CONSTRAINT UQ_SEC_USER_ACCOUNT_USERNAME    UNIQUE (USERNAME);
ALTER TABLE SEC_USER_ACCOUNT             ADD CONSTRAINT UQ_SEC_USER_ACCOUNT_EMAIL       UNIQUE (EMAIL);
ALTER TABLE SEC_ROLE                     ADD CONSTRAINT UQ_SEC_ROLE_ROLE_CODE           UNIQUE (ROLE_CODE);
ALTER TABLE SEC_MODULE                   ADD CONSTRAINT UQ_SEC_MODULE_MODULE_CODE       UNIQUE (MODULE_CODE);          -- [v1.3] RULE-SEC-010 pattern
ALTER TABLE SEC_PERMISSION               ADD CONSTRAINT UQ_SEC_PERMISSION_PERM_CODE     UNIQUE (PERMISSION_CODE);
ALTER TABLE SEC_PAGE                     ADD CONSTRAINT UQ_SEC_PAGE_PAGE_CODE           UNIQUE (PAGE_CODE);
ALTER TABLE SEC_REFRESH_TOKEN            ADD CONSTRAINT UQ_SEC_REFRESH_TOKEN_TOKEN      UNIQUE (TOKEN);
ALTER TABLE SEC_PASSWORD_RESET_TOKEN     ADD CONSTRAINT UQ_SEC_PWD_RESET_TOKEN_TOKEN    UNIQUE (TOKEN);
ALTER TABLE SEC_ACCOUNT_ACTIVATION_TOKEN ADD CONSTRAINT UQ_SEC_ACT_TOKEN_TOKEN          UNIQUE (TOKEN);

-- 5c. CHECK CONSTRAINTS
ALTER TABLE SEC_USER_ACCOUNT             ADD CONSTRAINT CHK_SEC_USER_ACCOUNT_ACTIVE_FL  CHECK (IS_ACTIVE_FL IN (0,1));
ALTER TABLE SEC_ROLE                     ADD CONSTRAINT CHK_SEC_ROLE_ACTIVE_FL          CHECK (IS_ACTIVE_FL IN (0,1));
ALTER TABLE SEC_MODULE                   ADD CONSTRAINT CHK_SEC_MODULE_ACTIVE_FL        CHECK (IS_ACTIVE_FL IN (0,1));  -- [v1.3]
ALTER TABLE SEC_PERMISSION               ADD CONSTRAINT CHK_SEC_PERMISSION_ACTIVE_FL    CHECK (IS_ACTIVE_FL IN (0,1));
ALTER TABLE SEC_PERMISSION               ADD CONSTRAINT CHK_SEC_PERMISSION_TYPE         CHECK (PERMISSION_TYPE IN ('VIEW','CREATE','UPDATE','DELETE'));
ALTER TABLE SEC_PAGE                     ADD CONSTRAINT CHK_SEC_PAGE_ACTIVE_FL          CHECK (IS_ACTIVE_FL IN (0,1));
ALTER TABLE SEC_REFRESH_TOKEN            ADD CONSTRAINT CHK_SEC_REFRESH_TOKEN_REVOKED_FL CHECK (REVOKED_FL IN (0,1));
ALTER TABLE SEC_PASSWORD_RESET_TOKEN     ADD CONSTRAINT CHK_SEC_PWD_RESET_TOKEN_USED_FL CHECK (USED_FL IN (0,1));
ALTER TABLE SEC_ACCOUNT_ACTIVATION_TOKEN ADD CONSTRAINT CHK_SEC_ACT_TOKEN_USED_FL       CHECK (USED_FL IN (0,1));

-- 5d. INTRA-MODULE FOREIGN KEYS
ALTER TABLE SEC_PERMISSION               ADD CONSTRAINT FK_SEC_PERMISSION_PAGE    FOREIGN KEY (PAGE_FK)         REFERENCES SEC_PAGE (ID);
ALTER TABLE SEC_PAGE                     ADD CONSTRAINT FK_SEC_PAGE_MODULE        FOREIGN KEY (MODULE_FK)       REFERENCES SEC_MODULE (ID);        -- [v1.3]
ALTER TABLE SEC_PAGE                     ADD CONSTRAINT FK_SEC_PAGE_PARENT        FOREIGN KEY (PARENT_PAGE_FK)  REFERENCES SEC_PAGE (ID);
ALTER TABLE SEC_REFRESH_TOKEN            ADD CONSTRAINT FK_SEC_REFRESH_TOKEN_USER FOREIGN KEY (USER_ACCOUNT_FK) REFERENCES SEC_USER_ACCOUNT (ID);
ALTER TABLE SEC_PASSWORD_RESET_TOKEN     ADD CONSTRAINT FK_SEC_PWD_RESET_TOKEN_USER FOREIGN KEY (USER_ACCOUNT_FK) REFERENCES SEC_USER_ACCOUNT (ID);
ALTER TABLE SEC_ACCOUNT_ACTIVATION_TOKEN ADD CONSTRAINT FK_SEC_ACT_TOKEN_USER     FOREIGN KEY (USER_ACCOUNT_FK) REFERENCES SEC_USER_ACCOUNT (ID);
ALTER TABLE SEC_USER_ROLE                ADD CONSTRAINT FK_SEC_USER_ROLE_USER     FOREIGN KEY (USER_ACCOUNT_FK) REFERENCES SEC_USER_ACCOUNT (ID);
ALTER TABLE SEC_USER_ROLE                ADD CONSTRAINT FK_SEC_USER_ROLE_ROLE     FOREIGN KEY (ROLE_FK)         REFERENCES SEC_ROLE (ID);
ALTER TABLE SEC_ROLE_PERMISSION          ADD CONSTRAINT FK_SEC_ROLE_PERM_ROLE     FOREIGN KEY (ROLE_FK)         REFERENCES SEC_ROLE (ID);
ALTER TABLE SEC_ROLE_PERMISSION          ADD CONSTRAINT FK_SEC_ROLE_PERM_PERM     FOREIGN KEY (PERMISSION_FK)   REFERENCES SEC_PERMISSION (ID);
ALTER TABLE SEC_ROLE_MODULE              ADD CONSTRAINT FK_SEC_ROLE_MODULE_ROLE   FOREIGN KEY (ROLE_FK)         REFERENCES SEC_ROLE (ID);          -- [v1.3]
ALTER TABLE SEC_ROLE_MODULE              ADD CONSTRAINT FK_SEC_ROLE_MODULE_MODULE FOREIGN KEY (MODULE_FK)       REFERENCES SEC_MODULE (ID);        -- [v1.3]

-- Note (RULE-SEC-014): the "no orphan screen permission" invariant — a SEC_ROLE_PERMISSION row is
-- valid only if the role also holds SEC_ROLE_MODULE for the module of that permission's page
-- (SEC_PERMISSION.PAGE_FK -> SEC_PAGE.MODULE_FK) — is a cross-table conditional invariant not
-- expressible as a declarative constraint in PostgreSQL without a trigger/assertion. Per medium-
-- complexity / no-workflow governance it is enforced at the application/service layer (P3.1) and in
-- the UI picker (P2.5). The DDL above supplies the structural backbone (Page.MODULE_FK NOT NULL + SEC_ROLE_MODULE).

-- ============================================================
-- BLOCK 6: TRIGGERS   -- (none)
-- ============================================================

-- ============================================================
-- BLOCK 7: INDEXES
-- ============================================================
CREATE INDEX IDX_SEC_USER_ACCOUNT_STATUS     ON SEC_USER_ACCOUNT (USER_STATUS_ID);
CREATE INDEX IDX_SEC_PERMISSION_PAGE_FK      ON SEC_PERMISSION (PAGE_FK);
CREATE INDEX IDX_SEC_PAGE_MODULE_FK          ON SEC_PAGE (MODULE_FK);                        -- [v1.3]
CREATE INDEX IDX_SEC_PAGE_PARENT_PAGE_FK     ON SEC_PAGE (PARENT_PAGE_FK);
CREATE INDEX IDX_SEC_REFRESH_TOKEN_USER_FK   ON SEC_REFRESH_TOKEN (USER_ACCOUNT_FK);
CREATE INDEX IDX_SEC_PWD_RESET_TOKEN_USER_FK ON SEC_PASSWORD_RESET_TOKEN (USER_ACCOUNT_FK);
CREATE INDEX IDX_SEC_ACT_TOKEN_USER_FK       ON SEC_ACCOUNT_ACTIVATION_TOKEN (USER_ACCOUNT_FK);
CREATE INDEX IDX_SEC_USER_ROLE_ROLE_FK       ON SEC_USER_ROLE (ROLE_FK);
CREATE INDEX IDX_SEC_ROLE_PERM_PERMISSION_FK ON SEC_ROLE_PERMISSION (PERMISSION_FK);
CREATE INDEX IDX_SEC_ROLE_MODULE_MODULE_FK   ON SEC_ROLE_MODULE (MODULE_FK);                 -- [v1.3]

-- ============================================================
-- BLOCK 8: LOOKUP SEED DATA
-- ============================================================
-- (none — LOV-SEC-001/002 runtime-loaded; Module is a REFERENCE table, not a LOV; no MD_MASTER_LOOKUP)

-- BLOCK 9: VIEWS            -- (none)
-- BLOCK 10: FUNCTIONS/PROCS -- (none)
-- BLOCK 11: DEFERRED FK     -- (none — no cross-module HARD-FKs; Tier-1 additions are intra-SEC)
-- ============================================================
```

## 5. SELF-CHECK GATE (re-run for v1.1)
```
[PASS] Naming — new objects follow conventions: table SEC_MODULE / join SEC_ROLE_MODULE; PK col ID;
       FK cols end _FK; flag ends _FL; SEQ_SEC_MODULE; constraint prefixes PK_/UQ_/CHK_/FK_/IDX_ (<=63 chars).
[PASS] DBF continuity — DBF-0001..0056 contiguous; DBF-0001..0048 preserved unchanged; +0049..0056 appended (no reorder/reuse).
[PASS] Referential integrity — every FK resolves intra-SEC: PAGE.MODULE_FK->SEC_MODULE, ROLE_MODULE.ROLE_FK->SEC_ROLE, ROLE_MODULE.MODULE_FK->SEC_MODULE. No dangling refs.
[PASS] SRS fidelity — all additions trace to srs-SEC v1.3 (ENTITY-SEC-010/011, ENTITY-SEC-004.moduleFk). Nothing invented beyond it. No new LOV (Module is reference).
[PASS] Rule mapping — RULE-SEC-013 -> SEC_ROLE_MODULE + API-SEC-019 (display filter, no module-level DB gate). RULE-SEC-014 -> structural backbone in DDL; cross-table invariant delegated to app layer (documented).
[PASS] DB_TARGET — POSTGRESQL_16, consistent with srs v1.3 and master-registry section 3.
[PASS] XM — no new cross-module dependency (Tier-1 intra-SEC); XM register unchanged.
GATE: PASSED
```

## 6. DB REGISTRY UPDATE — MODE 1.5 (Amendment v1.1)
```
REGISTRY UPDATE — 2026-09-02
Source Mode : MODE 1.5 (amendment) | Feature Code: SEC-001 | DBS-ID: DBS-SEC-001 | Script v1.1
Upstream    : srs-SEC v1.3 (prd-SEC v2 / domain-profile-ERP.md v2)
New Tables  : SEC_MODULE (reference), SEC_ROLE_MODULE (join)
Modified    : SEC_PAGE +MODULE_FK (FK -> SEC_MODULE, NOT NULL)
New Lookups : None
XM-IDs Open : None (Tier-1 intra-SEC)
OQ-IDs Open : None
Gate Status : PASSED
Next Action : P2.5 (ui-ux-spec-SEC / flow-diagram-SEC), then P3.1 (backend-execution-plan-SEC)
Table Registry rows to add (master-registry section 7):
  DBS-SEC-001 | SEC_MODULE | SEC | Tier-1 grantable unit + dashboard display unit (v1.3)
  DBS-SEC-001 | SEC_ROLE_MODULE (join) | SEC | Tier-1 role x module grant (v1.3)
Table Registry note: SEC_PAGE now carries MODULE_FK (FK -> SEC_MODULE).
Global XM Index rows to add (master-registry section 8): (none)
Pipeline Status Grid: SEC · P2 = done (amended v1.1)
```

---
*End of db-script-SEC.md | DBS-SEC-001 | v1.1 (CONTINUATION from v1.0) | POSTGRESQL_16 | 11 tables, 56 DBF-IDs, 0 outbound XM | two-tier RBAC (Tier-1 Module/RoleModule)*
*Upstream: srs-SEC v1.3 · Downstream re-align: P2.5 (UI/UX) -> P3.1 (Backend Plan)*
