<!-- Source: PHASE:DATAOM -->

## PHASE DATA+DOM — Entity & Domain Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : CORE ✓
Gate This Phase  : DATA+DOM ✓
Gate Status      : PASSED ✓
QR-IDs generated : QR-FILE-001, QR-FILE-002
─────────────────────────────────────────────────────────────────

### ENTITY-FILE-002 — FileCategory (declared first — intra-module FK parent)

```
Table            : FILE_CATEGORY (DBS-FILE-001)
Type              : SHARED (owner) — Reference Table (module_code-scoped, >15 values
                    platform-wide — master-registry Lookup Governance Rule)
Business Code     : NONE — documented deviation. categoryCode (FIELD-0002) is a
                    manually-assigned, lookupKey-like natural code, immutable after
                    creation — NOT a BC-RULE-2 sequential Business Code.
Entity base       : AuditableEntity
Operations        : Create, Read, Update, Deactivate (Admin) — see DRV-FILE-001 below
                    regarding missing API-ID for these operations.
Sequence          : SEQ_FILE_CATEGORY.NEXTVAL (exact name — db-script.md Block 1)

FIELDS:
  FIELD-0001 fileCategoryPk         : Long           PK  — SEQ_FILE_CATEGORY.NEXTVAL
  FIELD-0002 categoryCode           : String(50)     REQUIRED — immutable post-create
  FIELD-0003 moduleCode             : String(20)     REQUIRED — free text, owning module
  FIELD-0004 nameAr                 : String(200)    REQUIRED
  FIELD-0005 nameEn                 : String(100)    REQUIRED
  FIELD-0006 maxSizeBytesOverride   : Long           OPTIONAL — overrides 5MB default (RULE-FILE-001)
  FIELD-0007 allowedTypesNote       : String(500)    OPTIONAL — advisory only, not enforced
  FIELD-0008 isActiveFl             : Boolean        REQUIRED — default true
  FIELD-0009 createdBy              : String(255)    SYSTEM — AuditEntityListener
  FIELD-0010 createdAt              : LocalDateTime  SYSTEM — AuditEntityListener
  FIELD-0011 updatedBy              : String(255)    SYSTEM — AuditEntityListener
  FIELD-0012 updatedAt              : LocalDateTime  SYSTEM — AuditEntityListener

UNIQUE CONSTRAINT : UQ_FILE_CATEGORY_MODULE_CODE (MODULE_CODE, CATEGORY_CODE) —
  categoryCode is unique within its owning moduleCode, not platform-wide.

DOMAIN BEHAVIOR (embedded in Entity — per CORE):
  resolveMaxSizeBytes() → returns maxSizeBytesOverride if not null, else platform
  default 5,242,880 bytes (5MB) — consumed by RULE-FILE-001 at upload time.

QR-FILE-001 — FIND FileCategory by PK — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : FILE_CATEGORY
  DB Operation     : FIND_ONE
  Join strategy    : NONE
  Transaction bound: READ_ONLY
  Fetch strategy   : LAZY (project default — no DRV-ID required)
  Bulk operation   : NO — single-record fetch
  Used by     : API-FILE-001 (validates fileCategoryFk exists before issuing token)
```

### ENTITY-FILE-001 — FileDocument (child — intra-module FK to FileCategory)

```
Table            : FILE_DOCUMENT (DBS-FILE-001)
Type              : SHARED (owner) — consumers: NotificationService (HARD-FK, DEFERRED
                    via XM-NOTIF-001), AuditService (NOT-YET-ASSIGNED), 3.x modules
                    (NOT-YET-ASSIGNED)
Business Code     : NONE — documented deviation (Engine-managed, no human-facing code).
                    Sole identifier: fileDocumentPk (system sequence).
Entity base       : AuditableEntity
Operations        : Create (Upload), Read (Download/List), Delete (permanent-content) —
                    NO Update (replacement = delete + re-upload, per SRS A3).
Sequence          : SEQ_FILE_DOCUMENT.NEXTVAL (exact name — db-script.md Block 1)

FIELDS:
  FIELD-0013 fileDocumentPk    : Long           PK  — SEQ_FILE_DOCUMENT.NEXTVAL
  FIELD-0014 ownerId           : Long           REQUIRED — polymorphic, NO physical FK
                                  (target table determined by ownerType at runtime — ADAPT-05)
  FIELD-0015 ownerType         : String(100)    REQUIRED — free text, NOT a governed LOV
  FIELD-0016 moduleCode        : String(20)     REQUIRED — producing module code
  FIELD-0017 fileCategoryFk    : Long           REQUIRED — FK → FILE_CATEGORY (FK_FILE_DOCUMENT_FILE_CATEGORY)
  FIELD-0018 fileTypeId        : String(50)     SYSTEM — LOV-FILE-001, auto-detected (RULE-FILE-005)
  FIELD-0019 fileNameOriginal  : String(255)    REQUIRED — as uploaded by user
  FIELD-0020 mimeType          : String(100)    SYSTEM — auto-detected from content (RULE-FILE-005)
  FIELD-0021 fileSizeBytes     : Long           SYSTEM — computed at upload time
  FIELD-0022 fileContent       : byte[] (BYTEA) REQUIRED at create — see CORE binary-handling note
  FIELD-0023 fileStatusId      : String(50)     REQUIRED — LOV-FILE-002, Status Lifecycle (3 states)
  FIELD-0024 createdBy         : String(255)    SYSTEM — AuditEntityListener
  FIELD-0025 createdAt         : LocalDateTime  SYSTEM — AuditEntityListener
  FIELD-0026 updatedBy         : String(255)    SYSTEM — AuditEntityListener
  FIELD-0027 updatedAt         : LocalDateTime  SYSTEM — AuditEntityListener

FK CONSTRAINT (exact name from db-script.md):
  FK_FILE_DOCUMENT_FILE_CATEGORY — FILE_DOCUMENT.FILE_CATEGORY_FK → FILE_CATEGORY.FILE_CATEGORY_PK

STATUS LIFECYCLE (A6 — 3 states, no Workflow Engine — RULE-13):
  ACTIVE ──(archive)──► ARCHIVED
  ACTIVE / ARCHIVED ──(permanent content purge — RULE-FILE-006)──► DELETED (terminal)
  DELETED = row retained, fileContent set NULL at app layer, metadata + audit trail
  intact — resolves OQ-001: any future consumer HARD-FK stays valid, no orphaned reference.

DOMAIN BEHAVIOR (embedded in Entity — per CORE):
  purgeContent() → sets fileContent = null, fileStatusId = "DELETED". Called only from
  the delete flow after RULE-FILE-007 ownership/Admin check passes. Irreversible.

LOV BINDING:
  LOV-FILE-001 : lookupKey FILE_TYPE   — GET /api/lookups/FILE_TYPE?active=true
  LOV-FILE-002 : lookupKey FILE_STATUS — GET /api/lookups/FILE_STATUS?active=true

QR-FILE-002 — FIND FileDocument by PK — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : FILE_DOCUMENT
  DB Operation     : FIND_ONE
  Join strategy    : NONE
  Transaction bound: READ_ONLY
  Fetch strategy   : LAZY (project default — no DRV-ID required)
  Bulk operation   : NO — single-record fetch
  Used by     : API-FILE-003 (download), API-FILE-004 (delete, prior to purge)
```
