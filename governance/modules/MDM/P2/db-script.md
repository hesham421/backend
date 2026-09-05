<!-- DB Script — Governed by Database Governance Engine (Project 2 / MODE 1.5) -->

# DB SCRIPT — Master Data (MDM)

> ## ⓘ UPSTREAM CHANGE / RE-GATE — aligned to srs-MDM v1.3 (2026-09-04)
> **Trigger:** srs-MDM.md now at v1.3 (supersedes the v1.1 this script first re-gated against). Relevant changes across v1.1→v1.3:
> 1. **v1.1** — screen layer collapsed to a single CORE-9 composite master-detail screen (SCR-MDM-001, page_code `MDM_LOOKUP`). No effect here (no screen content).
> 2. **v1.2** — §A2 seeding exclusion fully lifted (Architect decision): MDM seeds the standard LOVs owned by other modules as governed seed, taken verbatim from the owning modules' authoritative SRS. API-only pattern unchanged.
> 3. **v1.3** — OQ-MDM-002 RESOLVED: `FILE_FILE_TYPE` enumeration sourced from **srs-FILE.md §A5 (LOV-FILE-001)** = IMAGE/DOCUMENT/SPREADSHEET/ARCHIVE/OTHER. Directs P2 to re-gate BLOCK 8 to include **all four** type sets.
> **Correction of a prior P2 error:** the earlier re-gate (v1.1) recorded FILE_FILE_TYPE as a value-less container claiming "no governed enumeration exists." That was WRONG — srs-FILE §A5 does enumerate it (verified directly). This re-gate seeds those governed values.
> **Data layer (A3):** unchanged — DDL (BLOCKS 1–7) valid as-is.
> **Prior version:** `_backup/MDM__db-script.md__2026-09-04-02.md` (backup-and-replace, CORE-10 Step C).

## 1. DB SCRIPT HEADER
```
DBS-ID          : DBS-MDM-001
Module          : Master Data (MDM)
SRS Feature Code: MDM-001 (srs-MDM.md v1.3, 2026-09-04)
Platform        : Foundation (Domain: ERP)
DB_TARGET       : POSTGRESQL_16 (confirmed by Architect 2026-09-02)
Date            : 2026-09-04
Status          : GATE PASSED (DDL + seed) — see §6 Governance Notes
Open Questions  : None (OQ-MDM-001, OQ-MDM-002 RESOLVED in srs-MDM v1.3)
Tables          : 2 (MDM_LOOKUP_TYPE [master], MDM_LOOKUP_VALUE [detail])
XM Dependencies : None — MDM is consumed via API (provider pattern); no cross-module FK
Lookup Tables   : This module IS the shared lookup provider (LookupType/LookupValue)
```

> Governed design notes (SRS-governs-DB, Layer 1 > Layer 2):
> - Master-detail: MDM_LOOKUP_VALUE.LOOKUP_TYPE_FK is an INTRA-MODULE FK to MDM_LOOKUP_TYPE(ID) (RULE-MDM-007, NOT NULL). Detail rows never exist without a master row.
> - typeCode / valueCode are natural keys, Read-Only after creation (RULE-MDM-002 / RULE-MDM-004). No Business Code (BC-RULE-0 = NO).
> - Composite UNIQUE(LOOKUP_TYPE_FK, VALUE_CODE) — valueCode is unique WITHIN a type only (srs-MDM A3).
> - Deactivation is soft via IS_ACTIVE_FL; no hard delete of a type that still has values (RULE-MDM-006).
> - No cross-module FK and no XM-ID: consumers read MDM strictly via `GET /api/v1/mdm/lookups/{typeCode}?active=true` and store the **code** as a SOFT reference (srs-MDM A2/A7; module-registry-MDM §SHARED ENTITIES CONSUMED = none).
> - createdBy identity read from SEC is the platform-standard audit pattern (SOFT, no FK) — not a governed XM dependency.

