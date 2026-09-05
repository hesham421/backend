<!-- Source: PHASE:DATA-DOM -->

## PHASE DATA+DOM — Entity & Domain Specifications

### ENTITY-MDM-001 — LookupType (نوع القائمة المرجعية) — MASTER
────────────────────────────────────────────────────────────────────────
SOURCE BINDINGS:
  DB Table       : MDM_LOOKUP_TYPE
  PK Column      : ID
  PK Sequence    : SEQ_MDM_LOOKUP_TYPE
  PK Trigger     : none (PostgreSQL — PK value obtained via `nextval('SEQ_MDM_LOOKUP_TYPE')`, no trigger construct)
  DBS-ID ref     : DBS-MDM-001

BUSINESS CODE: NOT APPLICABLE — BC-RULE-0 = NO. `typeCode` is a natural key (see FIELDS below), not a system-generated Business Code.

SOFT DEACTIVATION:
  Governed by CORE Deactivation Policy. DB binding: isActiveFl → IS_ACTIVE_FL — DBF-0005.
  Pre-deactivation usage check REQUIRED (DRV-008): block deactivation while ≥1 active
  LookupValue exists under this type (RULE-MDM-006) — see QR-MDM-0006.

AUDIT COLUMNS (AuditEntityListener — no DBF-ID, per db-script-MDM.md §2):
  createdBy / createdAt / updatedBy / updatedAt — String / LocalDateTime / String / LocalDateTime.
  ⚠ MUST NOT appear in CreateRequest/UpdateRequest DTOs. Never set manually.

────────────────────────────────────────────────────────────────────────
FIELDS:
────────────────────────────────────────────────────────────────────────
| FIELD-ID | Java Property | DB Column | DBF-ID | DB Type | Null | Read-Only | Constraint | Label-AR | Label-EN |
|---|---|---|---|---|---|---|---|---|---|
| FIELD-0001 | lookupTypePk | ID | DBF-0001 | BIGINT | No | System | PK — SEQ_MDM_LOOKUP_TYPE | المعرف | ID |
| FIELD-0002 | typeCode | TYPE_CODE | DBF-0002 | VARCHAR(50) | No | YES (RULE-MDM-002) | UNIQUE — UQ_MDM_LOOKUP_TYPE_CODE | رمز النوع | Type Code |
| FIELD-0003 | nameAr | NAME_AR | DBF-0003 | VARCHAR(200) | No | No | NOT NULL (RULE-MDM-005) | الاسم بالعربي | Name (Arabic) |
| FIELD-0004 | nameEn | NAME_EN | DBF-0004 | VARCHAR(100) | No | No | NOT NULL (RULE-MDM-005) | الاسم بالإنجليزي | Name (English) |
| FIELD-0005 | isActiveFl | IS_ACTIVE_FL | DBF-0005 | SMALLINT | No | System | DEFAULT 1, CHECK IN (0,1) | نشط | Active |
| FIELD-0006 | notes | NOTES | DBF-0006 | VARCHAR(2000) | Yes | No | — | ملاحظات | Notes |
| — | createdBy/createdAt/updatedBy/updatedAt | CREATED_BY/CREATED_AT/UPDATED_BY/UPDATED_AT | none (AuditEntityListener) | VARCHAR(255)/TIMESTAMP | Yes | System | — | أنشئ بواسطة / تاريخ الإنشاء / عُدِّل بواسطة / تاريخ التعديل | Created By / Created At / Updated By / Updated At |

DTO MEMBERSHIP:
  CreateRequest : typeCode, nameAr, nameEn, notes?
  UpdateRequest : nameAr, nameEn, notes? (typeCode EXCLUDED — RULE-MDM-002)
  ResponseDTO   : all fields including typeCode, isActiveFl, createdBy, createdAt, updatedBy, updatedAt

────────────────────────────────────────────────────────────────────────
DOMAIN RULES:
────────────────────────────────────────────────────────────────────────
RULE-MDM-001 — Type code uniqueness:
  Trigger    : On create or update of LookupType
  Statement  : The system MUST prevent creating a LookupType whose typeCode already exists.
  Message-AR : رمز النوع مستخدَم مسبقًا — اختر رمزًا فريدًا.
  Message-EN : This type code already exists — choose a unique code.
  Scope      : CREATE
  DB Enforce : UNIQUE constraint UQ_MDM_LOOKUP_TYPE_CODE
  ERR-ID     : ERR-0001
  Owned by   : domain (Entity method `LookupType.validateNewCode(...)`, backed by service-level EXISTS check QR-MDM-0003)

