<!-- Source: PHASE:CORE -->

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
