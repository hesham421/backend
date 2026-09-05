<!-- Source: PHASE:SVC-API / SUB:SVC-API-LOOKUP-TYPE -->
<!-- Context: see SVC-API-HEADER.md for phase-level strategy, registry table, and intro -->

### SUB-GROUP: LOOKUP TYPE (Master) — API-MDM-001..005

<!-- API:API-MDM-001:START -->
### API-MDM-001 — Create LookupType
─────────────────────────────────────────────────────────────────
Endpoint         : POST /api/v1/mdm/lookup-types
Controller       : MdmLookupController → method: createLookupType
Service          : MdmLookupTypeService → method: create
─────────────────────────────────────────────────────────────────
REQUEST:
  Content-Type   : application/json
  Request Body   : LookupTypeCreateRequest
    Fields:
      typeCode : String  REQUIRED — maps to TYPE_CODE, UNIQUE (RULE-MDM-001)
      nameAr   : String  REQUIRED — maps to NAME_AR (RULE-MDM-005)
      nameEn   : String  REQUIRED — maps to NAME_EN (RULE-MDM-005)
      notes    : String  OPTIONAL — maps to NOTES
    Excluded fields : lookupTypePk (system, SEQ_MDM_LOOKUP_TYPE), isActiveFl (system, default 1),
                       createdBy/createdAt/updatedBy/updatedAt (AuditEntityListener)

RESPONSE:
  Success code   : 201
  Response DTO   : LookupTypeResponse (all FIELD-0001..0006 + audit fields)
  Paginated      : NO

VALIDATIONS:
  1. RULE-MDM-001 — Type code uniqueness:
       Statement  : The system MUST prevent creating a LookupType whose typeCode already exists.
       Trigger    : On create
       Message-AR : رمز النوع مستخدَم مسبقًا — اختر رمزًا فريدًا.
       Message-EN : This type code already exists — choose a unique code.
  2. RULE-MDM-005 — Both names mandatory:
       Statement  : The system MUST require both nameAr and nameEn before saving.
       Trigger    : On save
       Message-AR : الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي.
       Message-EN : Both the Arabic name and the English name are required.

ERRORS:
  ERR-0001 → RULE-MDM-001 → HTTP 409
             Message-AR: رمز النوع مستخدَم مسبقًا — اختر رمزًا فريدًا.
             Message-EN: This type code already exists — choose a unique code.
  ERR-0002 → RULE-MDM-005 → HTTP 400
             Message-AR: الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي.
             Message-EN: Both the Arabic name and the English name are required.

SERVICE ORCHESTRATION:
  1. [validate]  — bean validation on request body (nameAr/nameEn required — RULE-MDM-005)
  2. [validate]  — RULE-MDM-001: QR-MDM-0003 EXISTS check on typeCode; if true → ERR-0001
  3. [persist]   — QR-MDM-0004 SAVE; PK from SEQ_MDM_LOOKUP_TYPE; isActiveFl defaults to 1

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0003 (EXISTS), QR-MDM-0004 (SAVE)
  Table      : MDM_LOOKUP_TYPE
  Transaction: READ_WRITE
  Sequence   : SEQ_MDM_LOOKUP_TYPE

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_CREATE (VIEW required as prerequisite — CORE-9)

LOCALIZATION: Error responses carry messageAr + messageEn from SECTION A. Name responses return both nameAr and nameEn.
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-001:END -->

<!-- API:API-MDM-002:START -->
### API-MDM-002 — Search LookupTypes
─────────────────────────────────────────────────────────────────
Endpoint         : GET /api/v1/mdm/lookup-types
Controller       : MdmLookupController → method: searchLookupTypes
Service          : MdmLookupTypeService → method: search
─────────────────────────────────────────────────────────────────
REQUEST:
  Query Params   : typeCode (String, optional, LIKE), nameAr (String, optional, LIKE),
                   nameEn (String, optional, LIKE), isActiveFl (Boolean, optional — default true
                   when omitted, DRV-011), page (int), size (int), sortBy, sortDir

RESPONSE:
  Success code   : 200
  Response DTO   : Page<LookupTypeResponse>
  Paginated      : YES — JPA Page<T>

VALIDATIONS: None (read-only search).
ERRORS: None dedicated — empty result set returns HTTP 200 with empty content (project standard), never 404.

SERVICE ORCHESTRATION:
  1. [load] — QR-MDM-0002 FIND_BY_CRITERIA with SpecBuilder.build(request, ALLOWED_SORT_FIELDS)
              and PageableBuilder.from(request, ALLOWED_SORT_FIELDS)
  ALLOWED_SORT_FIELDS: typeCode, nameAr, nameEn, isActiveFl

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0002
  Table      : MDM_LOOKUP_TYPE
  Transaction: READ_ONLY

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_VIEW
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-002:END -->

