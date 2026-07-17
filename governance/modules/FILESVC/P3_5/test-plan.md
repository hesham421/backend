<!-- PHASE:HEADER:START -->
# TEST-PLAN — File Service — PLAN-ID: PLAN-FILE-001
══════════════════════════════════════════════════════════════════
Source artifacts:
  execution-plan.md : PLAN-FILE-001 — Gate ALIGN ✓ confirmed (post-4A-correction)
  srs.md             : srs-file-001.md
  db-script.md       : dbs-file-001.md
Open Questions: None
══════════════════════════════════════════════════════════════════

## MODE 2.5 ENTRY GATE

```
╔══════════════════════════════════════════════════════════════════╗
║                MODE 2.5 — TEST PLAN ENTRY GATE                   ║
╠══════════════════════════════════════════════════════════════════╣
║ execution-plan.md uploaded?          ║ ✓                         ║
║ Gate ALIGN ✓ confirmed?              ║ ✓                         ║
║ srs.md uploaded?                     ║ ✓                         ║
║ db-script.md uploaded?               ║ ✓                         ║
╠══════════════════════════════════════════════════════════════════╣
║ Entry Gate: PASSED ✓ — test-plan.md generation proceeds          ║
╚══════════════════════════════════════════════════════════════════╝
```

## TARGET TC COUNT

```
MARK:JUNIT       : (7 RULE-IDs × ~2) + (5 API-IDs × 1) + 2 edge + 4 mandatory ≈ 25 TC
MARK:PLAYWRIGHT  : (1 SCR-ID × 3) + 2 mandatory + 2 INT Flow ≈ 6 TC
─────────────────────────────────────────────────────────────────
Total target      : 31 TC — within 25–40 target range ✓ (not over-engineered)
```

---
<!-- PHASE:HEADER:END -->

<!-- PHASE:JUNIT:START -->
<!-- MARK:JUNIT:START -->
## MARK:JUNIT — Backend tests executed via JUnit

Threshold check: 25 TCs > 12 → SUB split applied (RULE-SCENARIOS / API-SCENARIOS).

<!-- SUB:RULE-SCENARIOS:START -->
### SUB:RULE-SCENARIOS — Per-RULE-ID coverage (TP-SEC-1)

<!-- TC:TC-FILE-001:START -->
```
TC-FILE-001 — Upload within size limit succeeds
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-002
RULE-ID      : RULE-FILE-001
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Valid Encrypted Token issued for a FileCategory with no
                maxSizeBytesOverride (default 5MB ceiling applies); a
                file content payload of 2MB
When          : POST /upload/{encryptedToken} with the 2MB file
Then          : HTTP 201 (or 200) — FileUploadResponse returned with
                fileSizeBytes = 2097152, fileStatusId = "ACTIVE"

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-001:END -->

<!-- TC:TC-FILE-002:START -->
```
TC-FILE-002 — Upload exceeding size limit rejected
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-002
RULE-ID      : RULE-FILE-001
SCR-ID       : SCR-FILE-001
ERR-ID       : ERR-FILE-0001
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : Valid Encrypted Token issued for a FileCategory with
                default 5MB ceiling; a file content payload of 6MB
When          : POST /upload/{encryptedToken} with the 6MB file
Then          : HTTP 400 — ERR-FILE-0001 returned; no FileDocument row
                persisted (QR-FILE-003 not committed)

ERR-ID        : ERR-FILE-0001
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-002:END -->

<!-- TC:TC-FILE-003:START -->
```
TC-FILE-003 — Upload exactly at size limit boundary succeeds
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-002
RULE-ID      : RULE-FILE-001
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Boundary
Data class    : BOUNDARY

Given         : Valid Encrypted Token; FileCategory.maxSizeBytesOverride =
                1,048,576 (1MB, explicit numeric threshold)
When          : POST /upload/{encryptedToken} with a file of exactly
                1,048,576 bytes
Then          : HTTP 201 — upload succeeds (limit is inclusive, "exceeding"
                per RULE-FILE-001 wording means > threshold, not ≥)

ERR-ID        : —
Language      : —
Test-Hint     : RULE-FILE-001 has an explicit numeric threshold
                (maxSizeBytesOverride) — boundary trigger satisfied
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-003:END -->

<!-- TC:TC-FILE-004:START -->
```
TC-FILE-004 — Action within token TTL succeeds
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-003
RULE-ID      : RULE-FILE-002
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Encrypted Token issued 5 minutes ago (well within the
                100-minute TTL) for a valid ACTIVE FileDocument
