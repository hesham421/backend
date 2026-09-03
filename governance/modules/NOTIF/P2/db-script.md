<!-- DB Script — Governed by Database Governance Engine (Project 2 / MODE 1.5) -->

# DB SCRIPT — Notification Service (NOTIF)

## 1. DB SCRIPT HEADER
```
DBS-ID          : DBS-NOTIF-001
Module          : Notification Service (NOTIF)
SRS Feature Code: NOTIF-001  (srs-NOTIF.md v1.2 — OQ-NOTIF-001 RESOLVED)
Platform        : Foundation (Domain: ERP)
DB_TARGET       : POSTGRESQL_16   (confirmed by Architect 2026-09-02)
Date            : 2026-09-02
Status          : GATE PASSED
Open Questions  : None
Tables          : 3 (NOTIF_LOG, NOTIF_TEMPLATE, NOTIF_CHANNEL_CONFIG)
XM Dependencies : 2 SOFT-READ (XM-NOTIF-001 -> SEC, XM-NOTIF-002 -> FILE)
Lookup Tables   : None — LOV-NOTIF-001/002 are runtime-loaded codes per SRS A5 (no MD_MASTER_LOOKUP)
```

> Governed design notes (SRS-governs-DB, Layer 1 > Layer 2):
> - RECIPIENT_ID is a SOFT-READ to SEC's UserAccount (identity read at app layer, no FK) -> XM-NOTIF-001. Inactive-recipient dispatch is blocked at the app layer (RULE-NOTIF-007); historical logs retained.
> - ATTACHMENT_FILE_ID is a SOFT/service reference to FILE via the File Service API (no FK) -> XM-NOTIF-002.
> - CHANNEL_TYPE_ID (LOV-NOTIF-001) and NOTIFICATION_STATUS_ID (LOV-NOTIF-002) are runtime-loaded code columns — no lookup table, no CHECK.
> - NOTIF_CHANNEL_CONFIG uses IS_ENABLED_FL only (per SRS A3); no IS_ACTIVE_FL is invented.

## 2. DB FIELD TRACEABILITY MATRIX — Notification Service — DBS-ID: DBS-NOTIF-001
```
DBF-ID    | Table Name           | Column Name            | DB Type      | SRS Source
DBF-0001  | NOTIF_LOG            | ID                     | BIGINT       | ENTITY-NOTIF-001.notificationLogPk
DBF-0002  | NOTIF_LOG            | RECIPIENT_ID           | BIGINT       | ENTITY-NOTIF-001.recipientId (SOFT->SEC)
DBF-0003  | NOTIF_LOG            | CHANNEL_TYPE_ID        | VARCHAR(20)  | ENTITY-NOTIF-001.channelTypeId (LOV-NOTIF-001)
DBF-0004  | NOTIF_LOG            | NOTIFICATION_STATUS_ID | VARCHAR(30)  | ENTITY-NOTIF-001.notificationStatusId (LOV-NOTIF-002)
DBF-0005  | NOTIF_LOG            | MODULE_CODE            | VARCHAR(50)  | ENTITY-NOTIF-001.moduleCode
DBF-0006  | NOTIF_LOG            | REFERENCE_ID           | BIGINT       | ENTITY-NOTIF-001.referenceId
DBF-0007  | NOTIF_LOG            | REFERENCE_TYPE         | VARCHAR(100) | ENTITY-NOTIF-001.referenceType
DBF-0008  | NOTIF_LOG            | RETRY_COUNT            | SMALLINT     | ENTITY-NOTIF-001.retryCount
DBF-0009  | NOTIF_LOG            | ERROR_MESSAGE          | TEXT         | ENTITY-NOTIF-001.errorMessage
DBF-0010  | NOTIF_LOG            | SENT_AT                | TIMESTAMP    | ENTITY-NOTIF-001.sentAt
DBF-0011  | NOTIF_LOG            | TEMPLATE_FK            | BIGINT       | ENTITY-NOTIF-001.templateFk -> ENTITY-NOTIF-002
DBF-0012  | NOTIF_TEMPLATE       | ID                     | BIGINT       | ENTITY-NOTIF-002.notificationTemplatePk
DBF-0013  | NOTIF_TEMPLATE       | TEMPLATE_CODE          | VARCHAR(80)  | ENTITY-NOTIF-002.templateCode
DBF-0014  | NOTIF_TEMPLATE       | NAME_AR                | VARCHAR(200) | ENTITY-NOTIF-002.nameAr
DBF-0015  | NOTIF_TEMPLATE       | NAME_EN                | VARCHAR(100) | ENTITY-NOTIF-002.nameEn
DBF-0016  | NOTIF_TEMPLATE       | SUBJECT_AR             | VARCHAR(300) | ENTITY-NOTIF-002.subjectAr
DBF-0017  | NOTIF_TEMPLATE       | SUBJECT_EN             | VARCHAR(300) | ENTITY-NOTIF-002.subjectEn
DBF-0018  | NOTIF_TEMPLATE       | BODY_AR                | TEXT         | ENTITY-NOTIF-002.bodyAr
DBF-0019  | NOTIF_TEMPLATE       | BODY_EN                | TEXT         | ENTITY-NOTIF-002.bodyEn
DBF-0020  | NOTIF_TEMPLATE       | ATTACHMENT_FILE_ID     | BIGINT       | ENTITY-NOTIF-002.attachmentFileId (SOFT->FILE)
DBF-0021  | NOTIF_TEMPLATE       | IS_ACTIVE_FL           | SMALLINT     | ENTITY-NOTIF-002.isActiveFl
DBF-0022  | NOTIF_CHANNEL_CONFIG | ID                     | BIGINT       | ENTITY-NOTIF-003.notificationChannelConfigPk
DBF-0023  | NOTIF_CHANNEL_CONFIG | CHANNEL_TYPE_ID        | VARCHAR(20)  | ENTITY-NOTIF-003.channelTypeId (LOV-NOTIF-001)
DBF-0024  | NOTIF_CHANNEL_CONFIG | IS_ENABLED_FL          | SMALLINT     | ENTITY-NOTIF-003.isEnabledFl
DBF-0025  | NOTIF_CHANNEL_CONFIG | CONFIG_JSON            | TEXT         | ENTITY-NOTIF-003.configJson
Total: 25 DBF-IDs across 3 tables
```

