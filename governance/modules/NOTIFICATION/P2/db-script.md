<!-- ═══════════════════════════════════════════════════════════ -->
<!-- DB SCRIPT — قاعدة البيانات                                 -->
<!-- Governed by: Database Governance Engine (Project 2)        -->
<!-- MODE 1.5 — Structural Truth (Layer 2)                      -->
<!-- ═══════════════════════════════════════════════════════════ -->

# DB SCRIPT — Notification Service
## DBS-ID: DBS-NOTIF-001

---

## 1. DB SCRIPT HEADER

```
DBS-ID          : DBS-NOTIF-001
Module          : Notification Service (NOTIF)
SRS Feature Code: NOTIF-001 (srs-notif-001.md, v1.0)
DB_TARGET       : POSTGRESQL_16
Date            : 2026-07-11
Status          : GOVERNED ✓ MODE 1.5
Open Questions  : None — see OQ Log
Tables          : 3  (NOTIF_LOG, NOTIF_TEMPLATE, NOTIF_CHANNEL_CONFIG)
DBF-IDs         : 38
XM-IDs (outbound): 1 — XM-NOTIF-001 → FILE_DOCUMENT (File Service), DEFERRED
```

MODE 1.5 ENTRY GATE — confirmed prior to generation:

```
╔══════════════════════════════════════════════════════════════════╗
║          MODE 1.5 — DB ENTRY GATE — NOTIFICATION SERVICE         ║
╠══════════════════════════════════╦═══════════════════════════════╣
║ srs.md attached?                 ║ Yes — srs-notif-001.md         ║
║ SRS gate passed (MODE 1)?        ║ ✓ PASSED (2026-07-11)          ║
║ moduleRegistry.md loaded?        ║ ✓ module-registry-notif.md     ║
║ master-registry.md loaded?       ║ ✓                              ║
║ Existing db-script.md?           ║ No — fresh generation          ║
║ DB_TARGET declared?              ║ POSTGRESQL_16                  ║
╠══════════════════════════════════╩═══════════════════════════════╣
║ Extracted: 3 entities → 3 tables, 0 intra-module FK,              ║
║            1 outbound XM-ID (DEFERRED), 1 live cross-module FK    ║
║            to Security (PERMANENT EXCEPTION — no XM-ID)           ║
╠══════════════════════════════════════════════════════════════════╣
║ PROCEED: Yes                                                      ║
╚══════════════════════════════════════════════════════════════════╝
```

---

## 2. DB FIELD TRACEABILITY MATRIX — Notification Service — DBS-ID: DBS-NOTIF-001

