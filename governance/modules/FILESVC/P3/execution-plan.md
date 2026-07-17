<!-- ═══════════════════════════════════════════════════════════ -->
<!-- EXECUTION PLAN — Layer 3 (Execution Truth) + Layer 3.5 Summary -->
<!-- Governed by: Execution Plan Governance Engine (Project 3, v2) -->
<!-- MODE 2 — Single-File Agent-Ready Output                     -->
<!-- ═══════════════════════════════════════════════════════════ -->

# EXECUTION PLAN — File Service
## PLAN-ID: PLAN-FILE-001

```
Task Type        : 🆕 New Feature
Plan Name        : New Feature — Attachment Panel (Central File Storage) — File Service Module
Feature Code     : FILE-001 (srs-file-001.md v1.0)
DBS-ID           : DBS-FILE-001 (dbs-file-001.md — GOVERNED ✓ MODE 1.5)
DB_TARGET        : POSTGRESQL_16
Module Prefix    : FILE
Generated        : 2026-07-11
Governance Mode  : FULL (SRS ✓ + DB Script ✓ + module-registry ✓ + master-registry ✓)
```

---

## SECTION 2A — ARTIFACT EXTRACTION SUMMARY (pre-generation)

```
Extracted from srs-file-001.md  : 2 entities, 7 rules, 2 LOVs, 1 screen, 5 APIs, 0 open OQs
Extracted from dbs-file-001.md  : 2 tables, 27 DBF-IDs, 0 outbound XM-IDs
Extraction Failure Protocol Case: FileCategory (ENTITY-FILE-002) declares Create/Update/
  Deactivate operations in SRS A3, but SRS Part B defines NO screen and NO API-ID for
  them. Per Section 2A.3, no API is invented. Resolved as DRV-FILE-001 below — NOT a
  blocking gap (see Derivation Log).
GOVERNANCE REDUCED : No — both srs.md and db-script.md present, ALIGN gate PASSED source-side.
```

---

## SECTION 3 — EXECUTION PLAN INDEX

```
══════════════════════════════════════════════════════════════════
PLAN INDEX — File Service — PLAN-ID: PLAN-FILE-001
══════════════════════════════════════════════════════════════════
Entities   : ENTITY-FILE-001 (FileDocument, SHARED-owner), ENTITY-FILE-002 (FileCategory, SHARED-owner/Reference Table)
FIELD-IDs  : FIELD-0001..0027 (27, fully DBF-bound — see Section 4)
APIs       : API-FILE-001..005 (5)
RULE-IDs   : RULE-FILE-001..007 (7)
LOV-IDs    : LOV-FILE-001 (FILE_TYPE), LOV-FILE-002 (FILE_STATUS)
ERR-IDs    : ERR-FILE-0001..0009 (9 — see Error Catalog; 0007/0008/0009 each carry a
             single RULE-ID field value per CHECK-2.6, dual-cause splits documented
             via DRV-ID cross-reference only)
SCR-IDs    : SCR-FILE-001 (Attachment Panel — PATTERN-2)
QR-IDs     : QR-FILE-001..007 (7)
XM-IDs Outbound : None — 0 outbound (module-registry-file.md confirmed)
XM Inbound Stubs: XM-INBOUND-STUB-FILE-1 (NotificationService, DEFERRED — became XM-NOTIF-001 in
                   dbs-notif-001.md), XM-INBOUND-STUB-FILE-2 (AuditService, NOT-YET-ASSIGNED),
                   XM-INBOUND-STUB-FILE-3 (3.x modules, NOT-YET-ASSIGNED)
OQ-IDs Open: None (OQ-001 RESOLVED at MODE 1)
DRV-IDs    : DRV-FILE-001..011 (contiguous — see Derivation Log, embedded per phase + summarized in ALIGN)
══════════════════════════════════════════════════════════════════
```

---

## SECTION 4 — DB ALIGNMENT MANIFEST (CANONICAL — FIELD-ID → DBF-ID)

```
## DB ALIGNMENT MANIFEST — File Service — PLAN-ID: PLAN-FILE-001 / DBS-ID: DBS-FILE-001
══════════════════════════════════════════════════════════════════
FIELD-ID   │ DBF-ID   │ Plan Type      │ FK/XM-ID │ Match Status
───────────┼──────────┼────────────────┼──────────┼─────────────
FIELD-0001 │ DBF-0001 │ Long           │ —        │ ✓
FIELD-0002 │ DBF-0002 │ String(50)     │ —        │ ✓
FIELD-0003 │ DBF-0003 │ String(20)     │ —        │ ✓
FIELD-0004 │ DBF-0004 │ String(200)    │ —        │ ✓
FIELD-0005 │ DBF-0005 │ String(100)    │ —        │ ✓
FIELD-0006 │ DBF-0006 │ Long           │ —        │ ✓
FIELD-0007 │ DBF-0007 │ String(500)    │ —        │ ✓
FIELD-0008 │ DBF-0008 │ Boolean        │ —        │ ✓
FIELD-0009 │ DBF-0009 │ String(255)    │ —        │ ✓
FIELD-0010 │ DBF-0010 │ LocalDateTime  │ —        │ ✓
FIELD-0011 │ DBF-0011 │ String(255)    │ —        │ ✓
FIELD-0012 │ DBF-0012 │ LocalDateTime  │ —        │ ✓
FIELD-0013 │ DBF-0013 │ Long           │ —        │ ✓
FIELD-0014 │ DBF-0014 │ Long           │ — (polymorphic, no FK) │ ✓
FIELD-0015 │ DBF-0015 │ String(100)    │ —        │ ✓
FIELD-0016 │ DBF-0016 │ String(20)     │ —        │ ✓
FIELD-0017 │ DBF-0017 │ Long           │ FK → FILE_CATEGORY (intra-module) │ ✓
FIELD-0018 │ DBF-0018 │ String(50)     │ LOV-FILE-001 │ ✓
FIELD-0019 │ DBF-0019 │ String(255)    │ —        │ ✓
FIELD-0020 │ DBF-0020 │ String(100)    │ —        │ ✓
FIELD-0021 │ DBF-0021 │ Long           │ —        │ ✓
FIELD-0022 │ DBF-0022 │ Binary (BYTEA) │ — (RESOLUTION-01, extends CORE-8) │ ✓
FIELD-0023 │ DBF-0023 │ String(50)     │ LOV-FILE-002 │ ✓
FIELD-0024 │ DBF-0024 │ String(255)    │ —        │ ✓
FIELD-0025 │ DBF-0025 │ LocalDateTime  │ —        │ ✓
FIELD-0026 │ DBF-0026 │ String(255)    │ —        │ ✓
FIELD-0027 │ DBF-0027 │ LocalDateTime  │ —        │ ✓
══════════════════════════════════════════════════════════════════
Total: 27 FIELD-IDs, all DBF-bound, all Match Status ✓.
```

---

## SECTION 5 — OPEN QUESTIONS LOG — CONTINUATION

```
OQ-ID  │ Question                                    │ Status   │ Escalation
───────┼──────────────────────────────────────────────┼──────────┼───────────
OQ-001 │ Effect of permanent FileDocument delete on   │ RESOLVED │ LOCAL
       │ future HARD-FK consumers (RULE-FILE-006)     │ (MODE 1) │
───────────────────────────────────────────────────────────────────────────
No new OQ-IDs raised in MODE 2. See DRV-FILE-001 (Derivation Log) for the
FileCategory API-gap resolution — handled as a Finding/Derivation, not an OQ,
because the source artifacts (business-policies-file.md) already resolve it.
```

---

<!-- PHASE:CORE:START -->
## PHASE CORE — Architectural Policies
─────────────────────────────────────────────────────────────────
Gate Required    : MODE 2 Entry Gate PASSED ✓
Gate This Phase  : CORE ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

CANONICAL ARCHITECTURE — applies as defined platform-wide (PROJECT-3 §8.1):
Backend: controller/ → service/ → domain/ → repository/ → entity/ ; mapper/, dto/, exception/, config/ as needed.
Frontend: models/ → services/ → facades/ → components/ ; helpers/ as needed.
Layer boundaries and prohibitions apply exactly as declared in the Governance Engine
(no restatement here — single source of truth remains PROJECT-3 §8.1).

MODULE-SPECIFIC DECLARATIONS:

Domain behavior placement : "Domain behavior: embedded in Entity methods."
  Rationale: FileDocument's domain logic (status transition ACTIVE→ARCHIVED→DELETED,
  content purge on delete) and FileCategory's size-override resolution are small,
  entity-local behaviors — no separate domain/ class package is warranted for a
  2-entity Foundation module. Consistent with DBS-ORG-001 precedent scale reasoning.

Entity base        : AuditableEntity (uniform, no tenant variant) — both entities.
Error signaling     : LocalizedException — NotFoundException BANNED.
Audit fields        : AuditEntityListener — never accepted in CreateRequest/UploadRequest
                      (fileContent excepted — see below).
Search/Pagination   : SearchRequest extends BaseSearchContractRequest — used by
                      API-FILE-005 (list files by owner).
