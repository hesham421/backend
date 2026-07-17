<!-- Source: PHASE:DOC -->

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
API-FILE-005 │ /api/v1/files/{ownerId}            │ GET    │ — (path/query params)   │ Page<FileDocumentSummaryResponse> │ STABLE
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

---

Audited against the built erp-file module (FileController.java, all 8 DTOs,
FileErrorCodes.java, messages.properties/messages_ar.properties):

1. **API contracts** — FileController's 6 mappings (upload-token, access-token, upload,
   download, delete, listByOwner) match API-FILE-001..005 exactly (access-token is the
   GAP-FILE-001 addition, not a numbered API-ID, tracked separately in
   execution-state.json). Found and corrected one inaccuracy: API-FILE-005's Request DTO
   column named `FileListSearchRequest`, a class that does not exist anywhere in
   erp-file — `listByOwner` is a plain GET with `@PathVariable`/`@RequestParam` (no body
   DTO, per create-controller A.6.6), building the shared platform `com.erp.common.search.
   SearchRequest` internally. Table corrected above.

2. **DTOs** — `fileTypeId`/`fileStatusId` confirmed String (never ENUM) across
   FileDocument entity, FileDocumentSummaryResponse, FileUploadResponse,
   FileDeleteConfirmation. No Business Code field anywhere in erp-file. `isActiveFl`
   exists only on FileCategory; FileDocument deliberately uses `fileStatusId` lifecycle
   (LOV-FILE-002: ACTIVE/ARCHIVED/DELETED) instead, per its own inline comment.

3. **Error Catalog** — all 7 runtime-relevant ERR-FILE-IDs (0001–0004, 0007–0009) have a
   matching constant in FileErrorCodes.java and bilingual keys in both
   messages.properties and messages_ar.properties (13/13 code keys present, 0 missing in
   either file). ERR-FILE-0005 (informational — file type auto-detected) and
   ERR-FILE-0006 (client-confirm dialog copy) correctly have no throw site — neither is a
   runtime-checkable condition. 5 additional codes exist outside the SECTION-A catalog
   (FILE_DOCUMENT_NOT_FOUND, FILE_UPLOAD_READ_FAILED, FILE_NAME_REQUIRED,
   FILE_TOKEN_ISSUE_FAILED, FILE_ACCESS_TOKEN_ACTION_INVALID) — generic technical errors
   not tied to a specific RULE-ID, same pattern as ORG's non-catalog codes; all still
   have bilingual message keys.
