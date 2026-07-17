<!-- Source: MARK:JUNIT / SUB:API-SCENARIOS -->
<!-- Context: see JUNIT-HEADER.md for mark-level intro and mandatory scenarios -->

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