Deactivation        : FileCategory.isActiveFl = false (soft) — NOT deletion.
                      FileDocument has NO deactivation concept — its lifecycle is the
                      3-state Status Lifecycle (ACTIVE/ARCHIVED/DELETED), governed by
                      RULE-FILE-006, not by isActiveFl.

MODULE-SPECIFIC ARCHITECTURAL POLICY — Encrypted Token layer:
  A dedicated security component (NOT part of controller/service/domain/repository
  layering above) issues and validates the Encrypted Token (AES/GCM, 12-byte IV,
  128-bit GCM tag, 100-minute TTL, single-use). This lives in a module-local
  security/ package (e.g. FileTokenService + FileTokenFilter), invoked BEFORE the
  controller layer for /upload/{token}, /download/{token}, /{token} (delete) routes.
  This is a documented architectural deviation from the standard controller-first
  flow — sourced from ARCH-REF-1.10 AD-FILE-02/03, POLICY-CLI-02/03. Token
  algorithm/IV/Tag details are NOT business rules (srs-file-001.md A2) — implemented
  per ARCH-REF spec, not re-derived here.
  ✗ No JWT validation inside this module (POLICY-CLI-06 / ADAPT-03) — Security's
    filter chain governs the token-issuing endpoint (API-FILE-001) only.

BINARY CONTENT HANDLING (non-standard — declared explicitly):
  FILE_DOCUMENT.FILE_CONTENT (FIELD-0022, BYTEA) is the sole binary field in this
  plan. It is:
    - Accepted only via multipart/form-data on API-FILE-002 (never JSON body)
    - Never included in any list/search response DTO (API-FILE-005) — metadata only
    - Streamed (not loaded fully into a DTO field) on API-FILE-003 download
    - Set to NULL at the application layer (not DB-level trigger) on permanent
      delete (RULE-FILE-006) — see SVC+API API-FILE-004.

TYPE MAPPING: standard CORE-8 POSTGRESQL_16 → Java mapping applies (BIGINT→Long,
VARCHAR(N)→String, SMALLINT [_FL]→Boolean, TIMESTAMP→LocalDateTime). BYTEA→byte[]
(Java) — extension beyond the standard CORE-8 table, documented in DATA+DOM below.
<!-- PHASE:CORE:END -->

---

<!-- PHASE:DATAOM:START -->
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
<!-- PHASE:DATAOM:END -->

---

<!-- PHASE:SVCAPI:START -->
## PHASE SVC+API — Service & API Contract Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : DATA+DOM ✓
Gate This Phase  : SVC+API ✓
Gate Status      : PASSED ✓
QR-IDs generated : QR-FILE-003, QR-FILE-004, QR-FILE-005, QR-FILE-006, QR-FILE-007
─────────────────────────────────────────────────────────────────

> ⚠ Documented STACK-1 deviation (srs-file-001.md B5): API-FILE-002/003/004 use
> `/upload/{token}`, `/download/{token}`, `/{token}` — NOT the standard
> `/api/v1/[module]/[resource]` pattern. Source: ARCH-REF-1.10 AD-FILE-02/03
> (Encrypted Token embedded in URL path). HTTP methods and the standard
> ApiResponse<T> envelope still apply.

### API-FILE-001 — Issue Upload Token
─────────────────────────────────────────────────────────────────
Method / Path    : POST /api/v1/files/upload-token
Request DTO      : FileUploadTokenRequest { ownerId: Long, ownerType: String,
                    moduleCode: String, fileCategoryFk: Long }
Response DTO     : FileUploadTokenResponse { encryptedToken: String, expiresAt: LocalDateTime }

VALIDATIONS:
  RULE-FILE-004 — Single-use token
    Trigger    : On every new upload intent
    Statement  : The system MUST issue a new Encrypted Token for every upload intent —
                 a previously consumed token MUST NOT be accepted for a subsequent operation.
    Message-AR : هذا الرابط مُستخدَم مسبقاً
    Message-EN : This link has already been used
    Scope      : CREATE

SERVICE ORCHESTRATION:
  1. load      — FIND FileCategory by fileCategoryFk (QR-FILE-001) — validates existence
  2. validate  — (token issuance itself has no business rule beyond category existence)
  3. integrate — none (no XM dependency)
  4. persist   — none — token is stateless (self-contained AES/GCM payload:
                 ownerId+ownerType+moduleCode+action+ts+fileCategoryFk), not DB-stored.
                 Single-use enforcement (RULE-FILE-004) is implemented via the token's
                 embedded timestamp + a short-lived consumed-token cache — implementation
                 detail for P3 (module-local security/ package per CORE).

ERRORS:
  ERR-FILE-0009 → fileCategoryFk not found → HTTP 404 via LocalizedException (see
                  Error Catalog, RULE-ID = PLATFORM-STD, DRV-FILE-002)

SECURITY: Screen — none (system-to-system + Angular FileUploadComponent caller).
  Standard JWT-authenticated caller (any module screen embedding SCR-FILE-001).
LOCALIZATION: N/A for success path — token payload only.
─────────────────────────────────────────────────────────────────

### API-FILE-002 — Upload File
─────────────────────────────────────────────────────────────────
Method / Path    : POST /upload/{encryptedToken}
Request          : multipart/form-data (single file part)
Response DTO     : FileUploadResponse { fileDocumentPk: Long, fileNameOriginal: String,
                    fileTypeId: String, fileSizeBytes: Long, fileStatusId: String }

VALIDATIONS:
  RULE-FILE-001 — Content size limit
    Trigger   : On upload, before persisting fileContent
    Statement : The system MUST reject any uploaded file content exceeding 5MB (default)
                or the FileCategory-specific maxSizeBytesOverride when set.
    Message-AR: حجم الملف يتجاوز الحد المسموح به
    Message-EN: File size exceeds the allowed limit
    Scope     : CREATE
  RULE-FILE-002 — Token TTL
    Trigger   : On any token-bearing request
    Statement : The system MUST reject any request whose Encrypted Token exceeded its
                TTL (100 min default), returning 401 before reaching business logic.
    Message-AR: انتهت صلاحية الرابط — يرجى طلب رابط جديد
    Message-EN: This link has expired — please request a new one
    Scope     : ALL (token layer — pre-controller)
  RULE-FILE-003 — Tampered / mismatched token
    Trigger   : On any token-bearing request
    Statement : The system MUST reject any request whose token is missing, tampered
                (GCM tag mismatch), or whose embedded action doesn't match the endpoint.
    Message-AR: الرابط غير صالح
    Message-EN: The link is invalid
    Scope     : ALL (token layer — pre-controller)
  RULE-FILE-005 — MIME detection from content only
    Trigger   : On upload
    Statement : The system MUST determine MIME type from file content itself and MUST
                NOT rely on the client-supplied Content-Type header.
    Message-AR: نوع الملف يُحدَّد تلقائياً من محتواه
    Message-EN: File type is automatically determined from its content
    Scope     : CREATE

SERVICE ORCHESTRATION:
  1. load      — decode + validate token (RULE-FILE-002, RULE-FILE-003 — token layer,
                 before controller per CORE); FIND FileCategory by fileCategoryFk from
                 token payload (QR-FILE-001) to resolve size ceiling
  2. validate  — RULE-FILE-001 (size, against FileCategory.resolveMaxSizeBytes());
                 RULE-FILE-005 (server-side content-sniffing MIME detection — never
                 trusts multipart Content-Type header)
  3. integrate — none
  4. persist   — SAVE FileDocument (QR-FILE-003): ownerId/ownerType/moduleCode/
                 fileCategoryFk from token payload; fileTypeId + mimeType from content
                 detection; fileSizeBytes computed; fileStatusId = "ACTIVE";
                 sequence SEQ_FILE_DOCUMENT.NEXTVAL

QR-FILE-003 — SAVE FileDocument — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : FILE_DOCUMENT
  DB Operation     : SAVE
  Join strategy    : NONE
  Transaction bound: READ_WRITE
  Fetch strategy   : N/A (write operation — no fetch graph)
  Bulk operation   : NO — single-record insert
  Sequence    : SEQ_FILE_DOCUMENT.NEXTVAL

ERRORS:
  ERR-FILE-0001 → RULE-FILE-001 violation → HTTP 400
  ERR-FILE-0002 → RULE-FILE-002 violation → HTTP 401
  ERR-FILE-0003 → RULE-FILE-003 violation → HTTP 401/403 (403 specifically for
                  action-mismatch per RULE-FILE-003 Test-Hint; 401 for missing/tampered)

SECURITY: Screen — SCR-FILE-001, Permission — PERM_FILE_ATTACHMENT_CREATE (in addition
  to token validity — composite check, token layer + permission layer).
LOCALIZATION: Error responses carry messageAr + messageEn per Error Catalog.
─────────────────────────────────────────────────────────────────

### API-FILE-003 — Download File
─────────────────────────────────────────────────────────────────
Method / Path    : GET /download/{encryptedToken}
Request          : none (all context in token)
Response         : binary stream + Content-Type header + Content-Disposition header