## 3. CROSS-MODULE DEPENDENCY REGISTER (XM REGISTER) — Notification Service — DBS-ID: DBS-NOTIF-001
```
XM-ID         | Type      | This Table     | FK/Ref Column      | Target Table     | Target Module | Status
XM-NOTIF-001  | SOFT-READ | NOTIF_LOG      | RECIPIENT_ID       | SEC_USER_ACCOUNT | Security      | ACTIVE
XM-NOTIF-002  | SOFT-READ | NOTIF_TEMPLATE | ATTACHMENT_FILE_ID | FILE_DOCUMENT    | File Service  | ACTIVE
```
> XM-NOTIF-001 — recipient identity read from SEC at the app layer (no FK); dispatch to inactive recipients blocked by RULE-NOTIF-007. Target SEC_USER_ACCOUNT gated under DBS-SEC-001 -> ACTIVE.
> XM-NOTIF-002 — optional attachment resolved through the FILE FileService API (no FK). Target FILE_DOCUMENT gated under DBS-FILE-001 -> ACTIVE.

## 4. FULL_DATABASE_SCRIPT
```sql
-- ============================================================
-- FULL DATABASE SCRIPT — Notification Service (NOTIF) — DBS-NOTIF-001
-- Target: POSTGRESQL_16   |   Execute in psql / pgAdmin
-- ============================================================

-- ============================================================
-- BLOCK 1: SEQUENCES
-- ============================================================
CREATE SEQUENCE SEQ_NOTIF_LOG            START WITH 1 INCREMENT BY 1 NO CACHE NO CYCLE;
CREATE SEQUENCE SEQ_NOTIF_TEMPLATE       START WITH 1 INCREMENT BY 1 NO CACHE NO CYCLE;
CREATE SEQUENCE SEQ_NOTIF_CHANNEL_CONFIG START WITH 1 INCREMENT BY 1 NO CACHE NO CYCLE;

-- ============================================================
-- BLOCK 2: PARENT TABLES (no intra-module FK dependencies)
-- ============================================================
CREATE TABLE NOTIF_TEMPLATE (
  ID                 BIGINT        NOT NULL,
  TEMPLATE_CODE      VARCHAR(80)   NOT NULL,
  NAME_AR            VARCHAR(200)  NOT NULL,
  NAME_EN            VARCHAR(100)  NOT NULL,
  SUBJECT_AR         VARCHAR(300),
  SUBJECT_EN         VARCHAR(300),
  BODY_AR            TEXT          NOT NULL,
  BODY_EN            TEXT          NOT NULL,
  ATTACHMENT_FILE_ID BIGINT,
  IS_ACTIVE_FL       SMALLINT      DEFAULT 1 NOT NULL,
  CREATED_BY         VARCHAR(255),
  CREATED_AT         TIMESTAMP,
  UPDATED_BY         VARCHAR(255),
  UPDATED_AT         TIMESTAMP
);

CREATE TABLE NOTIF_CHANNEL_CONFIG (
  ID              BIGINT       NOT NULL,
  CHANNEL_TYPE_ID VARCHAR(20)  NOT NULL,
  IS_ENABLED_FL   SMALLINT     DEFAULT 1 NOT NULL,
  CONFIG_JSON     TEXT,
  CREATED_BY      VARCHAR(255),
  CREATED_AT      TIMESTAMP,
  UPDATED_BY      VARCHAR(255),
  UPDATED_AT      TIMESTAMP
);

-- ============================================================
-- BLOCK 3: CHILD TABLES (intra-module FK dependencies)
-- ============================================================
CREATE TABLE NOTIF_LOG (
  ID                     BIGINT        NOT NULL,
  RECIPIENT_ID           BIGINT        NOT NULL,
  CHANNEL_TYPE_ID        VARCHAR(20)   NOT NULL,
  NOTIFICATION_STATUS_ID VARCHAR(30)   NOT NULL,
  MODULE_CODE            VARCHAR(50)   NOT NULL,
  REFERENCE_ID           BIGINT,
  REFERENCE_TYPE         VARCHAR(100),
  RETRY_COUNT            SMALLINT      DEFAULT 0 NOT NULL,
  ERROR_MESSAGE          TEXT,
  SENT_AT                TIMESTAMP,
  TEMPLATE_FK            BIGINT        NOT NULL,
  CREATED_BY             VARCHAR(255),
  CREATED_AT             TIMESTAMP,
  UPDATED_BY             VARCHAR(255),
  UPDATED_AT             TIMESTAMP
);

-- ============================================================
-- BLOCK 4: COMMENTS
-- ============================================================
COMMENT ON TABLE NOTIF_LOG IS 'Per-channel notification log row (ENTITY-NOTIF-001); fan-out one row per requested channel (RULE-NOTIF-001).';
COMMENT ON COLUMN NOTIF_LOG.RECIPIENT_ID IS 'Recipient UserAccount id — SOFT-READ to SEC (no FK); XM-NOTIF-001.';
COMMENT ON COLUMN NOTIF_LOG.CHANNEL_TYPE_ID IS 'Channel code (LOV-NOTIF-001); runtime-loaded.';
COMMENT ON COLUMN NOTIF_LOG.NOTIFICATION_STATUS_ID IS 'Lifecycle code (LOV-NOTIF-002): PENDING/SENT/FAILED/CHANNEL_DISABLED.';
COMMENT ON COLUMN NOTIF_LOG.RETRY_COUNT IS 'Retry counter, <=5 then FAILED (RULE-NOTIF-002).';
COMMENT ON COLUMN NOTIF_LOG.TEMPLATE_FK IS 'FK to NOTIF_TEMPLATE.';
COMMENT ON TABLE NOTIF_TEMPLATE IS 'Bilingual notification template (ENTITY-NOTIF-002).';
COMMENT ON COLUMN NOTIF_TEMPLATE.ATTACHMENT_FILE_ID IS 'Optional attachment file id — SOFT/service to FILE via FileService (no FK); XM-NOTIF-002.';
COMMENT ON COLUMN NOTIF_TEMPLATE.IS_ACTIVE_FL IS 'Active flag: 1=active, 0=inactive.';
COMMENT ON TABLE NOTIF_CHANNEL_CONFIG IS 'Per-channel enable flag + provider config JSON (ENTITY-NOTIF-003).';
COMMENT ON COLUMN NOTIF_CHANNEL_CONFIG.CHANNEL_TYPE_ID IS 'Unique channel code (LOV-NOTIF-001).';
COMMENT ON COLUMN NOTIF_CHANNEL_CONFIG.IS_ENABLED_FL IS 'Runtime enable flag: 1=enabled, 0=disabled (RULE-NOTIF-003).';
COMMENT ON COLUMN NOTIF_CHANNEL_CONFIG.CONFIG_JSON IS 'Provider config (JSON as text); actual provider is a P3 decision.';

-- ============================================================
-- BLOCK 5: CONSTRAINTS
-- ============================================================
-- 5a. PRIMARY KEYS
ALTER TABLE NOTIF_TEMPLATE       ADD CONSTRAINT PK_NOTIF_TEMPLATE       PRIMARY KEY (ID);
ALTER TABLE NOTIF_CHANNEL_CONFIG ADD CONSTRAINT PK_NOTIF_CHANNEL_CONFIG PRIMARY KEY (ID);
ALTER TABLE NOTIF_LOG            ADD CONSTRAINT PK_NOTIF_LOG            PRIMARY KEY (ID);
-- 5b. UNIQUE  (RULE-NOTIF-006)
ALTER TABLE NOTIF_TEMPLATE       ADD CONSTRAINT UQ_NOTIF_TEMPLATE_CODE       UNIQUE (TEMPLATE_CODE);
ALTER TABLE NOTIF_CHANNEL_CONFIG ADD CONSTRAINT UQ_NOTIF_CHANNEL_CONFIG_TYPE UNIQUE (CHANNEL_TYPE_ID);
-- 5c. CHECK
ALTER TABLE NOTIF_TEMPLATE       ADD CONSTRAINT CHK_NOTIF_TEMPLATE_ACTIVE_FL CHECK (IS_ACTIVE_FL IN (0,1));
ALTER TABLE NOTIF_CHANNEL_CONFIG ADD CONSTRAINT CHK_NOTIF_CHANNEL_ENABLED_FL CHECK (IS_ENABLED_FL IN (0,1));
-- 5d. INTRA-MODULE FK
ALTER TABLE NOTIF_LOG            ADD CONSTRAINT FK_NOTIF_LOG_TEMPLATE FOREIGN KEY (TEMPLATE_FK) REFERENCES NOTIF_TEMPLATE (ID);

-- ============================================================
-- BLOCK 6: TRIGGERS       -- (none)
-- ============================================================

-- ============================================================
-- BLOCK 7: INDEXES
-- ============================================================
CREATE INDEX IDX_NOTIF_LOG_TEMPLATE_FK  ON NOTIF_LOG (TEMPLATE_FK);
CREATE INDEX IDX_NOTIF_LOG_RECIPIENT_ID ON NOTIF_LOG (RECIPIENT_ID);
CREATE INDEX IDX_NOTIF_LOG_STATUS       ON NOTIF_LOG (NOTIFICATION_STATUS_ID);
CREATE INDEX IDX_NOTIF_LOG_MODULE_CODE  ON NOTIF_LOG (MODULE_CODE);

-- ============================================================
-- BLOCK 8: LOOKUP SEED DATA
-- ============================================================
-- (none — LOV-NOTIF-001/002 are runtime-loaded codes; no MD_MASTER_LOOKUP per srs-NOTIF.md A5)

-- BLOCK 9: VIEWS            -- (none)
-- BLOCK 10: FUNCTIONS/PROCS -- (none)
-- BLOCK 11: DEFERRED FK BLOCKS
-- ============================================================
-- XM-NOTIF-001 (->SEC_USER_ACCOUNT) and XM-NOTIF-002 (->FILE_DOCUMENT) are SOFT-READ.
-- No physical FKs are created by design. No deferred patch required.
```