RULE-MDM-002 — Type code immutable after creation:
  Trigger    : On update of LookupType
  Statement  : The system MUST prevent modifying typeCode after creation.
  Message-AR : لا يمكن تعديل رمز النوع بعد الإنشاء — القيمة مرجع تعتمد عليه موديولات أخرى.
  Message-EN : Type code cannot be changed after creation — other modules reference it.
  Scope      : UPDATE
  DB Enforce : app-level (typeCode excluded entirely from UpdateRequest DTO)
  ERR-ID     : ERR-0003
  Owned by   : domain (Entity method rejects any attempt to set typeCode post-creation)

RULE-MDM-005 — Both names mandatory (applies to LookupType and LookupValue):
  Trigger    : On save (create/update)
  Statement  : The system MUST require both nameAr and nameEn before saving a LookupType or a LookupValue.
  Message-AR : الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي.
  Message-EN : Both the Arabic name and the English name are required.
  Scope      : CREATE, UPDATE
  DB Enforce : NOT NULL on NAME_AR, NAME_EN
  ERR-ID     : ERR-0002 (LookupType) / ERR-0009 (LookupValue)
  Owned by   : domain (bean-validation @NotBlank + Entity invariant)

RULE-MDM-006 — Soft deactivation only; block deactivating a type with active values:
  Trigger    : On deactivate request for LookupType
  Statement  : The system MUST deactivate reference data via isActiveFl (soft) rather than
               hard-delete, and MUST prevent hard-deleting/deactivating a LookupType that still
               has active LookupValues; deactivation cascades no change to external consumers.
  Message-AR : يُستبعَد العنصر بالتعطيل لا بالحذف؛ لا يمكن حذف نوع يحتوي قيمًا.
  Message-EN : Items are deactivated, not deleted; a type that still has values cannot be hard-deleted.
  Scope      : DELETE (soft)
  DB Enforce : app-level (QR-MDM-0006 usage check) — no hard-delete endpoint exists at all (DRV-008/009)
  ERR-ID     : ERR-0005
  Owned by   : domain, with service-level orchestration for the usage count (Section 8.1 CORE Deactivation Policy)

────────────────────────────────────────────────────────────────────────
CROSS-MODULE DEPENDENCIES: None. Consumed via API (provider pattern) — see srs-MDM §A7 / db-script §3 (XM Register: none).

REPOSITORY OPERATIONS REQUIRED:
  → QR-MDM-0001 : FIND_ONE by PK
  → QR-MDM-0002 : FIND_BY_CRITERIA (search: typeCode, nameAr, nameEn, isActiveFl)
  → QR-MDM-0003 : EXISTS by typeCode (uniqueness check)
  → QR-MDM-0004 : SAVE (create)
  → QR-MDM-0005 : UPDATE (nameAr/nameEn/notes only)
  → QR-MDM-0007 : UPDATE (soft deactivate)
  → QRC full entries in SECTION B.
────────────────────────────────────────────────────────────────────────

### ENTITY-MDM-002 — LookupValue (قيمة القائمة المرجعية) — DETAIL
────────────────────────────────────────────────────────────────────────
SOURCE BINDINGS:
  DB Table       : MDM_LOOKUP_VALUE
  PK Column      : ID
  PK Sequence    : SEQ_MDM_LOOKUP_VALUE
  PK Trigger     : none (PostgreSQL sequence, no trigger construct)
  DBS-ID ref     : DBS-MDM-001

BUSINESS CODE: NOT APPLICABLE — BC-RULE-0 = NO. `valueCode` is a natural key, unique within its parent type.

SOFT DEACTIVATION:
  Governed by CORE Deactivation Policy. DB binding: isActiveFl → IS_ACTIVE_FL — DBF-0013.
  No usage/child check required (DRV-008 — LookupValue is a leaf entity).

AUDIT COLUMNS (AuditEntityListener — no DBF-ID): createdBy/createdAt/updatedBy/updatedAt — same rules as ENTITY-MDM-001.

