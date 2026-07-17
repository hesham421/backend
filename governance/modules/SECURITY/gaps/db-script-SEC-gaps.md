# DATABASE GOVERNANCE ENGINE — MODE 1.5 SESSION OUTPUT
## Module: Security (SEC) — Scope: GAP ITEMS ONLY (per srs-security-gaps.md, cross-checked against srs-security-full.md)

```
╔══════════════════════════════════════════════════════════════════╗
║               MODE 1.5 — DB ENTRY GATE                            ║
╠══════════════════════════════════╦═══════════════════════════════╣
║ srs.md attached?                  ║ Yes — srs-security-full.md (v2.0) + srs-security-gaps.md (delta) ║
║ SRS gate passed (MODE 1)?         ║ ⚠ Not explicitly confirmed — srs-security-full.md carries 3 active OQs (OQ-001, OQ-002, OQ-003) and no explicit "Gate Status: GOVERNED" header. Proceeding with GOVERNANCE REDUCED-equivalent caution on the DataScope/extension scope only (see exception box below). Core entities (USERS/ROLES/PERMISSIONS/SEC_PAGES/REFRESH_TOKENS) are pre-existing AS-IS — not subject to this gate. ║
║ moduleRegistry.md loaded?         ║ Not uploaded this session — Not found. Impact: PK/column naming cross-checked against master-registry.md Section 4 + registry-security.md directly instead. ║
║ master-registry.md loaded?        ║ ✓ v2.7.7 loaded ║
║ Existing db-script.md?            ║ No formal governed db-script.md exists for Security — this is DBS-SEC-001 (first governed structural artifact for this module). Underlying physical schema for the 7 legacy tables already exists in production AS-IS (registry-security.md) and is NOT recreated here. ║
║ DB_TARGET declared?               ║ POSTGRESQL_16 (per srs-security-full.md A1, confirmed consistent with registry-security.md column types) ║
╠══════════════════════════════════╩═══════════════════════════════╣
║ Extracted: 4 new entities → 4 new tables, 2 outbound XM-IDs,      ║
║ 1 ALTER on existing table, 1 new Lookup registration              ║
╠══════════════════════════════════════════════════════════════════╣
║ PROCEED? Yes — WITH MANDATORY GOVERNANCE EXCEPTION FLAG BELOW      ║
╚══════════════════════════════════════════════════════════════════╝
```

---

## ⚠ GOVERNANCE EXCEPTION — MUST READ BEFORE EXECUTION

```
master-registry.md (LOCKED, v2.7.7) records Security as a PERMANENT
EXCEPTION module ("no code changes ever"), with exactly TWO approved
amendments to date (TENANT_ID removal — Conflict #17; PK-naming
migration — Conflict #18).

The objects generated in this document (SEC_USER_PROFILE, SEC_ROLE_BRANCH,
PASSWORD_RESET_TOKEN, ACCOUNT_ACTIVATION_TOKEN, USERS.EMAIL uniqueness)
belong to the "extension scope" that master-registry.md Section 15
records as:

    PARTIALLY_READY ⚠️ — BLOCKED pending BLK-SEC-002
    Conflict #19 (Section 13): OPEN — no architect sign-off has been
    provided for a THIRD exception to Security's "no code changes
    ever" policy. The extension scope's development is NOT YET
    AUTHORIZED.

Per srs-security-gaps.md §0 (this session's upstream decisions):
  — Conflict #20 / BLK-SEC-002 → CLOSED this session (Event-Based
    integration removes the NotificationService build cycle).
  — Conflict #19 → REMAINS OPEN. Explicitly described as a human/
    architecture-authority decision, out of SRS scope, "لا حل تلقائي"
    (no automatic resolution) — same class of decision as Conflict
    #17/#18.

GOVERNANCE POSITION TAKEN BY THIS ENGINE:
  Per RULE-4 (Conflict Resolution Protocol) this engine does not
  choose an interpretation on the human authority's behalf. This
  document is produced as the STRUCTURAL TRUTH PLANNING ARTIFACT
  (Layer 2) requested — it is NOT an authorization to execute DDL
  against production. Conflict #19 is CARRIED FORWARD, unresolved,
  and must be closed by architecture-authority sign-off (same
  mechanism as Conflict #17/#18) BEFORE FULL_DATABASE_SCRIPT below
  is run against any live schema.

  Gate Status for this session: CONDITIONAL — technically complete,
  execution-blocked pending Conflict #19 sign-off.
```

---