When          : GET /download/{encryptedToken}
Then          : HTTP 200 — binary stream returned with correct
                Content-Type/Content-Disposition headers

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-004:END -->

<!-- TC:TC-FILE-005:START -->
```
TC-FILE-005 — Expired token rejected
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-003
RULE-ID      : RULE-FILE-002
SCR-ID       : SCR-FILE-001
ERR-ID       : ERR-FILE-0002
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : Encrypted Token issued 101 minutes ago (TTL = 100 min, expired)
When          : GET /download/{encryptedToken}
Then          : HTTP 401 — ERR-FILE-0002 returned before reaching
                business logic (token layer rejects pre-controller)

ERR-ID        : ERR-FILE-0002
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-005:END -->

<!-- TC:TC-FILE-006:START -->
```
TC-FILE-006 — Token exactly at TTL boundary
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-003
RULE-ID      : RULE-FILE-002
SCR-ID       : SCR-FILE-001
ERR-ID       : ERR-FILE-0002
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Boundary
Data class    : BOUNDARY

Given         : Encrypted Token issued exactly 100 minutes ago (TTL edge)
When          : GET /download/{encryptedToken}
Then          : HTTP 401 — ERR-FILE-0002 (TTL is a hard ceiling — a
                request AT the 100-minute mark is treated as expired,
                not the last valid instant)

ERR-ID        : ERR-FILE-0002
Language      : BOTH
Test-Hint     : RULE-FILE-002 has an explicit numeric threshold
                (TTL = 100 minutes) — boundary trigger satisfied
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-006:END -->

<!-- TC:TC-FILE-007:START -->
```
TC-FILE-007 — Valid, non-tampered token accepted
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-004
RULE-ID      : RULE-FILE-003
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Encrypted Token correctly generated for the "delete" action,
                unmodified, matching endpoint
When          : DELETE /{encryptedToken}
Then          : HTTP 200 — proceeds to RULE-FILE-007 ownership check
                (not rejected at the token layer)

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-007:END -->

<!-- TC:TC-FILE-008:START -->
```
TC-FILE-008 — Tampered / action-mismatched token rejected
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-004
RULE-ID      : RULE-FILE-003
SCR-ID       : SCR-FILE-001
ERR-ID       : ERR-FILE-0003
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Security attack
Data class    : ATTACK

Given         : (a) A token whose GCM tag is deliberately corrupted by 1 byte;
                (b) a valid "download" token replayed against the DELETE endpoint
When          : DELETE /{encryptedToken} using each of (a) and (b)
Then          : HTTP 401 (tampered, case a) / HTTP 403 (action-mismatch,
                case b) — ERR-FILE-0003 in both cases

ERR-ID        : ERR-FILE-0003
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-008:END -->

<!-- TC:TC-FILE-009:START -->
```
TC-FILE-009 — First use of a fresh token succeeds
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-002
RULE-ID      : RULE-FILE-004
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : A newly issued upload Encrypted Token, never previously
                consumed
When          : POST /upload/{encryptedToken} with a valid file
Then          : HTTP 201 — upload succeeds; token is marked consumed
                internally (single-use cache/marker set)

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-009:END -->

<!-- TC:TC-FILE-010:START -->
```
TC-FILE-010 — Reuse of an already-consumed token rejected
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-002
RULE-ID      : RULE-FILE-004
SCR-ID       : SCR-FILE-001
ERR-ID       : ERR-FILE-0004
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : The same Encrypted Token used successfully once (TC-FILE-009)
When          : POST /upload/{encryptedToken} is called a second time with
                the identical token
Then          : HTTP 401 — ERR-FILE-0004 returned; no second FileDocument created

ERR-ID        : ERR-FILE-0004
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-010:END -->

<!-- TC:TC-FILE-011:START -->
```
TC-FILE-011 — MIME type detected from content, not client header
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-002
RULE-ID      : RULE-FILE-005
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : LOV-FILE-001
─────────────────────────────────────────────────────────────────
Scenario type : Edge case
Data class    : EDGE_CASE

Given         : A genuine PNG file uploaded with multipart Content-Type
                deliberately mislabeled as "application/pdf"
When          : POST /upload/{encryptedToken} with the mislabeled PNG
Then          : HTTP 201 — FileUploadResponse.fileTypeId = "IMAGE"
                (LOV-FILE-001) and mimeType reflects the sniffed PNG
                signature, NOT the client-supplied header

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-011:END -->

<!-- TC:TC-FILE-012:START -->
```
TC-FILE-012 — Permanent deletion purges content, retains row
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-004
RULE-ID      : RULE-FILE-006
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : LOV-FILE-002
─────────────────────────────────────────────────────────────────
Scenario type : State transition
Data class    : VALID