VALIDATIONS: RULE-FILE-002, RULE-FILE-003 (token layer, identical to API-FILE-002).

SERVICE ORCHESTRATION:
  1. load      — decode + validate token; FIND FileDocument by fileDocumentPk from
                 token payload (QR-FILE-004)
  2. validate  — RULE-FILE-002 / RULE-FILE-003 (token layer)
                 If fileStatusId = "DELETED": return the RULE-FILE-006 governance-note
                 behavior — HTTP 410 GONE with a "file no longer available" message
                 (DRV-FILE-003 — see Derivation Log) rather than streaming null content.
  3. integrate — none
  4. persist   — none (read-only)

QR-FILE-004 — FIND FileDocument by PK (for download) — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : FILE_DOCUMENT
  DB Operation     : FIND_ONE
  Join strategy    : NONE
  Transaction bound: READ_ONLY
  Fetch strategy   : LAZY (project default — no DRV-ID required)
  Bulk operation   : NO — single-record fetch

ERRORS:
  ERR-FILE-0002 → RULE-FILE-002 → HTTP 401
  ERR-FILE-0003 → RULE-FILE-003 → HTTP 401/403
  ERR-FILE-0008 → fileStatusId = DELETED → HTTP 410 (RULE-ID = PLATFORM-STD; DRV-FILE-003
                  cross-reference kept in Derivation Log only, see Error Catalog)

SECURITY: Screen — SCR-FILE-001, Permission — PERM_FILE_ATTACHMENT_VIEW (+ token validity).
LOCALIZATION: mimeType drives Content-Type; error responses carry messageAr + messageEn.
─────────────────────────────────────────────────────────────────

### API-FILE-004 — Delete File
─────────────────────────────────────────────────────────────────
Method / Path    : DELETE /{encryptedToken}
Request          : none (all context in token)
Response DTO     : FileDeleteConfirmation { fileDocumentPk: Long, fileStatusId: String }

VALIDATIONS:
  RULE-FILE-002, RULE-FILE-003 — token layer (as above).
  RULE-FILE-006 — Permanent deletion
    Trigger   : On delete
    Statement : The system MUST permanently delete the file's content upon a valid
                delete request — no Recycle Bin or recovery mechanism in this phase.
    Message-AR: سيتم حذف الملف نهائياً ولا يمكن استرجاعه
    Message-EN: The file will be permanently deleted and cannot be recovered
    Scope     : DELETE
    ⚠ Frontend confirmation dialog text (client-side, pre-submit) — NOT a server
      rejection. Server behavior: purge fileContent, set fileStatusId=DELETED,
      row retained (resolves OQ-001).
  RULE-FILE-007 — Ownership/Admin restriction
    Trigger   : On delete
    Statement : The system MUST reject a delete request (403) from an actor who is
                neither the file's owning entity's authorized actor nor an Admin.
    Message-AR: غير مصرح لك بحذف هذا الملف
    Message-EN: You are not authorized to delete this file
    Scope     : DELETE

SERVICE ORCHESTRATION:
  1. load      — decode + validate token; FIND FileDocument by fileDocumentPk (QR-FILE-005)
  2. validate  — RULE-FILE-002/003 (token layer); RULE-FILE-007 (ownership/Admin —
                 compares caller identity/role against ownerId/ownerType context,
                 or Admin role, resolved via Security's role/permission model per
                 module-registry-file.md DEPENDENCIES)
  3. integrate — none
  4. persist   — UPDATE FileDocument (QR-FILE-006): fileContent = NULL,
                 fileStatusId = "DELETED" — via Entity.purgeContent() domain method

QR-FILE-005 — FIND FileDocument by PK (for delete) — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : FILE_DOCUMENT
  DB Operation     : FIND_ONE
  Join strategy    : NONE
  Transaction bound: READ_ONLY
  Fetch strategy   : LAZY (project default — no DRV-ID required)
  Bulk operation   : NO — single-record fetch

QR-FILE-006 — UPDATE FileDocument (content purge) — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : FILE_DOCUMENT
  DB Operation     : UPDATE
  Join strategy    : NONE
  Transaction bound: READ_WRITE
  Fetch strategy   : EAGER — DRV-FILE-004 (the entity must be loaded in full before
                     the compound 2-column purge, not lazily proxied; entity is
                     tiny, no performance concern)
  Bulk operation   : NO — single-record update
  Note        : Compound UPDATE (2 columns: FILE_CONTENT, FILE_STATUS_ID) —
                DRV-FILE-004 (RULE-REPO-DRV — documented, not a deviation requiring
                a different strategy; simple two-field update, no native query needed)

ERRORS:
  ERR-FILE-0002, ERR-FILE-0003 → token layer (401/403)
  ERR-FILE-0007 → RULE-FILE-007 → HTTP 403

SECURITY: Screen — SCR-FILE-001, Permission — PERM_FILE_ATTACHMENT_DELETE
  (composite: token validity + permission + RULE-FILE-007 ownership/Admin check).
LOCALIZATION: messageAr + messageEn per Error Catalog.
─────────────────────────────────────────────────────────────────

### API-FILE-005 — List Files for Owner Record
─────────────────────────────────────────────────────────────────
Method / Path    : GET /api/v1/files/{ownerId}
Request DTO      : FileListSearchRequest extends BaseSearchContractRequest
                    { ownerId: Long [path], ownerType: String [OPTIONAL query], page, size }
Response DTO     : Page<FileDocumentSummaryResponse> { fileDocumentPk, fileNameOriginal,
                    fileCategoryFk (+ name via join), fileTypeId, fileSizeBytes,
                    fileStatusId, createdAt } — fileContent NEVER included.

VALIDATIONS: none (read-only listing; RULE-IDs "—" per SRS B5).

SERVICE ORCHESTRATION:
  1. load      — FIND_BY_CRITERIA FileDocument WHERE OWNER_ID = :ownerId
                 [AND OWNER_TYPE = :ownerType if provided] (QR-FILE-007)
  2. validate  — none
  3. integrate — none
  4. persist   — none (read-only)

QR-FILE-007 — FIND_BY_CRITERIA FileDocument by owner — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : FILE_DOCUMENT
  DB Operation     : FIND_BY_CRITERIA
  Join strategy    : LEFT JOIN to FILE_CATEGORY for nameAr/nameEn display — DRV-FILE-005:
                     join justified by SRS B3 "عرض القائمة" requiring FileCategory's name,
                     not just its FK
  Transaction bound: READ_ONLY
  Fetch strategy   : LAZY (project default — no DRV-ID required beyond the join itself)
  Bulk operation   : YES — paginated multi-record fetch (Page<T>, per DOC-3)
  Index used  : IDX_FILE_DOCUMENT_OWNER (OWNER_ID, OWNER_TYPE) — db-script.md Block 7
  ALLOWED_SORT_FIELDS : createdAt, fileNameOriginal, fileSizeBytes

ERRORS: none beyond platform-standard (empty result → HTTP 200, never 404 — DOC-3 standard).

SECURITY: Screen — SCR-FILE-001, Permission — PERM_FILE_ATTACHMENT_VIEW.
LOCALIZATION: nameAr/nameEn of joined FileCategory returned per LOC-B2-RULE-1.
─────────────────────────────────────────────────────────────────

**Finding / Derivation — DRV-FILE-001 (FileCategory administration):**
SRS A3 (ENTITY-FILE-002) declares Create/Read/Update/Deactivate operations for
FileCategory, but SRS Part B defines no screen and no API-ID for them. Cross-checked
against business-policies-file.md "Platform Integration Notes": *"each consuming
module seeds its own category rows (e.g. 'ATTACHMENT', 'REPORT',
'NOTIFICATION_TEMPLATE') independently, without touching this module's code."* This
confirms FileCategory rows in Phase 1 are managed via each consuming module's own
seed-data / migration scripts — NOT via a File-Service-owned runtime CRUD API or
screen. No API is invented (Section 2A.3 compliance). This plan does not generate
F1/F2/F3/SEC specs for a FileCategory screen because none is declared in the SRS.
If a future need for a File-Service-owned FileCategory admin screen arises, it must
first be added to srs-file-001.md (MODE 1) before MODE 2 can plan it.
<!-- PHASE:SVCAPI:END -->

---

<!-- PHASE:DOC:START -->
## PHASE DOC — Contract Stabilization
─────────────────────────────────────────────────────────────────
Gate Required    : SVC+API ✓
Gate This Phase  : DOC ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

### DOC-1: API Contract Summary
```
API-ID       │ Endpoint                          │ Method │ Request DTO              │ Response DTO                    │ Stability
─────────────┼────────────────────────────────────┼────────┼──────────────────────────┼──────────────────────────────────┼──────────
API-FILE-001 │ /api/v1/files/upload-token         │ POST   │ FileUploadTokenRequest   │ FileUploadTokenResponse         │ STABLE
API-FILE-002 │ /upload/{encryptedToken}           │ POST   │ multipart/form-data      │ FileUploadResponse              │ STABLE (path pattern DEVIATION — documented)
API-FILE-003 │ /download/{encryptedToken}         │ GET    │ —                        │ binary stream                   │ STABLE (path pattern DEVIATION — documented)
API-FILE-004 │ /{encryptedToken}                  │ DELETE │ —                        │ FileDeleteConfirmation          │ STABLE (path pattern DEVIATION — documented)
API-FILE-005 │ /api/v1/files/{ownerId}            │ GET    │ FileListSearchRequest    │ Page<FileDocumentSummaryResponse> │ STABLE
```
Unstable APIs: None.
Frontend-governed contracts: None.