## NEW OQ-IDs RAISED THIS SESSION (added to canonical OQ Log — srs-security-full.md, updated in place per CORE-7/CONTRACT-3)

| OQ-ID | Entity | Question | Status |
|---|---|---|---|
| OQ-004 | ENTITY-SEC-009 (SEC_USER_PROFILE) | `PREFERRED_LANG` has no declared data type/domain in SRS A3 ("غير محدَّد"). This engine has defaulted to `VARCHAR(10)` NULL pending a confirmed LOV/domain (e.g. AR/EN codes vs. a full LOV-SEC-00x). | OPEN |
| OQ-005 | ENTITY-SEC-009 (SEC_USER_PROFILE) | `EMPLOYEE_ID_FK` references a not-yet-built HR module (master-registry: HR not listed among registered modules / NOT STARTED). No target ENTITY-ID exists to bind an XM-ID to. Column is created as an unconstrained nullable BIGINT with no FK — **no XM-ID assigned** (would be invention beyond source per HR-1). Revisit when HR module is governed. | OPEN |

Both are non-blocking for this DB Script (defaults applied; column-level only) — orthogonal to Conflict #19.

---

## DB FIELD TRACEABILITY MATRIX — Security (SEC) — DBS-ID: DBS-SEC-001

```
Ownership note: This is the FIRST governed DB Field Traceability Matrix
for the Security module. It covers ONLY the gap/delta objects below.
The 7 pre-existing AS-IS tables (USERS, ROLES, PERMISSIONS, SEC_PAGES,
REFRESH_TOKENS, USER_ROLES, ROLE_PERMISSIONS) are OUT OF SCOPE for
DBF-ID cataloguing this session (PERMANENT EXCEPTION, Section 1/4 of
master-registry.md) — they are referenced by name only for FK targets,
never recreated, never re-catalogued.
```

| DBF-ID | Table Name | Column Name | DB Type | SRS Source |
|---|---|---|---|---|
| DBF-0001 | SEC_USER_PROFILE | USER_ID_FK | BIGINT | ENTITY-SEC-009 (shared PK, → USERS.USERS_PK) |
| DBF-0002 | SEC_USER_PROFILE | BRANCH_ID_FK | BIGINT | ENTITY-SEC-009 → ENTITY-ORG-002 (XM-SEC-001) |
| DBF-0003 | SEC_USER_PROFILE | FULL_NAME_AR | VARCHAR(200) | ENTITY-SEC-009 |
| DBF-0004 | SEC_USER_PROFILE | FULL_NAME_EN | VARCHAR(100) | ENTITY-SEC-009 |
| DBF-0005 | SEC_USER_PROFILE | PREFERRED_LANG | VARCHAR(10) | ENTITY-SEC-009 — inferred default, see OQ-004 |
| DBF-0006 | SEC_USER_PROFILE | EMPLOYEE_ID_FK | BIGINT | ENTITY-SEC-009 — unconstrained, see OQ-005 |
| DBF-0007 | SEC_USER_PROFILE | IS_ACTIVE_FL | SMALLINT | ENTITY-SEC-009 |
| DBF-0008 | SEC_USER_PROFILE | CREATED_BY | VARCHAR(255) | ENTITY-SEC-009 |
| DBF-0009 | SEC_USER_PROFILE | CREATED_AT | TIMESTAMP | ENTITY-SEC-009 |
| DBF-0010 | SEC_USER_PROFILE | UPDATED_BY | VARCHAR(255) | ENTITY-SEC-009 |
| DBF-0011 | SEC_USER_PROFILE | UPDATED_AT | TIMESTAMP | ENTITY-SEC-009 |
| DBF-0012 | SEC_ROLE_BRANCH | ROLE_ID_FK | BIGINT | ENTITY-SEC-010 (composite PK part 1, → ROLES.ROLES_PK) |
| DBF-0013 | SEC_ROLE_BRANCH | BRANCH_ID_FK | BIGINT | ENTITY-SEC-010 → ENTITY-ORG-002 (XM-SEC-002, composite PK part 2) |
| DBF-0014 | SEC_ROLE_BRANCH | DATA_ACCESS_LEVEL | VARCHAR(30) | ENTITY-SEC-010 (LOV-SEC-002) |
| DBF-0015 | SEC_ROLE_BRANCH | IS_ACTIVE_FL | SMALLINT | ENTITY-SEC-010 |
| DBF-0016 | SEC_ROLE_BRANCH | CREATED_BY | VARCHAR(255) | ENTITY-SEC-010 |
| DBF-0017 | SEC_ROLE_BRANCH | CREATED_AT | TIMESTAMP | ENTITY-SEC-010 |
| DBF-0018 | SEC_ROLE_BRANCH | UPDATED_BY | VARCHAR(255) | ENTITY-SEC-010 |
| DBF-0019 | SEC_ROLE_BRANCH | UPDATED_AT | TIMESTAMP | ENTITY-SEC-010 |
| DBF-0020 | PASSWORD_RESET_TOKEN | TOKEN_PK | BIGINT | ENTITY-SEC-011 |
| DBF-0021 | PASSWORD_RESET_TOKEN | TOKEN | VARCHAR(64) | ENTITY-SEC-011 |
| DBF-0022 | PASSWORD_RESET_TOKEN | USER_ID_FK | BIGINT | ENTITY-SEC-011 → USERS.USERS_PK |
| DBF-0023 | PASSWORD_RESET_TOKEN | CREATED_AT | TIMESTAMP | ENTITY-SEC-011 |
| DBF-0024 | PASSWORD_RESET_TOKEN | EXPIRES_AT | TIMESTAMP | ENTITY-SEC-011 |
| DBF-0025 | PASSWORD_RESET_TOKEN | USED_FL | SMALLINT | ENTITY-SEC-011 |
| DBF-0026 | ACCOUNT_ACTIVATION_TOKEN | TOKEN_PK | BIGINT | ENTITY-SEC-012 |
| DBF-0027 | ACCOUNT_ACTIVATION_TOKEN | TOKEN | VARCHAR(64) | ENTITY-SEC-012 |
| DBF-0028 | ACCOUNT_ACTIVATION_TOKEN | USER_ID_FK | BIGINT | ENTITY-SEC-012 → USERS.USERS_PK |
| DBF-0029 | ACCOUNT_ACTIVATION_TOKEN | CREATED_AT | TIMESTAMP | ENTITY-SEC-012 |
| DBF-0030 | ACCOUNT_ACTIVATION_TOKEN | EXPIRES_AT | TIMESTAMP | ENTITY-SEC-012 |
| DBF-0031 | ACCOUNT_ACTIVATION_TOKEN | USED_FL | SMALLINT | ENTITY-SEC-012 |
| DBF-0032 | USERS *(existing — reference only)* | EMAIL | VARCHAR(150) | ENTITY-SEC-001 — pre-existing column, UK added this session (RULE-SEC-041 / AQ-008) |