## 2. DB FIELD TRACEABILITY MATRIX — Master Data — DBS-ID: DBS-MDM-001
```
DBF-ID   | Table Name       | Column Name    | DB Type        | SRS Source
DBF-0001 | MDM_LOOKUP_TYPE  | ID             | BIGINT         | ENTITY-MDM-001.lookupTypePk
DBF-0002 | MDM_LOOKUP_TYPE  | TYPE_CODE      | VARCHAR(50)    | ENTITY-MDM-001.typeCode (UNIQUE, RO after create — RULE-MDM-002)
DBF-0003 | MDM_LOOKUP_TYPE  | NAME_AR        | VARCHAR(200)   | ENTITY-MDM-001.nameAr
DBF-0004 | MDM_LOOKUP_TYPE  | NAME_EN        | VARCHAR(100)   | ENTITY-MDM-001.nameEn
DBF-0005 | MDM_LOOKUP_TYPE  | IS_ACTIVE_FL   | SMALLINT       | ENTITY-MDM-001.isActiveFl
DBF-0006 | MDM_LOOKUP_TYPE  | NOTES          | VARCHAR(2000)  | ENTITY-MDM-001.notes
DBF-0007 | MDM_LOOKUP_VALUE | ID             | BIGINT         | ENTITY-MDM-002.lookupValuePk
DBF-0008 | MDM_LOOKUP_VALUE | LOOKUP_TYPE_FK | BIGINT         | ENTITY-MDM-002.lookupTypeFk -> ENTITY-MDM-001 (RULE-MDM-007)
DBF-0009 | MDM_LOOKUP_VALUE | VALUE_CODE     | VARCHAR(50)    | ENTITY-MDM-002.valueCode (RO after create — RULE-MDM-004)
DBF-0010 | MDM_LOOKUP_VALUE | NAME_AR        | VARCHAR(200)   | ENTITY-MDM-002.nameAr
DBF-0011 | MDM_LOOKUP_VALUE | NAME_EN        | VARCHAR(100)   | ENTITY-MDM-002.nameEn
DBF-0012 | MDM_LOOKUP_VALUE | SORT_ORDER     | SMALLINT       | ENTITY-MDM-002.sortOrder
DBF-0013 | MDM_LOOKUP_VALUE | IS_ACTIVE_FL   | SMALLINT       | ENTITY-MDM-002.isActiveFl
DBF-0014 | MDM_LOOKUP_VALUE | NOTES          | VARCHAR(2000)  | ENTITY-MDM-002.notes
Total: 14 DBF-IDs across 2 tables
Audit columns (CREATED_BY/AT, UPDATED_BY/AT) on both base tables: no DBF-ID (AuditEntityListener).
```

## 3. CROSS-MODULE DEPENDENCY REGISTER (XM REGISTER) — Master Data — DBS-ID: DBS-MDM-001
```
XM-ID | Type | This Table | FK/Ref Column | Target Table | Target Module | Status
(none)
```
> MDM owns no cross-module dependency. It is a provider consumed via REST API; consumers hold the value **code** as a SOFT reference with no FK back into or out of MDM (srs-MDM A7). The reverse repointing of FILE/NOTIF onto MDM is a SEPARATE governed amendment on those owner modules (see §6 GN-4), not part of DBS-MDM-001.