### DOC-2: DTO Typing Rules
LOV field typing: String (fileTypeId, fileStatusId) — never ENUM.
Business Code: N/A — neither entity has a Business Code (documented deviations, DATA+DOM).

### DOC-3: Pagination & Filter Standards
Standard platform rule applies as-is (JPA Page<T>, BaseSearchContractRequest,
empty result → HTTP 200). Used by API-FILE-005 only.

**DOC GATE CHECK:**
```
[ ✓ ] All API-IDs from SVC+API appear in API Contract Summary
[ ✓ ] Error Catalog complete with Arabic + English messages
[ ✓ ] All APIs marked STABLE or explicitly UNSTABLE
[ ✓ ] Pagination standard declared
DOC Gate: PASSED ✓
```
<!-- PHASE:DOC:END -->

---

<!-- PHASE:INTC:START -->
## PHASE INT-C — Integration Contract Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : DOC ✓
Gate This Phase  : INT-C ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

```
## INT-C SUMMARY — File Service — PLAN-ID: PLAN-FILE-001
══════════════════════════════════════════════════════════════════════════
XM-ID │ Classification │ Target Module │ Interface Type │ Contract Status
──────┼────────────────┼───────────────┼─────────────────┼────────────────
—     │ —              │ —             │ —               │ —
══════════════════════════════════════════════════════════════════════════
None — 0 outbound XM-IDs (dbs-file-001.md XM Register confirms). File Service
consumes no other module's SHARED entity by FK. Security dependency is a
Trust-Boundary only (JWT filter chain for API-FILE-001) — not a data-FK, no XM-ID.
```

**INBOUND XM STUBS (this module is the target — future consumers):**
```
XM-INBOUND-STUB-FILE-1
  Consumer module  : NotificationService (1.8)
  Entity exposed   : FileDocument (ENTITY-FILE-001) — HARD-FK
  XM-ID assignment : Consumer's own MODE 1.5 — already assigned as XM-NOTIF-001
                     (dbs-notif-001.md), Status: DEFERRED
  Current status   : DEFERRED (Phase 1 workaround on Notification's side: inline
                     template body storage)
  Unblock mechanism: RXE-NOTIF-[SEQ] per CONTRACT-8, fired by the Registry
                     Maintainer now that this module's DBS-FILE-001 is GOVERNED ✓
  DRV-FILE-006     : This plan takes no action for this stub — the unblock and
                     migration are Notification Service's own P3 execution work
                     (business-policies-file.md "Notification Service integration
                     (forward-looking)" confirms no action required from File
                     Service's own pipeline).

XM-INBOUND-STUB-FILE-2
  Consumer module  : AuditService (1.9)
  Entity exposed   : FileDocument — HARD-FK (archival exports)
  Current status   : NOT-YET-ASSIGNED — AuditService itself is NOT STARTED

XM-INBOUND-STUB-FILE-3
  Consumer module  : All 3.x modules (Procurement/Inventory/Sales/Finance)
  Entity exposed   : FileDocument — HARD-FK (generic attachment storage)
  Current status   : NOT-YET-ASSIGNED — consumers not yet built
```

**INT-C GATE CHECK:**
```
[ ✓ ] All XM-IDs from DB Script XM Register accounted for (0 outbound — none to account for)
[ ✓ ] Classification declared for each XM-ID (N/A — none outbound)
[ ✓ ] All DEFERRED have unblock condition (inbound stubs documented above)
[ ✓ ] No new XM-IDs invented
[ ✓ ] Open RXEs acknowledged (RXE-NOTIF-[SEQ] pending, Registry Maintainer's action)
[ ✓ ] Inbound XM stubs use INBOUND-STUB notation
INT-C Gate: PASSED ✓
```
<!-- PHASE:INTC:END -->

---

<!-- PHASE:INTR:START -->
## PHASE INT-R — Runtime Activation Status
─────────────────────────────────────────────────────────────────
Gate Required    : INT-C ✓
Gate This Phase  : INT-R ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

```
## INT-R STATUS — File Service — PLAN-ID: PLAN-FILE-001
══════════════════════════════════════════════════════════════════════════
XM-ID │ Status │ Workaround / Mock Strategy
──────┼────────┼────────────────────────────
—     │ —      │ — (no outbound XM-IDs — N/A for this module)
══════════════════════════════════════════════════════════════════════════
```
<!-- PHASE:INTR:END -->

---

<!-- PHASE:F1:START -->
## PHASE F1 — Frontend Model Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : INT-R ✓ (F1 Prerequisite: DOC ✓ confirmed)
Gate This Phase  : F1 ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

### F1-MODEL — ENTITY-FILE-001 — FileDocument
─────────────────────────────────────────────────────────────────
Model name       : FileDocument (maps to DTO: FileDocumentSummaryResponse / FileUploadResponse)

Fields:
  fileDocumentPk    : number      — PK — system only, never displayed as input
  ownerId           : number      — context-supplied (hidden), not user input
  ownerType         : string      — context-supplied (hidden), not user input
  moduleCode        : string      — context-supplied (hidden), not user input
  fileCategoryFk    : number      — user-selected on upload (dropdown, filtered by moduleCode)
  fileTypeId        : string      — LOV field, system-detected — LOV-FILE-001, lookupKey: FILE_TYPE
  fileNameOriginal  : string      — display + as-uploaded
  mimeType          : string      — system-detected, display only
  fileSizeBytes     : number      — display only (formatted human-readable in component)
  fileStatusId      : string      — LOV field — LOV-FILE-002, lookupKey: FILE_STATUS
  createdBy         : string      — audit field
  createdAt          : Date       — audit field
  updatedBy          : string     — audit field
  updatedAt           : Date      — audit field
  ⚠ fileContent : NEVER modeled in TypeScript — binary, handled via File object /
    Blob at the HTTP layer only, never a model field.

Readonly fields  : fileDocumentPk, fileTypeId, mimeType, fileSizeBytes, createdBy, createdAt
⚠ No Business Code field — none exists for this entity (documented deviation).
⚠ orgUnitId NEVER in model. Audit fields use createdAt/updatedAt naming.
─────────────────────────────────────────────────────────────────

### F1-MODEL — ENTITY-FILE-002 — FileCategory (reference only — no CRUD screen, DRV-FILE-001)
─────────────────────────────────────────────────────────────────
Model name       : FileCategory (maps to DTO: FileCategoryOptionResponse — used ONLY as a
                    dropdown source on SCR-FILE-001, no entry form exists per DRV-FILE-001)

Fields (read-only projection, dropdown use only):
  fileCategoryPk : number  — value
  categoryCode   : string  — not displayed (internal)
  nameAr / nameEn: string  — dropdown label (locale-aware)
  moduleCode     : string  — used client-side to filter options for the hosting screen
─────────────────────────────────────────────────────────────────

### F1-SCREEN — SCR-FILE-001 — Attachment Panel
─────────────────────────────────────────────────────────────────
Screen type      : COMPOSITE (embedded panel — not Search+Entry; PATTERN-2 Inline/Modal)
Entity           : ENTITY-FILE-001 (+ ENTITY-FILE-002 for dropdown selection only)