────────────────────────────────────────────────────────────────────────
FIELDS:
────────────────────────────────────────────────────────────────────────
| FIELD-ID | Java Property | DB Column | DBF-ID | DB Type | Null | Read-Only | Constraint | Label-AR | Label-EN |
|---|---|---|---|---|---|---|---|---|---|
| FIELD-0007 | lookupValuePk | ID | DBF-0007 | BIGINT | No | System | PK — SEQ_MDM_LOOKUP_VALUE | المعرف | ID |
| FIELD-0008 | lookupTypeFk | LOOKUP_TYPE_FK | DBF-0008 | BIGINT | No | System (set once, at create) | FK → MDM_LOOKUP_TYPE(ID) — FK_MDM_LOOKUP_VALUE_TYPE, NOT NULL (DRV-003) | نوع القائمة | Lookup Type |
| FIELD-0009 | valueCode | VALUE_CODE | DBF-0009 | VARCHAR(50) | No | YES (RULE-MDM-004) | UNIQUE(lookupTypeFk, valueCode) — UQ_MDM_LOOKUP_VALUE_TYPE_CODE | رمز القيمة | Value Code |
| FIELD-0010 | nameAr | NAME_AR | DBF-0010 | VARCHAR(200) | No | No | NOT NULL (RULE-MDM-005) | الاسم بالعربي | Name (Arabic) |
| FIELD-0011 | nameEn | NAME_EN | DBF-0011 | VARCHAR(100) | No | No | NOT NULL (RULE-MDM-005) | الاسم بالإنجليزي | Name (English) |
| FIELD-0012 | sortOrder | SORT_ORDER | DBF-0012 | SMALLINT | Yes | No | — | ترتيب العرض | Sort Order |
| FIELD-0013 | isActiveFl | IS_ACTIVE_FL | DBF-0013 | SMALLINT | No | System | DEFAULT 1, CHECK IN (0,1) | نشط | Active |
| FIELD-0014 | notes | NOTES | DBF-0014 | VARCHAR(2000) | Yes | No | — | ملاحظات | Notes |
| — | createdBy/createdAt/updatedBy/updatedAt | CREATED_BY/CREATED_AT/UPDATED_BY/UPDATED_AT | none (AuditEntityListener) | VARCHAR(255)/TIMESTAMP | Yes | System | — | (same as ENTITY-MDM-001) | (same) |

DTO MEMBERSHIP:
  CreateRequest : valueCode, nameAr, nameEn, sortOrder? (lookupTypeFk comes from the `{typeId}` path param — never a body field)
  UpdateRequest : nameAr, nameEn, sortOrder? (valueCode EXCLUDED — RULE-MDM-004; lookupTypeFk EXCLUDED — immutable parent)
  ResponseDTO   : all fields including valueCode, sortOrder, isActiveFl, createdBy, createdAt, updatedBy, updatedAt

────────────────────────────────────────────────────────────────────────
DOMAIN RULES:
────────────────────────────────────────────────────────────────────────
RULE-MDM-003 — Value code uniqueness within type:
  Trigger    : On create or update of LookupValue
  Statement  : The system MUST prevent creating a LookupValue whose valueCode already exists under the same lookupTypeFk.
  Message-AR : رمز القيمة مستخدَم مسبقًا ضمن هذا النوع — اختر رمزًا فريدًا.
  Message-EN : This value code already exists under this type — choose a unique code.
  Scope      : CREATE
  DB Enforce : UNIQUE constraint UQ_MDM_LOOKUP_VALUE_TYPE_CODE (composite: LOOKUP_TYPE_FK, VALUE_CODE)
  ERR-ID     : ERR-0008
  Owned by   : domain (Entity method, backed by QR-MDM-0010)

RULE-MDM-004 — Value code immutable after creation:
  Trigger    : On update of LookupValue
  Statement  : The system MUST prevent modifying valueCode after creation.
  Message-AR : لا يمكن تعديل رمز القيمة بعد الإنشاء — القيمة مرجع تخزّنه موديولات أخرى.
  Message-EN : Value code cannot be changed after creation — other modules store it.
  Scope      : UPDATE
  DB Enforce : app-level (valueCode excluded entirely from UpdateRequest DTO)
  ERR-ID     : ERR-0010
  Owned by   : domain

RULE-MDM-005 — Both names mandatory: see full text under ENTITY-MDM-001 (Scope here: LookupValue half). ERR-ID: ERR-0009.

RULE-MDM-006 — Soft deactivation: see full text under ENTITY-MDM-001. For LookupValue, only the soft-deactivate-not-hard-delete half applies (no child-usage check, DRV-008). No dedicated ERR-ID beyond standard 404 (ERR-0012) — deactivation itself cannot fail once the row is found.

────────────────────────────────────────────────────────────────────────
CROSS-MODULE DEPENDENCIES: None directly. `lookupTypeFk` is an INTRA-MODULE FK to ENTITY-MDM-001 (same module, same DBS-ID) — not an XM dependency.

REPOSITORY OPERATIONS REQUIRED:
  → QR-MDM-0008 : FIND_ONE by PK
  → QR-MDM-0009 : FIND_BY_CRITERIA (list under type, filter isActiveFl)
  → QR-MDM-0010 : EXISTS by (lookupTypeFk, valueCode)
  → QR-MDM-0011 : EXISTS LookupType by PK (parent existence — DRV-004)
  → QR-MDM-0012 : SAVE (create)
  → QR-MDM-0013 : UPDATE (nameAr/nameEn/sortOrder)
  → QR-MDM-0014 : UPDATE (soft deactivate)
  → QRC full entries in SECTION B.
────────────────────────────────────────────────────────────────────────
