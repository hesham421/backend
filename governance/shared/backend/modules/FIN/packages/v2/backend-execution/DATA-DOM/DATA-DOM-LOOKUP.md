<!-- source: PHASE:DATA-DOM / SUB:DATA-DOM-LOOKUP -->
<!-- context: DATA-DOM-HEADER.md — phase-level preamble -->
<!-- traces: REQ-FIN-004, REQ-FIN-007, REQ-FIN-022, REQ-FIN-025, REQ-FIN-049, REQ-FIN-054, REQ-FIN-062 -->
<!-- SUB:DATA-DOM-LOOKUP:START traces=REQ-FIN-004,REQ-FIN-007,REQ-FIN-022,REQ-FIN-025,REQ-FIN-049,REQ-FIN-054,REQ-FIN-062 -->
### SUB — DATA-DOM-LOOKUP (FIN's own config and lookup-kind structural data)

#### ENT-FIN-002 — Dimension      kind: config
BINDINGS       table `FIN_DIMENSION` · PK `dimensionPk` (DBF-FIN-014) · PK generation `sequence` → `SEQ_FIN_DIMENSION` (V22) · db-script v2
FIELDS         DBF-FIN-014..022 · DTO: create `{code, nameAr, nameEn}`; no update and, by decision, no deactivate (SRS SCR-REQ-FIN-002 §B4)
DOMAIN RULES   none scoped alone; code unique by UQ_FIN_DIMENSION_CODE (QR-FIN-046)
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → QR-FIN-007, QR-FIN-008, QR-FIN-046; v2 QR-FIN-061 (a tag's dimension)

#### ENT-FIN-003 — DimensionValue      kind: lookup
BINDINGS       table `FIN_DIMENSION_VALUE` · PK `dimensionValuePk` (DBF-FIN-023) · PK generation `sequence` → `SEQ_FIN_DIMENSION_VALUE` (V22) · db-script v2
FIELDS         DBF-FIN-023..033 — the profile's lookup defaults (code, nameAr, nameEn, sortOrder, isActiveFl) plus PK, FK and audit
DOMAIN RULES
- RULE-FIN-002 (SRS §A5) — QR-FIN-010 · UQ_FIN_DIMENSION_VALUE_DIM_CODE
- RULE-FIN-027 (v2) — trigger: on build (event-sourced entry, per BUSINESS_FIELD dimension tag). Statement: the system shall reject the event with the unresolvable-dimension-value error when the event's value for a tag's business field equals the code of no dimension value of the tagged dimension. Message: ar "قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد" · en "The dimension value in the event matches no defined value of this dimension". Resolution is by exact code within the dimension (ADR-FIN-026), served by UQ_FIN_DIMENSION_VALUE_DIM_CODE · application layer (QR-FIN-058) · owner `DimensionValueDomain.assertResolvedByCode`. A match that is inactive is RULE-FIN-009's rejection, not this one
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-009, QR-FIN-010, QR-FIN-011; v2 QR-FIN-058, QR-FIN-061

#### ENT-FIN-009 — EventTypeRule      kind: config
BINDINGS       table `FIN_EVENT_TYPE_RULE` · PK `eventTypeRulePk` (DBF-FIN-089) · PK generation `sequence` → `SEQ_FIN_EVENT_TYPE_RULE` (V22) · db-script v2
FIELDS         DBF-FIN-089..097; eventTypeCode lookup-backed (XM-FIN-001). v2: its eventTypeCode is the rule's half of every account-mapping key (RULE-FIN-020)
DOMAIN RULES   one rule per event type — UQ_FIN_EVENT_TYPE_RULE_CODE and QR-FIN-014; RULE-FIN-005 (QR-FIN-027)
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-012, QR-FIN-013, QR-FIN-014, QR-FIN-027

#### ENT-FIN-010 — RuleLine      kind: config   (MODIFIED v2)
BINDINGS       table `FIN_RULE_LINE` · PK `ruleLinePk` (DBF-FIN-098) · PK generation `sequence` → `SEQ_FIN_RULE_LINE` (V22) · db-script v2
FIELDS         DBF-FIN-098..108 and DBF-FIN-148. v2: accountDerivationTypeCode (DBF-FIN-101) is CONSTANT or MAPPING; accountDerivationValue (DBF-FIN-102) is NULL-able and holds only the constant account code; accountBusinessFieldCode (DBF-FIN-148) names the business field a MAPPING line reads
DTO MEMBERSHIP create request (API-FIN-011): every business field plus `dimensionTags[]` (ENT-FIN-016); excluded {ruleLinePk, lineNo — assigned, createdAt}; no update, no by-id read, no delete (SRS SCR-REQ-FIN-003 §B4); the response carries the line with its tags
LOOKUP FIELDS  accountDerivationTypeCode → `ACCOUNT_DERIVATION_TYPE`; accountBusinessFieldCode → `FIN_EVENT_BUSINESS_FIELD`; amountSourceTypeCode → `AMOUNT_SOURCE_TYPE`; directionCode → `DEBIT_CREDIT`; distributionTypeCode → `DISTRIBUTION_TYPE`
DOMAIN RULES
- RULE-FIN-003 (SRS §A5) — exactly one remainder line once the set is a compound or percentage distribution; `isRemainderFl` (DBF-FIN-107) is the single marker and must agree with the line's own REMAINDER type code · QR-FIN-016 · `EventTypeRuleDomain`
- RULE-FIN-018 (v2) — trigger: on create (rule line). Statement: the system shall reject a rule line whose account derivation type is not an active ACCOUNT_DERIVATION_TYPE value, so that CONSTANT and MAPPING are the only accepted derivations and no derivation reads an account code from the event. Message: ar "نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط" · en "Unsupported account derivation type; only constant or mapping is allowed". Scope CREATE · application layer, membership in MDL's active values (XM-FIN-001) · `EventTypeRuleDomain.assertDerivationSupported`
- RULE-FIN-019 (v2) — trigger: on create (rule line). Statement: the system shall require a CONSTANT rule line to carry an account code in accountDerivationValue and no business field, and a MAPPING rule line to carry a business field in accountBusinessFieldCode and no account code. Message: ar "مواصفة اشتقاق الحساب لا تطابق نوعه" · en "The account derivation spec does not match its type". Scope CREATE · DB: CHK_FIN_RULE_LINE_DERIVATION_SPEC (the exclusive-or), application: which half the type needs · `EventTypeRuleDomain.assertDerivationSpec`
- RULE-FIN-021 (v2) — the MAPPING half, checked when an event is built (full text in ENT-FIN-016)
STATE MACHINE  none — the line lives and retires with its rule
CROSS-MODULE   DBF-FIN-101, DBF-FIN-103, DBF-FIN-105, DBF-FIN-106 and v2 DBF-FIN-148 touch XM-FIN-001
OPERATIONS     CREATE
REPOSITORY OPS → QR-FIN-015 (MODIFIED), QR-FIN-016; read by QR-FIN-025 and QR-FIN-028 at build time

#### ENT-FIN-011 — RecurringTemplate      kind: config
BINDINGS       table `FIN_RECURRING_TEMPLATE` · PK `recurringTemplatePk` (DBF-FIN-109) · PK generation `sequence` → `SEQ_FIN_RECURRING_TEMPLATE` (V22) · db-script v2
FIELDS         DBF-FIN-109..121; scheduleTypeCode and frequencyCode lookup-backed (XM-FIN-001)
DOMAIN RULES   none scoped alone; its run posts through the shared pipeline (RULE-FIN-006 … RULE-FIN-010, cited on API-FIN-014) and `RecurringTemplateDomain.assertCanRun()` refuses a deactivated template (recorded human decision, not a RULE)
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-017, QR-FIN-018, QR-FIN-019

#### ENT-FIN-012 — RecurringTemplateLine      kind: config
BINDINGS       table `FIN_RECURRING_TEMPLATE_LINE` · PK `recurringTemplateLinePk` (DBF-FIN-122) · PK generation `sequence` → `SEQ_FIN_RECURRING_TEMPLATE_LINE` (V22) · db-script v2
FIELDS         DBF-FIN-122..129; directionCode lookup-backed (XM-FIN-001); amount positive by CHK_FIN_RECURRING_TPL_LINE_AMOUNT_POS
DOMAIN RULES   none scoped alone
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → written by QR-FIN-018; read by QR-FIN-019 at run time

#### ENT-FIN-013 — AllocationRule      kind: config
BINDINGS       table `FIN_ALLOCATION_RULE` · PK `allocationRulePk` (DBF-FIN-130) · PK generation `sequence` → `SEQ_FIN_ALLOCATION_RULE` (V22) · db-script v2
FIELDS         DBF-FIN-130..138
DOMAIN RULES   none scoped alone; `AllocationRuleDomain.assertCanRun()` refuses a deactivated rule (recorded human decision, not a RULE)
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-020, QR-FIN-021, QR-FIN-022

#### ENT-FIN-014 — AllocationTarget      kind: config
BINDINGS       table `FIN_ALLOCATION_TARGET` · PK `allocationTargetPk` (DBF-FIN-139) · PK generation `sequence` → `SEQ_FIN_ALLOCATION_TARGET` (V22) · db-script v2
FIELDS         DBF-FIN-139..146; distributionTypeCode lookup-backed (XM-FIN-001)
DOMAIN RULES
- RULE-FIN-003 (SRS §A5, reused) — exactly one remainder target; `isRemainderFl` (DBF-FIN-146 — v1 cited DBF-FIN-141, the lineNo column, a slip corrected here) is the single marker the API-FIN-017 builder reads · QR-FIN-016 · `AllocationRuleDomain`
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → written by QR-FIN-021; read by QR-FIN-022 at run time

#### ENT-FIN-015 — AccountMapping      kind: config   (ADDED v2)
BINDINGS       table `FIN_ACCOUNT_MAPPING` · PK `accountMappingPk` (DBF-FIN-149) · PK generation `sequence` → `SEQ_FIN_ACCOUNT_MAPPING` (db-script v2 BLOCK 1) · db-script v2
BUSINESS CODE  none — a mapping is identified by its key (eventTypeCode, businessFieldCode, businessValue); nothing outside the system quotes it
DEFAULT FIELDS config defaults adapted (SRS A3): `key` → the three-column natural key, unique among ACTIVE rows; `valueAr`/`valueEn` → not stored, the label is the referenced account's nameAr/nameEn; `isActiveFl` kept; four audit fields
FIELDS         DBF-FIN-149..158 — see the DB Alignment Manifest
DTO MEMBERSHIP create `{eventTypeCode, businessFieldCode, businessValue, accountId}`; update `{accountId}` only — the key is fixed after create (ADR-FIN-024); excluded everywhere {accountMappingPk, isActiveFl, audit}; the response carries every field plus the account's code, nameAr and nameEn (QR-FIN-050)
LOOKUP FIELDS  eventTypeCode → `ACCOUNTING_EVENT_TYPE`; businessFieldCode → `FIN_EVENT_BUSINESS_FIELD`; businessValue → `PAYMENT_METHOD` only when businessFieldCode = PAYMENT_METHOD, otherwise the host's code stored as-is — every one a stored code, never a numeric FK
DOMAIN RULES
- RULE-FIN-020 — trigger: on build (event-sourced entry, per MAPPING rule line). Statement: the system shall reject the event with the dedicated unmapped-value error when no active account mapping exists for the rule's event type, the rule line's business field and the event's value for that field; it never substitutes a default account. Message: ar "لا يوجد ربط حساب نشط للقيمة التجارية في الحدث" · en "No active account mapping exists for the business value in the event". Scope build · application layer (QR-FIN-055) · `AccountMappingDomain.assertResolved`. An inactive mapping counts as none
- RULE-FIN-022 — trigger: on create (account mapping). Statement: the system shall reject an account mapping whose event type, business field and business value already have an active mapping. Message: ar "يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها" · en "An active mapping already exists for this event type, business field and value". Scope CREATE · DB: partial unique index UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY; application: QR-FIN-052 · `AccountMappingDomain.assertKeyFree`
- RULE-FIN-023 — trigger: on create / update (account mapping). Statement: the system shall reject an account mapping whose account is not a leaf account or is inactive. Message: ar "الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا" · en "The mapped account must be an active leaf account". Scope CREATE, UPDATE · application layer (QR-FIN-060) · `AccountMappingDomain.assertAccountEligible`. Does not replace RULE-FIN-007 at post time
- RULE-FIN-024 — trigger: on create (account mapping). Statement: the system shall reject an account mapping for the PAYMENT_METHOD business field whose business value is not an active PAYMENT_METHOD code. Message: ar "القيمة ليست طريقة دفع معرَّفة" · en "The value is not a defined payment method". Scope CREATE · application layer, membership in MDL's active PAYMENT_METHOD values (XM-FIN-001) · `AccountMappingDomain.assertBusinessValueDefined`
STATE MACHINE  `isActiveFl` binary; soft deactivation, no reactivation — a deactivated key is restored by creating a new mapping (ADR-FIN-024)
CROSS-MODULE   eventTypeCode, businessFieldCode and (conditionally) businessValue touch XM-FIN-001
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-050, QR-FIN-051, QR-FIN-052, QR-FIN-053, QR-FIN-054, QR-FIN-055

#### ENT-FIN-016 — RuleLineDimension      kind: config   (ADDED v2)
BINDINGS       table `FIN_RULE_LINE_DIM` · PK `ruleLineDimensionPk` (DBF-FIN-159) · PK generation `sequence` → `SEQ_FIN_RULE_LINE_DIM` (db-script v2 BLOCK 1) · db-script v2
BUSINESS CODE  none
DEFAULT FIELDS config defaults adapted (SRS A3): `key` → (ruleLineId, dimensionId), one tag per dimension per line; no stored names (labels come from ENT-FIN-002 / ENT-FIN-003); no `isActiveFl` and no update audit — a tag lives and retires with its rule line (ADR-FIN-025); `createdAt` only
FIELDS         DBF-FIN-159..165 — see the DB Alignment Manifest
DTO MEMBERSHIP submitted only inside API-FIN-011 as `dimensionTags[]`: {dimensionId, valueSourceCode, dimensionValueId (CONSTANT), businessFieldCode (BUSINESS_FIELD)}; excluded {ruleLineDimensionPk, ruleLineId, createdAt}; no endpoint of its own
LOOKUP FIELDS  valueSourceCode → `FIN_DIMENSION_VALUE_SOURCE`; businessFieldCode → `FIN_EVENT_BUSINESS_FIELD`
DOMAIN RULES
- RULE-FIN-025 — trigger: on create (rule line with its dimension tags). Statement: the system shall reject a rule line that carries more than one dimension tag for the same dimension. Message: ar "لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة" · en "A rule line cannot tag the same dimension twice". Scope CREATE · DB: UQ_FIN_RULE_LINE_DIM_LINE_DIM; application: over the submitted list · `EventTypeRuleDomain.assertTagsDistinct`
- RULE-FIN-026 — trigger: on create (rule line with its dimension tags). Statement: the system shall require a CONSTANT dimension tag to carry a dimension value that belongs to the tagged dimension and no business field, and a BUSINESS_FIELD dimension tag to carry a business field and no dimension value. Message: ar "وسم البُعد لا يطابق مصدر قيمته" · en "The dimension tag does not match its value source". Scope CREATE · DB: CHK_FIN_RULE_LINE_DIM_VALUE_SPEC (the exclusive-or); application: which half the source needs, and that a CONSTANT value belongs to the dimension (QR-FIN-061) · `EventTypeRuleDomain.assertTagMatchesSource`
- RULE-FIN-021 — trigger: on build (event-sourced entry, per MAPPING rule line and per BUSINESS_FIELD dimension tag). Statement: the system shall reject the event with the missing-business-field error when the event does not carry the business field named by a MAPPING rule line or by a BUSINESS_FIELD dimension tag. Message: ar "الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة" · en "The event does not carry the business field the rule requires". Scope build · application layer (reads DBF-FIN-148 or DBF-FIN-161 against the event's `fields`) · `EventTypeRuleDomain.assertFieldPresent`
- RULE-FIN-027 — resolution of a BUSINESS_FIELD tag's value (full text in ENT-FIN-003)
STATE MACHINE  none
CROSS-MODULE   valueSourceCode and businessFieldCode touch XM-FIN-001
OPERATIONS     CREATE
REPOSITORY OPS → QR-FIN-056, QR-FIN-057, QR-FIN-061
<!-- SUB:DATA-DOM-LOOKUP:END -->