List Model — FileDocumentListView (no dedicated Search filter — always scoped to
  hosting screen's ownerId/ownerType, per SRS B2/B3):
  Result columns   : fileNameOriginal, fileCategoryFk (resolved name), fileTypeId,
                      fileSizeBytes, createdAt
  Pagination       : page (number), size (number) — via API-FILE-005

Upload Form Model — FileUploadFormModel:
  Form fields:
    file            : File (browser File object)  REQUIRED
    fileCategoryFk  : number                        REQUIRED — LOV-driven dropdown,
                      filtered client-side by hosting screen's moduleCode
    ownerId/ownerType/moduleCode : hidden, supplied by hosting screen — NOT user input
  Excluded         : fileDocumentPk, fileTypeId, mimeType, fileSizeBytes, fileStatusId
                     (all system-managed / post-upload only)
─────────────────────────────────────────────────────────────────
<!-- PHASE:F1:END -->

---

<!-- PHASE:F2:START -->
## PHASE F2 — Frontend Service Contract Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : F1 ✓
Gate This Phase  : F2 ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

### F2-SERVICE blocks (one per API-ID) — AMEND-P3-C fields
```
API-FILE-001 — FileTokenService.requestUploadToken(ownerId, ownerType, moduleCode, fileCategoryFk)
  Observable type   : Observable<FileUploadTokenResponse>
  Error handling    : ERR-FILE-0009 (404, category not found) — routed to inline
                      form error on the upload control; no ERR-FILE-0004 possible
                      here (token not yet issued)
  Loading state     : LOCAL — spinner on the upload control only, does not block
                      the rest of SCR-FILE-001
  Caching strategy  : NONE — token is single-use and time-bound; never cached
  XM-ID impact      : None — no XM dependency on this call path

API-FILE-002 — FileTokenService.uploadFile(encryptedToken, file: File)
  Observable type   : Observable<FileUploadResponse> (multipart)
  Error handling    : ERR-FILE-0001 (400 size), ERR-FILE-0002 (401 TTL),
                      ERR-FILE-0003 (401/403 invalid token), ERR-FILE-0004 (401
                      single-use) — all routed to inline error banner on the
                      upload control
  Loading state     : LOCAL — upload progress indicator on the upload control;
                      does not block the file list below it
  Caching strategy  : NONE — write operation, never cached; triggers a files-list
                      refetch (cache-busting) on success
  XM-ID impact      : None — no XM dependency on this call path

API-FILE-003 — FileTokenService.downloadFile(encryptedToken)
  Observable type   : Observable<Blob> (+ headers for filename/mime)
  Error handling    : ERR-FILE-0002 (401 TTL), ERR-FILE-0003 (401/403 invalid
                      token), ERR-FILE-0008 (410 file no longer available) —
                      routed to a toast (does not interrupt the list view)
  Loading state     : LOCAL — spinner on the specific row's download action only
  Caching strategy  : NONE — binary content, never cached client-side; each
                      download re-issues a fresh single-use token (RULE-FILE-004)
  XM-ID impact      : None — no XM dependency on this call path

API-FILE-004 — FileTokenService.deleteFile(encryptedToken)
  Observable type   : Observable<FileDeleteConfirmation>
  Error handling    : ERR-FILE-0002 (401 TTL), ERR-FILE-0003 (401/403 invalid
                      token), ERR-FILE-0007 (403 ownership/Admin) — routed to
                      inline error banner
  Loading state     : LOCAL — spinner on the specific row's delete action only
  Caching strategy  : NONE — write operation; triggers a files-list refetch on success
  XM-ID impact      : None on THIS module's own call path. Downstream awareness
                      only: a future consumer holding XM-NOTIF-001 (or any other
                      inbound stub) against the deleted FileDocument must handle
                      fileStatusId=DELETED per srs-file-001.md A7 — not this
                      facade's concern to enforce.

API-FILE-005 — FileDocumentService.listByOwner(ownerId, ownerType?, page, size)
  Observable type   : Observable<Page<FileDocumentSummaryResponse>>
  Error handling    : none beyond platform-standard (empty result → HTTP 200,
                      never surfaced as an error — DOC-3)
  Loading state     : LOCAL — spinner on the file list region only, scoped to
                      SCR-FILE-001's embedded panel (never GLOBAL — this is a
                      panel embedded in a host screen)
  Caching strategy  : SHORT-LIVED (facade-level, in-memory only, invalidated on
                      any upload/delete success within the same panel instance) —
                      NOT a persisted/HTTP-level cache
  XM-ID impact      : None — no XM dependency on this call path
```

### F2-LOV-SERVICE blocks
```
LOV-FILE-001 → LovService.loadOptions('FILE_TYPE')   → Observable<LovOption[]>
               (used for display formatting only — not a user-editable dropdown)
               Loading state: LOCAL (label resolution only) | Caching: SESSION
               (lookups are platform-shared and rarely change — standard LovService
               session cache, not module-specific)
LOV-FILE-002 → LovService.loadOptions('FILE_STATUS')  → Observable<LovOption[]>
               Loading state: LOCAL | Caching: SESSION (same as above)
FileCategory dropdown → FileCategoryService.listOptionsByModule(moduleCode)
               → Observable<FileCategoryOptionResponse[]> (via a lightweight options
               endpoint — NOT a governed LOV, but structurally identical pattern —
               DRV note: reuses LOV-style loader against FileCategory table, not
               MD_LOOKUP_DETAIL, since FileCategory is a Reference Table per DATA+DOM)
               Loading state: LOCAL | Caching: SESSION (per moduleCode)

⚠ CHECK-9.2 WAIVER CANDIDATE: LOV-FILE-001 and LOV-FILE-002 are consumed exclusively
  via the platform-shared GET /api/lookups/{lookupKey} endpoint (owned by
  MasterData), not a File-Service-owned B2 endpoint. This is the platform's
  centralized-lookup architecture (consistent with the DBS-ORG-001 precedent), not
  a File-Service-specific gap. Flagged for formal reconciliation between CHECK-9.2
  and the shared-lookup-service pattern at the governance-framework level — not an
  action item for this plan.
```

### F2-SCREEN-INIT / F2-FACADE — SCR-FILE-001
```
Facade name      : FileAttachmentFacade
State owned      : files: signal<FileDocumentSummaryResponse[]>,
                    currentPage/pageSize (derived from last search request — never
                    independent state, per DOC-3), uploading: signal<boolean>,
                    categoryOptions: signal<FileCategoryOptionResponse[]>
Init sequence    : 1) load categoryOptions filtered by hosting moduleCode
                    2) call API-FILE-005 for current ownerId/ownerType → populate files
Operations       : upload() → API-FILE-001 then API-FILE-002 (two-step token flow) →
                    refresh files list
                    download(fileDocumentPk) → resolve token via API-FILE-001-equivalent
                    download-token flow (Section note: download uses its own token
                    issuance — see API-FILE-001 which issues tokens generically per
                    "action" embedded in payload) → API-FILE-003 → trigger browser save
                    delete(fileDocumentPk) → confirm dialog (RULE-FILE-006 message) →
                    issue delete token → API-FILE-004 → refresh files list
Error routing    : HTTP 400/401/403/410 → toast/inline message using messageAr/messageEn
                    from Error Catalog, keyed by ERR-ID returned in response body
```
<!-- PHASE:F2:END -->

---

<!-- PHASE:F3:START -->
## PHASE F3 — Frontend Validation Rule Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : F2 ✓
Gate This Phase  : F3 ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

### F3-VALIDATION — RULE-FILE-001 — Content size limit
─────────────────────────────────────────────────────────────────
Statement  : The system MUST reject any uploaded file content exceeding 5MB (default)
             or the FileCategory-specific maxSizeBytesOverride when set.
Message-AR : حجم الملف يتجاوز الحد المسموح به
Message-EN : File size exceeds the allowed limit
Scope      : CREATE
Field            : file (FileUploadFormModel)
DB Column        : FILE_SIZE_BYTES  DBF-0021
Validation type  : BUSINESS_RULE (client-side pre-check using selected FileCategory's
                    max size, mirrored server-side as authoritative)
When evaluated   : ON_CHANGE (file picker selection) — advisory; ON_SUBMIT is authoritative
ERR-ID           : ERR-FILE-0001
─────────────────────────────────────────────────────────────────

### F3-VALIDATION — RULE-FILE-002 — Token TTL
─────────────────────────────────────────────────────────────────
Statement  : The system MUST reject any request whose Encrypted Token exceeded its TTL.
Message-AR : انتهت صلاحية الرابط — يرجى طلب رابط جديد
Message-EN : This link has expired — please request a new one
Scope      : ALL
Field            : N/A (token layer, not a form field)
Validation type  : BUSINESS_RULE — server response HTTP 401 caught and shown
When evaluated   : ON_SUBMIT (any upload/download/delete action)
ERR-ID           : ERR-FILE-0002
─────────────────────────────────────────────────────────────────

### F3-VALIDATION — RULE-FILE-003 — Tampered/mismatched token
─────────────────────────────────────────────────────────────────
Statement  : The system MUST reject any request whose token is missing, tampered, or action-mismatched.
Message-AR : الرابط غير صالح
Message-EN : The link is invalid
Scope      : ALL
Validation type  : BUSINESS_RULE — server response HTTP 401/403 caught and shown
When evaluated   : ON_SUBMIT
ERR-ID           : ERR-FILE-0003
─────────────────────────────────────────────────────────────────

### F3-VALIDATION — RULE-FILE-005 — MIME detection from content
─────────────────────────────────────────────────────────────────
Statement  : The system MUST determine MIME type from content, not client Content-Type header.
Message-AR : نوع الملف يُحدَّد تلقائياً من محتواه
Message-EN : File type is automatically determined from its content
Scope      : CREATE
Field            : fileTypeId (display-only, post-upload)
Validation type  : BUSINESS_RULE (informational — no client-side rejection possible;
                    this is server-authoritative and surfaced only as post-upload display text)
When evaluated   : N/A (informational, shown after successful upload)
ERR-ID           : ERR-FILE-0005 (informational entry — Error Catalog, no HTTP status)
─────────────────────────────────────────────────────────────────

### F3-VALIDATION — RULE-FILE-006 — Permanent deletion (confirmation)
─────────────────────────────────────────────────────────────────
Statement  : The system MUST permanently delete the file's content — no recovery.
Message-AR : سيتم حذف الملف نهائياً ولا يمكن استرجاعه
Message-EN : The file will be permanently deleted and cannot be recovered
Scope      : DELETE
Field            : N/A — client-side confirm dialog before calling delete()
Validation type  : BUSINESS_RULE (UX confirmation gate, not a form validator)
When evaluated   : ON_SUBMIT (before dispatching delete request)
ERR-ID           : ERR-FILE-0006 (message-catalog entry; not an HTTP error)
─────────────────────────────────────────────────────────────────

### F3-VALIDATION — RULE-FILE-007 — Ownership/Admin restriction on delete
─────────────────────────────────────────────────────────────────
Statement  : The system MUST reject a delete request from a non-owner, non-Admin actor.
Message-AR : غير مصرح لك بحذف هذا الملف
Message-EN : You are not authorized to delete this file
Scope      : DELETE
Field            : N/A (server-enforced; client hides delete button per F3-SEC-RULE-1
                    when canDelete = false, but the composite ownership check is
                    server-side authoritative — see F3-SEC-RULE-1 below)
Validation type  : BUSINESS_RULE — server response HTTP 403 caught and shown
When evaluated   : ON_SUBMIT
ERR-ID           : ERR-FILE-0007
─────────────────────────────────────────────────────────────────

**F3 Business Code Rules:** N/A — neither entity in this module has a Business Code.

**F3 Localization Rules:** F3-LOC-RULE-1/2/3 apply as platform-standard (no deviation).

**F3 Permission-Based Field Behavior:**
```
F3-SEC-RULE-1 — SCR-FILE-001 field/button visibility governed by permissions loaded
  on init: canCreate=false → upload control hidden; canDelete=false → delete button
  hidden. RULE-FILE-007's ownership check is IN ADDITION to canDelete — a user may
  have PERM_FILE_ATTACHMENT_DELETE generally but still be rejected server-side if
  neither owner nor Admin (composite check, not purely permission-based).
```
<!-- PHASE:F3:END -->

---

<!-- PHASE:F4:START -->
## PHASE F4 — Frontend Routing & Component Structure
─────────────────────────────────────────────────────────────────
Gate Required    : F3 ✓
Gate This Phase  : F4 ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

### F4-SCREEN — SCR-FILE-001 — Attachment Panel
─────────────────────────────────────────────────────────────────
**Deviation notice (DRV-FILE-010, DRV-FILE-011 — see Derivation Log):**
SCR-FILE-001 is a SHARED, EMBEDDED component (Reference: dbs-file-001.md
Security Seed — `page_code = FILE_ATTACHMENT, parent_id_fk = NULL, "Shared
Component"`), not a top-level routed screen. It has no Search/Entry split
(F4-RULE-5 does not apply — not a PATTERN-1 Composite Screen) and no
dedicated route of its own (F4-RULE-1/3 adapted below — it is consumed
by embedding, not navigated to directly).

Route path       : NONE — SCR-FILE-001 has no standalone route. It is
                    embedded via `<app-file-attachment [ownerId]
                    [ownerType] [moduleCode]>` inside whichever host
                    screen's Entry view needs attachment support (e.g. a
                    Purchase Order Entry screen). The host screen's own
                    route (and its own AuthGuard/PermissionGuard) governs
                    navigation; this component carries no route of its own.