<!-- API:API-MDM-003:START -->
### API-MDM-003 — Update LookupType
─────────────────────────────────────────────────────────────────
Endpoint         : PUT /api/v1/mdm/lookup-types/{id}
Controller       : MdmLookupController → method: updateLookupType
Service          : MdmLookupTypeService → method: update
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : id: Long
  Request Body   : LookupTypeUpdateRequest
    Fields:
      nameAr : String  REQUIRED — maps to NAME_AR (RULE-MDM-005)
      nameEn : String  REQUIRED — maps to NAME_EN (RULE-MDM-005)
      notes  : String  OPTIONAL — maps to NOTES
    Excluded fields : typeCode (RULE-MDM-002 — immutable), isActiveFl, audit fields, lookupTypePk

RESPONSE:
  Success code   : 200
  Response DTO   : LookupTypeResponse

VALIDATIONS:
  1. RULE-MDM-002 — Type code immutable: enforced by DTO shape (typeCode not accepted); if a client
     sends `typeCode` in the body it is silently ignored by the mapper — this is not itself an error
     condition unless a project-wide "unknown field" strictness policy exists (none declared here).
  2. RULE-MDM-005 — Both names mandatory (same as API-MDM-001).

ERRORS:
  ERR-0003 → RULE-MDM-002 → HTTP 409 — (reserved; used only if a stricter client contract later
             rejects a typeCode field explicitly present in the request body)
             Message-AR: لا يمكن تعديل رمز النوع بعد الإنشاء — القيمة مرجع تعتمد عليه موديولات أخرى.
             Message-EN: Type code cannot be changed after creation — other modules reference it.
  ERR-0004 → RULE-MDM-005 → HTTP 400
             Message-AR: الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي.
             Message-EN: Both the Arabic name and the English name are required.
  ERR-0006 → PLATFORM-STD → HTTP 404 — LookupType not found for given id.

SERVICE ORCHESTRATION:
  1. [load]     — QR-MDM-0001 FIND_ONE by PK; not found → ERR-0006
  2. [validate] — RULE-MDM-005 bean validation
  3. [persist]  — QR-MDM-0005 UPDATE (nameAr/nameEn/notes only)

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0001, QR-MDM-0005
  Table      : MDM_LOOKUP_TYPE
  Transaction: READ_WRITE

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_UPDATE
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-003:END -->

<!-- API:API-MDM-004:START -->
### API-MDM-004 — Deactivate LookupType (soft)
─────────────────────────────────────────────────────────────────
Endpoint         : DELETE /api/v1/mdm/lookup-types/{id}
Controller       : MdmLookupController → method: deactivateLookupType
Service          : MdmLookupTypeService → method: deactivate
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : id: Long

RESPONSE:
  Success code   : 204

VALIDATIONS:
  1. RULE-MDM-006 — Block deactivation while active LookupValues exist under this type.

ERRORS:
  ERR-0005 → RULE-MDM-006 → HTTP 409
             Message-AR: يُستبعَد العنصر بالتعطيل لا بالحذف؛ لا يمكن حذف نوع يحتوي قيمًا.
             Message-EN: Items are deactivated, not deleted; a type that still has values cannot be hard-deleted.
  ERR-0006 → PLATFORM-STD → HTTP 404 — LookupType not found for given id.

SERVICE ORCHESTRATION:
  1. [load]     — QR-MDM-0001 FIND_ONE by PK; not found → ERR-0006
  2. [validate] — QR-MDM-0006 COUNT active LookupValue WHERE lookupTypeFk = :id; count > 0 → ERR-0005
  3. [persist]  — QR-MDM-0007 UPDATE isActiveFl = 0 (no cascade to LookupValue rows — DRV-009)

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0001, QR-MDM-0006, QR-MDM-0007
  Table      : MDM_LOOKUP_TYPE (+ read-only usage count on MDM_LOOKUP_VALUE)
  Transaction: READ_WRITE

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_DELETE
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-004:END -->

<!-- API:API-MDM-005:START -->
### API-MDM-005 — Get LookupType by id
─────────────────────────────────────────────────────────────────
Endpoint         : GET /api/v1/mdm/lookup-types/{id}
Controller       : MdmLookupController → method: getLookupType
Service          : MdmLookupTypeService → method: getById
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : id: Long

RESPONSE:
  Success code   : 200
  Response DTO   : LookupTypeResponse

VALIDATIONS: None.
ERRORS:
  ERR-0006 → PLATFORM-STD → HTTP 404 — LookupType not found for given id.

SERVICE ORCHESTRATION:
  1. [load] — QR-MDM-0001 FIND_ONE by PK; not found → LocalizedException(NOT_FOUND, ERR-0006)

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0001
  Table      : MDM_LOOKUP_TYPE
  Transaction: READ_ONLY

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_VIEW
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-005:END -->

