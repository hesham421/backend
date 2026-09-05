-- ============================================================
-- V4 — Master Data (MDM) — full module schema + seed
-- Source: governance/modules/MDM/P2/db-script.md SECTION 4 (FULL_DATABASE_SCRIPT), DBS-MDM-001
-- Target: POSTGRESQL_16 | 2 tables (master-detail), 2 sequences | 14 DBF-IDs, 0 XM
-- Seed: 4 types / 17 values (NOTIF_CHANNEL x5, NOTIF_STATUS x4, FILE_FILE_STATUS x3, FILE_FILE_TYPE x5).
-- Flyway wraps this migration in its own transaction (no explicit COMMIT — matches V2/V3).
-- ============================================================

-- ============================================================
-- BLOCK 1: SEQUENCES
-- ============================================================
CREATE SEQUENCE SEQ_MDM_LOOKUP_TYPE  START WITH 1 INCREMENT BY 1 CACHE 1 NO CYCLE;
CREATE SEQUENCE SEQ_MDM_LOOKUP_VALUE START WITH 1 INCREMENT BY 1 CACHE 1 NO CYCLE;

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
-- All CODES are taken VERBATIM from the owning modules' authoritative SRS — NO value invented.
-- NAME_AR/NAME_EN are engine-authored display labels (revisable via normal UPDATE).
--
-- CODE PROVENANCE (governed sources):
--   NOTIF_CHANNEL     : srs-NOTIF.md A2  (EMAIL/SMS/WHATSAPP/PUSH/INTERNAL)
--   NOTIF_STATUS      : srs-NOTIF.md A6  (PENDING/SENT/FAILED/CHANNEL_DISABLED)
--   FILE_FILE_STATUS  : srs-FILE.md  A6  (ACTIVE/ARCHIVED/DELETED)
--   FILE_FILE_TYPE    : srs-FILE.md  §A5 (LOV-FILE-001)  (IMAGE/DOCUMENT/SPREADSHEET/ARCHIVE/OTHER)

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