Module           : SharedComponentsModule — eagerly available to any
                    feature module that imports it (not a separately
                    lazy-loaded routed module, since it has no route)
Module path      : app/shared/components/file-attachment/

Route guard      : N/A at the route level (no route exists). Permission
                    enforcement instead happens at the COMPONENT level via
                    the Facade's init-time permission check (F2/SEC) —
                    canView/canCreate/canDelete gate rendering of the
                    upload control and delete buttons, not route access.
PERM_* required  : PERM_FILE_ATTACHMENT_VIEW (render the panel)
                    PERM_FILE_ATTACHMENT_CREATE (show upload control)
                    PERM_FILE_ATTACHMENT_DELETE (show delete buttons)
                    [sourced from the SEC phase Permissions Matrix — same
                    codes, no F4-only permission names]

Child routes     : NONE (embedded component — no child routing)

COMPONENTS:
  FileAttachmentComponent
    Path       : app/shared/components/file-attachment/file-attachment.component.ts
    Route      : N/A — embedded, not routed
    Facade     : FileAttachmentFacade (F2)
    Inputs     : ownerId: number, ownerType: string, moduleCode: string
                 (host screen supplies these — the only case in this
                 module where component context comes via @Input, since
                 FileAttachmentComponent is reached by composition, not
                 routing — F4-RULE-7 governs EntryComponent mode
                 resolution specifically and does not apply here, as this
                 is neither a SearchComponent nor an EntryComponent)

SharedModule imports : CommonModule, ReactiveFormsModule (upload form),
                        SharedUiModule (spinner/toast primitives)
─────────────────────────────────────────────────────────────────

**F4 Gate Checklist (self-check):**
```
[✓] SCR-FILE-001 has exactly one F4-SCREEN block
[N/A] No tree-bearing entity in this module — no TreeComponent required
[N/A] Route guards — no route exists (component-level permission gating
      documented above instead; DRV-FILE-011)
[✓] Every PERM_* referenced in F4 also appears in SEC's list for SCR-FILE-001
[✓] No component name uses "Page" or "Container" suffix
[N/A] Search/Entry separation — SCR-FILE-001 is not a PATTERN-1 Composite
      Screen (DRV-FILE-010)
[N/A] EntryComponent mode resolution — no EntryComponent exists in this module
```
<!-- PHASE:F4:END -->

---

<!-- PHASE:SEC:START -->
## PHASE SEC — Security Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : F4 ✓
Gate This Phase  : SEC ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

### SEC — SCR-FILE-001 — Attachment Panel
─────────────────────────────────────────────────────────────────
Screen guard     : canView = true required for the panel to render within its host screen.
Permission-based UI behavior:
  canView   = false → panel not rendered

  canCreate = false → upload control hidden
  canEdit   = false → N/A (no Update operation exists for FileDocument)
  canDelete = false → delete button hidden
  canApprove= false → N/A (no approval workflow)

API-level enforcement: API-FILE-002 requires PERM_FILE_ATTACHMENT_CREATE (+ token
  validity); API-FILE-003/005 require PERM_FILE_ATTACHMENT_VIEW (+ token validity for
  003); API-FILE-004 requires PERM_FILE_ATTACHMENT_DELETE (+ token validity +
  RULE-FILE-007 ownership/Admin check — composite, not permission-alone).

EXCEPTION module scope: This module has NO JWT validation of its own (POLICY-CLI-06).
  Screen-level permission checks (canView/canCreate/canDelete) are still evaluated
  against Security's standard permission model — the deviation is only that the
  Encrypted Token layer (not JWT) additionally gates /upload, /download, /{token}
  routes.
─────────────────────────────────────────────────────────────────

SECURITY SEED DATA REQUIREMENTS (already present in dbs-file-001.md Block "SECURITY SEED"
— referenced here, not redefined):
```
SEC_PAGES  : page_code = FILE_ATTACHMENT, parent_id_fk = NULL (Shared Component)
PERMISSIONS: PERM_FILE_ATTACHMENT_VIEW / CREATE / UPDATE / DELETE
  PERM_FILE_ATTACHMENT_UPDATE is auto-generated (CORE-9) but functionally unused —
  no Update API exists for FileDocument (documented in SRS B4).
```

SEC Governance Rules: SEC-IMPL-RULE-1..4 apply as platform-standard (no deviation).
<!-- PHASE:SEC:END -->

---

<!-- PHASE:TEST:START -->
## PHASE TEST — TC Coverage Matrix Summary (SECTION D)
─────────────────────────────────────────────────────────────────
Gate Required    : SEC ✓
Gate This Phase  : SECTION D ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

```
TC COVERAGE MATRIX SUMMARY — File Service — PLAN-ID: PLAN-FILE-001
══════════════════════════════════════════════════════════════════
RULE-ID COVERAGE:
RULE-ID          │ Happy path TC     │ Violation TC      │ Status
─────────────────┼───────────────────┼───────────────────┼──────────────
RULE-FILE-001    │ TC-FILE-001       │ TC-FILE-002       │ COVERED ✓
RULE-FILE-002    │ —                 │ TC-FILE-003       │ COVERED ✓ (violation-only rule)
RULE-FILE-003    │ —                 │ TC-FILE-004       │ COVERED ✓ (violation-only rule)
RULE-FILE-004    │ —                 │ TC-FILE-005       │ COVERED ✓ (violation-only rule)
RULE-FILE-005    │ TC-FILE-006       │ —                 │ COVERED ✓ (detection-only, no violation path)
RULE-FILE-006    │ TC-FILE-007       │ —                 │ COVERED ✓ (behavioral — content purge verified)
RULE-FILE-007    │ —                 │ TC-FILE-008       │ COVERED ✓ (violation-only rule)
──────────────────────────────────────────────────────────────────
Rule coverage    : 7 / 7 covered — 0 deferred — 0 gaps

API-ID COVERAGE:
API-ID           │ Success TC        │ Status
─────────────────┼───────────────────┼──────────────
API-FILE-001     │ TC-FILE-009       │ COVERED ✓
API-FILE-002     │ TC-FILE-001       │ COVERED ✓ (shared with RULE-FILE-001 happy path)
API-FILE-003     │ TC-FILE-010       │ COVERED ✓
API-FILE-004     │ TC-FILE-007       │ COVERED ✓ (shared with RULE-FILE-006)
API-FILE-005     │ TC-FILE-011       │ COVERED ✓
──────────────────────────────────────────────────────────────────
API coverage     : 5 / 5 covered — 0 deferred

DEFERRED TC REGISTRY: None.
══════════════════════════════════════════════════════════════════
Gate SECTION D: PASSED ✓ — no GAP ✗ entries.
```
<!-- PHASE:TEST:END -->

