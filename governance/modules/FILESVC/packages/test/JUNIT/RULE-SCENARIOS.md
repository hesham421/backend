<!-- Source: MARK:JUNIT / SUB:RULE-SCENARIOS -->
<!-- Context: see JUNIT-HEADER.md for mark-level intro and mandatory scenarios -->

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