Total: 32 DBF-IDs across 4 new tables + 1 altered pre-existing column.

---

## CROSS-MODULE DEPENDENCY REGISTER (XM REGISTER) — Security (SEC) — DBS-ID: DBS-SEC-001

| XM-ID | Type | This Table | FK/Ref Column | Target Table | Target Module | Status |
|---|---|---|---|---|---|---|
| XM-SEC-001 | HARD-FK | SEC_USER_PROFILE | BRANCH_ID_FK | ORG_BRANCH | Organization | READY |
| XM-SEC-002 | HARD-FK | SEC_ROLE_BRANCH | BRANCH_ID_FK | ORG_BRANCH | Organization | READY |

```
Both READY: master-registry.md Section 4 confirms Branch/ORG_BRANCH as
GOVERNED ✓ (owner: Organization, DBS-ORG-001, Used In: All). No deferral
needed — FK constraints created live in FULL_DATABASE_SCRIPT below.

Note: master-registry.md Section 13 (Conflict entry re: XM Inbound Stub
gap) flags that Organization's registry-exec-ORG.md XM Inbound Stub list
does NOT list Security as an anticipated consumer of Branch, despite this
HARD-FK dependency existing in the SRS. This is a P2→P3(ORG) traceability
gap, not a blocker for this module's own XM Register — recommend the
Registry Maintainer add Security to Organization's XM Inbound Stub list
on next ORG session (registry-exec-ORG.md currently shows 4 stubs, none
naming Security).
```

XM Register namespace check: 0 pre-existing XM-SEC-IDs found in master-registry Global XM Dependency Index — sequence starts fresh at XM-SEC-001.

---

## LOV DDL REGISTER (new this session)

| LOV-ID | Table/Type name | Code values |
|---|---|---|
| LOV-SEC-002 | MD_LOOKUP_DETAIL (lookupKey: `DATA_ACCESS_LEVEL`) | BRANCH_ONLY, BRANCH_AND_CHILDREN, ALL |