══════════════════════════════════════════════════════════════════════════════════════
| DBF-ID   | Table Name           | Column Name                   | DB Type       | SRS Source |
|----------|----------------------|--------------------------------|---------------|------------|
| DBF-0001 | NOTIF_LOG            | NOTIFICATION_LOG_PK            | BIGINT        | ENTITY-NOTIF-001.notificationLogPk |
| DBF-0002 | NOTIF_LOG            | RECIPIENT_ID                   | BIGINT        | ENTITY-NOTIF-001.recipientId → Security USERS_PK (EXCEPTION) |
| DBF-0003 | NOTIF_LOG            | NOTIFICATION_TYPE_ID           | VARCHAR(20)   | ENTITY-NOTIF-001.notificationTypeId (LOV-NOTIF-001) |
| DBF-0004 | NOTIF_LOG            | TEMPLATE_CODE                  | VARCHAR(50)   | ENTITY-NOTIF-001.templateCode (natural-key ref — no FK) |
| DBF-0005 | NOTIF_LOG            | SUBJECT                        | VARCHAR(500)  | ENTITY-NOTIF-001.subject |
| DBF-0006 | NOTIF_LOG            | BODY_PREVIEW                   | VARCHAR(1000) | ENTITY-NOTIF-001.bodyPreview |
| DBF-0007 | NOTIF_LOG            | NOTIFICATION_STATUS_ID         | VARCHAR(20)   | ENTITY-NOTIF-001.notificationStatusId (LOV-NOTIF-002) |
| DBF-0008 | NOTIF_LOG            | RETRY_COUNT                    | SMALLINT      | ENTITY-NOTIF-001.retryCount (see governance note below) |
| DBF-0009 | NOTIF_LOG            | SENT_AT                        | TIMESTAMP     | ENTITY-NOTIF-001.sentAt |
| DBF-0010 | NOTIF_LOG            | MODULE_CODE                    | VARCHAR(20)   | ENTITY-NOTIF-001.moduleCode |
| DBF-0011 | NOTIF_LOG            | REFERENCE_ID                   | BIGINT        | ENTITY-NOTIF-001.referenceId (polymorphic — no FK) |
| DBF-0012 | NOTIF_LOG            | REFERENCE_TYPE                 | VARCHAR(50)   | ENTITY-NOTIF-001.referenceType |
| DBF-0013 | NOTIF_LOG            | CREATED_BY                     | VARCHAR(255)  | ENTITY-NOTIF-001.createdBy |
| DBF-0014 | NOTIF_LOG            | CREATED_AT                     | TIMESTAMP     | ENTITY-NOTIF-001.createdAt |
| DBF-0015 | NOTIF_LOG            | UPDATED_BY                     | VARCHAR(255)  | ENTITY-NOTIF-001.updatedBy |
| DBF-0016 | NOTIF_LOG            | UPDATED_AT                     | TIMESTAMP     | ENTITY-NOTIF-001.updatedAt |
| DBF-0017 | NOTIF_TEMPLATE       | NOTIFICATION_TEMPLATE_PK       | BIGINT        | ENTITY-NOTIF-002.notificationTemplatePk |
| DBF-0018 | NOTIF_TEMPLATE       | TEMPLATE_CODE                  | VARCHAR(50)   | ENTITY-NOTIF-002.templateCode |
| DBF-0019 | NOTIF_TEMPLATE       | TEMPLATE_NAME_AR               | VARCHAR(200)  | ENTITY-NOTIF-002.templateNameAr |
| DBF-0020 | NOTIF_TEMPLATE       | TEMPLATE_NAME_EN               | VARCHAR(200)  | ENTITY-NOTIF-002.templateNameEn |
| DBF-0021 | NOTIF_TEMPLATE       | CHANNEL_TYPE_ID                | VARCHAR(20)   | ENTITY-NOTIF-002.channelTypeId (LOV-NOTIF-001) |
| DBF-0022 | NOTIF_TEMPLATE       | MODULE_CODE                    | VARCHAR(20)   | ENTITY-NOTIF-002.moduleCode |
| DBF-0023 | NOTIF_TEMPLATE       | TEMPLATE_BODY_AR               | TEXT          | ENTITY-NOTIF-002.templateBodyAr |
| DBF-0024 | NOTIF_TEMPLATE       | TEMPLATE_BODY_EN               | TEXT          | ENTITY-NOTIF-002.templateBodyEn |
| DBF-0025 | NOTIF_TEMPLATE       | FILE_FK                        | BIGINT        | ENTITY-NOTIF-002.fileFk → ENTITY-FILE-001 (XM-NOTIF-001, DEFERRED) |
| DBF-0026 | NOTIF_TEMPLATE       | IS_ACTIVE_FL                   | SMALLINT      | ENTITY-NOTIF-002.isActiveFl |
| DBF-0027 | NOTIF_TEMPLATE       | CREATED_BY                     | VARCHAR(255)  | ENTITY-NOTIF-002.createdBy |
| DBF-0028 | NOTIF_TEMPLATE       | CREATED_AT                     | TIMESTAMP     | ENTITY-NOTIF-002.createdAt |
| DBF-0029 | NOTIF_TEMPLATE       | UPDATED_BY                     | VARCHAR(255)  | ENTITY-NOTIF-002.updatedBy |
| DBF-0030 | NOTIF_TEMPLATE       | UPDATED_AT                     | TIMESTAMP     | ENTITY-NOTIF-002.updatedAt |
| DBF-0031 | NOTIF_CHANNEL_CONFIG | NOTIFICATION_CHANNEL_CONFIG_PK | BIGINT        | ENTITY-NOTIF-003.notificationChannelConfigPk |
| DBF-0032 | NOTIF_CHANNEL_CONFIG | CHANNEL_TYPE_ID                | VARCHAR(20)   | ENTITY-NOTIF-003.channelTypeId (LOV-NOTIF-001) |
| DBF-0033 | NOTIF_CHANNEL_CONFIG | IS_ENABLED_FL                  | SMALLINT      | ENTITY-NOTIF-003.isEnabledFl |
| DBF-0034 | NOTIF_CHANNEL_CONFIG | CONFIG_JSON                    | TEXT          | ENTITY-NOTIF-003.configJson |
| DBF-0035 | NOTIF_CHANNEL_CONFIG | CREATED_BY                     | VARCHAR(255)  | ENTITY-NOTIF-003.createdBy |
| DBF-0036 | NOTIF_CHANNEL_CONFIG | CREATED_AT                     | TIMESTAMP     | ENTITY-NOTIF-003.createdAt |
| DBF-0037 | NOTIF_CHANNEL_CONFIG | UPDATED_BY                     | VARCHAR(255)  | ENTITY-NOTIF-003.updatedBy |
| DBF-0038 | NOTIF_CHANNEL_CONFIG | UPDATED_AT                     | TIMESTAMP     | ENTITY-NOTIF-003.updatedAt |
══════════════════════════════════════════════════════════════════════════════════════

Total: 38 DBF-IDs across 3 tables.

