<!-- source: PHASE:SVC-API / SUB:SVC-API-CRUD -->
<!-- context: SVC-API-HEADER.md — phase-level preamble -->
<!-- traces: DBF-FIN-002, DBF-FIN-003, DBF-FIN-004, DBF-FIN-005, DBF-FIN-006, DBF-FIN-007, DBF-FIN-008, DBF-FIN-009, DBF-FIN-015, DBF-FIN-016, DBF-FIN-017, DBF-FIN-024, DBF-FIN-025, DBF-FIN-028, DBF-FIN-029, DBF-FIN-036, DBF-FIN-037, DBF-FIN-038, DBF-FIN-044, DBF-FIN-090, DBF-FIN-091, DBF-FIN-092, DBF-FIN-093, DBF-FIN-099, DBF-FIN-101, DBF-FIN-102, DBF-FIN-103, DBF-FIN-105, DBF-FIN-106, DBF-FIN-107, DBF-FIN-112, DBF-FIN-113, DBF-FIN-114, DBF-FIN-115, DBF-FIN-117, DBF-FIN-133, DBF-FIN-134, DBF-FIN-142, DBF-FIN-144, DBF-FIN-148, DBF-FIN-149, DBF-FIN-150, DBF-FIN-151, DBF-FIN-152, DBF-FIN-153, DBF-FIN-154, DBF-FIN-160, DBF-FIN-161, DBF-FIN-163, DBF-FIN-164, REQ-FIN-001, REQ-FIN-002, REQ-FIN-003, REQ-FIN-004, REQ-FIN-005, REQ-FIN-006, REQ-FIN-007, REQ-FIN-008, REQ-FIN-009, REQ-FIN-014, REQ-FIN-015, REQ-FIN-017, REQ-FIN-018, REQ-FIN-019, REQ-FIN-020, REQ-FIN-021, REQ-FIN-022, REQ-FIN-025, REQ-FIN-044, REQ-FIN-045, REQ-FIN-048, REQ-FIN-049, REQ-FIN-054, REQ-FIN-056, REQ-FIN-057, REQ-FIN-058, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061, REQ-FIN-062, REQ-FIN-064, REQ-FIN-065 -->
<!-- SUB:SVC-API-CRUD:START traces=REQ-FIN-001,REQ-FIN-002,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-014,REQ-FIN-022,REQ-FIN-025,REQ-FIN-048,REQ-FIN-049,REQ-FIN-054,REQ-FIN-056,REQ-FIN-057,REQ-FIN-062 -->
### SUB — SVC-API-CRUD