Given         : An ACTIVE FileDocument owned by the calling actor
When          : DELETE /{encryptedToken} (valid delete token, owner caller)
Then          : HTTP 200 — DB row for FILE_DOCUMENT still exists;
                FILE_CONTENT column is NULL; FILE_STATUS_ID = "DELETED"
                (QR-FILE-006 compound update verified via direct DB read)

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-012:END -->

<!-- TC:TC-FILE-013:START -->
```
TC-FILE-013 — Owner successfully deletes own file
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-004
RULE-ID      : RULE-FILE-007
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Caller identity matches the FileDocument's owning
                entity's authorized actor (ownerId/ownerType context)
When          : DELETE /{encryptedToken}
Then          : HTTP 200 — deletion proceeds (RULE-FILE-006 purge applied)

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-013:END -->

<!-- TC:TC-FILE-014:START -->
```
TC-FILE-014 — Non-owner, non-Admin delete rejected
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-004
RULE-ID      : RULE-FILE-007
SCR-ID       : SCR-FILE-001
ERR-ID       : ERR-FILE-0007
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Permission
Data class    : ATTACK

Given         : Caller identity is neither the owning entity's authorized
                actor nor an Admin, but holds a validly-issued delete
                token (crafted via a different actor's session — cross-user attempt)
When          : DELETE /{encryptedToken}
Then          : HTTP 403 — ERR-FILE-0007 returned; row unchanged
                (fileContent and fileStatusId untouched)

ERR-ID        : ERR-FILE-0007
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-014:END -->

<!-- SUB:RULE-SCENARIOS:END -->

<!-- SUB:API-SCENARIOS:START -->
### SUB:API-SCENARIOS — Per-API-ID happy path (TP-SEC-2) + edge cases + mandatory scenarios

<!-- TC:TC-FILE-015:START -->
```
TC-FILE-015 — Issue upload token (API-FILE-001 happy path)
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-001
RULE-ID      : —
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : A valid, existing FileCategory (fileCategoryFk)
When          : POST /api/v1/files/upload-token with ownerId, ownerType,
                moduleCode, fileCategoryFk
Then          : HTTP 200 — FileUploadTokenResponse with a non-null
                encryptedToken and expiresAt ~100 minutes ahead

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-015:END -->

<!-- TC:TC-FILE-016:START -->
```
TC-FILE-016 — Upload file (API-FILE-002 happy path, distinct from TC-FILE-001)
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-002
RULE-ID      : —
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Valid upload token from TC-FILE-015
When          : POST /upload/{encryptedToken} with a well-formed multipart
                file part
Then          : HTTP 201 — FileUploadResponse contains fileDocumentPk,
                fileNameOriginal, fileTypeId, fileSizeBytes, fileStatusId="ACTIVE"

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-016:END -->

<!-- TC:TC-FILE-017:START -->
```
TC-FILE-017 — Download file (API-FILE-003 happy path)
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-003
RULE-ID      : —
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : An ACTIVE FileDocument and a freshly issued download token
When          : GET /download/{encryptedToken}
Then          : HTTP 200 — response body byte-for-byte matches the
                originally uploaded content; Content-Type matches
                the detected mimeType

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-017:END -->

<!-- TC:TC-FILE-018:START -->
```
TC-FILE-018 — Delete file (API-FILE-004 happy path, distinct from TC-FILE-012/013)
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-004
RULE-ID      : —
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : An ACTIVE FileDocument owned by the caller, valid delete token
When          : DELETE /{encryptedToken}
Then          : HTTP 200 — FileDeleteConfirmation { fileDocumentPk,
                fileStatusId: "DELETED" } returned

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-018:END -->

<!-- TC:TC-FILE-019:START -->
```
TC-FILE-019 — List files by owner (API-FILE-005 happy path)
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-005
RULE-ID      : —
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : 3 FileDocument rows exist for ownerId=42, ownerType="PURCHASE_ORDER"
When          : GET /api/v1/files/42?ownerType=PURCHASE_ORDER&page=0&size=10
Then          : HTTP 200 — Page<FileDocumentSummaryResponse> with 3 items,
                each carrying the joined FileCategory nameAr/nameEn
                (QR-FILE-007 LEFT JOIN verified); fileContent absent
                from every item

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-019:END -->

<!-- TC:TC-FILE-020:START -->
```
TC-FILE-020 — Download of a DELETED file returns 410 (edge case)
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-003
RULE-ID      : —
SCR-ID       : SCR-FILE-001
ERR-ID       : ERR-FILE-0008
LOV-ID       : LOV-FILE-002
─────────────────────────────────────────────────────────────────
Scenario type : Edge case
Data class    : EDGE_CASE

Given         : A FileDocument previously deleted (fileStatusId="DELETED",
                fileContent=NULL — per TC-FILE-012); a freshly issued
                download token targeting the same fileDocumentPk
When          : GET /download/{encryptedToken}
Then          : HTTP 410 — ERR-FILE-0008 returned; no binary stream sent

ERR-ID        : ERR-FILE-0008
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-020:END -->

<!-- TC:TC-FILE-021:START -->
```
TC-FILE-021 — Upload-token request with unknown fileCategoryFk (edge case)
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-001
RULE-ID      : —
SCR-ID       : SCR-FILE-001
ERR-ID       : ERR-FILE-0009
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Edge case
Data class    : INVALID

Given         : fileCategoryFk = 999999 (does not exist in FILE_CATEGORY)
When          : POST /api/v1/files/upload-token with fileCategoryFk=999999
Then          : HTTP 404 — ERR-FILE-0009 returned; no token issued

ERR-ID        : ERR-FILE-0009
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-021:END -->

<!-- TC:TC-FILE-022:START -->
```
TC-FILE-022 — MANDATORY-J-3: Arabic error message present (API level)
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-002
RULE-ID      : RULE-FILE-001
SCR-ID       : —
ERR-ID       : ERR-FILE-0001
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Arabic message
Data class    : INVALID

Given         : A 6MB file upload (violates RULE-FILE-001, same setup as TC-FILE-002)
When          : POST /upload/{encryptedToken}
Then          : Response body contains messageAr = "حجم الملف يتجاوز الحد
                المسموح به" (exact match) AND messageEn = "File size
                exceeds the allowed limit" (exact match) — both present

ERR-ID        : ERR-FILE-0001
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-022:END -->

<!-- TC:TC-FILE-023:START -->
```
TC-FILE-023 — MANDATORY-J-5: Permission enforcement (API level)
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-002
RULE-ID      : —
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Permission
Data class    : INVALID

Given         : Authenticated user WITHOUT PERM_FILE_ATTACHMENT_CREATE;
                otherwise-valid upload token and file
When          : POST /upload/{encryptedToken}
Then          : HTTP 403 returned (permission-layer rejection, distinct
                from and in addition to the token-layer check)

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-023:END -->

<!-- TC:TC-FILE-024:START -->
```
TC-FILE-024 — MANDATORY-J-7: Empty list result returns 200, not 404
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-005
RULE-ID      : —
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Edge case
Data class    : EDGE_CASE

Given         : ownerId=999888 has zero FileDocument rows
When          : GET /api/v1/files/999888
Then          : HTTP 200 — Page<FileDocumentSummaryResponse> with an
                empty content array and totalElements=0 — NOT HTTP 404

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-024:END -->

<!-- TC:TC-FILE-025:START -->
```
TC-FILE-025 — MANDATORY-J-8: SQL injection resistance
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-002
RULE-ID      : —
SCR-ID       : —
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Security attack
Data class    : ATTACK

Given         : Valid upload token; multipart filename field set to
                fileNameOriginal = "test' OR '1'='1"
When          : POST /upload/{encryptedToken}
Then          : HTTP 201 — value stored as a literal string in
                FILE_NAME_ORIGINAL (parameterized query); DB not affected,
                no other rows altered or leaked

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
Data class    : ATTACK
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-025:END -->

<!-- SUB:API-SCENARIOS:END -->

**MANDATORY-J-1 / MANDATORY-J-2 (Business Code auto-generation / immutability): N/A** —
neither ENTITY-FILE-001 (FileDocument) nor ENTITY-FILE-002 (FileCategory) has a
Business Code (documented deviation, DATA+DOM phase of execution-plan-file-001.md).
No TC generated — not invented.

**MANDATORY-J-4 (LOV invalid value rejected, API level): N/A** — fileTypeId and
fileStatusId (the only two LOV-bound fields, LOV-FILE-001/002) are both
SYSTEM-managed (auto-detected on upload / auto-set on status transition) — neither
is a user-submitted field on any Create/Update request in this module. No TC
generated — not invented.

**MANDATORY-J-6 (Soft deactivation with usage check): N/A** — FileDocument has no
deactivation concept (permanent content-purge deletion only, RULE-FILE-006).
FileCategory's deactivation has no governed API in this plan (DRV-FILE-001 — no
SRS-defined endpoint). No TC generated — not invented.
<!-- MARK:JUNIT:END -->
<!-- PHASE:JUNIT:END -->

---

<!-- PHASE:PLAYWRIGHT:START -->
<!-- MARK:PLAYWRIGHT:START -->
## MARK:PLAYWRIGHT — UI + Integration tests executed via Playwright

Threshold check: 6 TCs ≤ 8 → no SUB split required; all TCs directly under MARK:PLAYWRIGHT.

<!-- TC:TC-FILE-026:START -->
```
TC-FILE-026 — SCR-FILE-001 happy UI flow (view file list)
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-005
RULE-ID      : —
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : SCR-FILE-001 embedded panel open on a host screen with
                3 existing attachments
When          : Panel initializes (facade init sequence per F2)
Then          : 3 rows render with fileNameOriginal, category name,
                fileTypeId, fileSizeBytes, createdAt — no console errors

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-026:END -->

<!-- TC:TC-FILE-027:START -->
```
TC-FILE-027 — Upload via UI succeeds (create flow)
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-001, API-FILE-002
RULE-ID      : —
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : SCR-FILE-001 panel open, user has PERM_FILE_ATTACHMENT_CREATE
When          : User selects a category, picks a valid 1MB file, clicks Upload
Then          : Upload progress shown (LOCAL loading state per F2), new row
                appears in the list without a full page reload

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-027:END -->

<!-- TC:TC-FILE-028:START -->
```
TC-FILE-028 — MANDATORY-P-1 + rule violation on UI: oversized upload
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-002
RULE-ID      : RULE-FILE-001
SCR-ID       : SCR-FILE-001
ERR-ID       : ERR-FILE-0001
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Arabic message
Data class    : INVALID

Given         : User locale = AR, SCR-FILE-001 panel open
When          : User picks a 6MB file and clicks Upload
Then          : Inline error banner shows "حجم الملف يتجاوز الحد المسموح
                به" (Arabic, primary) with the English equivalent also
                visible; no row added to the list

ERR-ID        : ERR-FILE-0001
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-028:END -->

<!-- TC:TC-FILE-029:START -->
```
TC-FILE-029 — MANDATORY-P-3: Permission enforcement (UI level)
─────────────────────────────────────────────────────────────────
API-ID       : —
RULE-ID      : —
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Permission
Data class    : VALID

Given         : User WITHOUT PERM_FILE_ATTACHMENT_CREATE and WITHOUT
                PERM_FILE_ATTACHMENT_DELETE
When          : SCR-FILE-001 panel renders
Then          : Upload control is not rendered; delete buttons are not
                rendered on any row; list itself remains visible (VIEW
                permission still present)

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-029:END -->

<!-- TC:TC-FILE-030:START -->
```
TC-FILE-030 — Module INT Flow, part 1: Upload → List (verify appears)
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-002, API-FILE-005
RULE-ID      : —
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Edge case
Data class    : VALID

Given         : Clean module state (no attachments for the test owner record)
When          : User uploads "report.pdf" via SCR-FILE-001, then the panel
                refreshes its list
Then          : "report.pdf" appears exactly once in the list with the
                correct category and size

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-030:END -->

<!-- TC:TC-FILE-031:START -->
```
TC-FILE-031 — Module INT Flow, part 2: Delete → List (verify removed)
─────────────────────────────────────────────────────────────────
API-ID       : API-FILE-004, API-FILE-005
RULE-ID      : RULE-FILE-006
SCR-ID       : SCR-FILE-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : State transition
Data class    : VALID

Given         : Continuing from TC-FILE-030 — "report.pdf" present in the list
When          : User clicks Delete, confirms the RULE-FILE-006 confirmation
                dialog, deletion completes
Then          : "report.pdf" no longer appears in the active list (list
                refetch confirms removal); a subsequent download attempt
                against the same file would return HTTP 410 (TC-FILE-020)

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-FILE-031:END -->

**MANDATORY-P-2 (Composite Screen UX separation, CORE-9): N/A** — SCR-FILE-001 is a
COMPOSITE embedded panel (PATTERN-2 Inline/Modal per F1), not a Search+Entry
(PATTERN-1) screen. The CORE-9 Search-view/Entry-form separation rule targets
PATTERN-1 screens specifically; SCR-FILE-001's list and upload control are
designed to coexist in one embedded view by architecture. No TC generated — not
invented.
<!-- MARK:PLAYWRIGHT:END -->
<!-- PHASE:PLAYWRIGHT:END -->

---

<!-- PHASE:TRACEABILITY:START -->
## TP-SEC-5 — TC TRACEABILITY INDEX (mandatory)

```
TC TRACEABILITY INDEX — File Service
══════════════════════════════════════════════════════════════════

MARK:JUNIT
──────────────────────────────────────────────────────────────────
RULE-ID → TC-IDs:
RULE-FILE-001  → TC-FILE-001 (happy) | TC-FILE-002 (violation) | TC-FILE-003 (boundary) | TC-FILE-022 (AR message)
RULE-FILE-002  → TC-FILE-004 (happy) | TC-FILE-005 (violation) | TC-FILE-006 (boundary)
RULE-FILE-003  → TC-FILE-007 (happy) | TC-FILE-008 (violation)
RULE-FILE-004  → TC-FILE-009 (happy) | TC-FILE-010 (violation)
RULE-FILE-005  → TC-FILE-011 (happy — informational, no violation path)
RULE-FILE-006  → TC-FILE-012 (state transition — behavioral, no violation path)
RULE-FILE-007  → TC-FILE-013 (happy) | TC-FILE-014 (violation)

API-ID → TC-IDs:
API-FILE-001   → TC-FILE-015 (happy) | TC-FILE-021 (edge — category not found)
API-FILE-002   → TC-FILE-016 (happy) | TC-FILE-023 (permission) | TC-FILE-025 (attack)
API-FILE-003   → TC-FILE-017 (happy) | TC-FILE-020 (edge — deleted file)
API-FILE-004   → TC-FILE-018 (happy)
API-FILE-005   → TC-FILE-019 (happy) | TC-FILE-024 (empty result)

ERR-ID → TC-IDs:
ERR-FILE-0001  → TC-FILE-002 | TC-FILE-022 | TC-FILE-028 (Playwright)
ERR-FILE-0002  → TC-FILE-005 | TC-FILE-006
ERR-FILE-0003  → TC-FILE-008
ERR-FILE-0004  → TC-FILE-010
ERR-FILE-0005  → N/A (informational rule, no HTTP rejection — behavior covered by TC-FILE-011)
ERR-FILE-0006  → N/A (client-side confirmation only — behavior covered by TC-FILE-031's confirm step)
ERR-FILE-0007  → TC-FILE-014
ERR-FILE-0008  → TC-FILE-020
ERR-FILE-0009  → TC-FILE-021

MARK:PLAYWRIGHT
──────────────────────────────────────────────────────────────────
SCR-ID → TC-IDs (UI Flows):
SCR-FILE-001   → TC-FILE-026 (list flow) | TC-FILE-027 (create flow)
                 TC-FILE-028 (rule violation on screen) | TC-FILE-029 (permission)

Module INT Flow → TC-IDs:
FILE lifecycle → TC-FILE-030 (upload→list) | TC-FILE-031 (delete→list)

══════════════════════════════════════════════════════════════════
Coverage summary:
  RULE-IDs covered  : 7 / 7
  API-IDs covered   : 5 / 5
  SCR-IDs covered   : 1 / 1
  Total TCs         : 31 — within target range 25–40 ✓
  JUNIT TCs         : 25
  PLAYWRIGHT TCs    : 6
══════════════════════════════════════════════════════════════════
```

---
*End of test-plan-file-001.md*
*Governed by: Execution Plan Governance Engine (Project 3, v2), Section 16*
*PLAN-ID: PLAN-FILE-001 | Next Mode: MODE 4A (audit this test-plan.md against CHECK-4) → MODE 3 (agent execution)*
<!-- PHASE:TRACEABILITY:END -->