Governance notes:
- **DBF-0004 (`TEMPLATE_CODE` on NOTIF_LOG)**: SRS explicitly declares this a
  natural-key logical reference, not a numeric FK ("ليس FK رقمي — لا يُسمَّى
  templateFk", srs-notif-001.md A3). RULE-NOTIF-006 also requires a missing
  `templateCode` to fall back to a default template rather than fail the
  send — a hard DB-level FK would contradict that graceful-fallback design.
  No FK constraint is created; this is a documented deviation per Section
  4.3 governance-note requirement.
- **DBF-0008 (`RETRY_COUNT`)**: SRS field type is generic "NUMERIC". Mapped
  to `SMALLINT` (CORE-8 "short codes/flags" mapping) since the governed
  ceiling is 5 retries (RULE-NOTIF-004) — governance note per Section 4.3.
- **DBF-0002 (`RECIPIENT_ID`)**: References Security's actual `USERS_PK`
  column (PERMANENT EXCEPTION, master-registry.md Section 4) — not a
  standard `usersFk` name. Live FK created in Block 5d (cross-module,
  Security AS-IS — see XM Register note below).

---

## 3. CROSS-MODULE DEPENDENCY REGISTER (XM REGISTER) — Notification Service — DBS-ID: DBS-NOTIF-001

══════════════════════════════════════════════════════════════════════════════════
| XM-ID        | Type    | This Table     | FK/Ref Column | Target Table  | Target Module | Status   |
|--------------|---------|----------------|----------------|---------------|----------------|----------|
| XM-NOTIF-001 | HARD-FK | NOTIF_TEMPLATE | FILE_FK        | FILE_DOCUMENT | File Service   | DEFERRED |
══════════════════════════════════════════════════════════════════════════════════

Total: 1 XM-ID.

**XM-NOTIF-001 detail:**
```
Type              : HARD-FK
Column             : NOTIF_TEMPLATE.FILE_FK (NULLABLE, DBF-0025)
Target             : FILE_DOCUMENT.FILE_DOCUMENT_PK (File Service, DBS-FILE-001)
Status              : DEFERRED — explicit Phase-1 design decision, not merely
                      "target not yet gated". srs-notif-001.md A3/A7,
                      module-registry-notif.md AD-NOTIF-05/AD-NOTIF-11, and
                      business-policies-notif.md POLICY-CLI-07 all specify
                      template bodies stay inline (TEMPLATE_BODY_AR/EN) in
                      Phase 1 regardless of File Service's own gating status.
Unblock condition  : RXE-NOTIF-[SEQ] per SHARED-ARTIFACT-CONTRACTS.md
                      CONTRACT-8 — fired by the Registry Maintainer when
                      DBS-FILE-001 is confirmed (this session, see
                      dbs-file-001.md Section 5). Receipt of the RXE triggers
                      Notification's own P3 execution-phase migration
                      (reading inline bodies → creating FILE_DOCUMENT rows →
                      populating FILE_FK) — not an automatic action of this
                      DB script.
Resilience          : TEMPLATE_BODY_AR/EN are retained permanently post-
                      migration as a fallback if File Service is transiently
                      unavailable (AD-NOTIF-11).
```

**Security dependency — not an XM-ID:**
`NOTIF_LOG.RECIPIENT_ID → USERS.USERS_PK` is a live, immediately-created
cross-module FK to Security's `USERS` table. Security is a **PERMANENT
EXCEPTION** module (pre-existing, used AS-IS — master-registry.md Section
4) — srs-notif-001.md A7 explicitly states no formal XM-ID is assigned
for dependencies on EXCEPTION-status modules ("لا XM رسمي يُصاغ لموديول
EXCEPTION"). This overrides the informal `XM-NOTIF-[TBD] → Security USERS`
placeholder in module-registry-notif.md's earlier P0 draft — SRS is Layer-1
Functional Truth and governs (CORE-1). The FK is created directly in
Block 5d below, no deferred patch, no XM Register row.

---

## 4. FULL_DATABASE_SCRIPT

> Complete executable script for Notification Service — DBS-ID: DBS-NOTIF-001
> DB_TARGET: POSTGRESQL_16 | Schema: no schema prefix (consistent with DBS-ORG-001)
> Generated: 2026-07-11 | SRS Feature Code: NOTIF-001
> Run in psql or pgAdmin against a clean schema. No manual editing required.
> Prerequisite: Security's USERS table must already exist in the target schema
> (PERMANENT EXCEPTION module, deployed independently of this pipeline).

```sql
-- ============================================================
-- NOTIFICATION SERVICE — COMPLETE DATABASE SCRIPT
-- DBS-ID     : DBS-NOTIF-001
-- SRS Code   : NOTIF-001
-- DB_TARGET  : POSTGRESQL_16
-- Generated  : 2026-07-11
-- ============================================================

-- ============================================================
-- BLOCK 1: SEQUENCES
-- ============================================================
CREATE SEQUENCE SEQ_NOTIF_LOG
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

CREATE SEQUENCE SEQ_NOTIF_TEMPLATE
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

CREATE SEQUENCE SEQ_NOTIF_CHANNEL_CONFIG
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

-- ============================================================
-- BLOCK 2: PARENT TABLES
-- (No intra-module FK dependencies exist among this module's three
--  tables — TEMPLATE_CODE on NOTIF_LOG is a natural-key reference, not
--  a physical FK — see Section 2 governance note. All three tables are
--  created here.)
-- ============================================================
CREATE TABLE NOTIF_LOG (
  NOTIFICATION_LOG_PK     BIGINT          NOT NULL,
  RECIPIENT_ID             BIGINT          NOT NULL,
  NOTIFICATION_TYPE_ID      VARCHAR(20)     NOT NULL,
  TEMPLATE_CODE             VARCHAR(50)     NOT NULL,
  SUBJECT                   VARCHAR(500),
  BODY_PREVIEW              VARCHAR(1000),
  NOTIFICATION_STATUS_ID     VARCHAR(20)     NOT NULL,
  RETRY_COUNT                SMALLINT        NOT NULL DEFAULT 0,
  SENT_AT                    TIMESTAMP,
  MODULE_CODE                 VARCHAR(20)     NOT NULL,
  REFERENCE_ID                 BIGINT,
  REFERENCE_TYPE                VARCHAR(50),
  CREATED_BY                    VARCHAR(255)    NOT NULL,
  CREATED_AT                    TIMESTAMP       NOT NULL,
  UPDATED_BY                     VARCHAR(255),
  UPDATED_AT                     TIMESTAMP
);

CREATE TABLE NOTIF_TEMPLATE (
  NOTIFICATION_TEMPLATE_PK   BIGINT          NOT NULL,
  TEMPLATE_CODE                VARCHAR(50)     NOT NULL,
  TEMPLATE_NAME_AR              VARCHAR(200)    NOT NULL,
  TEMPLATE_NAME_EN               VARCHAR(200)    NOT NULL,
  CHANNEL_TYPE_ID                 VARCHAR(20)     NOT NULL,
  MODULE_CODE                      VARCHAR(20)     NOT NULL,
  TEMPLATE_BODY_AR                  TEXT            NOT NULL,
  TEMPLATE_BODY_EN                   TEXT            NOT NULL,
  FILE_FK                             BIGINT,
  IS_ACTIVE_FL                         SMALLINT        NOT NULL DEFAULT 1,
  CREATED_BY                            VARCHAR(255)    NOT NULL,
  CREATED_AT                             TIMESTAMP       NOT NULL,
  UPDATED_BY                              VARCHAR(255),
  UPDATED_AT                               TIMESTAMP
);

CREATE TABLE NOTIF_CHANNEL_CONFIG (
  NOTIFICATION_CHANNEL_CONFIG_PK  BIGINT          NOT NULL,
  CHANNEL_TYPE_ID                   VARCHAR(20)     NOT NULL,
  IS_ENABLED_FL                      SMALLINT        NOT NULL DEFAULT 1,
  CONFIG_JSON                         TEXT,
  CREATED_BY                           VARCHAR(255)    NOT NULL,
  CREATED_AT                            TIMESTAMP       NOT NULL,
  UPDATED_BY                             VARCHAR(255),
  UPDATED_AT                              TIMESTAMP
);

-- ============================================================
-- BLOCK 3: CHILD TABLES
-- (none — no intra-module FK dependency chain in this module)
-- ============================================================

-- ============================================================
-- BLOCK 4: COMMENTS
-- ============================================================
COMMENT ON TABLE NOTIF_LOG IS 'SHARED (owner) — append-only delivery log, one row per (event, channel) fan-out. ENTITY-NOTIF-001. No Update/Delete — status transitions only.';
COMMENT ON COLUMN NOTIF_LOG.NOTIFICATION_LOG_PK IS 'Primary key — auto-generated, PK population handled by application framework.';
COMMENT ON COLUMN NOTIF_LOG.RECIPIENT_ID IS 'FK → Security USERS.USERS_PK (PERMANENT EXCEPTION column name) — not usersFk.';
COMMENT ON COLUMN NOTIF_LOG.NOTIFICATION_TYPE_ID IS 'Channel used for this row — LOV-NOTIF-001 (lookupKey: NOTIFICATION_CHANNEL). One independent row per requested channel — RULE-NOTIF-003.';
COMMENT ON COLUMN NOTIF_LOG.TEMPLATE_CODE IS 'Natural-key logical reference to NOTIF_TEMPLATE.TEMPLATE_CODE — no physical FK (graceful fallback per RULE-NOTIF-006).';
COMMENT ON COLUMN NOTIF_LOG.SUBJECT IS 'Notification subject (primarily Email channel).';
COMMENT ON COLUMN NOTIF_LOG.BODY_PREVIEW IS 'Short preview of the sent content.';
COMMENT ON COLUMN NOTIF_LOG.NOTIFICATION_STATUS_ID IS 'Status Lifecycle (4 states: PENDING/SENT/FAILED/CHANNEL_DISABLED) — LOV-NOTIF-002 (lookupKey: NOTIFICATION_STATUS).';
COMMENT ON COLUMN NOTIF_LOG.RETRY_COUNT IS 'Delivery retry attempts, default 0, ceiling 5 — RULE-NOTIF-004. SMALLINT per governance note (Section 2).';
COMMENT ON COLUMN NOTIF_LOG.SENT_AT IS 'Actual send timestamp — null until sent.';
COMMENT ON COLUMN NOTIF_LOG.MODULE_CODE IS 'Publishing module code.';
COMMENT ON COLUMN NOTIF_LOG.REFERENCE_ID IS 'Polymorphic reference to the related business record — no physical FK; same pattern as FILE_DOCUMENT.OWNER_ID.';
COMMENT ON COLUMN NOTIF_LOG.REFERENCE_TYPE IS 'Related entity type name from the publishing module — free text, not a governed lookup.';
COMMENT ON COLUMN NOTIF_LOG.CREATED_BY IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN NOTIF_LOG.CREATED_AT IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN NOTIF_LOG.UPDATED_BY IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN NOTIF_LOG.UPDATED_AT IS 'Audit — populated by AuditEntityListener.';

COMMENT ON TABLE NOTIF_TEMPLATE IS 'PRIVATE — bilingual notification templates. ENTITY-NOTIF-002. Consumes SHARED ENTITY-FILE-001 (DEFERRED — XM-NOTIF-001).';
COMMENT ON COLUMN NOTIF_TEMPLATE.NOTIFICATION_TEMPLATE_PK IS 'Primary key — auto-generated, PK population handled by application framework.';
COMMENT ON COLUMN NOTIF_TEMPLATE.TEMPLATE_CODE IS 'Unique template code, immutable after creation — RULE-NOTIF-007.';
COMMENT ON COLUMN NOTIF_TEMPLATE.TEMPLATE_NAME_AR IS 'Template display name — Arabic.';
COMMENT ON COLUMN NOTIF_TEMPLATE.TEMPLATE_NAME_EN IS 'Template display name — English.';
COMMENT ON COLUMN NOTIF_TEMPLATE.CHANNEL_TYPE_ID IS 'Target channel for this template — LOV-NOTIF-001 (lookupKey: NOTIFICATION_CHANNEL).';
COMMENT ON COLUMN NOTIF_TEMPLATE.MODULE_CODE IS 'Owning module code for this template.';
COMMENT ON COLUMN NOTIF_TEMPLATE.TEMPLATE_BODY_AR IS 'Template body, Arabic — Phase-1 inline storage (RESOLUTION-02). Supports placeholders. Retained permanently as fallback after File Service migration.';
COMMENT ON COLUMN NOTIF_TEMPLATE.TEMPLATE_BODY_EN IS 'Template body, English — Phase-1 inline storage (RESOLUTION-02). Retained permanently as fallback after File Service migration.';
COMMENT ON COLUMN NOTIF_TEMPLATE.FILE_FK IS 'DEFERRED FK to FILE_DOCUMENT (File Service) — XM-NOTIF-001. NULLABLE, unused in Phase 1 — activated on RXE-NOTIF receipt without changing TEMPLATE_CODE.';
COMMENT ON COLUMN NOTIF_TEMPLATE.IS_ACTIVE_FL IS 'Active flag — 1 = active, 0 = inactive.';
COMMENT ON COLUMN NOTIF_TEMPLATE.CREATED_BY IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN NOTIF_TEMPLATE.CREATED_AT IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN NOTIF_TEMPLATE.UPDATED_BY IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN NOTIF_TEMPLATE.UPDATED_AT IS 'Audit — populated by AuditEntityListener.';

COMMENT ON TABLE NOTIF_CHANNEL_CONFIG IS 'PRIVATE (Configuration) — one fixed row per channel (5 seed rows), toggled by Admin. ENTITY-NOTIF-003. No Create/Delete from the user.';
COMMENT ON COLUMN NOTIF_CHANNEL_CONFIG.NOTIFICATION_CHANNEL_CONFIG_PK IS 'Primary key — auto-generated, PK population handled by application framework.';
COMMENT ON COLUMN NOTIF_CHANNEL_CONFIG.CHANNEL_TYPE_ID IS 'Channel this config row governs — LOV-NOTIF-001 (lookupKey: NOTIFICATION_CHANNEL). Unique — one row per channel.';
COMMENT ON COLUMN NOTIF_CHANNEL_CONFIG.IS_ENABLED_FL IS 'Whether this channel is currently enabled — RULE-NOTIF-005. Default 1 for all 5 channels, Phase 1 — no channel deferred.';
COMMENT ON COLUMN NOTIF_CHANNEL_CONFIG.CONFIG_JSON IS 'Provider-specific adapter configuration (e.g. SMS/WhatsApp provider credentials, AQ-010/AQ-011) — free text, interpreted by the application.';
COMMENT ON COLUMN NOTIF_CHANNEL_CONFIG.CREATED_BY IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN NOTIF_CHANNEL_CONFIG.CREATED_AT IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN NOTIF_CHANNEL_CONFIG.UPDATED_BY IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN NOTIF_CHANNEL_CONFIG.UPDATED_AT IS 'Audit — populated by AuditEntityListener.';

-- ============================================================
-- BLOCK 5: CONSTRAINTS
-- ============================================================

-- 5a: Primary Keys
ALTER TABLE NOTIF_LOG ADD CONSTRAINT PK_NOTIF_LOG PRIMARY KEY (NOTIFICATION_LOG_PK);
ALTER TABLE NOTIF_TEMPLATE ADD CONSTRAINT PK_NOTIF_TEMPLATE PRIMARY KEY (NOTIFICATION_TEMPLATE_PK);
ALTER TABLE NOTIF_CHANNEL_CONFIG ADD CONSTRAINT PK_NOTIF_CHANNEL_CONFIG PRIMARY KEY (NOTIFICATION_CHANNEL_CONFIG_PK);

-- 5b: Unique Constraints
ALTER TABLE NOTIF_TEMPLATE ADD CONSTRAINT UQ_NOTIF_TEMPLATE_CODE UNIQUE (TEMPLATE_CODE);
ALTER TABLE NOTIF_CHANNEL_CONFIG ADD CONSTRAINT UQ_NOTIF_CHANNEL_CONFIG_TYPE UNIQUE (CHANNEL_TYPE_ID);

-- 5c: Check Constraints
-- (none — NOTIFICATION_TYPE_ID / NOTIFICATION_STATUS_ID / CHANNEL_TYPE_ID
--  are lookup-governed via MD_LOOKUP_DETAIL, not fixed DB-level CHECK sets,
--  consistent with DBS-ORG-001 precedent)

-- 5d: Cross-Module Foreign Keys (live — Security PERMANENT EXCEPTION,
--     see Section 3 note; NOT an intra-module FK, but created live since
--     Security's USERS table is a pre-existing AS-IS dependency, not
--     subject to this pipeline's DBS-ID gating)
ALTER TABLE NOTIF_LOG ADD CONSTRAINT FK_NOTIF_LOG_USERS
  FOREIGN KEY (RECIPIENT_ID) REFERENCES USERS (USERS_PK);

-- (No intra-module FKs — TEMPLATE_CODE on NOTIF_LOG is a natural-key
--  reference, not a physical FK — see Section 2 governance note)

-- ============================================================
-- BLOCK 6: TRIGGERS
-- (Omitted — SRS does not require audit triggers; AuditEntityListener
--  populates audit columns at the application layer. No auto-PK triggers.)
-- ============================================================

-- ============================================================
-- BLOCK 7: INDEXES
-- ============================================================
CREATE INDEX IDX_NOTIF_LOG_RECIPIENT ON NOTIF_LOG (RECIPIENT_ID);
CREATE INDEX IDX_NOTIF_LOG_STATUS ON NOTIF_LOG (NOTIFICATION_STATUS_ID);
CREATE INDEX IDX_NOTIF_LOG_TYPE ON NOTIF_LOG (NOTIFICATION_TYPE_ID);

CREATE INDEX IDX_NOTIF_TEMPLATE_FILE_FK ON NOTIF_TEMPLATE (FILE_FK);
CREATE INDEX IDX_NOTIF_TEMPLATE_CHANNEL ON NOTIF_TEMPLATE (CHANNEL_TYPE_ID);
CREATE INDEX IDX_NOTIF_TEMPLATE_MODULE ON NOTIF_TEMPLATE (MODULE_CODE);

-- (NOTIF_CHANNEL_CONFIG.CHANNEL_TYPE_ID already indexed via UQ_NOTIF_CHANNEL_CONFIG_TYPE)

-- ============================================================
-- BLOCK 8: LOOKUP SEED DATA + MODULE SEED DATA
-- (MD_MASTER_LOOKUP / MD_LOOKUP_DETAIL are shared system tables, owned by
--  MasterData, never recreated here. NOTIF_CHANNEL_CONFIG below is this
--  module's own operational seed data, not a lookup. Both idempotent.)
-- ============================================================

-- LOV-NOTIF-001 — NOTIFICATION_CHANNEL
INSERT INTO MD_MASTER_LOOKUP (ID_PK, LOOKUP_KEY, LOOKUP_NAME, LOOKUP_NAME_EN, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_MASTER_LOOKUP_SEQ'), 'NOTIFICATION_CHANNEL', 'قناة الإشعار', 'Notification Channel', 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'NOTIFICATION_CHANNEL');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'NOTIFICATION_CHANNEL'), 'EMAIL', 'بريد إلكتروني', 'Email', 1, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'NOTIFICATION_CHANNEL' AND d.CODE = 'EMAIL');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'NOTIFICATION_CHANNEL'), 'SMS', 'رسالة نصية', 'SMS', 2, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'NOTIFICATION_CHANNEL' AND d.CODE = 'SMS');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'NOTIFICATION_CHANNEL'), 'WHATSAPP', 'واتساب', 'WhatsApp', 3, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'NOTIFICATION_CHANNEL' AND d.CODE = 'WHATSAPP');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'NOTIFICATION_CHANNEL'), 'PUSH', 'إشعار فوري (تطبيق)', 'Push', 4, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'NOTIFICATION_CHANNEL' AND d.CODE = 'PUSH');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'NOTIFICATION_CHANNEL'), 'INTERNAL', 'إشعار داخلي', 'Internal', 5, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'NOTIFICATION_CHANNEL' AND d.CODE = 'INTERNAL');

-- LOV-NOTIF-002 — NOTIFICATION_STATUS
INSERT INTO MD_MASTER_LOOKUP (ID_PK, LOOKUP_KEY, LOOKUP_NAME, LOOKUP_NAME_EN, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_MASTER_LOOKUP_SEQ'), 'NOTIFICATION_STATUS', 'حالة الإشعار', 'Notification Status', 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'NOTIFICATION_STATUS');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'NOTIFICATION_STATUS'), 'PENDING', 'قيد الانتظار', 'Pending', 1, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'NOTIFICATION_STATUS' AND d.CODE = 'PENDING');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'NOTIFICATION_STATUS'), 'SENT', 'تم الإرسال', 'Sent', 2, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'NOTIFICATION_STATUS' AND d.CODE = 'SENT');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'NOTIFICATION_STATUS'), 'FAILED', 'فشل', 'Failed', 3, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'NOTIFICATION_STATUS' AND d.CODE = 'FAILED');

INSERT INTO MD_LOOKUP_DETAIL (ID_PK, MASTER_LOOKUP_ID_FK, CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'), (SELECT ID_PK FROM MD_MASTER_LOOKUP WHERE LOOKUP_KEY = 'NOTIFICATION_STATUS'), 'CHANNEL_DISABLED', 'القناة معطَّلة', 'Channel Disabled', 4, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL d JOIN MD_MASTER_LOOKUP m ON m.ID_PK = d.MASTER_LOOKUP_ID_FK WHERE m.LOOKUP_KEY = 'NOTIFICATION_STATUS' AND d.CODE = 'CHANNEL_DISABLED');

COMMIT;

-- ------------------------------------------------------------
-- NOTIF_CHANNEL_CONFIG SEED DATA (module's own table — not a lookup)
-- 5 fixed rows, one per channel — all enabled Phase 1 (no deferral,
-- final decision 2026-07-11, module-registry-notif.md AUTO-DECISIONS)
-- ------------------------------------------------------------
INSERT INTO NOTIF_CHANNEL_CONFIG (NOTIFICATION_CHANNEL_CONFIG_PK, CHANNEL_TYPE_ID, IS_ENABLED_FL, CREATED_BY, CREATED_AT)
SELECT nextval('SEQ_NOTIF_CHANNEL_CONFIG'), 'EMAIL', 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM NOTIF_CHANNEL_CONFIG WHERE CHANNEL_TYPE_ID = 'EMAIL');

INSERT INTO NOTIF_CHANNEL_CONFIG (NOTIFICATION_CHANNEL_CONFIG_PK, CHANNEL_TYPE_ID, IS_ENABLED_FL, CREATED_BY, CREATED_AT)
SELECT nextval('SEQ_NOTIF_CHANNEL_CONFIG'), 'SMS', 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM NOTIF_CHANNEL_CONFIG WHERE CHANNEL_TYPE_ID = 'SMS');

INSERT INTO NOTIF_CHANNEL_CONFIG (NOTIFICATION_CHANNEL_CONFIG_PK, CHANNEL_TYPE_ID, IS_ENABLED_FL, CREATED_BY, CREATED_AT)
SELECT nextval('SEQ_NOTIF_CHANNEL_CONFIG'), 'WHATSAPP', 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM NOTIF_CHANNEL_CONFIG WHERE CHANNEL_TYPE_ID = 'WHATSAPP');

INSERT INTO NOTIF_CHANNEL_CONFIG (NOTIFICATION_CHANNEL_CONFIG_PK, CHANNEL_TYPE_ID, IS_ENABLED_FL, CREATED_BY, CREATED_AT)
SELECT nextval('SEQ_NOTIF_CHANNEL_CONFIG'), 'PUSH', 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM NOTIF_CHANNEL_CONFIG WHERE CHANNEL_TYPE_ID = 'PUSH');

INSERT INTO NOTIF_CHANNEL_CONFIG (NOTIFICATION_CHANNEL_CONFIG_PK, CHANNEL_TYPE_ID, IS_ENABLED_FL, CREATED_BY, CREATED_AT)
SELECT nextval('SEQ_NOTIF_CHANNEL_CONFIG'), 'INTERNAL', 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM NOTIF_CHANNEL_CONFIG WHERE CHANNEL_TYPE_ID = 'INTERNAL');

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
-- BLOCK 11: DEFERRED FK PATCH BLOCKS (DO NOT UNCOMMENT UNTIL
--           FILE SERVICE'S MIGRATION IS TRIGGERED VIA RXE-NOTIF)
-- ============================================================
-- DEFERRED FK — XM-NOTIF-001
-- Target module : File Service
-- Apply when    : RXE-NOTIF-[SEQ] received (CONTRACT-8) AND Notification's
--                  own P3 migration (POLICY-CLI-07 / AD-NOTIF-11) has
--                  populated FILE_FK for the affected rows — this is a
--                  Phase-1 architectural deferral, not merely a "target
--                  not yet gated" deferral (see Section 3 detail above).
-- ALTER TABLE NOTIF_TEMPLATE
--   ADD CONSTRAINT FK_NOTIF_TEMPLATE_FILE_DOCUMENT
--     FOREIGN KEY (FILE_FK) REFERENCES FILE_DOCUMENT (FILE_DOCUMENT_PK);

-- ============================================================
-- SECURITY SEED — SEC_PAGES + PERMISSIONS
-- SEC_PAGES / PERMISSIONS are Security's PERMANENT EXCEPTION tables
-- (master-registry.md Section 4) — used AS-IS, never recreated here.
-- PERMISSIONS_PK is GENERATED ALWAYS AS IDENTITY — no manual PK value
-- inserted. SEC_PAGES_PK uses SEC_PAGES_SEQ (owned by Security).
-- Idempotent — safe on redeployment.
-- ============================================================

-- SCR-NOTIF-001 — لوحة إشعاراتي (Header component, no parent)
INSERT INTO SEC_PAGES (SEC_PAGES_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, MODULE, PARENT_ID_FK, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('SEC_PAGES_SEQ'), 'NOTIFICATION_INBOX', 'لوحة إشعاراتي', 'Notification Inbox', '/notifications/inbox', 'NOTIFICATION', NULL, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_INBOX');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_NOTIFICATION_INBOX_VIEW', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_INBOX'), 'VIEW', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_NOTIFICATION_INBOX_VIEW');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_NOTIFICATION_INBOX_CREATE', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_INBOX'), 'CREATE', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_NOTIFICATION_INBOX_CREATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_NOTIFICATION_INBOX_UPDATE', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_INBOX'), 'UPDATE', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_NOTIFICATION_INBOX_UPDATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_NOTIFICATION_INBOX_DELETE', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_INBOX'), 'DELETE', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_NOTIFICATION_INBOX_DELETE');

-- Parent settings page for Template/ChannelConfig (referenced as
-- [NOTIFICATION_SETTINGS] in srs-notif-001.md B4 — created here as the
-- structural parent so PARENT_ID_FK below is resolvable)
INSERT INTO SEC_PAGES (SEC_PAGES_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, MODULE, PARENT_ID_FK, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('SEC_PAGES_SEQ'), 'NOTIFICATION_SETTINGS', 'إعدادات الإشعارات', 'Notification Settings', '/notifications/settings', 'NOTIFICATION', NULL, 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_SETTINGS');

-- SCR-NOTIF-002 — إدارة قوالب الإشعارات
INSERT INTO SEC_PAGES (SEC_PAGES_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, MODULE, PARENT_ID_FK, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('SEC_PAGES_SEQ'), 'NOTIFICATION_TEMPLATE', 'إدارة قوالب الإشعارات', 'Notification Templates', '/notifications/templates', 'NOTIFICATION', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_SETTINGS'), 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_TEMPLATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_NOTIFICATION_TEMPLATE_VIEW', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_TEMPLATE'), 'VIEW', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_NOTIFICATION_TEMPLATE_VIEW');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_NOTIFICATION_TEMPLATE_CREATE', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_TEMPLATE'), 'CREATE', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_NOTIFICATION_TEMPLATE_CREATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_NOTIFICATION_TEMPLATE_UPDATE', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_TEMPLATE'), 'UPDATE', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_NOTIFICATION_TEMPLATE_UPDATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_NOTIFICATION_TEMPLATE_DELETE', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_TEMPLATE'), 'DELETE', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_NOTIFICATION_TEMPLATE_DELETE');

-- SCR-NOTIF-003 — إعدادات قنوات الإشعار
INSERT INTO SEC_PAGES (SEC_PAGES_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, MODULE, PARENT_ID_FK, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('SEC_PAGES_SEQ'), 'NOTIFICATION_CHANNEL_CONFIG', 'إعدادات قنوات الإشعار', 'Notification Channel Settings', '/notifications/channel-config', 'NOTIFICATION', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_SETTINGS'), 1, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_CHANNEL_CONFIG');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_NOTIFICATION_CHANNEL_CONFIG_VIEW', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_CHANNEL_CONFIG'), 'VIEW', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_NOTIFICATION_CHANNEL_CONFIG_VIEW');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_NOTIFICATION_CHANNEL_CONFIG_CREATE', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_CHANNEL_CONFIG'), 'CREATE', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_NOTIFICATION_CHANNEL_CONFIG_CREATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_NOTIFICATION_CHANNEL_CONFIG_UPDATE', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_CHANNEL_CONFIG'), 'UPDATE', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_NOTIFICATION_CHANNEL_CONFIG_UPDATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_NOTIFICATION_CHANNEL_CONFIG_DELETE', (SELECT SEC_PAGES_PK FROM SEC_PAGES WHERE PAGE_CODE = 'NOTIFICATION_CHANNEL_CONFIG'), 'DELETE', 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_NOTIFICATION_CHANNEL_CONFIG_DELETE');

COMMIT;
```

---

## 5. DB REGISTRY UPDATE

```
## REGISTRY UPDATE — 2026-07-11
────────────────────────────────────────────────────────────────
Source Mode    : MODE 1.5
Feature Code   : NOTIF-001
DBS-ID         : DBS-NOTIF-001
Plan ID        : —
────────────────────────────────────────────────────────────────
New Entities   : —
New Tables     : NOTIF_LOG, NOTIF_TEMPLATE, NOTIF_CHANNEL_CONFIG
New Lookups    : NOTIFICATION_CHANNEL (5 values), NOTIFICATION_STATUS
                 (4 values) — seeded
XM-IDs Open    : XM-NOTIF-001 → FILE_DOCUMENT (File Service) — DEFERRED
OQ-IDs Open    : None
Gate Status    : PASSED ✓
Next Action    : Trigger MODE 2 — Execution Plan Governance Engine
                 (NotificationService)
────────────────────────────────────────────────────────────────
```

**Registry cascade rule (SHARED-GOVERNANCE-RULES.md Section 6, REG-3):**
This is a fresh DBS-ID with one outbound DEFERRED XM-ID. The Registry
Maintainer registers `XM-NOTIF-001` in the Global XM Dependency Index
(To Module = FileService, Status = DEFERRED). Per this same session's
`dbs-file-001.md` Section 5, DBS-FILE-001 is now also confirmed —
however, `XM-NOTIF-001` remains DEFERRED regardless: this is an explicit
Phase-1 architectural decision (inline template storage), not a
"target not yet gated" deferral, so DBS-FILE-001's confirmation alone
does not auto-flip it to READY. The Registry Maintainer should still
log the cascade evaluation per REG-3, and may create `RXE-NOTIF-001`
if/when the platform decides to trigger the File Service migration —
that decision and its execution (P3 phase) are out of this engine's
scope.

---
*End of dbs-notif-001.md*
*Governed by: Database Governance Engine (Project 2)*
*DBS-ID: DBS-NOTIF-001 | DB_TARGET: POSTGRESQL_16*
*Next Mode: MODE 2 — Execution Plan Governance Engine (Project 3)*