Per CORE-8 / Section 4.2 — no independent lookup table created; seed data only, into the existing shared MD_MASTER_LOOKUP / MD_LOOKUP_DETAIL tables (not recreated here).

---

## FULL_DATABASE_SCRIPT

> Complete executable delta script for Security (SEC) — Gap items only — DBS-ID: DBS-SEC-001
> DB_TARGET: POSTGRESQL_16 | Schema: no schema prefix (consistent with registry-security.md / registry-db-org.md)
> Generated: 2026-07-09 | SRS Feature Code: SEC-001
> Run in: psql or pgAdmin
> ⚠ DO NOT EXECUTE until Conflict #19 (master-registry.md Section 13) is signed off by architecture authority.
> Pre-requisites: ORG_BRANCH (DBS-ORG-001), USERS, ROLES (registry-security.md AS-IS) must already exist in the target schema.

```sql
-- ============================================================
-- SECURITY (SEC) — GAP DELTA DATABASE SCRIPT
-- DBS-ID     : DBS-SEC-001
-- SRS Code   : SEC-001
-- DB_TARGET  : POSTGRESQL_16
-- Generated  : 2026-07-09
-- Scope      : srs-security-gaps.md items 1–2 only.
--              Pre-existing AS-IS tables (USERS, ROLES, PERMISSIONS,
--              SEC_PAGES, REFRESH_TOKENS, USER_ROLES, ROLE_PERMISSIONS)
--              are NOT recreated — PERMANENT EXCEPTION, master-registry
--              Section 1/4.
-- GOVERNANCE : ⚠ Conflict #19 OPEN — execution requires architecture
--              authority sign-off before running against any live schema.
-- ============================================================

-- ============================================================
-- BLOCK 1: SEQUENCES
-- ============================================================
CREATE SEQUENCE PASSWORD_RESET_TOKEN_SEQ
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

CREATE SEQUENCE ACCOUNT_ACTIVATION_TOKEN_SEQ
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

-- ============================================================
-- BLOCK 2: PARENT TABLES
-- (none created in this delta — USERS, ROLES, ORG_BRANCH already exist)
-- ============================================================

-- ============================================================
-- BLOCK 3: CHILD TABLES
-- ============================================================

-- ENTITY-SEC-009 — user profile / branch assignment (1:1 with USERS)
CREATE TABLE SEC_USER_PROFILE (
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
CREATE TABLE SEC_ROLE_BRANCH (
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
CREATE TABLE PASSWORD_RESET_TOKEN (
  TOKEN_PK      BIGINT        NOT NULL,
  TOKEN         VARCHAR(64)   NOT NULL,
  USER_ID_FK    BIGINT        NOT NULL,
  CREATED_AT    TIMESTAMP     NOT NULL,
  EXPIRES_AT    TIMESTAMP     NOT NULL,
  USED_FL       SMALLINT      DEFAULT 0 NOT NULL
);

-- ENTITY-SEC-012 — account activation token
CREATE TABLE ACCOUNT_ACTIVATION_TOKEN (
  TOKEN_PK      BIGINT        NOT NULL,
  TOKEN         VARCHAR(64)   NOT NULL,
  USER_ID_FK    BIGINT        NOT NULL,
  CREATED_AT    TIMESTAMP     NOT NULL,
  EXPIRES_AT    TIMESTAMP     NOT NULL,
  USED_FL       SMALLINT      DEFAULT 0 NOT NULL
);

-- ============================================================
-- BLOCK 4: COMMENTS
-- ============================================================
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

-- ============================================================
-- BLOCK 5: CONSTRAINTS
-- ============================================================

-- 5a: Primary Keys
ALTER TABLE SEC_USER_PROFILE ADD CONSTRAINT PK_SEC_USER_PROFILE
  PRIMARY KEY (USER_ID_FK);

ALTER TABLE SEC_ROLE_BRANCH ADD CONSTRAINT PK_SEC_ROLE_BRANCH
  PRIMARY KEY (ROLE_ID_FK, BRANCH_ID_FK);

ALTER TABLE PASSWORD_RESET_TOKEN ADD CONSTRAINT PK_PASSWORD_RESET_TOKEN
  PRIMARY KEY (TOKEN_PK);

ALTER TABLE ACCOUNT_ACTIVATION_TOKEN ADD CONSTRAINT PK_ACCOUNT_ACTIVATION_TOKEN
  PRIMARY KEY (TOKEN_PK);

-- 5b: Unique Constraints
ALTER TABLE PASSWORD_RESET_TOKEN ADD CONSTRAINT UK_PASSWORD_RESET_TOKEN_TOKEN
  UNIQUE (TOKEN);

ALTER TABLE ACCOUNT_ACTIVATION_TOKEN ADD CONSTRAINT UK_ACCOUNT_ACTIVATION_TOKEN_TOKEN
  UNIQUE (TOKEN);

-- ⚠ GOVERNANCE EXCEPTION — ALTER on pre-existing AS-IS table (USERS).
-- Requires Conflict #19 sign-off before execution — RULE-SEC-041 / AQ-008.
ALTER TABLE USERS ADD CONSTRAINT UK_USERS_EMAIL
  UNIQUE (EMAIL);

-- 5c: Check Constraints
ALTER TABLE PASSWORD_RESET_TOKEN ADD CONSTRAINT CHK_PASSWORD_RESET_TOKEN_USED_FL
  CHECK (USED_FL IN (0,1));

ALTER TABLE ACCOUNT_ACTIVATION_TOKEN ADD CONSTRAINT CHK_ACCOUNT_ACTIVATION_TOKEN_USED_FL
  CHECK (USED_FL IN (0,1));

-- 5d: Foreign Keys — Intra-module (to pre-existing Security tables)
ALTER TABLE SEC_USER_PROFILE ADD CONSTRAINT FK_SEC_USER_PROFILE_USER
  FOREIGN KEY (USER_ID_FK) REFERENCES USERS (USERS_PK);

ALTER TABLE SEC_ROLE_BRANCH ADD CONSTRAINT FK_SEC_ROLE_BRANCH_ROLE
  FOREIGN KEY (ROLE_ID_FK) REFERENCES ROLES (ROLES_PK);

ALTER TABLE PASSWORD_RESET_TOKEN ADD CONSTRAINT FK_PASSWORD_RESET_TOKEN_USER
  FOREIGN KEY (USER_ID_FK) REFERENCES USERS (USERS_PK);

ALTER TABLE ACCOUNT_ACTIVATION_TOKEN ADD CONSTRAINT FK_ACCOUNT_ACTIVATION_TOKEN_USER
  FOREIGN KEY (USER_ID_FK) REFERENCES USERS (USERS_PK);

-- 5e: Foreign Keys — Cross-module (XM-SEC-001, XM-SEC-002 — both READY, target GOVERNED)
ALTER TABLE SEC_USER_PROFILE ADD CONSTRAINT FK_SEC_USER_PROFILE_BRANCH
  FOREIGN KEY (BRANCH_ID_FK) REFERENCES ORG_BRANCH (BRANCH_PK);

ALTER TABLE SEC_ROLE_BRANCH ADD CONSTRAINT FK_SEC_ROLE_BRANCH_BRANCH
  FOREIGN KEY (BRANCH_ID_FK) REFERENCES ORG_BRANCH (BRANCH_PK);

-- ============================================================
-- BLOCK 6: TRIGGERS
-- (none — no auto-PK triggers; audit fields populated by
--  AuditEntityListener at the application layer, consistent with
--  existing Security tables. No SRS-mandated audit trigger.)
-- ============================================================

-- ============================================================
-- BLOCK 7: INDEXES
-- ============================================================
CREATE INDEX IDX_SEC_USER_PROFILE_BRANCH ON SEC_USER_PROFILE (BRANCH_ID_FK);
CREATE INDEX IDX_SEC_USER_PROFILE_EMPLOYEE ON SEC_USER_PROFILE (EMPLOYEE_ID_FK);

CREATE INDEX IDX_SEC_ROLE_BRANCH_BRANCH ON SEC_ROLE_BRANCH (BRANCH_ID_FK);

CREATE INDEX IDX_PASSWORD_RESET_TOKEN_USER ON PASSWORD_RESET_TOKEN (USER_ID_FK);
CREATE INDEX IDX_PASSWORD_RESET_TOKEN_EXPIRES ON PASSWORD_RESET_TOKEN (EXPIRES_AT);

CREATE INDEX IDX_ACCT_ACTIVATION_TOKEN_USER ON ACCOUNT_ACTIVATION_TOKEN (USER_ID_FK);
CREATE INDEX IDX_ACCT_ACTIVATION_TOKEN_EXPIRES ON ACCOUNT_ACTIVATION_TOKEN (EXPIRES_AT);

-- Note: UK_USERS_EMAIL above already provides an index on USERS.EMAIL —
-- no separate CREATE INDEX needed (Postgres auto-indexes UNIQUE constraints).

-- ============================================================
-- BLOCK 8: LOOKUP SEED DATA
-- (MD_MASTER_LOOKUP / MD_LOOKUP_DETAIL are shared system tables,
--  never recreated here — seed INSERTs only)
-- ============================================================
INSERT INTO MD_MASTER_LOOKUP
  (ID_PK, LOOKUP_KEY, LOOKUP_NAME, LOOKUP_NAME_EN, IS_ACTIVE)
VALUES
  (MD_MASTER_LOOKUP_SEQ.NEXTVAL, 'DATA_ACCESS_LEVEL', 'مستوى نطاق البيانات', 'Data Access Level', 1);

INSERT INTO MD_LOOKUP_DETAIL
  (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE)
VALUES
  (MD_LOOKUP_DETAIL_SEQ.NEXTVAL,
   (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'DATA_ACCESS_LEVEL'),
   'BRANCH_ONLY', 'الفرع فقط', 'Branch Only', 1, 1);

INSERT INTO MD_LOOKUP_DETAIL
  (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE)
VALUES
  (MD_LOOKUP_DETAIL_SEQ.NEXTVAL,
   (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'DATA_ACCESS_LEVEL'),
   'BRANCH_AND_CHILDREN', 'الفرع وفروعه التابعة', 'Branch and Children', 2, 1);

INSERT INTO MD_LOOKUP_DETAIL
  (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE)
VALUES
  (MD_LOOKUP_DETAIL_SEQ.NEXTVAL,
   (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'DATA_ACCESS_LEVEL'),
   'ALL', 'كل الفروع', 'All', 3, 1);

COMMIT;

-- ============================================================
-- BLOCK 9: VIEWS
-- (none required by SRS for this delta)
-- ============================================================

-- ============================================================
-- BLOCK 10: FUNCTIONS AND PROCEDURES
-- (none required by SRS for this delta)
-- ============================================================

-- ============================================================
-- BLOCK 11: DEFERRED FK PATCH BLOCKS
-- (No DEFERRED HARD-FK — both XM-SEC-001 and XM-SEC-002 are READY
--  and applied live in BLOCK 5e above. EMPLOYEE_ID_FK is intentionally
--  left unconstrained — no XM-ID exists for an ungoverned HR module.
--  Uncomment and assign an XM-ID once HR is governed — see OQ-005.)
-- ============================================================
-- -- FUTURE — pending HR module governance (OQ-005, no XM-ID yet)
-- -- ALTER TABLE SEC_USER_PROFILE
-- --   ADD CONSTRAINT FK_SEC_USER_PROFILE_EMPLOYEE
-- --     FOREIGN KEY (EMPLOYEE_ID_FK) REFERENCES HR_EMPLOYEE (EMPLOYEE_PK);
```