---

<!-- PHASE:ALIGN:START -->
## ALIGN GATE — File Service — PLAN-ID: PLAN-FILE-001
═══════════════════════════════════════════════════════════════════════════
TRACEABILITY CHECKS                                        │ Status
All FIELD-IDs used in phases appear in Plan Index          │ ✓
All API-IDs used in phases appear in Plan Index            │ ✓
All RULE-IDs used in phases appear in Plan Index           │ ✓
All ERR-IDs used in F3/SECTION D appear in Error Catalog        │ ✓
All QR-IDs in QRC appear in Plan Index QRC Summary         │ ✓ (corrected to QR-FILE-001..007, see SVC+API note)
Derivation Log complete — no undocumented inferences       │ ✓ — DRV-FILE-001..006
DB Structural Alignment confirms field coverage            │ ✓ — 27/27 DBF-IDs bound
───────────────────────────────────────────────────────────┼──────────────
SCREEN STRUCTURE CHECKS                                    │ ✓ (1/1 SCR-ID, F1/F2/F3/F4/SEC all present;
                                                             │   F4/F5 route-level N/A items documented via
                                                             │   DRV-FILE-010/011 — embedded component, no route)
LOV / LOOKUP CHECKS                                        │ ✓ (2/2 LOV-IDs, both String-typed; CHECK-9.2 WAIVER
                                                             │   CANDIDATE flagged — see F2-LOV-SERVICE note)
BUSINESS CODE CHECKS                                       │ N/A — no entity in this module has a Business Code (documented)
LOCALIZATION CHECKS                                        │ ✓ — all RULE-IDs with user-facing text have Message-AR + ERR-ID
SECURITY CHECKS                                            │ ✓
QUERY REFERENCE CATALOG CHECKS                              │ ✓ — 7 QR-IDs, all agent-reference labeled, all with
                                                             │   REPOSITORY STRATEGY (Fetch/Bulk) per AMEND-P3-B
