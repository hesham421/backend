<!-- Source: PHASE:SVC-API / SUB:SVC-API-LOOKUP-VALUE -->
<!-- Context: see SVC-API-HEADER.md for phase-level strategy, registry table, and intro -->

### SUB-GROUP: LOOKUP VALUE (Detail) — API-MDM-006..010

<!-- API:API-MDM-006:START -->
### API-MDM-006 — Create LookupValue
─────────────────────────────────────────────────────────────────
Endpoint         : POST /api/v1/mdm/lookup-types/{typeId}/values
Controller       : MdmLookupController → method: createLookupValue
Service          : MdmLookupValueService → method: create
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : typeId: Long
  Request Body   : LookupValueCreateRequest
    Fields:
      valueCode : String  REQUIRED — maps to VALUE_CODE, UNIQUE within typeId (RULE-MDM-003)
      nameAr    : String  REQUIRED — maps to NAME_AR (RULE-MDM-005)
      nameEn    : String  REQUIRED — maps to NAME_EN (RULE-MDM-005)
      sortOrder : Short   OPTIONAL — maps to SORT_ORDER
    Excluded fields : lookupValuePk (system), lookupTypeFk (from path param, never body),
                       isActiveFl, audit fields

RESPONSE:
  Success code   : 201
  Response DTO   : LookupValueResponse

VALIDATIONS:
  1. RULE-MDM-003 — Value code uniqueness within type.
  2. RULE-MDM-005 — Both names mandatory.

ERRORS:
  ERR-0007 → PLATFORM-STD → HTTP 404 — parent LookupType not found for given typeId (DRV-004).
  ERR-0008 → RULE-MDM-003 → HTTP 409
             Message-AR: رمز القيمة مستخدَم مسبقًا ضمن هذا النوع — اختر رمزًا فريدًا.
             Message-EN: This value code already exists under this type — choose a unique code.
  ERR-0009 → RULE-MDM-005 → HTTP 400
             Message-AR: الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي.
             Message-EN: Both the Arabic name and the English name are required.

SERVICE ORCHESTRATION:
  1. [load]     — QR-MDM-0011 EXISTS LookupType by PK (typeId); not found → ERR-0007
  2. [validate] — QR-MDM-0010 EXISTS by (typeId, valueCode); true → ERR-0008
  3. [validate] — RULE-MDM-005 bean validation → ERR-0009
  4. [persist]  — QR-MDM-0012 SAVE; lookupTypeFk = typeId; PK from SEQ_MDM_LOOKUP_VALUE

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0011, QR-MDM-0010, QR-MDM-0012
  Table      : MDM_LOOKUP_VALUE (+ existence check on MDM_LOOKUP_TYPE)
  Join       : NONE for the SAVE itself — QR-MDM-0011 is a separate EXISTS check, not a join (DRV-004)
  Transaction: READ_WRITE
  Sequence   : SEQ_MDM_LOOKUP_VALUE

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_CREATE
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-006:END -->

<!-- API:API-MDM-007:START -->
### API-MDM-007 — List LookupValues under type
─────────────────────────────────────────────────────────────────
Endpoint         : GET /api/v1/mdm/lookup-types/{typeId}/values
Controller       : MdmLookupController → method: listLookupValues
Service          : MdmLookupValueService → method: search
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : typeId: Long
  Query Params   : isActiveFl (Boolean, optional — default true when omitted, DRV-011),
                   page (int), size (int), sortBy, sortDir

RESPONSE:
  Success code   : 200
  Response DTO   : Page<LookupValueResponse>
  Paginated      : YES

VALIDATIONS: None beyond parent existence.
ERRORS:
  ERR-0007 → PLATFORM-STD → HTTP 404 — parent LookupType not found for given typeId (DRV-004).

SERVICE ORCHESTRATION:
  1. [load] — QR-MDM-0011 EXISTS LookupType by PK; not found → ERR-0007
  2. [load] — QR-MDM-0009 FIND_BY_CRITERIA WHERE lookupTypeFk = :typeId
  ALLOWED_SORT_FIELDS: valueCode, nameAr, nameEn, sortOrder, isActiveFl

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0011, QR-MDM-0009
  Table      : MDM_LOOKUP_VALUE
  Transaction: READ_ONLY

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_VIEW
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-007:END -->