## 5. DB REGISTRY UPDATE — MODE 1.5
```
REGISTRY UPDATE — 2026-09-02
Source Mode : MODE 1.5 | Feature Code: NOTIF-001 | DBS-ID: DBS-NOTIF-001
New Tables  : NOTIF_LOG, NOTIF_TEMPLATE, NOTIF_CHANNEL_CONFIG
New Lookups : None
XM-IDs Open : XM-NOTIF-001 (SOFT-READ -> SEC_USER_ACCOUNT) ACTIVE;
              XM-NOTIF-002 (SOFT-READ -> FILE_DOCUMENT) ACTIVE
OQ-IDs Open : None
Gate Status : PASSED
Next Action : Trigger Project 3.1 — Execution Plan Governance Engine (Backend pass)
Table Registry rows to add (master-registry §7):
  DBS-NOTIF-001 | NOTIF_LOG | NOTIF
  DBS-NOTIF-001 | NOTIF_TEMPLATE | NOTIF
  DBS-NOTIF-001 | NOTIF_CHANNEL_CONFIG | NOTIF
Global XM Index rows to add (master-registry §8):
  XM-NOTIF-001 | NOTIF | SEC  | SOFT-READ | ACTIVE
  XM-NOTIF-002 | NOTIF | FILE | SOFT-READ | ACTIVE
Pipeline Status Grid: NOTIF · P2 = done
```

---
*End of db-script-NOTIF.md | DBS-NOTIF-001 | POSTGRESQL_16 | 3 tables, 25 DBF-IDs, 2 SOFT-READ XM | Next: Project 3.1*
