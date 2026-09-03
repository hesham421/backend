<!-- DB Script — Governed by Database Governance Engine (Project 2 / MODE 1.5) -->

# DB SCRIPT — Common Utils (CU)

## 1. DB SCRIPT HEADER
```
DBS-ID          : DBS-CU-001
Module          : Common Utils (CU)
SRS Feature Code: CU-001  (srs-CU.md v1.0)
Platform        : Foundation (Domain: ERP)
DB_TARGET       : POSTGRESQL_16   (confirmed by Architect 2026-09-02; aligns to master-registry v1.0.0)
Date            : 2026-09-02
Status          : GATE PASSED
Open Questions  : None
Tables          : 1 (CU_APP_CONFIGURATION)
XM Dependencies : None (ROOT module — no cross-module dependencies)
Lookup Tables   : None — LOVs are runtime-loaded codes per SRS A5 (no MD_MASTER_LOOKUP in Foundation scope)
```

> Governed design note (SRS-governs-DB, Layer 1 > Layer 2): srs-CU.md A5 declares CU owns zero LOVs and explicitly excludes any central MD_MASTER_LOOKUP in Foundation scope. The Project-2 default shared-lookup pattern is therefore overridden by the authoritative SRS — no lookup tables and no seed data are generated.
> Audit columns (CREATED_BY, CREATED_AT, UPDATED_BY, UPDATED_AT) are mandatory on every table and populated by AuditEntityListener. They are created in DDL but, per convention, are not assigned DBF-IDs.

## 2. DB FIELD TRACEABILITY MATRIX — Common Utils — DBS-ID: DBS-CU-001
```
DBF-ID    | Table Name           | Column Name    | DB Type        | SRS Source
DBF-0001  | CU_APP_CONFIGURATION | ID             | BIGINT         | ENTITY-CU-001.appConfigurationPk
DBF-0002  | CU_APP_CONFIGURATION | CONFIG_KEY     | VARCHAR(150)   | ENTITY-CU-001.configKey
DBF-0003  | CU_APP_CONFIGURATION | CONFIG_VALUE   | TEXT           | ENTITY-CU-001.configValue
DBF-0004  | CU_APP_CONFIGURATION | NOTES          | VARCHAR(2000)  | ENTITY-CU-001.notes
DBF-0005  | CU_APP_CONFIGURATION | IS_ACTIVE_FL   | SMALLINT       | ENTITY-CU-001.isActiveFl
Total: 5 DBF-IDs across 1 table
```

## 3. CROSS-MODULE DEPENDENCY REGISTER (XM REGISTER) — Common Utils — DBS-ID: DBS-CU-001
```
(none) — CU is the ROOT cross-cutting library; it has no outbound cross-module dependencies.
```
> Note: SEC/FILE/NOTIF consume CU as a library (code injection), which is not an XM dependency (master-registry §8).

