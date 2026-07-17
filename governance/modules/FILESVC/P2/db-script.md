<!-- ═══════════════════════════════════════════════════════════ -->
<!-- DB SCRIPT — قاعدة البيانات                                 -->
<!-- Governed by: Database Governance Engine (Project 2)        -->
<!-- MODE 1.5 — Structural Truth (Layer 2)                      -->
<!-- ═══════════════════════════════════════════════════════════ -->

# DB SCRIPT — File Service
## DBS-ID: DBS-FILE-001

---

## 1. DB SCRIPT HEADER

```
DBS-ID          : DBS-FILE-001
Module          : File Service (FILE)
SRS Feature Code: FILE-001 (srs-file-001.md, v1.0)
DB_TARGET       : POSTGRESQL_16
Date            : 2026-07-11
Status          : GOVERNED ✓ MODE 1.5
Open Questions  : None — see OQ Log (OQ-001 RESOLVED at MODE 1)
Tables          : 2  (FILE_CATEGORY, FILE_DOCUMENT)
DBF-IDs         : 27
XM-IDs (outbound): 0 — File Service consumes no other module's SHARED
                   entity by FK (module-registry-file.md — confirmed).
                   Security dependency is Trust-Boundary only (JWT filter
                   chain) — no data-FK, no XM-ID (module-registry-file.md).
```

MODE 1.5 ENTRY GATE — confirmed prior to generation:

```
╔══════════════════════════════════════════════════════════════════╗
║               MODE 1.5 — DB ENTRY GATE — FILE SERVICE            ║
╠══════════════════════════════════╦═══════════════════════════════╣
║ srs.md attached?                 ║ Yes — srs-file-001.md          ║
║ SRS gate passed (MODE 1)?        ║ ✓ PASSED (2026-07-11)          ║
║ moduleRegistry.md loaded?        ║ ✓ module-registry-file.md      ║
║ master-registry.md loaded?       ║ ✓                              ║
║ Existing db-script.md?           ║ No — fresh generation          ║
║ DB_TARGET declared?              ║ POSTGRESQL_16                  ║
╠══════════════════════════════════╩═══════════════════════════════╣
║ Extracted: 2 entities → 2 tables, 1 intra-module FK, 0 XM-IDs    ║
╠══════════════════════════════════════════════════════════════════╣
║ PROCEED: Yes                                                      ║
╚══════════════════════════════════════════════════════════════════╝
```

---

## 2. DB FIELD TRACEABILITY MATRIX — File Service — DBS-ID: DBS-FILE-001