---

## DB REGISTRY UPDATE — 2026-07-09

```
────────────────────────────────────────────────────────────────
Source Mode    : MODE 1.5
Feature Code   : SEC-001
DBS-ID         : DBS-SEC-001
Plan ID        : —
────────────────────────────────────────────────────────────────
New Entities   : —
New Tables     : SEC_USER_PROFILE, SEC_ROLE_BRANCH,
                 PASSWORD_RESET_TOKEN, ACCOUNT_ACTIVATION_TOKEN
Altered Tables : USERS (UK_USERS_EMAIL added — pending Conflict #19)
New Lookups    : DATA_ACCESS_LEVEL (LOV-SEC-002)
New APIs       : —
XM-IDs Open    : XM-SEC-001 (READY), XM-SEC-002 (READY)
OQ-IDs Open    : OQ-004 (PREFERRED_LANG domain), OQ-005 (EMPLOYEE_ID_FK
                 target — HR ungoverned) — both non-blocking
Gate Status    : CONDITIONAL — technically complete; EXECUTION BLOCKED
                 pending Conflict #19 architecture-authority sign-off
                 (master-registry.md Section 13)
Next Action    : (1) Route Conflict #19 to architecture authority for
                 sign-off (same mechanism as Conflict #17/#18).
                 (2) Once signed off: MODE 2 — Execution Plan
                 Governance Engine may consume this db-script.md.
────────────────────────────────────────────────────────────────
```

Registry cascade check (SHARED-GOVERNANCE-RULES.md REG-3): DBS-SEC-001 is a new DBS-ID. No DEFERRED XM-IDs in the Global XM Dependency Index currently target Security — no cascade/RXE triggered by this gate.

---
*End of db-script-SEC-gaps.md — MODE 1.5 output, Database Governance Engine (Project 2)*