## 4. FULL_DATABASE_SCRIPT
```sql
-- ============================================================
-- FULL DATABASE SCRIPT — Common Utils (CU) — DBS-CU-001
-- Target: POSTGRESQL_16   |   Execute in psql / pgAdmin
-- ============================================================

-- ============================================================
-- BLOCK 1: SEQUENCES
-- ============================================================
CREATE SEQUENCE SEQ_CU_APP_CONFIGURATION
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

-- ============================================================
-- BLOCK 2: PARENT TABLES (no FK dependencies)
-- ============================================================
CREATE TABLE CU_APP_CONFIGURATION (
  ID            BIGINT           NOT NULL,
  CONFIG_KEY    VARCHAR(150)     NOT NULL,
  CONFIG_VALUE  TEXT             NOT NULL,
  NOTES         VARCHAR(2000),
  IS_ACTIVE_FL  SMALLINT         DEFAULT 1 NOT NULL,
  CREATED_BY    VARCHAR(255),
  CREATED_AT    TIMESTAMP,
  UPDATED_BY    VARCHAR(255),
  UPDATED_AT    TIMESTAMP
);

-- ============================================================
-- BLOCK 3: CHILD TABLES (intra-module FK dependencies)
-- ============================================================
-- (none)

-- ============================================================
-- BLOCK 4: COMMENTS
-- ============================================================
COMMENT ON TABLE CU_APP_CONFIGURATION IS 'Platform runtime key/value configuration store (ENTITY-CU-001 / CU-001).';
COMMENT ON COLUMN CU_APP_CONFIGURATION.ID IS 'PK — populated by framework via SEQ_CU_APP_CONFIGURATION.';
COMMENT ON COLUMN CU_APP_CONFIGURATION.CONFIG_KEY IS 'Unique configuration key; read-only after creation (RULE-CU-003).';
COMMENT ON COLUMN CU_APP_CONFIGURATION.CONFIG_VALUE IS 'Configuration value (text).';
COMMENT ON COLUMN CU_APP_CONFIGURATION.NOTES IS 'Optional description.';
COMMENT ON COLUMN CU_APP_CONFIGURATION.IS_ACTIVE_FL IS 'Active flag: 1=active, 0=inactive (soft deactivate).';

-- ============================================================
-- BLOCK 5: CONSTRAINTS
-- ============================================================
-- 5a. PRIMARY KEY
ALTER TABLE CU_APP_CONFIGURATION ADD CONSTRAINT PK_CU_APP_CONFIGURATION PRIMARY KEY (ID);
-- 5b. UNIQUE  (RULE-CU-001)
ALTER TABLE CU_APP_CONFIGURATION ADD CONSTRAINT UQ_CU_APP_CONFIG_CONFIG_KEY UNIQUE (CONFIG_KEY);
-- 5c. CHECK
ALTER TABLE CU_APP_CONFIGURATION ADD CONSTRAINT CHK_CU_APP_CONFIG_ACTIVE_FL CHECK (IS_ACTIVE_FL IN (0,1));
-- 5d. INTRA-MODULE FK
-- (none)

-- ============================================================
-- BLOCK 6: TRIGGERS
-- ============================================================
-- (none — PK population handled by application framework; no audit triggers governed by SRS)

-- ============================================================
-- BLOCK 7: INDEXES
-- ============================================================
-- (CONFIG_KEY already indexed via UQ constraint; no additional indexes required)

-- ============================================================
-- BLOCK 8: LOOKUP SEED DATA
-- ============================================================
-- (none — CU owns zero LOVs; no MD_MASTER_LOOKUP in Foundation scope per srs-CU.md A5)

-- ============================================================
-- BLOCK 9: VIEWS
-- ============================================================
-- (none)

-- ============================================================
-- BLOCK 10: FUNCTIONS AND PROCEDURES
-- ============================================================
-- (none)

-- ============================================================
-- BLOCK 11: DEFERRED FK PATCH BLOCKS
-- ============================================================
-- (none — no cross-module HARD-FKs)
```

## 5. DB REGISTRY UPDATE — MODE 1.5
```
REGISTRY UPDATE — 2026-09-02
Source Mode    : MODE 1.5
Feature Code   : CU-001
DBS-ID         : DBS-CU-001
New Tables     : CU_APP_CONFIGURATION
New Lookups    : None
XM-IDs Open    : None
OQ-IDs Open    : None
Gate Status    : PASSED
Next Action    : Trigger Project 3.1 — Execution Plan Governance Engine (Backend pass)
Table Registry rows to add (master-registry §7):
  DBS-CU-001 | CU_APP_CONFIGURATION | CU | key/value config store
Global XM Index rows to add (master-registry §8):
  (none)
Pipeline Status Grid: CU · P2 = done
```

---
*End of db-script-CU.md | DBS-CU-001 | POSTGRESQL_16 | 1 table, 5 DBF-IDs, 0 XM | Next: Project 3.1*