══════════════════════════════════════════════════════════════════════════════════════
| DBF-ID   | Table Name    | Column Name              | DB Type       | SRS Source |
|----------|---------------|--------------------------|---------------|------------|
| DBF-0001 | FILE_CATEGORY | FILE_CATEGORY_PK         | BIGINT        | ENTITY-FILE-002.fileCategoryPk |
| DBF-0002 | FILE_CATEGORY | CATEGORY_CODE            | VARCHAR(50)   | ENTITY-FILE-002.categoryCode |
| DBF-0003 | FILE_CATEGORY | MODULE_CODE              | VARCHAR(20)   | ENTITY-FILE-002.moduleCode |
| DBF-0004 | FILE_CATEGORY | NAME_AR                  | VARCHAR(200)  | ENTITY-FILE-002.nameAr |
| DBF-0005 | FILE_CATEGORY | NAME_EN                  | VARCHAR(100)  | ENTITY-FILE-002.nameEn |
| DBF-0006 | FILE_CATEGORY | MAX_SIZE_BYTES_OVERRIDE  | BIGINT        | ENTITY-FILE-002.maxSizeBytesOverride |
| DBF-0007 | FILE_CATEGORY | ALLOWED_TYPES_NOTE       | VARCHAR(500)  | ENTITY-FILE-002.allowedTypesNote |
| DBF-0008 | FILE_CATEGORY | IS_ACTIVE_FL             | SMALLINT      | ENTITY-FILE-002.isActiveFl |
| DBF-0009 | FILE_CATEGORY | CREATED_BY               | VARCHAR(255)  | ENTITY-FILE-002.createdBy |
| DBF-0010 | FILE_CATEGORY | CREATED_AT               | TIMESTAMP     | ENTITY-FILE-002.createdAt |
| DBF-0011 | FILE_CATEGORY | UPDATED_BY               | VARCHAR(255)  | ENTITY-FILE-002.updatedBy |
| DBF-0012 | FILE_CATEGORY | UPDATED_AT               | TIMESTAMP     | ENTITY-FILE-002.updatedAt |
| DBF-0013 | FILE_DOCUMENT | FILE_DOCUMENT_PK         | BIGINT        | ENTITY-FILE-001.fileDocumentPk |
| DBF-0014 | FILE_DOCUMENT | OWNER_ID                 | BIGINT        | ENTITY-FILE-001.ownerId (polymorphic — no FK) |
| DBF-0015 | FILE_DOCUMENT | OWNER_TYPE               | VARCHAR(100)  | ENTITY-FILE-001.ownerType |
| DBF-0016 | FILE_DOCUMENT | MODULE_CODE              | VARCHAR(20)   | ENTITY-FILE-001.moduleCode |
| DBF-0017 | FILE_DOCUMENT | FILE_CATEGORY_FK         | BIGINT        | ENTITY-FILE-001.fileCategoryFk → ENTITY-FILE-002 |
| DBF-0018 | FILE_DOCUMENT | FILE_TYPE_ID             | VARCHAR(50)   | ENTITY-FILE-001.fileTypeId (LOV-FILE-001) |
| DBF-0019 | FILE_DOCUMENT | FILE_NAME_ORIGINAL       | VARCHAR(255)  | ENTITY-FILE-001.fileNameOriginal |
| DBF-0020 | FILE_DOCUMENT | MIME_TYPE                | VARCHAR(100)  | ENTITY-FILE-001.mimeType |
| DBF-0021 | FILE_DOCUMENT | FILE_SIZE_BYTES          | BIGINT        | ENTITY-FILE-001.fileSizeBytes |
| DBF-0022 | FILE_DOCUMENT | FILE_CONTENT             | BYTEA         | ENTITY-FILE-001.fileContent (RESOLUTION-01 — extends CORE-8) |
| DBF-0023 | FILE_DOCUMENT | FILE_STATUS_ID           | VARCHAR(50)   | ENTITY-FILE-001.fileStatusId (LOV-FILE-002) |
| DBF-0024 | FILE_DOCUMENT | CREATED_BY               | VARCHAR(255)  | ENTITY-FILE-001.createdBy |
| DBF-0025 | FILE_DOCUMENT | CREATED_AT               | TIMESTAMP     | ENTITY-FILE-001.createdAt |
| DBF-0026 | FILE_DOCUMENT | UPDATED_BY               | VARCHAR(255)  | ENTITY-FILE-001.updatedBy |
| DBF-0027 | FILE_DOCUMENT | UPDATED_AT               | TIMESTAMP     | ENTITY-FILE-001.updatedAt |
══════════════════════════════════════════════════════════════════════════════════════

Total: 27 DBF-IDs across 2 tables.

Governance note (DBF-0022): `FILE_CONTENT BYTEA` extends the CORE-8 PostgreSQL
type table (which has no binary-content row). This is a documented architectural
decision (RESOLUTION-01 / business-policies-file.md "Platform Integration Notes"),
not an invented type — POSTGRESQL_16 BYTEA only, no Large Objects.

---

## 3. CROSS-MODULE DEPENDENCY REGISTER (XM REGISTER) — File Service — DBS-ID: DBS-FILE-001

══════════════════════════════════════════════════════════════════════════════════
| XM-ID | Type | This Table | FK/Ref Column | Target Table | Target Module | Status |
|-------|------|------------|----------------|---------------|----------------|--------|
| —     | —    | —          | —              | —             | —              | —      |
══════════════════════════════════════════════════════════════════════════════════