## 4. FULL_DATABASE_SCRIPT
```sql
-- ============================================================
-- FULL DATABASE SCRIPT — Master Data (MDM) — DBS-MDM-001
-- Target: POSTGRESQL_16 | Execute in psql / pgAdmin
-- ============================================================

-- ============================================================
-- BLOCK 1: SEQUENCES
-- ============================================================
CREATE SEQUENCE SEQ_MDM_LOOKUP_TYPE  START WITH 1 INCREMENT BY 1 NO CACHE NO CYCLE;
CREATE SEQUENCE SEQ_MDM_LOOKUP_VALUE START WITH 1 INCREMENT BY 1 NO CACHE NO CYCLE;

-- ============================================================
-- BLOCK 2: PARENT TABLES (no intra-module FK dependencies)
-- MDM_LOOKUP_TYPE is the MASTER; referenced by MDM_LOOKUP_VALUE.
-- ============================================================
CREATE TABLE MDM_LOOKUP_TYPE (
  ID            BIGINT        NOT NULL,
  TYPE_CODE     VARCHAR(50)   NOT NULL,
  NAME_AR       VARCHAR(200)  NOT NULL,
  NAME_EN       VARCHAR(100)  NOT NULL,
  IS_ACTIVE_FL  SMALLINT      DEFAULT 1 NOT NULL,
  NOTES         VARCHAR(2000),
  CREATED_BY    VARCHAR(255),
  CREATED_AT    TIMESTAMP,
  UPDATED_BY    VARCHAR(255),
  UPDATED_AT    TIMESTAMP
);

-- ============================================================
-- BLOCK 3: CHILD TABLES (intra-module FK dependencies)
-- MDM_LOOKUP_VALUE is the DETAIL; FK -> MDM_LOOKUP_TYPE(ID).
-- ============================================================
CREATE TABLE MDM_LOOKUP_VALUE (
  ID              BIGINT        NOT NULL,
  LOOKUP_TYPE_FK  BIGINT        NOT NULL,
  VALUE_CODE      VARCHAR(50)   NOT NULL,
  NAME_AR         VARCHAR(200)  NOT NULL,
  NAME_EN         VARCHAR(100)  NOT NULL,
  SORT_ORDER      SMALLINT,
  IS_ACTIVE_FL    SMALLINT      DEFAULT 1 NOT NULL,
  NOTES           VARCHAR(2000),
  CREATED_BY      VARCHAR(255),
  CREATED_AT      TIMESTAMP,
  UPDATED_BY      VARCHAR(255),
  UPDATED_AT      TIMESTAMP
);

-- ============================================================
-- BLOCK 4: COMMENTS
-- ============================================================
COMMENT ON TABLE  MDM_LOOKUP_TYPE               IS 'Master lookup type / list category (ENTITY-MDM-001). Platform-wide shared reference catalog; consumed via API only.';
COMMENT ON COLUMN MDM_LOOKUP_TYPE.TYPE_CODE     IS 'Natural key (e.g. FILE_FILE_TYPE); UNIQUE; read-only after creation (RULE-MDM-002).';
COMMENT ON COLUMN MDM_LOOKUP_TYPE.IS_ACTIVE_FL  IS 'Active flag: 1=active, 0=inactive (soft deactivate — RULE-MDM-006).';
COMMENT ON TABLE  MDM_LOOKUP_VALUE              IS 'Detail lookup value under a type (ENTITY-MDM-002). Never exists without a master row (RULE-MDM-007).';
COMMENT ON COLUMN MDM_LOOKUP_VALUE.LOOKUP_TYPE_FK IS 'FK to MDM_LOOKUP_TYPE(ID); NOT NULL (master-detail rule, RULE-MDM-007).';
COMMENT ON COLUMN MDM_LOOKUP_VALUE.VALUE_CODE   IS 'Value code (e.g. EMAIL); UNIQUE within its type; read-only after creation (RULE-MDM-004).';
COMMENT ON COLUMN MDM_LOOKUP_VALUE.SORT_ORDER   IS 'Optional display order within the type.';
COMMENT ON COLUMN MDM_LOOKUP_VALUE.IS_ACTIVE_FL IS 'Active flag: 1=active, 0=inactive (soft deactivate — RULE-MDM-006).';

-- ============================================================
-- BLOCK 5: CONSTRAINTS
-- ============================================================
-- 5a. PRIMARY KEYS
ALTER TABLE MDM_LOOKUP_TYPE  ADD CONSTRAINT PK_MDM_LOOKUP_TYPE  PRIMARY KEY (ID);
ALTER TABLE MDM_LOOKUP_VALUE ADD CONSTRAINT PK_MDM_LOOKUP_VALUE PRIMARY KEY (ID);
-- 5b. UNIQUE (RULE-MDM-001 / RULE-MDM-003)
ALTER TABLE MDM_LOOKUP_TYPE  ADD CONSTRAINT UQ_MDM_LOOKUP_TYPE_CODE   UNIQUE (TYPE_CODE);
ALTER TABLE MDM_LOOKUP_VALUE ADD CONSTRAINT UQ_MDM_LOOKUP_VALUE_TYPE_CODE UNIQUE (LOOKUP_TYPE_FK, VALUE_CODE);
-- 5c. CHECK
ALTER TABLE MDM_LOOKUP_TYPE  ADD CONSTRAINT CHK_MDM_LOOKUP_TYPE_ACTIVE_FL  CHECK (IS_ACTIVE_FL IN (0,1));
ALTER TABLE MDM_LOOKUP_VALUE ADD CONSTRAINT CHK_MDM_LOOKUP_VALUE_ACTIVE_FL CHECK (IS_ACTIVE_FL IN (0,1));
-- 5d. INTRA-MODULE FK (master-detail; parent PK already exists)
ALTER TABLE MDM_LOOKUP_VALUE ADD CONSTRAINT FK_MDM_LOOKUP_VALUE_TYPE
  FOREIGN KEY (LOOKUP_TYPE_FK) REFERENCES MDM_LOOKUP_TYPE (ID);

-- ============================================================
-- BLOCK 6: TRIGGERS -- (none)
-- ============================================================

-- ============================================================
-- BLOCK 7: INDEXES
-- ============================================================
CREATE INDEX IDX_MDM_LOOKUP_VALUE_TYPE_FK ON MDM_LOOKUP_VALUE (LOOKUP_TYPE_FK);

-- ============================================================
-- BLOCK 8: LOOKUP SEED DATA   (P2 seed phase — sanctioned by srs-MDM v1.2/v1.3 §A2)
-- ============================================================
-- Seeds the platform LOVs owned privately by FILE and NOTIF into MDM.
-- All CODES are taken VERBATIM from the owning modules' authoritative SRS —
-- NO value is invented. NAME_AR/NAME_EN are engine-authored display labels
-- (revisable via normal UPDATE; codes are the governed identifiers, labels are not).
--
-- CODE PROVENANCE (governed sources):
--   NOTIF_CHANNEL     : srs-NOTIF.md A2  (EMAIL/SMS/WHATSAPP/PUSH/INTERNAL)
--   NOTIF_STATUS      : srs-NOTIF.md A6  (PENDING/SENT/FAILED/CHANNEL_DISABLED)
--   FILE_FILE_STATUS  : srs-FILE.md  A6  (ACTIVE/ARCHIVED/DELETED)
--   FILE_FILE_TYPE    : srs-FILE.md  §A5 (LOV-FILE-001)  (IMAGE/DOCUMENT/SPREADSHEET/ARCHIVE/OTHER)
-- typeCode namespacing is governed by registry-srs-FILE / registry-srs-NOTIF
-- (FILE_FILE_TYPE, FILE_FILE_STATUS, NOTIF_CHANNEL, NOTIF_STATUS) — no collision.

-- ---- Type: NOTIF_CHANNEL (from NOTIF) ----
INSERT INTO MDM_LOOKUP_TYPE (ID, TYPE_CODE, NAME_AR, NAME_EN, IS_ACTIVE_FL)
VALUES (nextval('SEQ_MDM_LOOKUP_TYPE'), 'NOTIF_CHANNEL', 'قناة الإشعار', 'Notification Channel', 1);

INSERT INTO MDM_LOOKUP_VALUE (ID, LOOKUP_TYPE_FK, VALUE_CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE_FL) VALUES
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='NOTIF_CHANNEL'), 'EMAIL',    'بريد إلكتروني', 'Email',    1, 1),
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='NOTIF_CHANNEL'), 'SMS',      'رسالة نصية',    'SMS',      2, 1),
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='NOTIF_CHANNEL'), 'WHATSAPP', 'واتساب',        'WhatsApp', 3, 1),
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='NOTIF_CHANNEL'), 'PUSH',     'إشعار فوري',    'Push',     4, 1),
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='NOTIF_CHANNEL'), 'INTERNAL', 'داخلي',         'Internal', 5, 1);

-- ---- Type: NOTIF_STATUS (from NOTIF) ----
INSERT INTO MDM_LOOKUP_TYPE (ID, TYPE_CODE, NAME_AR, NAME_EN, IS_ACTIVE_FL)
VALUES (nextval('SEQ_MDM_LOOKUP_TYPE'), 'NOTIF_STATUS', 'حالة الإشعار', 'Notification Status', 1);

INSERT INTO MDM_LOOKUP_VALUE (ID, LOOKUP_TYPE_FK, VALUE_CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE_FL) VALUES
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='NOTIF_STATUS'), 'PENDING',          'قيد الانتظار',   'Pending',          1, 1),
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='NOTIF_STATUS'), 'SENT',             'أُرسل',          'Sent',             2, 1),
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='NOTIF_STATUS'), 'FAILED',           'فشل',            'Failed',           3, 1),
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='NOTIF_STATUS'), 'CHANNEL_DISABLED', 'القناة معطّلة',  'Channel Disabled', 4, 1);

-- ---- Type: FILE_FILE_STATUS (from FILE) ----
INSERT INTO MDM_LOOKUP_TYPE (ID, TYPE_CODE, NAME_AR, NAME_EN, IS_ACTIVE_FL)
VALUES (nextval('SEQ_MDM_LOOKUP_TYPE'), 'FILE_FILE_STATUS', 'حالة الملف', 'File Status', 1);

INSERT INTO MDM_LOOKUP_VALUE (ID, LOOKUP_TYPE_FK, VALUE_CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE_FL) VALUES
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='FILE_FILE_STATUS'), 'ACTIVE',   'نشط',    'Active',   1, 1),
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='FILE_FILE_STATUS'), 'ARCHIVED', 'مؤرشف',  'Archived', 2, 1),
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='FILE_FILE_STATUS'), 'DELETED',  'محذوف',  'Deleted',  3, 1);

-- ---- Type: FILE_FILE_TYPE (from FILE) ----
-- Values + Arabic labels taken verbatim from srs-FILE.md §A5 (LOV-FILE-001).
INSERT INTO MDM_LOOKUP_TYPE (ID, TYPE_CODE, NAME_AR, NAME_EN, IS_ACTIVE_FL)
VALUES (nextval('SEQ_MDM_LOOKUP_TYPE'), 'FILE_FILE_TYPE', 'نوع الملف', 'File Type', 1);

INSERT INTO MDM_LOOKUP_VALUE (ID, LOOKUP_TYPE_FK, VALUE_CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE_FL) VALUES
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='FILE_FILE_TYPE'), 'IMAGE',       'صورة',  'Image',       1, 1),
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='FILE_FILE_TYPE'), 'DOCUMENT',    'مستند', 'Document',    2, 1),
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='FILE_FILE_TYPE'), 'SPREADSHEET', 'جدول',  'Spreadsheet', 3, 1),
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='FILE_FILE_TYPE'), 'ARCHIVE',     'أرشيف', 'Archive',     4, 1),
 (nextval('SEQ_MDM_LOOKUP_VALUE'), (SELECT ID FROM MDM_LOOKUP_TYPE WHERE TYPE_CODE='FILE_FILE_TYPE'), 'OTHER',       'أخرى',  'Other',       5, 1);

COMMIT;

-- BLOCK 9:  VIEWS      -- (none)
-- BLOCK 10: FUNCTIONS/PROCS -- (none)
-- BLOCK 11: DEFERRED FK BLOCKS
-- ============================================================
-- None. MDM has no cross-module FK by design (consumed via API, SOFT code refs).
-- ============================================================
```