<!-- API:API-FIN-002:START traces=REQ-FIN-001,REQ-FIN-002,DBF-FIN-002,DBF-FIN-005,DBF-FIN-006,DBF-FIN-007,DBF-FIN-008 -->
### API-FIN-002 — create account
Entity       : ENT-FIN-001 · operation CREATE
Endpoint: POST /api/v1/fin/accounts · Layers: `AccountController.create`→`AccountService.create`
Request: (writes DBF-FIN-002 code · DBF-FIN-003 nameAr · DBF-FIN-004 nameEn · DBF-FIN-005 accountTypeCode · DBF-FIN-006 natureCode · DBF-FIN-007 parentAccountId · DBF-FIN-008 isLeafFl) `{code, nameAr, nameEn, accountTypeCode, natureCode, parentAccountId?, isLeafFl}`
Response: 201 · `AccountResponse`
Validations: RULE-FIN-001 (leaf requires no children — only relevant if `parentAccountId`
is later given children; at create this only matters when the same call sets a parent
that itself must remain non-leaf — QR-FIN-006 checks the *parent*, not the new row);
uniqueness of code (QR-FIN-005); accountTypeCode/natureCode validated via XM-FIN-001
Errors: `FIN-409-ACCOUNT-DUP`, `FIN-409-PARENT-NOT-LEAF-ELIGIBLE`, `FIN-400-INVALID-LOOKUP`
Orchestration: (sets DBF-FIN-009 isActiveFl=TRUE on insert; flips the parent's DBF-FIN-008 isLeafFl to FALSE) validate lookups (XM-FIN-001) → validate uniqueness (QR-FIN-005) → if
parentAccountId given, flip the parent's isLeafFl to false via RULE-FIN-001's inverse
effect (a parent gaining its first child can no longer itself be a leaf) → persist (QR-FIN-002) → return
Repository: QR-FIN-002, QR-FIN-005, QR-FIN-006 · join NONE · READ_WRITE
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_CREATE` · Localization: nameAr/nameEn required
Concurrency  : the pre-check is the friendly path; UQ_FIN_ACCOUNT_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_ACCOUNT_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-ACCOUNT-DUP — v1 behaviour as built, recorded here and not changed by v2. Creating a child also flips the named parent's isLeafFl without a lock, racing a concurrent API-FIN-003 that is deciding RULE-FIN-001 over the same child set; the pair can leave an account both leaf and parent, which RULE-FIN-007 would then accept as postable. v1 as built; stated here, not changed by v2 (ADR-FIN-042)
<!-- API:API-FIN-002:END -->

<!-- API:API-FIN-003:START traces=REQ-FIN-002,DBF-FIN-003,DBF-FIN-004,DBF-FIN-008 -->
### API-FIN-003 — update account
Entity       : ENT-FIN-001 · operation UPDATE
Endpoint: PUT /api/v1/fin/accounts/{id} · Layers: `AccountController.update`→`AccountService.update`
Request: (writes DBF-FIN-003 nameAr · DBF-FIN-004 nameEn · DBF-FIN-008 isLeafFl) `{nameAr, nameEn, isLeafFl}` — excludes {accountPk, code, accountTypeCode, natureCode, isActiveFl, audit}
Response: 200 · `AccountResponse`
Validations: RULE-FIN-002-equivalent — RULE-FIN-001 (full text: DATA-DOM §ENT-FIN-001) —
`isLeafFl=true` rejected if the account has any child (QR-FIN-006)
Errors: `FIN-409-HAS-CHILDREN`, `FIN-404-ACCOUNT`
Orchestration: load → check RULE-FIN-001 if isLeafFl changing to true (QR-FIN-006) → update (QR-FIN-003) → return
Repository: QR-FIN-003, QR-FIN-006 · join NONE · READ_WRITE
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_UPDATE` · Localization: both name fields updatable
Concurrency  : RULE-FIN-001 is decided on the child set QR-FIN-006 reads, which a concurrent API-FIN-002 naming this account as parent can change; no lock is specified (v1 as built) — recorded, not changed by v2. The same race can leave the account both leaf and parent, a state RULE-FIN-007 accepts as postable (ADR-FIN-042)
<!-- API:API-FIN-003:END -->

<!-- API:API-FIN-004:START traces=REQ-FIN-003,DBF-FIN-009 -->
### API-FIN-004 — deactivate account
Entity       : ENT-FIN-001 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/accounts/{id}/deactivate · Layers: `AccountController.deactivate`→`AccountService.deactivate`
Request: path `id`, no body · Response: 200 · `AccountResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-ACCOUNT`
Orchestration: (sets DBF-FIN-009 isActiveFl=FALSE) load → `Account.deactivate()` → persist (QR-FIN-004) → return (REQ-FIN-019
subsequently rejects any posting to it)
Repository: QR-FIN-004 · join NONE · READ_WRITE
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_UPDATE` · Localization: n/a
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-004:END -->

<!-- API:API-FIN-006:START traces=REQ-FIN-004,DBF-FIN-015,DBF-FIN-016,DBF-FIN-017 -->
### API-FIN-006 — create dimension
Entity       : ENT-FIN-002 · operation CREATE
Endpoint: POST /api/v1/fin/dimensions · Layers: `DimensionController.create`→`DimensionService.create`
Request: (writes DBF-FIN-015 code · DBF-FIN-016 nameAr · DBF-FIN-017 nameEn) `{code, nameAr, nameEn}` · Response: 201 · `DimensionResponse`
Validations: uniqueness of code (QR-FIN-046, DB `UQ_FIN_DIMENSION_CODE` friendly-checked at service layer)
Errors: `FIN-409-DIMENSION-DUP`
Orchestration: (sets DBF-FIN-018 isActiveFl=TRUE on insert) validate → persist (QR-FIN-008) → return · Repository: QR-FIN-008 · join NONE · READ_WRITE
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_CREATE` · Localization: nameAr/nameEn required
Concurrency  : the pre-check is the friendly path; UQ_FIN_DIMENSION_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_DIMENSION_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-DIMENSION-DUP — v1 behaviour as built, recorded here and not changed by v2
<!-- API:API-FIN-006:END -->

<!-- API:API-FIN-007:START traces=REQ-FIN-005,REQ-FIN-006,DBF-FIN-024,DBF-FIN-025,DBF-FIN-028 -->
### API-FIN-007 — create dimension value
Entity       : ENT-FIN-003 · operation CREATE
Endpoint: POST /api/v1/fin/dimensions/{id}/values · Layers: `DimensionController.createDimensionValue`→`DimensionValueService.create`
Request: (writes DBF-FIN-024 dimensionId from the path · DBF-FIN-025 code · DBF-FIN-026 nameAr · DBF-FIN-027 nameEn · DBF-FIN-028 sortOrder) path `id` (dimensionId); body `{code, nameAr, nameEn, sortOrder}`
Response: 201 · `DimensionValueResponse`
Validations: RULE-FIN-002 (full text: DATA-DOM §ENT-FIN-003) — code unique within the
dimension (QR-FIN-010)
Errors: `FIN-409-DIMVALUE-DUP`, `FIN-404-DIMENSION`
Orchestration: (sets DBF-FIN-029 isActiveFl=TRUE on insert) validate dimension exists → check RULE-FIN-002 (QR-FIN-010) → persist (QR-FIN-009) → return
Repository: QR-FIN-009, QR-FIN-010 · join NONE · READ_WRITE
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_CREATE` · Localization: nameAr/nameEn required
Concurrency  : the pre-check is the friendly path; UQ_FIN_DIMENSION_VALUE_DIM_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_DIMENSION_VALUE_DIM_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-DIMVALUE-DUP — v1 behaviour as built, recorded here and not changed by v2
<!-- API:API-FIN-007:END -->

<!-- API:API-FIN-035:START traces=REQ-FIN-005,REQ-FIN-021,DBF-FIN-029 -->
### API-FIN-035 — deactivate dimension value
Entity       : ENT-FIN-003 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/dimensions/values/{id}/deactivate · Layers: `DimensionController.deactivateDimensionValue`→`DimensionValueService.deactivate`
Request: path `id` (dimensionValuePk), no body · Response: 200 · `DimensionValueResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-DIMVALUE`
Orchestration: (sets DBF-FIN-029 isActiveFl=FALSE) load → `DimensionValue.deactivate()` (the entity's own helper, never a direct field assignment) → persist → return. Effect: `DimensionValueDomain.checkUsableOnLine` reads exactly this flag (DBF-FIN-029), so every posting path citing the value afterwards answers `FIN-409-INVALID-DIMENSION` (RULE-FIN-009 / REQ-FIN-021, via API-FIN-019, 020, 014, 017)
Repository: `DimensionValueRepository.findById` + `save` — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join NONE · READ_WRITE
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_UPDATE` · Localization: n/a
New in this delivery: `FIN-404-DIMVALUE` (`FinErrorCodes.FIN_404_DIMVALUE`, added to BOTH i18n bundles) and `PERM_FIN_DIMENSIONS_UPDATE`. This is the first UPDATE-class endpoint on FIN_DIMENSIONS, so migration `V28__fin_dimensions_update_action.sql` registers the `FIN_DIMENSIONS / UPDATE` action row AND explicitly grants it to `SYS_ADMIN` — V25 grants by a `SELECT` over the registry and has already run, so a later row would otherwise be registered-but-ungrantable.
Why it exists: RULE-FIN-009 / REQ-FIN-021 reject a journal line citing an INACTIVE dimension value and `DimensionValueDomain` implements that check, but nothing could set the flag false, so the branch was unreachable and untestable. Deliberately NOT built (a decision, not a backlog item): a deactivate on the PARENT `Dimension` — no REQ/AC/RULE requires one and `Dimension.isActiveFl` (DBF-FIN-018) drives no behaviour. No `activate` counterpart either, matching the delivered `AccountService.deactivate` precedent.
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-035:END -->

<!-- API:API-FIN-010:START traces=REQ-FIN-007,REQ-FIN-044,REQ-FIN-045,DBF-FIN-090,DBF-FIN-091,DBF-FIN-092 -->
### API-FIN-010 — create event-type rule
Entity       : ENT-FIN-009 · operation CREATE
Endpoint: POST /api/v1/fin/event-rules · Layers: `EventTypeRuleController.create`→`EventTypeRuleService.create`
Request: (writes DBF-FIN-090 eventTypeCode · DBF-FIN-091 nameAr · DBF-FIN-092 nameEn) `{eventTypeCode, nameAr, nameEn}` · Response: 201 · `EventTypeRuleResponse`
Validations: eventTypeCode validated via XM-FIN-001; uniqueness of one-active-rule-per-type (QR-FIN-014)
Errors: `FIN-409-RULE-DUP`, `FIN-400-INVALID-LOOKUP`
Orchestration: (sets DBF-FIN-093 isActiveFl=TRUE on insert) validate lookup → check uniqueness (QR-FIN-014) → persist (QR-FIN-013) → return
Repository: QR-FIN-013, QR-FIN-014 · join NONE · READ_WRITE
Security: screen FIN_RULES · `PERM_FIN_RULES_CREATE` · Localization: nameAr/nameEn required
Precondition: this endpoint is only reachable once FIN's onboarding has completed —
FIN registered as a module with its screens/actions into SEC (REQ-FIN-044) and FIN's lookup types (13 in v1, 15 since v2),
including ACCOUNTING_EVENT_TYPE, registered into MDL (REQ-FIN-045); both run
once at deployment, not per request.
Concurrency  : the pre-check is the friendly path; UQ_FIN_EVENT_TYPE_RULE_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_EVENT_TYPE_RULE_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-RULE-DUP — v1 behaviour as built, recorded here and not changed by v2
<!-- API:API-FIN-010:END -->

<!-- API:API-FIN-011:START traces=REQ-FIN-008,REQ-FIN-009,REQ-FIN-048,REQ-FIN-049,REQ-FIN-062,REQ-FIN-064,REQ-FIN-065,DBF-FIN-099,DBF-FIN-101,DBF-FIN-102,DBF-FIN-148,DBF-FIN-103,DBF-FIN-105,DBF-FIN-106,DBF-FIN-107,DBF-FIN-160,DBF-FIN-161,DBF-FIN-163,DBF-FIN-164 -->
### API-FIN-011 — add rule line (MODIFIED v2 — MAPPING business field, dimension tags)
Entity       : ENT-FIN-010, ENT-FIN-016 · operation CREATE (a line and its tags, gated as an update of the rule)
Endpoint     : POST /api/v1/fin/event-rules/{id}/lines   verb: POST
Layers       : `EventTypeRuleController.createRuleLine` → `RuleLineService.create`; every decision in `EventTypeRuleDomain`
Request      : path `id` → DBF-FIN-099 eventTypeRuleId; body `RuleLineCreateRequest` — `accountDerivationTypeCode` DBF-FIN-101 (String, required) · `accountDerivationValue` DBF-FIN-102 (String, CONSTANT only: the constant account code) · `accountBusinessFieldCode` DBF-FIN-148 (String, MAPPING only) · `amountSourceTypeCode` DBF-FIN-103 (required) · `amountSourceValue` DBF-FIN-104 (required unless REMAINDER) · `directionCode` DBF-FIN-105 (required) · `distributionTypeCode` DBF-FIN-106 (required) · `isRemainderFl` DBF-FIN-107 (Boolean, default false) · `dimensionTags[]`, each {`dimensionId` DBF-FIN-163 (required) · `valueSourceCode` DBF-FIN-160 (required) · `dimensionValueId` DBF-FIN-164 (CONSTANT only) · `businessFieldCode` DBF-FIN-161 (BUSINESS_FIELD only)}; excluded: ruleLinePk, lineNo, ruleLineDimensionPk, ruleLineId, createdAt
Response     : 201 · `ApiResponse<RuleLineResponse>` — the line with its `dimensionTags[]`
Validations  : in this order, inside one transaction; nothing is written if any fails
- lookup codes amountSourceTypeCode, directionCode, distributionTypeCode, every valueSourceCode and every business-field code (FIN_EVENT_BUSINESS_FIELD) through XM-FIN-001 → FIN-400-INVALID-LOOKUP
- RULE-FIN-018 — trigger: on create (rule line). Statement: reject a rule line whose account derivation type is not an active ACCOUNT_DERIVATION_TYPE value, so that CONSTANT and MAPPING are the only accepted derivations and no derivation reads an account code from the event. Message: ar "نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط" · en "Unsupported account derivation type; only constant or mapping is allowed" → FIN-422-UNSUPPORTED-DERIVATION. Membership is read from MDL (`readActiveValuesByKey("ACCOUNT_DERIVATION_TYPE")`, XM-FIN-001); DIRECT is deactivated there by db-script BLOCK 8, so a DIRECT line fails this rule and not the generic lookup check (AC-FIN-048)
- RULE-FIN-019 — trigger: on create (rule line). Statement: require a CONSTANT rule line to carry an account code in accountDerivationValue and no business field, and a MAPPING rule line to carry a business field in accountBusinessFieldCode and no account code. Message: ar "مواصفة اشتقاق الحساب لا تطابق نوعه" · en "The account derivation spec does not match its type" → FIN-422-DERIVATION-SPEC. CHK_FIN_RULE_LINE_DERIVATION_SPEC holds the exclusive-or in the store; the domain check answers first (AC-FIN-049)
- RULE-FIN-003 (v1, SRS §A5) — exactly one remainder line once the set is a compound or percentage distribution, and `isRemainderFl` (DBF-FIN-107) agrees with the line's own REMAINDER type code → FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER (QR-FIN-016). v1 cited the marker as DBF-FIN-103; that column is amountSourceTypeCode — corrected here
- RULE-FIN-025 — trigger: on create (rule line with its dimension tags). Statement: reject a rule line that carries more than one dimension tag for the same dimension. Message: ar "لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة" · en "A rule line cannot tag the same dimension twice" → FIN-422-DIMENSION-TAG-DUP, checked over the submitted list (AC-FIN-069); UQ_FIN_RULE_LINE_DIM_LINE_DIM is the store's guarantee
- RULE-FIN-026 — trigger: on create (rule line with its dimension tags). Statement: require a CONSTANT dimension tag to carry a dimension value that belongs to the tagged dimension and no business field, and a BUSINESS_FIELD dimension tag to carry a business field and no dimension value. Message: ar "وسم البُعد لا يطابق مصدر قيمته" · en "The dimension tag does not match its value source" → FIN-422-DIMENSION-TAG-SOURCE (AC-FIN-070: a CONSTANT BRANCH tag citing an ORGANISATION value). An unknown dimension answers FIN-404-DIMENSION and an unknown value FIN-404-DIMVALUE (QR-FIN-061)
Errors       : FIN-404-RULE, FIN-400-INVALID-LOOKUP, FIN-422-UNSUPPORTED-DERIVATION, FIN-422-DERIVATION-SPEC, FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER, FIN-404-DIMENSION, FIN-404-DIMVALUE, FIN-422-DIMENSION-TAG-DUP, FIN-422-DIMENSION-TAG-SOURCE, FIN-403-FORBIDDEN, FIN-500
Orchestration: (sets DBF-FIN-100 lineNo = the rule's highest lineNo + 1 · each tag's DBF-FIN-162 ruleLineId from the saved line) load the rule under a row lock (FIN-404-RULE) → validate the lookup codes (XM-FIN-001) → `assertDerivationSupported` (RULE-FIN-018) → `assertDerivationSpec` (RULE-FIN-019) → RULE-FIN-003 over the rule's existing lines plus the new one (QR-FIN-016) → load each tag's dimension and CONSTANT value (QR-FIN-061) → `assertTagsDistinct` (RULE-FIN-025) → `assertTagMatchesSource` (RULE-FIN-026) → persist the line (QR-FIN-015) and its tags (QR-FIN-056) → return the line with its tags
Repository   : QR-FIN-016, QR-FIN-061, QR-FIN-015, QR-FIN-056 · join NONE · READ_WRITE (one transaction)
Concurrency  : lineNo is allocated from a read (the rule's highest lineNo + 1), and RULE-FIN-003 decides on the sibling set this call then extends. Two simultaneous additions to one rule are serialized by a PESSIMISTIC_WRITE load of the parent FIN_EVENT_TYPE_RULE row before either is read. FIN_RULE_LINE has no unique constraint on (event_type_rule_id, line_no), so that lock is the only guard
Security     : screen FIN_RULES · `PERM_FIN_RULES_UPDATE` — adding a line, with its tags, is an update of the rule (SRS SCR-REQ-FIN-003 §B4; tags add no action, ADR-FIN-025) — checked before processing
Localization : every message ar + en
<!-- API:API-FIN-011:END -->

<!-- API:API-FIN-034:START traces=REQ-FIN-007,DBF-FIN-093 -->
### API-FIN-034 — deactivate event-type rule
Entity       : ENT-FIN-009 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/event-rules/{id}/deactivate · Layers: `EventTypeRuleController.deactivate`→`EventTypeRuleService.deactivate`
Request: path `id`, no body · Response: 200 · `EventTypeRuleResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-RULE`
Orchestration: (sets DBF-FIN-093 isActiveFl=FALSE) load → clear the active flag (DBF-FIN-093) → persist → return
Repository: `EventTypeRuleRepository.findById` + `save` — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join NONE · READ_WRITE
Security: screen FIN_RULES · `PERM_FIN_RULES_UPDATE` (pre-existing — no new constant, no migration) · Localization: n/a
Why it exists: until this endpoint landed no rule could ever be retired, so `FIN-404-NO-ACTIVE-RULE` (RULE-FIN-005, API-FIN-020) was unreachable. **Stated limitation**, recorded in the service's own javadoc: deactivating does NOT free the event type for a replacement rule, because `EventTypeRuleService.create` guards uniqueness with `existsByEventTypeCode`, which is not scoped to the active flag. No `activate` counterpart, and no rule-line delete — ENT-FIN-010 carries no active-flag column and FIN publishes no `DELETE` endpoint on any screen (a deliberate v1 exclusion, see srs-fin.md SCR-REQ-FIN-003 §B4).
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-034:END -->

<!-- API:API-FIN-013:START traces=REQ-FIN-022,DBF-FIN-112,DBF-FIN-113,DBF-FIN-114,DBF-FIN-115 -->
### API-FIN-013 — create template
Entity       : ENT-FIN-011, ENT-FIN-012 · operation CREATE
Endpoint: POST /api/v1/fin/recurring-templates · Layers: `RecurringTemplateController.create`→`RecurringTemplateService.create`
Request: (writes DBF-FIN-110 nameAr · DBF-FIN-111 nameEn · DBF-FIN-112 scheduleTypeCode · DBF-FIN-113 frequencyCode · DBF-FIN-114 startDate · DBF-FIN-116 endDate; per line DBF-FIN-125 accountId · DBF-FIN-126 amount · DBF-FIN-127 directionCode · DBF-FIN-128 dimensionValueId) `{nameAr, nameEn, scheduleTypeCode, frequencyCode?, startDate, endDate?, lines: [...]}`
Response: 201 · `RecurringTemplateResponse`
Validations: scheduleTypeCode/frequencyCode/line directionCode validated via XM-FIN-001;
frequencyCode required unless scheduleTypeCode=REVERSING
Errors: `FIN-400-MISSING-FREQUENCY`, `FIN-400-INVALID-LOOKUP`
Orchestration: (sets DBF-FIN-115 nextRunDate=startDate · DBF-FIN-117 isActiveFl=TRUE · per line DBF-FIN-123 recurringTemplateId and DBF-FIN-124 lineNo in submission order) validate → set nextRunDate=startDate → persist template+lines (QR-FIN-018) → return
Repository: QR-FIN-018 · join NONE · READ_WRITE
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_CREATE` · Localization: nameAr/nameEn required
Concurrency  : NONE — nothing unique is allocated: every PK comes from its table's own sequence and lineNo is the position in this one request
<!-- API:API-FIN-013:END -->

<!-- API:API-FIN-036:START traces=REQ-FIN-022,DBF-FIN-117 -->
### API-FIN-036 — deactivate template
Entity       : ENT-FIN-011 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/recurring-templates/{id}/deactivate · Layers: `RecurringTemplateController.deactivate`→`RecurringTemplateService.deactivate`
Request: path `id`, no body · Response: 200 · `RecurringTemplateResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-TEMPLATE`
Orchestration: (sets DBF-FIN-117 isActiveFl=FALSE) load → `RecurringTemplate.deactivate()` (the entity's own helper, never a direct field assignment) → persist → re-read the template's lines and hand them to the mapper → return. The line re-read is not decoration: `RecurringTemplateResponse.lineCount` is derived from the list the mapper is handed, so passing an empty list would misreport the aggregate as having no lines. It is orchestration (load → map), not a rule.
Repository: `RecurringTemplateRepository.findById` + `save`, plus `RecurringTemplateLineRepository.findByRecurringTemplatePk` for the response's lines — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join NONE · READ_WRITE
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_UPDATE` (pre-existing — V24 seeds the `FIN_RECURRING_TEMPLATES / UPDATE` action row and V25's blanket Tier-3 grant already covers it, so no new constant, no new error code and no migration) · Localization: n/a
**DEFECT CLOSED 2026-09-12 — deactivate now DOES stop the run.** This paragraph used to report an OPEN DEFECT: `RecurringTemplateService.run` (API-FIN-014) ignored `isActiveFl`, so a deactivated template still ran and still posted. That is no longer true. `RecurringTemplateDomain.assertCanRun()` — a NEW Domain companion for ENT-FIN-011 — is called by `RecurringTemplateService.run` immediately after the row is loaded and before anything else is resolved, and refuses a deactivated template with `FIN-409-NOT-ACTIVE` (`Status.CONFLICT` → HTTP 409; ar "هذا التعريف غير نشط ولا يمكن تشغيله", en "This definition is deactivated and cannot be run"). The screen's 404 was deliberately not reused: the template does exist. **This gate is a RECORDED HUMAN DECISION, not spec.** No RULE-FIN-* states it, and AC-FIN-023 is written "Given an active recurring template" without stating any outcome for an inactive one — a later reader must not mistake the gate for a requirement that was always there. What remains OPEN on this screen is only that a template cannot be UPDATED after creation (next paragraph). Recorded identically at srs-fin.md SCR-REQ-FIN-004 §B4 and §"Access summary".
Why it exists: srs-fin.md SCR-REQ-FIN-004 §B4 recorded the absence of a template deactivate as an OPEN DEFECT rather than a scope decision — `IS_ACTIVE_FL` is NOT NULL, the entity's `activate()`/`deactivate()` shipped with zero callers, B2 advertises an `isActiveFl(EXACT)` filter over a column nothing could set to FALSE, and AC-FIN-023 is written "Given an active recurring template", presupposing a state nothing could produce. Deliberately NOT built, and NOT taken by this endpoint: a template `update` — a correct update must decide the fate of the template's existing child lines, which is a design question no REQ, AC or RULE answers, so it remains the still-open half of the same gap. No `activate` counterpart either; FIN ships none for any entity.
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-036:END -->

<!-- API:API-FIN-016:START traces=REQ-FIN-025,DBF-FIN-133,DBF-FIN-142,DBF-FIN-144 -->
### API-FIN-016 — create allocation rule
Entity       : ENT-FIN-013, ENT-FIN-014 · operation CREATE
Endpoint: POST /api/v1/fin/allocation-rules · Layers: `AllocationRuleController.create`→`AllocationRuleService.create`
Request: (writes DBF-FIN-131 nameAr · DBF-FIN-132 nameEn · DBF-FIN-133 sourceAccountId; per target DBF-FIN-142 targetAccountId · DBF-FIN-143 dimensionValueId · DBF-FIN-144 distributionTypeCode · DBF-FIN-145 distributionValue · DBF-FIN-146 isRemainderFl) `{nameAr, nameEn, sourceAccountId, targets: [...]}`
Response: 201 · `AllocationRuleResponse`
Validations: RULE-FIN-003 (reused) — exactly one remainder target when any sibling is
PERCENTAGE or any target is already marked remainder (QR-FIN-016, reused), and each target's
`isRemainderFl` (DBF-FIN-146) must agree with its own `distributionTypeCode`;
distributionTypeCode validated via XM-FIN-001
Errors: `FIN-409-REMAINDER-COUNT`, `FIN-422-REMAINDER-MARKER`, `FIN-404-ACCOUNT`, `FIN-400-INVALID-LOOKUP`
Orchestration: (sets DBF-FIN-134 isActiveFl=TRUE · per target DBF-FIN-140 allocationRuleId and DBF-FIN-141 lineNo in submission order) validate → check RULE-FIN-003 across targets (QR-FIN-016) → persist rule+targets (QR-FIN-021) → return
Repository: QR-FIN-021, QR-FIN-016 · join NONE · READ_WRITE
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_CREATE` · Localization: nameAr/nameEn required
Concurrency  : NONE — nothing unique is allocated: every PK comes from its table's own sequence and lineNo is the position in this one request
<!-- API:API-FIN-016:END -->

<!-- API:API-FIN-037:START traces=REQ-FIN-025,DBF-FIN-134 -->
### API-FIN-037 — deactivate allocation rule
Entity       : ENT-FIN-013 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/allocation-rules/{id}/deactivate · Layers: `AllocationRuleController.deactivate`→`AllocationRuleService.deactivate`
Request: path `id`, no body · Response: 200 · `AllocationRuleResponse` (isActiveFl=false)
Validations: none beyond existence — RULE-FIN-003, the one rule `AllocationRuleDomain` owns, governs the remainder-target SET at create and run time and says nothing about the active flag · Errors: `FIN-404-ALLOCATION-RULE`
Orchestration: (sets DBF-FIN-134 isActiveFl=FALSE) load → `AllocationRule.deactivate()` (the entity's own helper, never a direct field assignment) → persist → re-read the rule's targets and hand them to the mapper → return. The target re-read is not decoration: `AllocationRuleResponse.targetCount` is derived from the list the mapper is handed, so passing an empty list would misreport the aggregate as having no targets. It is orchestration (load → map), not a rule.
Repository: `AllocationRuleRepository.findById` + `save`, plus `AllocationTargetRepository.findByAllocationRulePk` for the response's targets — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join NONE · READ_WRITE
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_UPDATE` (pre-existing — V24 seeds the `FIN_ALLOCATION_RULES / UPDATE` action row and V25's blanket Tier-3 grant already covers it, so no new constant, no new error code and no migration) · Localization: n/a
**DEFECT CLOSED 2026-09-12 — deactivate now DOES stop the run.** This paragraph used to report an OPEN DEFECT: `AllocationRuleService.run` (API-FIN-017) ignored `isActiveFl`, so a deactivated rule still ran and still posted, and `AllocationRuleDomain.isActive()` had no caller. That is no longer true. `AllocationRuleDomain.assertCanRun()` is called by `AllocationRuleService.run` immediately after the row is loaded and before the target set is even fetched, and refuses a deactivated rule with `FIN-409-NOT-ACTIVE` (`Status.CONFLICT` → HTTP 409; ar "هذا التعريف غير نشط ولا يمكن تشغيله", en "This definition is deactivated and cannot be run"). The screen's 404 was deliberately not reused: the rule does exist. **This gate is a RECORDED HUMAN DECISION, not spec** — no RULE-FIN-* states it, and a later reader must not mistake it for a requirement that was always there. What remains OPEN on this screen is only that a rule cannot be UPDATED after creation (next paragraph). Recorded identically at srs-fin.md SCR-REQ-FIN-005 §B4 and §"Access summary".
Why it exists: srs-fin.md SCR-REQ-FIN-005 §B4 recorded the absence of a rule deactivate as an OPEN DEFECT rather than a scope decision — `IS_ACTIVE_FL` is NOT NULL, the entity's `activate()`/`deactivate()` shipped with zero callers, `AllocationRuleDomain.isActive()` was dead code, and B2 advertises an `isActiveFl(EXACT)` filter over a column nothing could set to FALSE. Deliberately NOT built, and NOT taken by this endpoint: a rule `update` — a correct update must decide the fate of the rule's existing targets (and therefore of RULE-FIN-003's remainder-target set), which is a design question no REQ, AC or RULE answers, so it remains the still-open half of the same gap. No `activate` counterpart either; FIN ships none for any entity.
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-037:END -->

<!-- API:API-FIN-019:START traces=REQ-FIN-014,REQ-FIN-015,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,DBF-FIN-036,DBF-FIN-037,DBF-FIN-038,DBF-FIN-044 -->
### API-FIN-019 — create manual entry
Entity       : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 · operation CREATE
Endpoint: POST /api/v1/fin/journal-entries · Layers: `JournalEntryController.createManual`→`JournalEntryService.createManual`
Request: (writes DBF-FIN-036 docDate · DBF-FIN-037 fiscalYearId · DBF-FIN-038 periodId · DBF-FIN-044 descriptionAr · DBF-FIN-045 descriptionEn; per line DBF-FIN-054 accountId · DBF-FIN-055 amount · DBF-FIN-056 directionCode · DBF-FIN-058 descriptionAr · DBF-FIN-059 descriptionEn; per line dimension DBF-FIN-063 dimensionId · DBF-FIN-064 dimensionValueId) `{docDate, fiscalYearId, periodId, journalTypeCode="MANUAL", descriptionAr,
descriptionEn, lines: [{accountId, amount, directionCode, descriptionAr?, descriptionEn?,
dimensions: [{dimensionId, dimensionValueId}]}]}` — excludes {journalEntryPk, docNo,
statusCode, postedAt, audit}
Response: 201 · `JournalEntryResponse` (statusCode=POSTED on success)
Validations: RULE-FIN-006 (debit=credit, QR-FIN-029), RULE-FIN-007 (leaf/active accounts,
QR-FIN-030), RULE-FIN-008 (period open, QR-FIN-031), RULE-FIN-009 (dimension valid,
QR-FIN-032) — every failure returned together (REQ-FIN-015), nothing posts if any fails.
Header coherence (RULE-FIN-017) runs FIRST and fail-fast: the submitted `periodId` must
belong to the submitted `fiscalYearId` (DBF-FIN-076) and `docDate` must fall inside that
period's [startDate, endDate] (DBF-FIN-080/081) — an incoherent triple makes RULE-FIN-008's
own period gate meaningless, lets the entry take a docNo from the wrong year's series, and
corrupts every period-scoped report and the year-end close. This is the only API that needs
it: every system-generated entry derives the three facts from one another
Errors: `FIN-400-PERIOD-NOT-IN-YEAR`, `FIN-400-DOCDATE-OUTSIDE-PERIOD`, `FIN-409-UNBALANCED`,
`FIN-409-NOT-POSTABLE-ACCOUNT`, `FIN-409-PERIOD-NOT-OPEN`, `FIN-409-INVALID-DIMENSION`
Numbering: `docNo` = `JV-{fiscalYearCode}-{NNNNNN}` — literal prefix `JV-`, the owning
fiscal year's `code` (DBF-FIN-066, VARCHAR(10)), `-`, then a zero-padded 6-digit counter
starting at `000001` (e.g. `JV-2026-000123`; worst case 20 chars, inside
`doc_no VARCHAR(30)`). Counter scoped per `fiscalYearId`, restarting at `000001` each
fiscal year — one counter for all journal types, never segmented by `journalTypeCode`;
`UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO (fiscal_year_id, doc_no)` backs uniqueness at the
database level. Produced by a FIN-local generator in `com.erp.fin`, deliberately NOT a
shared `com.erp.common` component (no platform numbering engine exists in this repo);
implemented in this phase. Assigned once on create, immutable thereafter — excluded from
every create/update request DTO, present only in responses.
Orchestration: (sets DBF-FIN-035 docNo · DBF-FIN-039 journalTypeCode=MANUAL · DBF-FIN-040 statusCode DRAFT→POSTED · DBF-FIN-046 postedAt · per line DBF-FIN-052 journalEntryId, DBF-FIN-053 lineNo and DBF-FIN-057 isRemainderFl=FALSE · per line dimension DBF-FIN-062 journalLineId) resolve fiscal year UNDER A ROW LOCK (`SELECT ... FOR UPDATE` on
FIN_FISCAL_YEAR, the docNo series' allocation lock) → resolve period → check RULE-FIN-017 →
generate docNo (FIN-local generator) → build DRAFT (QR-FIN-024) → validate
(QR-FIN-029..032) → on success: post (QR-FIN-033); on failure: discard the whole attempt
(one transaction, REQ-FIN-015) → return. The lock is what makes the per-fiscal-year counter
safe under concurrency: two simultaneous creates can no longer observe the same predecessor,
so `UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO` stays an unreachable backstop instead of surfacing as an
unlocalized data-integrity 409. A sequence was rejected (the counter restarts per year and the
schema declares none) and so was catch-and-retry (a service may not catch
DataIntegrityViolationException)
Repository: QR-FIN-024, QR-FIN-029, QR-FIN-030, QR-FIN-031, QR-FIN-032, QR-FIN-033 · join
NONE · READ_WRITE (one transaction, build-through-post)
Security: screen FIN_JOURNAL_ENTRIES · `PERM_FIN_JOURNAL_ENTRIES_CREATE` · Localization: descriptionAr/En
Concurrency  : the docNo series — allocated from a read of the fiscal year's last number and then written — is guarded by the fiscal-year row lock taken first in the Orchestration line; UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO stays an unreachable backstop
<!-- API:API-FIN-019:END -->

<!-- API:API-FIN-039:START traces=REQ-FIN-054,REQ-FIN-059,REQ-FIN-060,REQ-FIN-061,DBF-FIN-150,DBF-FIN-151,DBF-FIN-152,DBF-FIN-153,DBF-FIN-154 -->
### API-FIN-039 — create account mapping
Entity       : ENT-FIN-015 · operation CREATE
Endpoint     : POST /api/v1/fin/account-mappings   verb: POST
Layers       : `AccountMappingController.create` → `AccountMappingService.create`; every decision in `AccountMappingDomain`
Request      : body `AccountMappingCreateRequest` — `eventTypeCode` DBF-FIN-150 (String, required, ACCOUNTING_EVENT_TYPE) · `businessFieldCode` DBF-FIN-151 (String, required, FIN_EVENT_BUSINESS_FIELD) · `businessValue` DBF-FIN-152 (String, required, ≤ 100, a code stored as-is) · `accountId` DBF-FIN-153 (Long, required, FK_ACCOUNT_MAPPING_ACCOUNT); excluded: accountMappingPk (from SEQ_FIN_ACCOUNT_MAPPING), isActiveFl, audit fields
Response     : 201 · `ApiResponse<AccountMappingResponse>` (isActiveFl = true)
Validations  : in this order
- lookup codes eventTypeCode (ACCOUNTING_EVENT_TYPE) and businessFieldCode (FIN_EVENT_BUSINESS_FIELD) through XM-FIN-001 → FIN-400-INVALID-LOOKUP
- RULE-FIN-024 — trigger: on create (account mapping). Statement: reject an account mapping for the PAYMENT_METHOD business field whose business value is not an active PAYMENT_METHOD code. Message: ar "القيمة ليست طريقة دفع معرَّفة" · en "The value is not a defined payment method" → FIN-422-INVALID-PAYMENT-METHOD. Only when businessFieldCode = PAYMENT_METHOD; membership in `readActiveValuesByKey("PAYMENT_METHOD")` (XM-FIN-001; AC-FIN-065)
- RULE-FIN-023 — trigger: on create / update (account mapping). Statement: reject an account mapping whose account is not a leaf account or is inactive. Message: ar "الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا" · en "The mapped account must be an active leaf account" → FIN-422-MAPPED-ACCOUNT-INELIGIBLE (QR-FIN-060; AC-FIN-064). An unknown accountId → FIN-404-ACCOUNT
- RULE-FIN-022 — trigger: on create (account mapping). Statement: reject an account mapping whose event type, business field and business value already have an active mapping. Message: ar "يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها" · en "An active mapping already exists for this event type, business field and value" → FIN-409-MAPPING-DUP (QR-FIN-052; AC-FIN-063)
Errors       : FIN-400-INVALID-LOOKUP, FIN-422-INVALID-PAYMENT-METHOD, FIN-404-ACCOUNT, FIN-422-MAPPED-ACCOUNT-INELIGIBLE, FIN-409-MAPPING-DUP, FIN-403-FORBIDDEN, FIN-500
Orchestration: (sets DBF-FIN-154 isActiveFl=TRUE; the PK is drawn from SEQ_FIN_ACCOUNT_MAPPING) validate the two lookup codes (XM-FIN-001) → when businessFieldCode = PAYMENT_METHOD, hand MDL's active PAYMENT_METHOD codes to `assertBusinessValueDefined` (RULE-FIN-024) → load the account (QR-FIN-060) and hand its leaf and active flags to `assertAccountEligible` (RULE-FIN-023) → look for an active mapping on the key (QR-FIN-052) and hand the answer to `assertKeyFree` (RULE-FIN-022) → persist, flushed (QR-FIN-051) → return (AC-FIN-058)
Repository   : QR-FIN-060, QR-FIN-052, QR-FIN-051 · join NONE · READ_WRITE
Concurrency  : two simultaneous creates for one key can both pass QR-FIN-052. The partial unique index UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY (one ACTIVE row per event type, business field and value) makes the second impossible: its flush is refused and FinPersistenceRefusalAdvisor answers FIN-409-MAPPING-DUP, the same answer the pre-check gives (ADR-FIN-030). The pre-check is the friendly path and the index is the guarantee
Security     : screen FIN_ACCOUNT_MAPPINGS · `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`, checked before processing (VIEW is the gateway)
Localization : every message ar + en; no name is stored — the mapping is labelled by its account's nameAr/nameEn
<!-- API:API-FIN-039:END -->

<!-- API:API-FIN-040:START traces=REQ-FIN-056,REQ-FIN-058,REQ-FIN-060,DBF-FIN-149,DBF-FIN-153 -->
### API-FIN-040 — update account mapping (the account only)
Entity       : ENT-FIN-015 · operation UPDATE (change the account)
Endpoint     : PUT /api/v1/fin/account-mappings/{id}   verb: PUT
Layers       : `AccountMappingController.update` → `AccountMappingService.update`
Request      : path `id` → DBF-FIN-149 accountMappingPk; body `AccountMappingUpdateRequest` — `accountId` DBF-FIN-153 (Long, required) and nothing else. eventTypeCode, businessFieldCode and businessValue are fixed after create and are not in the body (ADR-FIN-024); to move a mapping to another key, deactivate it and create a new one
Response     : 200 · `ApiResponse<AccountMappingResponse>`
Validations  :
- RULE-FIN-023 — trigger: on update (account mapping); statement and message as in API-FIN-039 → FIN-422-MAPPED-ACCOUNT-INELIGIBLE (QR-FIN-060); an unknown accountId → FIN-404-ACCOUNT
- no active-state gate: the SRS states none, and a deactivated mapping resolves nothing whatever its account (RULE-FIN-020)
Errors       : FIN-404-ACCOUNT-MAPPING, FIN-404-ACCOUNT, FIN-422-MAPPED-ACCOUNT-INELIGIBLE, FIN-403-FORBIDDEN, FIN-500
Orchestration: (sets DBF-FIN-153 accountId only; updatedBy and updatedAt are filled by the platform) load the mapping (FIN-404-ACCOUNT-MAPPING) → load the account (QR-FIN-060) → `AccountMappingDomain.assertAccountEligible` (RULE-FIN-023) → update (QR-FIN-053) → return (AC-FIN-060). REQ-FIN-058 holds by structure: a posted line stores its resolved accountId and no line references a mapping, so this write cannot reach a posted entry (AC-FIN-062)
Repository   : QR-FIN-060, QR-FIN-053 · join NONE · READ_WRITE
Concurrency  : NONE for the mapping — it sets one column from the request and allocates nothing; two concurrent changes end last-writer-wins, each validated against its own account. The account's eligibility is read and not locked, so an account deactivated a moment later is caught at post time by RULE-FIN-007 (REQ-FIN-052)
Security     : screen FIN_ACCOUNT_MAPPINGS · `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`, checked before processing
Localization : every message ar + en
<!-- API:API-FIN-040:END -->

<!-- API:API-FIN-041:START traces=REQ-FIN-057,REQ-FIN-058,DBF-FIN-149,DBF-FIN-154 -->
### API-FIN-041 — deactivate account mapping
Entity       : ENT-FIN-015 · operation UPDATE (deactivate)
Endpoint     : PUT /api/v1/fin/account-mappings/{id}/deactivate   verb: PUT — FIN's house style: a soft deactivate under UPDATE, no DELETE endpoint (SRS SCR-REQ-FIN-013 §B4/§B5)
Layers       : `AccountMappingController.deactivate` → `AccountMappingService.deactivate`
Request      : path `id` → DBF-FIN-149 accountMappingPk, no body
Response     : 200 · `ApiResponse<AccountMappingResponse>` (isActiveFl = false)
Validations  : none beyond existence; deactivating a mapping that is already inactive is not an error (the API-FIN-004 precedent)
Errors       : FIN-404-ACCOUNT-MAPPING, FIN-403-FORBIDDEN, FIN-500
Orchestration: (sets DBF-FIN-154 isActiveFl=FALSE) load (FIN-404-ACCOUNT-MAPPING) → `AccountMapping.deactivate()` → persist (QR-FIN-054) → return. From the next event on QR-FIN-055 no longer finds the mapping, so an event that needs it answers FIN-422-UNMAPPED-VALUE (REQ-FIN-057, AC-FIN-061); posted lines keep their account (REQ-FIN-058). No `activate` counterpart (ADR-FIN-024)
Repository   : QR-FIN-054 · join NONE · READ_WRITE
Concurrency  : NONE — flips one flag; nothing is allocated or decided on a read
Security     : screen FIN_ACCOUNT_MAPPINGS · `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`, checked before processing
Localization : n/a
<!-- API:API-FIN-041:END -->
<!-- SUB:SVC-API-CRUD:END -->