None — zero outbound cross-module dependencies (module-registry-file.md
"OUTBOUND XM CANDIDATES: None — File Service consumes no other module's
SHARED entity by FK").

Note — Security dependency: `CREATED_BY` / `UPDATED_BY` are audit fields
populated by `AuditEntityListener` (VARCHAR username strings, not FK columns —
same convention as Organization's DBS-ORG-001). File Service's Security
dependency is a Trust-Boundary only (JWT filter chain for the Encrypted
Token issuing endpoint) — there is no physical FK from any FILE_* table to
any SEC_* table. No XM-ID is assigned for this dependency (srs-file-001.md
A7 confirms; module-registry-file.md DEPENDENCIES section classifies it as
non-data HARD dependency).

Total: 0 XM-IDs.

Inbound awareness (informational — not this module's XM Register; owned by
the consuming modules' own MODE 1.5 sessions):
`FILE_DOCUMENT` is the target of `XM-NOTIF-001` (DEFERRED — see
dbs-notif-001.md, this session) and future stubs from AuditService and
3.x modules (module-registry-file.md INBOUND XM CANDIDATES). No action is
required in this module's own script for those — CONTRACT-8 governs the
RXE trigger when this DBS-ID gates.

---

## 4. FULL_DATABASE_SCRIPT

> Complete executable script for File Service — DBS-ID: DBS-FILE-001
> DB_TARGET: POSTGRESQL_16 | Schema: no schema prefix (consistent with DBS-ORG-001)
> Generated: 2026-07-11 | SRS Feature Code: FILE-001
> Run in psql or pgAdmin against a clean schema. No manual editing required.

```sql
-- ============================================================
-- FILE SERVICE — COMPLETE DATABASE SCRIPT
-- DBS-ID     : DBS-FILE-001
-- SRS Code   : FILE-001
-- DB_TARGET  : POSTGRESQL_16
-- Generated  : 2026-07-11
-- ============================================================

-- ============================================================
-- BLOCK 1: SEQUENCES
-- ============================================================
CREATE SEQUENCE SEQ_FILE_CATEGORY
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

CREATE SEQUENCE SEQ_FILE_DOCUMENT
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

-- ============================================================
-- BLOCK 2: PARENT TABLES (no intra-module FK dependencies)
-- ============================================================
CREATE TABLE FILE_CATEGORY (
  FILE_CATEGORY_PK         BIGINT         NOT NULL,
  CATEGORY_CODE             VARCHAR(50)    NOT NULL,
  MODULE_CODE                VARCHAR(20)    NOT NULL,
  NAME_AR                    VARCHAR(200)   NOT NULL,
  NAME_EN                    VARCHAR(100)   NOT NULL,
  MAX_SIZE_BYTES_OVERRIDE     BIGINT,
  ALLOWED_TYPES_NOTE          VARCHAR(500),
  IS_ACTIVE_FL                SMALLINT       NOT NULL DEFAULT 1,
  CREATED_BY                  VARCHAR(255)   NOT NULL,
  CREATED_AT                  TIMESTAMP      NOT NULL,
  UPDATED_BY                  VARCHAR(255),
  UPDATED_AT                  TIMESTAMP
);

-- ============================================================
-- BLOCK 3: CHILD TABLES (intra-module FK dependencies)
-- ============================================================
CREATE TABLE FILE_DOCUMENT (
  FILE_DOCUMENT_PK    BIGINT         NOT NULL,
  OWNER_ID              BIGINT         NOT NULL,
  OWNER_TYPE             VARCHAR(100)   NOT NULL,
  MODULE_CODE             VARCHAR(20)    NOT NULL,
  FILE_CATEGORY_FK        BIGINT         NOT NULL,
  FILE_TYPE_ID             VARCHAR(50)    NOT NULL,
  FILE_NAME_ORIGINAL       VARCHAR(255)   NOT NULL,
  MIME_TYPE                VARCHAR(100)   NOT NULL,
  FILE_SIZE_BYTES          BIGINT         NOT NULL,
  FILE_CONTENT             BYTEA          NOT NULL,
  FILE_STATUS_ID           VARCHAR(50)    NOT NULL,
  CREATED_BY               VARCHAR(255)   NOT NULL,
  CREATED_AT               TIMESTAMP      NOT NULL,
  UPDATED_BY                VARCHAR(255),
  UPDATED_AT                TIMESTAMP
);

-- ============================================================
-- BLOCK 4: COMMENTS
-- ============================================================
COMMENT ON TABLE FILE_CATEGORY IS 'Reference Table — business document-category taxonomy, module_code-scoped, extensible by Admin. ENTITY-FILE-002.';
COMMENT ON COLUMN FILE_CATEGORY.FILE_CATEGORY_PK IS 'Primary key — auto-generated, PK population handled by application framework.';
COMMENT ON COLUMN FILE_CATEGORY.CATEGORY_CODE IS 'Category code — unique within MODULE_CODE, immutable after creation (lookupKey-like pattern).';
COMMENT ON COLUMN FILE_CATEGORY.MODULE_CODE IS 'Owning module code for this category (e.g. NOTIFICATION, PRC) — free text, not a governed lookup.';
COMMENT ON COLUMN FILE_CATEGORY.NAME_AR IS 'Category display name — Arabic.';
COMMENT ON COLUMN FILE_CATEGORY.NAME_EN IS 'Category display name — English.';
COMMENT ON COLUMN FILE_CATEGORY.MAX_SIZE_BYTES_OVERRIDE IS 'Optional per-category override of the default 5MB content size limit — RULE-FILE-001.';
COMMENT ON COLUMN FILE_CATEGORY.ALLOWED_TYPES_NOTE IS 'Free-text advisory note of allowed file types for this category — not an enforced constraint.';
COMMENT ON COLUMN FILE_CATEGORY.IS_ACTIVE_FL IS 'Active flag — 1 = active, 0 = inactive.';
COMMENT ON COLUMN FILE_CATEGORY.CREATED_BY IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN FILE_CATEGORY.CREATED_AT IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN FILE_CATEGORY.UPDATED_BY IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN FILE_CATEGORY.UPDATED_AT IS 'Audit — populated by AuditEntityListener.';

COMMENT ON TABLE FILE_DOCUMENT IS 'SHARED (owner) — central binary file storage for all platform modules. ENTITY-FILE-001. No Update operation — replacement is delete+re-upload.';
COMMENT ON COLUMN FILE_DOCUMENT.FILE_DOCUMENT_PK IS 'Primary key — auto-generated, PK population handled by application framework.';
COMMENT ON COLUMN FILE_DOCUMENT.OWNER_ID IS 'Polymorphic reference to the owning business record — no physical FK; target table determined by OWNER_TYPE (ADAPT-05).';
COMMENT ON COLUMN FILE_DOCUMENT.OWNER_TYPE IS 'Owning entity type name from the producing module (e.g. PURCHASE_ORDER) — free text, not a governed lookup.';
COMMENT ON COLUMN FILE_DOCUMENT.MODULE_CODE IS 'Producing module code.';
COMMENT ON COLUMN FILE_DOCUMENT.FILE_CATEGORY_FK IS 'FK to FILE_CATEGORY — intra-module.';
COMMENT ON COLUMN FILE_DOCUMENT.FILE_TYPE_ID IS 'System-detected technical file type — LOV-FILE-001 (lookupKey: FILE_TYPE) — RULE-FILE-005.';
COMMENT ON COLUMN FILE_DOCUMENT.FILE_NAME_ORIGINAL IS 'Original file name as uploaded by the user.';
COMMENT ON COLUMN FILE_DOCUMENT.MIME_TYPE IS 'MIME type detected server-side from file content — never trusts client Content-Type header — RULE-FILE-005.';
COMMENT ON COLUMN FILE_DOCUMENT.FILE_SIZE_BYTES IS 'File content size in bytes, computed at upload time.';
COMMENT ON COLUMN FILE_DOCUMENT.FILE_CONTENT IS 'Binary file content (BYTEA) — RESOLUTION-01, extends CORE-8. Purged (set to NULL at app layer) on permanent delete while the row is retained — see FILE_STATUS_ID.';
COMMENT ON COLUMN FILE_DOCUMENT.FILE_STATUS_ID IS 'Status Lifecycle (3 states: ACTIVE/ARCHIVED/DELETED) — LOV-FILE-002 (lookupKey: FILE_STATUS). DELETED = content purged, metadata + audit trail retained, so any consumer HARD-FK stays valid — resolves OQ-001.';
COMMENT ON COLUMN FILE_DOCUMENT.CREATED_BY IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN FILE_DOCUMENT.CREATED_AT IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN FILE_DOCUMENT.UPDATED_BY IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN FILE_DOCUMENT.UPDATED_AT IS 'Audit — populated by AuditEntityListener.';

-- ============================================================
-- BLOCK 5: CONSTRAINTS
-- ============================================================

-- 5a: Primary Keys
ALTER TABLE FILE_CATEGORY ADD CONSTRAINT PK_FILE_CATEGORY PRIMARY KEY (FILE_CATEGORY_PK);
ALTER TABLE FILE_DOCUMENT ADD CONSTRAINT PK_FILE_DOCUMENT PRIMARY KEY (FILE_DOCUMENT_PK);

-- 5b: Unique Constraints
ALTER TABLE FILE_CATEGORY ADD CONSTRAINT UQ_FILE_CATEGORY_MODULE_CODE UNIQUE (MODULE_CODE, CATEGORY_CODE);

-- 5c: Check Constraints
-- (none — FILE_TYPE_ID / FILE_STATUS_ID are lookup-governed via MD_LOOKUP_DETAIL,
--  not fixed DB-level CHECK sets, consistent with DBS-ORG-001 precedent)

-- 5d: Intra-Module Foreign Keys
ALTER TABLE FILE_DOCUMENT ADD CONSTRAINT FK_FILE_DOCUMENT_FILE_CATEGORY
  FOREIGN KEY (FILE_CATEGORY_FK) REFERENCES FILE_CATEGORY (FILE_CATEGORY_PK);

-- ============================================================
-- BLOCK 6: TRIGGERS
-- (Omitted — SRS does not require audit triggers; AuditEntityListener
--  populates audit columns at the application layer. No auto-PK triggers.)
-- ============================================================

-- ============================================================
-- BLOCK 7: INDEXES
-- ============================================================
CREATE INDEX IDX_FILE_DOCUMENT_FILE_CATEGORY_FK ON FILE_DOCUMENT (FILE_CATEGORY_FK);
CREATE INDEX IDX_FILE_DOCUMENT_OWNER ON FILE_DOCUMENT (OWNER_ID, OWNER_TYPE);

-- ============================================================
-- BLOCK 8: LOOKUP SEED DATA
-- (Only seed INSERTs — MD_MASTER_LOOKUP and MD_LOOKUP_DETAIL are shared
--  system tables, owned by MasterData, never recreated here.
--  Idempotent — safe on redeployment.)
-- ============================================================

-- LOV-FILE-001 — FILE_TYPE
INSERT INTO MD_MASTER_LOOKUP (ID_PK, LOOKUP_KEY, LOOKUP_NAME, LOOKUP_NAME_EN, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_MASTER_LOOKUP_SEQ'), 'FILE_TYPE', 'نوع الملف', 'File Type', 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'FILE_TYPE');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'FILE_TYPE'), 'IMAGE', 'صورة', 'Image', 1, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'FILE_TYPE' AND d.CODE = 'IMAGE');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'FILE_TYPE'), 'DOCUMENT', 'مستند', 'Document', 2, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'FILE_TYPE' AND d.CODE = 'DOCUMENT');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'FILE_TYPE'), 'SPREADSHEET', 'جدول بيانات', 'Spreadsheet', 3, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'FILE_TYPE' AND d.CODE = 'SPREADSHEET');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'FILE_TYPE'), 'ARCHIVE', 'أرشيف مضغوط', 'Archive', 4, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'FILE_TYPE' AND d.CODE = 'ARCHIVE');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'FILE_TYPE'), 'OTHER', 'أخرى', 'Other', 5, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'FILE_TYPE' AND d.CODE = 'OTHER');

-- LOV-FILE-002 — FILE_STATUS
INSERT INTO MD_MASTER_LOOKUP (ID_PK, LOOKUP_KEY, LOOKUP_NAME, LOOKUP_NAME_EN, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_MASTER_LOOKUP_SEQ'), 'FILE_STATUS', 'حالة الملف', 'File Status', 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'FILE_STATUS');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'FILE_STATUS'), 'ACTIVE', 'نشط', 'Active', 1, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'FILE_STATUS' AND d.CODE = 'ACTIVE');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'FILE_STATUS'), 'ARCHIVED', 'مؤرشف', 'Archived', 2, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'FILE_STATUS' AND d.CODE = 'ARCHIVED');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'FILE_STATUS'), 'DELETED', 'محذوف', 'Deleted', 3, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'FILE_STATUS' AND d.CODE = 'DELETED');

COMMIT;

-- ============================================================
-- BLOCK 9: VIEWS
-- (none required by SRS)
-- ============================================================

-- ============================================================
-- BLOCK 10: FUNCTIONS AND PROCEDURES
-- (none required by SRS)
-- ============================================================

-- ============================================================
-- BLOCK 11: DEFERRED FK PATCH BLOCKS
-- (none — File Service has zero outbound XM-IDs; see Section 3, XM Register)
-- ============================================================

-- ============================================================
-- SECURITY SEED — SEC_PAGES + PERMISSIONS (SCR-FILE-001)
-- SEC_PAGES / PERMISSIONS are Security's PERMANENT EXCEPTION tables
-- (master-registry.md Section 4) — used AS-IS, never recreated here.
-- PERMISSIONS_PK is GENERATED ALWAYS AS IDENTITY — no manual PK value
-- inserted. SEC_PAGES_PK uses SEC_PAGES_SEQ (owned by Security).
-- Idempotent — safe on redeployment.
-- ============================================================

INSERT INTO SEC_PAGES (SEC_PAGES_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, MODULE, PARENT_ID_FK, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('SEC_PAGES_SEQ'), 'FILE_ATTACHMENT', 'لوحة إدارة المرفقات', 'Attachment Panel', '/shared/file-attachment', 'FILE', NULL, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM SEC_PAGES WHERE PAGE_CODE = 'FILE_ATTACHMENT');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_FILE_ATTACHMENT_VIEW', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'FILE_ATTACHMENT'), 'VIEW', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_FILE_ATTACHMENT_VIEW');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_FILE_ATTACHMENT_CREATE', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'FILE_ATTACHMENT'), 'CREATE', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_FILE_ATTACHMENT_CREATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_FILE_ATTACHMENT_UPDATE', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'FILE_ATTACHMENT'), 'UPDATE', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_FILE_ATTACHMENT_UPDATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_FILE_ATTACHMENT_DELETE', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'FILE_ATTACHMENT'), 'DELETE', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_FILE_ATTACHMENT_DELETE');

COMMIT;
```

---

## 5. DB REGISTRY UPDATE

```
## REGISTRY UPDATE — 2026-07-11
────────────────────────────────────────────────────────────────
Source Mode    : MODE 1.5
Feature Code   : FILE-001
DBS-ID         : DBS-FILE-001
Plan ID        : —
────────────────────────────────────────────────────────────────
New Entities   : —
New Tables     : FILE_CATEGORY, FILE_DOCUMENT
New Lookups    : FILE_TYPE (5 values), FILE_STATUS (3 values) — seeded
XM-IDs Open    : None (0 outbound)
OQ-IDs Open    : None
Gate Status    : PASSED ✓
Next Action    : Trigger MODE 2 — Execution Plan Governance Engine (FileService)
────────────────────────────────────────────────────────────────
```

**Registry cascade rule (SHARED-GOVERNANCE-RULES.md Section 6, REG-3):**
This DBS-ID confirmation is the trigger event for `XM-NOTIF-001`
(DEFERRED, target module = File Service — see dbs-notif-001.md). Per
CONTRACT-8 and module-registry-file.md's own INBOUND XM CANDIDATES
declaration, the **Registry Maintainer** — not this engine — now
evaluates the Global XM Dependency Index for all DEFERRED rows where
`To Module = FileService` and creates `RXE-NOTIF-[SEQ]` if conditions
are met. This DB script does not perform that evaluation or flip
Notification's XM status; that remains the Registry Maintainer's
action, followed by Notification Service's own P3 migration work
(POLICY-CLI-07 / AD-NOTIF-11), per the explicit Phase-1 design in
srs-notif-001.md.

---
*End of dbs-file-001.md*
*Governed by: Database Governance Engine (Project 2)*
*DBS-ID: DBS-FILE-001 | DB_TARGET: POSTGRESQL_16*
*Next Mode: MODE 2 — Execution Plan Governance Engine (Project 3)*