## 5. DB REGISTRY UPDATE — MODE 1.5
```
REGISTRY UPDATE — 2026-09-04 (re-gate, aligned to srs-MDM v1.3)
Source Mode : MODE 1.5 | Feature Code: MDM-001 | DBS-ID: DBS-MDM-001
New Tables  : MDM_LOOKUP_TYPE, MDM_LOOKUP_VALUE
New Lookups : (this module IS the shared lookup provider)
Seed        : 4 types / 17 values (NOTIF_CHANNEL x5, NOTIF_STATUS x4, FILE_FILE_STATUS x3, FILE_FILE_TYPE x5)
XM-IDs Open : None
OQ-IDs Open : None (OQ-MDM-001, OQ-MDM-002 RESOLVED in srs-MDM v1.3)
Gate Status : PASSED (DDL + seed)
Next Action : Backend planning (Project 3.1) may proceed. Seed complete — no pending values.
Table Registry rows to add (master-registry §7):
  DBS-MDM-001 | MDM_LOOKUP_TYPE  | MDM
  DBS-MDM-001 | MDM_LOOKUP_VALUE | MDM
Global XM Index rows to add (master-registry §8): (none)
Pipeline Status Grid: MDM · P2 = done
```

## 6. GOVERNANCE NOTES (resolution record)
```
GN-1 (srs scope) — RESOLVED. srs-MDM v1.2/v1.3 §A2 brings governed seeding into
     scope and directs P2 to perform it. BLOCK 8 is the sanctioned P2-seed phase.

GN-2 (FILE_FILE_TYPE values) — RESOLVED with governed values. CORRECTION: an earlier
     P2 re-gate wrongly recorded "no governed enumeration exists" and seeded a value-less
     container. srs-FILE.md §A5 (LOV-FILE-001) DOES enumerate it — IMAGE/DOCUMENT/
     SPREADSHEET/ARCHIVE/OTHER with Arabic labels صورة/مستند/جدول/أرشيف/أخرى — verified
     directly and confirmed by srs-MDM v1.3 (OQ-MDM-002 RESOLVED). Now seeded verbatim.
     Zero invention.

GN-3 (bilingual labels) — RESOLVED. NAME_AR/NAME_EN are accepted as engine-authored
     display defaults (revisable anytime via normal UPDATE). Codes remain the governed
     identifiers; labels are non-identifier display strings, so this is within P2's
     delegated authority.

GN-4 (owner-module repointing, out of scope here) — Having FILE/NOTIF actually consume
     MDM instead of their private runtime codes is a separate governed amendment on
     db-script-FILE / db-script-NOTIF (and their SRS), tracked as INTEGRATION CANDIDATES
     in srs-MDM §A7 / module-registry-MDM. DBS-MDM-001 does not modify those modules.
```

---
*End of db-script-MDM.md | DBS-MDM-001 | POSTGRESQL_16 | 2 tables (master-detail), 14 DBF-IDs, 0 XM | GATE PASSED (DDL + seed: 4 types / 17 values) | aligned to srs-MDM v1.3 | Next: Project 3.1*
