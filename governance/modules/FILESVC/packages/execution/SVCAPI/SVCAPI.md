<!-- Source: PHASE:SVCAPI -->

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