<!-- API:API-MDM-008:START -->
### API-MDM-008 — Update LookupValue
─────────────────────────────────────────────────────────────────
Endpoint         : PUT /api/v1/mdm/lookup-values/{id}
Controller       : MdmLookupController → method: updateLookupValue
Service          : MdmLookupValueService → method: update
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : id: Long
  Request Body   : LookupValueUpdateRequest
    Fields:
      nameAr    : String  REQUIRED
      nameEn    : String  REQUIRED
      sortOrder : Short   OPTIONAL
    Excluded fields : valueCode (RULE-MDM-004), lookupTypeFk (immutable parent), isActiveFl, audit fields

RESPONSE:
  Success code   : 200
  Response DTO   : LookupValueResponse

VALIDATIONS:
  1. RULE-MDM-004 — Value code immutable (enforced by DTO shape).
  2. RULE-MDM-005 — Both names mandatory.

ERRORS:
  ERR-0010 → RULE-MDM-004 → HTTP 409 — (reserved, same basis as ERR-0003)
             Message-AR: لا يمكن تعديل رمز القيمة بعد الإنشاء — القيمة مرجع تخزّنه موديولات أخرى.
             Message-EN: Value code cannot be changed after creation — other modules store it.
  ERR-0011 → RULE-MDM-005 → HTTP 400
             Message-AR: الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي.
             Message-EN: Both the Arabic name and the English name are required.
  ERR-0012 → PLATFORM-STD → HTTP 404 — LookupValue not found for given id.

SERVICE ORCHESTRATION:
  1. [load]     — QR-MDM-0008 FIND_ONE by PK; not found → ERR-0012
  2. [validate] — RULE-MDM-005 bean validation
  3. [persist]  — QR-MDM-0013 UPDATE (nameAr/nameEn/sortOrder only)

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0008, QR-MDM-0013
  Table      : MDM_LOOKUP_VALUE
  Transaction: READ_WRITE

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_UPDATE
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-008:END -->

<!-- API:API-MDM-009:START -->
### API-MDM-009 — Deactivate LookupValue (soft)
─────────────────────────────────────────────────────────────────
Endpoint         : DELETE /api/v1/mdm/lookup-values/{id}
Controller       : MdmLookupController → method: deactivateLookupValue
Service          : MdmLookupValueService → method: deactivate
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : id: Long

RESPONSE:
  Success code   : 204

VALIDATIONS: None beyond existence (DRV-008 — no child/usage check for LookupValue).
ERRORS:
  ERR-0012 → PLATFORM-STD → HTTP 404 — LookupValue not found for given id.

SERVICE ORCHESTRATION:
  1. [load]    — QR-MDM-0008 FIND_ONE by PK; not found → ERR-0012
  2. [persist] — QR-MDM-0014 UPDATE isActiveFl = 0

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0008, QR-MDM-0014
  Table      : MDM_LOOKUP_VALUE
  Transaction: READ_WRITE

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_DELETE
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-009:END -->

<!-- API:API-MDM-010:START -->
### API-MDM-010 — Get LookupValue by id
─────────────────────────────────────────────────────────────────
Endpoint         : GET /api/v1/mdm/lookup-values/{id}
Controller       : MdmLookupController → method: getLookupValue
Service          : MdmLookupValueService → method: getById
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : id: Long

RESPONSE:
  Success code   : 200
  Response DTO   : LookupValueResponse

VALIDATIONS: None.
ERRORS:
  ERR-0012 → PLATFORM-STD → HTTP 404 — LookupValue not found for given id.

SERVICE ORCHESTRATION:
  1. [load] — QR-MDM-0008 FIND_ONE by PK; not found → ERR-0012

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0008
  Table      : MDM_LOOKUP_VALUE
  Transaction: READ_ONLY

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_VIEW
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-010:END -->