TEST COVERAGE CHECKS                                        │ ✓ — SECTION D present, 0 gaps
CROSS-MODULE DEPENDENCY CHECKS                               │ ✓ — 0 outbound XM-IDs; 3 inbound stubs documented
ARTIFACT BINDING CHECKS                                      │ ✓ — no placeholders; all sequence/column names exact
PLAN COMPLETENESS CHECKS                                     │ ✓
═══════════════════════════════════════════════════════════════════════════
ALIGN GATE RESULT: PASSED ✓
Auto-correction applied: QR-ID renumbering (QR-FILE-006 UPDATE reused →
  API-FILE-005's search operation reassigned to QR-FILE-007) — DRV-FILE-007
  (GOVERNANCE EXCEPTION re-numbering event, PRINCIPLE-8 compliant — see
  Derivation Log; supersedes the prior non-standard "DRV-FILE-006-B" label
  raised in 4A-FILE-001-004).
═══════════════════════════════════════════════════════════════════════════

**Table 2 — Operations Coverage (F4 Route column added per AMEND-P3-J):**
```
Operation │ API-ID       │ UI Action (SCR-ID)                    │ F4 Route            │ TC-ID       │ QR-ID       │ XM-ID │ Status
──────────┼──────────────┼────────────────────────────────────────┼─────────────────────┼─────────────┼─────────────┼───────┼───────
Issue token│ API-FILE-001 │ SCR-FILE-001 Upload control (init)    │ N/A — embedded, no route (DRV-FILE-011) │ TC-FILE-015 │ QR-FILE-001 │ —     │ ✓
Upload    │ API-FILE-002 │ SCR-FILE-001 Upload control            │ N/A — embedded, no route (DRV-FILE-011) │ TC-FILE-016 │ QR-FILE-003 │ —     │ ✓
Download  │ API-FILE-003 │ SCR-FILE-001 Download action per row   │ N/A — embedded, no route (DRV-FILE-011) │ TC-FILE-017 │ QR-FILE-004 │ —     │ ✓
Delete    │ API-FILE-004 │ SCR-FILE-001 Delete action per row     │ N/A — embedded, no route (DRV-FILE-011) │ TC-FILE-018 │ QR-FILE-005,006 │ — │ ✓
List      │ API-FILE-005 │ SCR-FILE-001 Panel init (list view)    │ N/A — embedded, no route (DRV-FILE-011) │ TC-FILE-019 │ QR-FILE-007 │ —     │ ✓
```
Note: all F4 Route cells are explicitly "N/A — embedded, no route" (not blank) —
this satisfies the ALIGN gate's requirement that a missing F4 Route not be
silently blank; the N/A is itself sourced from the F4-SCREEN block's documented
DRV-FILE-011 deviation, not an omission.

**Table 4 — XM Dependency Gate:**
```
XM-ID │ Type │ Status │ Blocks │ Workaround
—     │ —    │ —      │ —      │ — (no outbound XM-IDs for this module)
```
<!-- PHASE:ALIGN:END -->

---

## DERIVATION LOG (controlled inference — Section 6.4)

```
DRV-FILE-001 │ FileCategory has no CRUD API/screen in SRS despite A3 declaring
             │ Create/Update/Deactivate ops. Resolved: Phase-1 admin is via
             │ consuming-module seed data (business-policies-file.md). No API
             │ invented. Not a blocking gap.
DRV-FILE-002 │ ERR-FILE-0009 (fileCategoryFk not found, API-FILE-001) classified
             │ as PLATFORM-STD (HTTP 404 via LocalizedException) — infrastructure
             │ concern, not a client-facing business RULE-ID. Originally mis-encoded
             │ as ERR-FILE-0007 with a slash-combined RULE-ID field
             │ ("RULE-FILE-007 / PLATFORM-STD") — corrected under DRV-FILE-008
             │ below (4A-FILE-001-003).
DRV-FILE-003 │ Download of a DELETED file (RULE-FILE-006 governance note) returns
             │ HTTP 410 GONE with a "file no longer available" message rather than
             │ streaming null content — inferred directly from srs-file-001.md A7's
             │ explicit instruction to consumers ("عرض رسالة مناسبة... بدل خطأ FK").
DRV-FILE-004 │ QR-FILE-006 (content purge) is a compound 2-column UPDATE, EAGER
             │ fetch (entity loaded in full before the purge, not lazily proxied) —
             │ documented per RULE-REPO-DRV; no native query required.
DRV-FILE-005 │ QR-FILE-007 (list files) joins FILE_DOCUMENT → FILE_CATEGORY to
             │ resolve category display name — justified by SRS B3 "عرض القائمة"
             │ column requirement (category name, not just its FK).
DRV-FILE-006 │ No action taken in this plan for XM-INBOUND-STUB-FILE-1
             │ (Notification's XM-NOTIF-001) — unblock/migration is entirely
             │ Notification Service's own P3 execution work per
             │ business-policies-file.md.
DRV-FILE-007 │ GOVERNANCE EXCEPTION re-numbering event (PRINCIPLE-8): QR-ID
             │ sequencing correction — API-FILE-005's search operation is
             │ QR-FILE-007, not a reuse of QR-FILE-006 (already assigned to the
             │ delete-purge UPDATE in API-FILE-004). Total QR-IDs = 7. This entry
             │ formally supersedes the non-standard "DRV-FILE-006-B" suffix label
             │ originally used inline (flagged 4A-FILE-001-004) — now assigned its
             │ own sequential 3-digit DRV-ID per PRINCIPLE-8 ID Continuity.
DRV-FILE-008 │ CORRECTION (4A-FILE-001-003, CHECK-2.6 compliance): ERR-FILE-0007
             │ and ERR-FILE-0008 originally carried dual-cause RULE-ID field values
             │ ("RULE-FILE-007 / PLATFORM-STD" and "DRV-FILE-003 (PLATFORM-STD)"
             │ respectively) — not permitted; RULE-ID must be exactly one of
             │ "RULE-[MOD]-N" or "PLATFORM-STD". Resolved: ERR-FILE-0007 now
             │ carries RULE-ID = RULE-FILE-007 only (403, API-FILE-004 ownership
             │ check). The PLATFORM-STD 404 case (API-FILE-001, fileCategoryFk not
             │ found) is split out to a new ERR-ID, ERR-FILE-0009, RULE-ID =
             │ PLATFORM-STD only. ERR-FILE-0008 now carries RULE-ID = PLATFORM-STD
             │ only; its DRV-FILE-003 cross-reference lives in this Derivation Log
             │ exclusively, not in the Error Catalog's RULE-ID cell.
DRV-FILE-009 │ CHECK-9.2 WAIVER CANDIDATE (4A-FILE-001-008): LOV-FILE-001 and
             │ LOV-FILE-002 are consumed via the platform-shared
             │ GET /api/lookups/{lookupKey} endpoint (MasterData-owned), not a
             │ File-Service-owned B2 endpoint — a platform-wide centralized-lookup
             │ architecture decision (DBS-ORG-001 precedent), not a module-specific
             │ defect. Flagged for the governance framework to formally reconcile
             │ CHECK-9.2 with the shared-lookup-service pattern; no plan-level
             │ action required.
DRV-FILE-010 │ F4-RULE-5 (PATTERN-1 Search/Entry separation) N/A for
             │ SCR-FILE-001 — it is a COMPOSITE embedded panel (PATTERN-2
             │ Inline/Modal per F1), not a PATTERN-1 Composite Screen. No
             │ SearchComponent/EntryComponent split exists or is required.
DRV-FILE-011 │ SCR-FILE-001 has no top-level route (F4-RULE-1/3 adapted) —
             │ it is a SHARED, embedded component consumed via composition
             │ by host screens' Entry views (dbs-file-001.md Security Seed
             │ confirms `page_code=FILE_ATTACHMENT, parent_id_fk=NULL,
             │ "Shared Component"`). Route-level AuthGuard/PermissionGuard
             │ (F4-RULE-3) do not apply; permission enforcement instead
             │ happens at the component level via the Facade's init-time
             │ check (same PERM_* codes as SEC — no new codes invented).
```

---

## ERROR CATALOG — File Service — PLAN-ID: PLAN-FILE-001

```
══════════════════════════════════════════════════════════════════════════════════════════
ERR-ID        │ RULE-ID       │ API-ID       │ HTTP │ Message-AR                              │ Message-EN
──────────────┼───────────────┼──────────────┼──────┼──────────────────────────────────────────┼───────────────────────────────────────
ERR-FILE-0001 │ RULE-FILE-001 │ API-FILE-002 │ 400  │ حجم الملف يتجاوز الحد المسموح به          │ File size exceeds the allowed limit
ERR-FILE-0002 │ RULE-FILE-002 │ API-FILE-002/003/004 │ 401 │ انتهت صلاحية الرابط — يرجى طلب رابط جديد │ This link has expired — please request a new one
ERR-FILE-0003 │ RULE-FILE-003 │ API-FILE-002/003/004 │ 401/403 │ الرابط غير صالح                    │ The link is invalid
ERR-FILE-0004 │ RULE-FILE-004 │ API-FILE-001 │ 401  │ هذا الرابط مُستخدَم مسبقاً                 │ This link has already been used
ERR-FILE-0005 │ RULE-FILE-005 │ API-FILE-002 │ N/A (informational) │ نوع الملف يُحدَّد تلقائياً من محتواه │ File type is automatically determined from its content
ERR-FILE-0006 │ RULE-FILE-006 │ API-FILE-004 │ N/A (client confirm) │ سيتم حذف الملف نهائياً ولا يمكن استرجاعه │ The file will be permanently deleted and cannot be recovered
ERR-FILE-0007 │ RULE-FILE-007 │ API-FILE-004 │ 403 │ غير مصرح لك بحذف هذا الملف │ You are not authorized to delete this file
ERR-FILE-0008 │ PLATFORM-STD │ API-FILE-003 │ 410 │ الملف لم يعد متاحاً │ This file is no longer available
ERR-FILE-0009 │ PLATFORM-STD │ API-FILE-001 │ 404 │ الفئة غير موجودة │ Category not found
══════════════════════════════════════════════════════════════════════════════════════════
Total Errors: 9
Note: RULE-ID field carries exactly one value per CHECK-2.6 ("RULE-[MOD]-N" or
"PLATFORM-STD"). Dual-cause cross-references (e.g. ERR-FILE-0008 → DRV-FILE-003,
ERR-FILE-0002/0003 duality across TTL vs. tamper) live in the Derivation Log only.
```

---

## QUERY REFERENCE CATALOG (FULL — AGENT REFERENCE)

```
╔══════════════════════════════════════════════════════════════════╗
║  ⚠ AGENT REFERENCE ONLY — REWRITE using actual JPA entity/field   ║
║  names and the project's query strategy. These entries express    ║
║  INTENT — not code to copy-paste.                                 ║
╚══════════════════════════════════════════════════════════════════╝

QR-FILE-001 │ FIND FileCategory by PK       │ Table: FILE_CATEGORY │ READ_ONLY
QR-FILE-002 │ FIND FileDocument by PK       │ Table: FILE_DOCUMENT │ READ_ONLY
QR-FILE-003 │ SAVE FileDocument             │ Table: FILE_DOCUMENT │ READ_WRITE │ Seq: SEQ_FILE_DOCUMENT.NEXTVAL
QR-FILE-004 │ FIND FileDocument by PK (dl)  │ Table: FILE_DOCUMENT │ READ_ONLY
QR-FILE-005 │ FIND FileDocument by PK (del) │ Table: FILE_DOCUMENT │ READ_ONLY
QR-FILE-006 │ UPDATE FileDocument (purge)   │ Table: FILE_DOCUMENT │ READ_WRITE │ compound 2-column update
QR-FILE-007 │ FIND_BY_CRITERIA FileDocument WHERE OWNER_ID=:ownerId [AND OWNER_TYPE=:ownerType]
            │ Table: FILE_DOCUMENT LEFT JOIN FILE_CATEGORY │ READ_ONLY │ Index: IDX_FILE_DOCUMENT_OWNER
```

---

## REGISTRY UPDATE — 2026-07-11

```
────────────────────────────────────────────────────────────────
Source Mode    : MODE 2
Feature Code   : FILE-001
DBS-ID         : DBS-FILE-001
Plan ID        : PLAN-FILE-001
────────────────────────────────────────────────────────────────
New Entities   : — (both already governed at MODE 1/1.5)
New Tables     : — (already governed at MODE 1.5)
New Lookups    : — (FILE_TYPE, FILE_STATUS already seeded)
New APIs       : API-FILE-001..005
QR-IDs Created : QR-FILE-001..007 (7)
XM-IDs Open    : None outbound. Inbound: XM-NOTIF-001 (Notification, DEFERRED —
                 tracked in dbs-notif-001.md, not this module's register)
OQ-IDs Open    : None
Gate Status    : ALIGN PASSED ✓
Next Action    : Trigger MODE 4A — Governance Audit Engine (Project 4), then
                 MODE 2.5 — generate test-plan.md for File Service
────────────────────────────────────────────────────────────────
```

---

## PLAN COMPLETION BLOCK

```
╔══════════════════════════════════════════════════════════════════╗
║           EXECUTION PLAN — STAGE 1 COMPLETE ✓                    ║
╠═══════════════════════╦══════════════════════════════════════════╣
║ Plan Name             ║ New Feature — Attachment Panel — File Service ║
║ Plan ID               ║ PLAN-FILE-001                            ║
║ Output                ║ STAGE 1 — execution-plan.md Agent-Ready  ║
║ Phases Complete       ║ CORE✓ DATA+DOM✓ SVC+API✓ DOC✓ INT-C✓   ║
║                       ║ INT-R✓ F1✓ F2✓ F3✓ F4✓ SEC✓ SECTION D✓ ALIGN✓ ║
║ TC Coverage Summary   ║ SECTION D present — 7/7 rules, 5/5 APIs covered — see test-plan.md ║
║ Open Questions        ║ None                                     ║
║ XM DEFERRED           ║ 0 outbound. 1 inbound stub active (XM-NOTIF-001, tracked externally) ║
║ Blocked Elements      ║ None                                     ║
║ QR-IDs Generated      ║ 7 — see Query Reference Catalog          ║
║ Next Stage            ║ MODE 2.5 → generate test-plan.md         ║
╠═══════════════════════╩══════════════════════════════════════════╣
║  ⚠ AGENT INSTRUCTIONS                                            ║
║  1. Read the full plan before writing any code                   ║
║  2. Rewrite ALL Query Reference Catalog entries from scratch      ║
║  3. Follow architectural policies declared in CORE phase          ║
║  4. Apply all Business Rules declared in DATA+DOM/SVC+API phases  ║
║  5. Implement the Encrypted Token layer per CORE's module-specific║
║     policy — before controller layer, for /upload/{token},        ║
║     /download/{token}, /{token} routes only                       ║
║  6. Implement security checks per SEC phase                       ║
║  7. Write tests per test-plan.md (separate file, MODE 2.5)        ║
╚══════════════════════════════════════════════════════════════════╝
```

---
*End of execution-plan-file-001.md*
*Governed by: Execution Plan Governance Engine (Project 3, v2)*
*PLAN-ID: PLAN-FILE-001 | DBS-ID: DBS-FILE-001 | Feature Code: FILE-001*
*Next Mode: MODE 4A (Pre-flight audit) → MODE 3 (Agent execution) | MODE 2.5 (test-plan.md)*
