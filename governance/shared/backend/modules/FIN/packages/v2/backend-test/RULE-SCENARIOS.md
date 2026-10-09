<!-- source: PHASE:TEST-PLAN-BE / SUB:RULE-SCENARIOS -->
<!-- context: TEST-PLAN-BE-HEADER.md — phase-level preamble -->
<!-- traces: AC-FIN-002, AC-FIN-003, AC-FIN-006, AC-FIN-009, AC-FIN-011, AC-FIN-012, AC-FIN-013, AC-FIN-014, AC-FIN-015, AC-FIN-016, AC-FIN-017, AC-FIN-018, AC-FIN-019, AC-FIN-020, AC-FIN-021, AC-FIN-026, AC-FIN-029, AC-FIN-030, AC-FIN-032, AC-FIN-033, AC-FIN-034, AC-FIN-035, AC-FIN-048, AC-FIN-053, AC-FIN-054, AC-FIN-055, AC-FIN-056, AC-FIN-057, AC-FIN-061, AC-FIN-062, AC-FIN-063, AC-FIN-064, AC-FIN-065, AC-FIN-069, AC-FIN-070, AC-FIN-072, AC-FIN-073, AC-FIN-074, AC-FIN-077, AC-FIN-078, AC-FIN-079, AC-FIN-080, AC-FIN-082, API-FIN-003, API-FIN-004, API-FIN-007, API-FIN-011, API-FIN-017, API-FIN-019, API-FIN-020, API-FIN-021, API-FIN-022, API-FIN-024, API-FIN-025, API-FIN-026, API-FIN-028, API-FIN-039, API-FIN-040, API-FIN-041, REQ-FIN-002, REQ-FIN-003, REQ-FIN-006, REQ-FIN-009, REQ-FIN-011, REQ-FIN-012, REQ-FIN-013, REQ-FIN-014, REQ-FIN-015, REQ-FIN-016, REQ-FIN-017, REQ-FIN-018, REQ-FIN-019, REQ-FIN-020, REQ-FIN-021, REQ-FIN-026, REQ-FIN-029, REQ-FIN-030, REQ-FIN-032, REQ-FIN-033, REQ-FIN-034, REQ-FIN-035, REQ-FIN-048, REQ-FIN-051, REQ-FIN-052, REQ-FIN-053, REQ-FIN-057, REQ-FIN-058, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061, REQ-FIN-064, REQ-FIN-065, REQ-FIN-067, REQ-FIN-068, REQ-FIN-069, REQ-FIN-072, REQ-FIN-073, REQ-FIN-074, REQ-FIN-076 -->
<!-- SUB:RULE-SCENARIOS:START traces=REQ-FIN-002,REQ-FIN-003,REQ-FIN-006,REQ-FIN-009,REQ-FIN-011,REQ-FIN-012,REQ-FIN-013,REQ-FIN-014,REQ-FIN-015,REQ-FIN-016,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-026,REQ-FIN-029,REQ-FIN-030,REQ-FIN-032,REQ-FIN-033,REQ-FIN-034,REQ-FIN-035,REQ-FIN-048,REQ-FIN-051,REQ-FIN-052,REQ-FIN-053,REQ-FIN-057,REQ-FIN-058,REQ-FIN-059,REQ-FIN-060,REQ-FIN-061,REQ-FIN-064,REQ-FIN-065,REQ-FIN-067,REQ-FIN-068,REQ-FIN-069,REQ-FIN-072,REQ-FIN-073,REQ-FIN-074,REQ-FIN-076,AC-FIN-002,AC-FIN-003,AC-FIN-006,AC-FIN-009,AC-FIN-011,AC-FIN-012,AC-FIN-013,AC-FIN-014,AC-FIN-015,AC-FIN-016,AC-FIN-017,AC-FIN-018,AC-FIN-019,AC-FIN-020,AC-FIN-021,AC-FIN-026,AC-FIN-029,AC-FIN-030,AC-FIN-032,AC-FIN-033,AC-FIN-034,AC-FIN-035,AC-FIN-048,AC-FIN-053,AC-FIN-054,AC-FIN-055,AC-FIN-056,AC-FIN-057,AC-FIN-061,AC-FIN-062,AC-FIN-063,AC-FIN-064,AC-FIN-065,AC-FIN-069,AC-FIN-070,AC-FIN-072,AC-FIN-073,AC-FIN-074,AC-FIN-077,AC-FIN-078,AC-FIN-079,AC-FIN-080,AC-FIN-082 -->
### SUB — RULE-SCENARIOS

The ACs whose Then names a `RULE-*` violation or a state transition. Each one asserts the catalog
code and both message strings, or the stored state the rule governs.

**v2 cases open the SUB** (TC-FIN-094 … TC-FIN-128, the rule-driven half of CS-FIN-001), and the
v1 cases follow unchanged. Every API-FIN-020 case below is called by the event consumer's own
service principal holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`. Each one asserts that a rejected
event leaves **no row at all**: no JournalEntry carrying its reference, no line, no line
dimension and no `docNo` drawn from the fiscal-year series (SRS A7). The search API-FIN-018 has
no `eventReference` filter, so that absence is read at the store (DBF-FIN-041).

<!-- TC:TC-FIN-094:START traces=AC-FIN-048,REQ-FIN-048,API-FIN-011 -->
### TC-FIN-094 — a DIRECT rule line is refused as an unsupported derivation
Derived from : AC-FIN-048  (REQ-FIN-048)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-018 → FIN-422-UNSUPPORTED-DERIVATION (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: `ACCOUNT_DERIVATION_TYPE` holds exactly two active values in MDL, CONSTANT and
               MAPPING, DIRECT having been deactivated by db-script v2 BLOCK 8; an EventTypeRule
               (ENT-FIN-009) exists; a finance administrator holding `PERM_FIN_RULES_UPDATE`
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with
                  `accountDerivationTypeCode` = DIRECT and otherwise well-formed line fields
Expected     : 422 with code FIN-422-UNSUPPORTED-DERIVATION —
               ar: «نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط» ·
               en: "Unsupported account derivation type; only constant or mapping is allowed".
               The answer is this rule's own code, **not** the generic FIN-400-INVALID-LOOKUP:
               membership in MDL's active values is RULE-FIN-018's own check. No RuleLine row is
               created and the rule's line set is unchanged
Test data    : DIRECT — AC-FIN-048's own value; the other line fields are placeholders
<!-- TC:TC-FIN-094:END -->

<!-- TC:TC-FIN-099:START traces=AC-FIN-053,REQ-FIN-051,API-FIN-020 -->
### TC-FIN-099 — an event missing the business field its MAPPING line reads is refused
Derived from : AC-FIN-053  (REQ-FIN-051)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-021 → FIN-422-MISSING-BUSINESS-FIELD (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the PAYMENT_RECEIVED rule of AC-FIN-050, whose debit line is MAPPING on
               PAYMENT_METHOD, with its mappings for "CASH" and "CHEQUE"; an Open period covering
               the event date
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) · PAYMENT_METHOD CASH (active) — each
               created through API-MDL-006 `POST /api/v1/mdl/lookup-types/{id}/values` on its key's type
               (SRS A6: host data, never a product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3005`, type
                  PAYMENT_RECEIVED and a `fields` map that has **no** PAYMENT_METHOD key
Expected     : 422 with code FIN-422-MISSING-BUSINESS-FIELD —
               ar: «الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة» ·
               en: "The event does not carry the business field the rule requires".
               No default account is substituted: a missing fact is never read as "cash". No
               JournalEntry exists with eventReference `EVT-3005`
Test data    : `EVT-3005` — AC-FIN-053's own reference; the amount is a placeholder
<!-- TC:TC-FIN-099:END -->

<!-- TC:TC-FIN-100:START traces=AC-FIN-054,REQ-FIN-052,API-FIN-020 -->
### TC-FIN-100 — a mapped account that has since gained a child is refused at post time
Derived from : AC-FIN-054  (REQ-FIN-052)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event, after
               API-FIN-002 POST /api/v1/fin/accounts as setup
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409)
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: the mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102 was
               saved while 1102 was an active leaf; a child account has **since** been created
               under 1102 through API-FIN-002, which sets 1102's `isLeafFl` (DBF-FIN-008) to
               false; the PAYMENT_RECEIVED rule of AC-FIN-050; an Open period
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3006` and
                  `fields.PAYMENT_METHOD` = "CHEQUE"
Expected     : 409 with code FIN-409-NOT-POSTABLE-ACCOUNT —
               ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en: "The target account does not accept direct posting".
               The mapped account is validated exactly as a CONSTANT one: the save-time check of
               RULE-FIN-023 does not replace the post-time check. No JournalEntry exists with
               eventReference `EVT-3006`
Test data    : 1102 and `EVT-3006` — AC-FIN-054's own values; the child account is a placeholder
<!-- TC:TC-FIN-100:END -->

<!-- TC:TC-FIN-101:START traces=AC-FIN-055,REQ-FIN-052,API-FIN-020 -->
### TC-FIN-101 — a mapped account that has since been deactivated is refused at post time
Derived from : AC-FIN-055  (REQ-FIN-052)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event, after
               API-FIN-004 PUT /api/v1/fin/accounts/{id}/deactivate as setup
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409)
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: the mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102, saved
               while 1102 was active; 1102 has **since** been deactivated through API-FIN-004
               (`isActiveFl` false, DBF-FIN-009); the PAYMENT_RECEIVED rule of AC-FIN-050; an Open
               period
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3007` and
                  `fields.PAYMENT_METHOD` = "CHEQUE"
Expected     : 409 with code FIN-409-NOT-POSTABLE-ACCOUNT and the same message as TC-FIN-100 —
               ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en: "The target account does not accept direct posting".
               No JournalEntry exists with eventReference `EVT-3007`
Test data    : 1102 and `EVT-3007` — AC-FIN-055's own values
<!-- TC:TC-FIN-101:END -->

<!-- TC:TC-FIN-102:START traces=AC-FIN-056,REQ-FIN-053,API-FIN-020 -->
### TC-FIN-102 — a business value with no mapping is refused, never defaulted
Derived from : AC-FIN-056  (REQ-FIN-053)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-020 → FIN-422-UNMAPPED-VALUE (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the PAYMENT_RECEIVED rule and mappings of AC-FIN-050 ("CASH" → 1101,
               "CHEQUE" → 1102), and **no** mapping of any state for the PAYMENT_METHOD value
               "TRANSFER_102"; an Open period
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) · PAYMENT_METHOD CASH (active) — each
               created through API-MDL-006 `POST /api/v1/mdl/lookup-types/{id}/values` on its key's type
               (SRS A6: host data, never a product seed — ADR-FIN-068); TRANSFER_102 is never created —
               this case needs it on no mapping in any state
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3008` and
                  `fields.PAYMENT_METHOD` = "TRANSFER_102"
Expected     : 422 with code FIN-422-UNMAPPED-VALUE —
               ar: «لا يوجد ربط حساب نشط للقيمة التجارية في الحدث» ·
               en: "No active account mapping exists for the business value in the event".
               Neither 1101 nor 1102 is used in its place. No JournalEntry exists with
               eventReference `EVT-3008`
Test data    : "TRANSFER_102" and `EVT-3008` — AC-FIN-056's own values, the payment method renamed
               for this case so that no other case's mapping can reach it (ADR-FIN-068)
<!-- TC:TC-FIN-102:END -->

<!-- TC:TC-FIN-103:START traces=AC-FIN-057,REQ-FIN-053,API-FIN-020 -->
### TC-FIN-103 — an inactive mapping counts as no mapping
Derived from : AC-FIN-057  (REQ-FIN-053)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event, after
               API-FIN-041 PUT /api/v1/fin/account-mappings/{id}/deactivate as setup
Rule / code  : RULE-FIN-020 → FIN-422-UNMAPPED-VALUE (422)
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: the mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CASH_103") → account 1101 exists with
               `isActiveFl` false (DBF-FIN-154), and no other mapping for "CASH_103"; the
               PAYMENT_RECEIVED rule of AC-FIN-050; an Open period
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) · PAYMENT_METHOD CASH_103 (active; its
               mapping is saved, then deactivated through API-FIN-041) — each created through
               API-MDL-006 `POST /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host
               data, never a product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3009` and
                  `fields.PAYMENT_METHOD` = "CASH_103"
Expected     : 422 with code FIN-422-UNMAPPED-VALUE and the message of TC-FIN-102 —
               ar: «لا يوجد ربط حساب نشط للقيمة التجارية في الحدث» ·
               en: "No active account mapping exists for the business value in the event".
               The inactive row is not read as a fallback. No JournalEntry exists with
               eventReference `EVT-3009`
Test data    : "CASH_103", 1101 and `EVT-3009` — AC-FIN-057's own values, the payment method
               renamed for this case, because every other case needs that method's mapping active (ADR-FIN-068)
<!-- TC:TC-FIN-103:END -->

<!-- TC:TC-FIN-107:START traces=AC-FIN-061,REQ-FIN-057,API-FIN-041,API-FIN-020 -->
### TC-FIN-107 — a deactivated mapping stops resolving from the next event on
Derived from : AC-FIN-061  (REQ-FIN-057)
Exercises    : API-FIN-041 PUT /api/v1/fin/account-mappings/{id}/deactivate, then
               API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-020 → FIN-422-UNMAPPED-VALUE (422), on the event that follows
Scenario     : STATE · data class VALID · language ALL
Preconditions: an active mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CASH") → account 1101; the
               PAYMENT_RECEIVED rule of AC-FIN-050; an Open period; a finance administrator
               holding `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) · PAYMENT_METHOD CASH (active) — each
               created through API-MDL-006 `POST /api/v1/mdl/lookup-types/{id}/values` on its key's type
               (SRS A6: host data, never a product seed — ADR-FIN-068)
Steps        : 1. PUT `/api/v1/fin/account-mappings/{id}/deactivate`
               2. POST `/api/v1/fin/journal-entries/from-event` with a new reference and
                  `fields.PAYMENT_METHOD` = "CASH"
Expected     : step 1 → 200 · the mapping with `isActiveFl` = false; the row is kept, never
                  hard-deleted.
               step 2 → 422 with code FIN-422-UNMAPPED-VALUE —
                  ar: «لا يوجد ربط حساب نشط للقيمة التجارية في الحدث» ·
                  en: "No active account mapping exists for the business value in the event" —
                  exactly as TC-FIN-103. Entries posted through the mapping before step 1 keep
                  their account (REQ-FIN-058)
Test data    : "CASH" and 1101 — AC-FIN-061's own values; the event reference is a placeholder
<!-- TC:TC-FIN-107:END -->

<!-- TC:TC-FIN-108:START traces=AC-FIN-062,REQ-FIN-058,API-FIN-040,API-FIN-022,API-FIN-028 -->
### TC-FIN-108 — changing a mapping never reaches an entry already posted through it
Derived from : AC-FIN-062  (REQ-FIN-058)
Exercises    : API-FIN-040 PUT /api/v1/fin/account-mappings/{id}, then
               API-FIN-022 GET /api/v1/fin/journal-entries/{id} and
               API-FIN-028 GET /api/v1/fin/reports/account-ledger
Rule / code  : — (no rule fires; the invariant holds by structure — a posted line stores its
               resolved accountId and references no mapping)
Scenario     : STATE · data class VALID · language ALL
Preconditions: entry `JV-2026-000500`, POSTED from event `EVT-3010`, whose debit line is on
               account 5220 through the mapping (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT");
               account 5225 is an active leaf; a caller holding
               `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`, `PERM_FIN_JOURNAL_ENTRIES_VIEW` and
               `PERM_FIN_ACCOUNT_LEDGER_VIEW`
Host data    : ACCOUNTING_EVENT_TYPE EXPENSE_PAID (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. PUT `/api/v1/fin/account-mappings/{RENT id}` with `accountId` = 5225
               2. GET `/api/v1/fin/journal-entries/{JV-2026-000500 id}`
               3. GET `/api/v1/fin/reports/account-ledger` for account 5220 over the entry's date
Expected     : step 1 → 200 with `accountId` = 5225.
               step 2 → the debit line of `JV-2026-000500` still shows account 5220.
               step 3 → the ledger of 5220 still contains that line, and the ledger of 5225 does
                  not. A mapping governs later derivations only
Test data    : `JV-2026-000500`, `EVT-3010`, 5220, 5225 and "RENT" — AC-FIN-062's own values
<!-- TC:TC-FIN-108:END -->

<!-- TC:TC-FIN-109:START traces=AC-FIN-063,REQ-FIN-059,API-FIN-039 -->
### TC-FIN-109 — a second active mapping for the same key is refused
Derived from : AC-FIN-063  (REQ-FIN-059)
Exercises    : API-FIN-039 POST /api/v1/fin/account-mappings
Rule / code  : RULE-FIN-022 → FIN-409-MAPPING-DUP (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an active mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102;
               account 1103 is an active leaf, so nothing but the key can fail; a finance
               administrator holding `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/account-mappings` with (PAYMENT_RECEIVED, PAYMENT_METHOD,
                  "CHEQUE") and `accountId` = 1103
               2. POST `/api/v1/fin/account-mappings/search` with those three key filters and
                  `isActiveFl` = true
Expected     : step 1 → 409 with code FIN-409-MAPPING-DUP —
                  ar: «يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها» ·
                  en: "An active mapping already exists for this event type, business field and
                  value".
               step 2 → exactly 1 active row for that key, still on account 1102
Test data    : 1102, 1103 and "CHEQUE" — AC-FIN-063's own values
<!-- TC:TC-FIN-109:END -->

<!-- TC:TC-FIN-110:START traces=AC-FIN-064,REQ-FIN-060,API-FIN-039 -->
### TC-FIN-110 — a mapping to a parent account is refused when it is saved
Derived from : AC-FIN-064  (REQ-FIN-060)
Exercises    : API-FIN-039 POST /api/v1/fin/account-mappings
Rule / code  : RULE-FIN-023 → FIN-422-MAPPED-ACCOUNT-INELIGIBLE (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: account 1100 is a parent (rollup) account with `isLeafFl` = false; the mapping
               key submitted has no active mapping and its codes are valid, so only the account
               can fail; a finance administrator holding `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`
Steps        : 1. POST `/api/v1/fin/account-mappings` with a valid key and `accountId` = 1100
Expected     : 422 with code FIN-422-MAPPED-ACCOUNT-INELIGIBLE —
               ar: «الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا» ·
               en: "The mapped account must be an active leaf account".
               No AccountMapping row is written: the gap is caught at configuration, not first
               discovered as a failed event
Test data    : 1100 — AC-FIN-064's own value; the key is a placeholder
<!-- TC:TC-FIN-110:END -->

<!-- TC:TC-FIN-111:START traces=AC-FIN-065,REQ-FIN-061,API-FIN-039 -->
### TC-FIN-111 — a misspelt payment method is refused as a mapping value
Derived from : AC-FIN-065  (REQ-FIN-061)
Exercises    : API-FIN-039 POST /api/v1/fin/account-mappings
Rule / code  : RULE-FIN-024 → FIN-422-INVALID-PAYMENT-METHOD (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: PAYMENT_METHOD includes CASH and CHEQUE as active values in MDL (CHEQUE seeded by
               db-script v2 BLOCK 8, CASH added as host data); the account submitted is an active
               leaf; a finance administrator holding `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) · PAYMENT_METHOD CASH (active) — each
               created through API-MDL-006 `POST /api/v1/mdl/lookup-types/{id}/values` on its key's type
               (SRS A6: host data, never a product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/account-mappings` with (PAYMENT_RECEIVED, PAYMENT_METHOD,
                  "CHEQEU") and that account
Expected     : 422 with code FIN-422-INVALID-PAYMENT-METHOD —
               ar: «القيمة ليست طريقة دفع معرَّفة» ·
               en: "The value is not a defined payment method".
               No AccountMapping row is written. The check applies only because the business
               field is PAYMENT_METHOD; for any other field the host's code is stored as-is
Test data    : "CHEQEU" — AC-FIN-065's own misspelling; the account is a placeholder
<!-- TC:TC-FIN-111:END -->

<!-- TC:TC-FIN-115:START traces=AC-FIN-069,REQ-FIN-064,API-FIN-011 -->
### TC-FIN-115 — a rule line tagging one dimension twice is refused
Derived from : AC-FIN-069  (REQ-FIN-064)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-025 → FIN-422-DIMENSION-TAG-DUP (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an EventTypeRule; the dimension BRANCH is active and holds a value with code
               "BR-01"; a finance administrator holding `PERM_FIN_RULES_UPDATE`
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with an otherwise valid line
                  whose `dimensionTags` are {BRANCH: CONSTANT "BR-01"} and
                  {BRANCH: BUSINESS_FIELD BRANCH_CODE}
Expected     : 422 with code FIN-422-DIMENSION-TAG-DUP —
               ar: «لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة» ·
               en: "A rule line cannot tag the same dimension twice".
               No RuleLine row is created and no RuleLineDimension row either: the line and its
               tags are written in one transaction or not at all
Test data    : BRANCH, "BR-01" and BRANCH_CODE — AC-FIN-069's own values
<!-- TC:TC-FIN-115:END -->

<!-- TC:TC-FIN-116:START traces=AC-FIN-070,REQ-FIN-065,API-FIN-011 -->
### TC-FIN-116 — a CONSTANT tag citing another dimension's value is refused
Derived from : AC-FIN-070  (REQ-FIN-065)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-026 → FIN-422-DIMENSION-TAG-SOURCE (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the dimensions BRANCH and ORGANISATION are active, and "ORG-1" is a value of
               ORGANISATION; an EventTypeRule; a finance administrator holding
               `PERM_FIN_RULES_UPDATE`
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with an otherwise valid line
                  tagged {BRANCH: CONSTANT "ORG-1"}
Expected     : 422 with code FIN-422-DIMENSION-TAG-SOURCE —
               ar: «وسم البُعد لا يطابق مصدر قيمته» ·
               en: "The dimension tag does not match its value source".
               No RuleLine row is created
Test data    : BRANCH, ORGANISATION and "ORG-1" — AC-FIN-070's own values
<!-- TC:TC-FIN-116:END -->

<!-- TC:TC-FIN-118:START traces=AC-FIN-072,REQ-FIN-067,API-FIN-020 -->
### TC-FIN-118 — an event resolving to an inactive dimension value is refused
Derived from : AC-FIN-072  (REQ-FIN-067)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event, after
               API-FIN-035 PUT /api/v1/fin/dimensions/values/{id}/deactivate as setup
Rule / code  : RULE-FIN-009 → FIN-409-INVALID-DIMENSION (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the rule line of AC-FIN-066 on the PAYMENT_RECEIVED rule; BRANCH holds a value
               with code "BR-09" that has been deactivated through API-FIN-035; an Open period
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3013` and
                  `fields.BRANCH_CODE` = "BR-09"
Expected     : 409 with code FIN-409-INVALID-DIMENSION —
               ar: «قيمة البُعد غير صالحة» · en: "The dimension value is invalid".
               The value **matches** a defined code, so this is RULE-FIN-009's rejection and
               **not** FIN-422-UNRESOLVED-DIMENSION-VALUE (RULE-FIN-027's Test-Hint). The check is
               the same one a manual line receives. No JournalEntry exists with eventReference
               `EVT-3013`
Test data    : "BR-09" and `EVT-3013` — AC-FIN-072's own values
<!-- TC:TC-FIN-118:END -->

<!-- TC:TC-FIN-119:START traces=AC-FIN-073,REQ-FIN-068,API-FIN-020 -->
### TC-FIN-119 — an event missing the business field a dimension tag names is refused
Derived from : AC-FIN-073  (REQ-FIN-068)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-021 → FIN-422-MISSING-BUSINESS-FIELD (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the rule line of AC-FIN-066 on the PAYMENT_RECEIVED rule, whose BRANCH tag is
               BUSINESS_FIELD BRANCH_CODE; an Open period
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3014` and a
                  `fields` map that has **no** BRANCH_CODE key
Expected     : 422 with code FIN-422-MISSING-BUSINESS-FIELD —
               ar: «الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة» ·
               en: "The event does not carry the business field the rule requires".
               The entry is never posted without its branch attribution. No JournalEntry exists
               with eventReference `EVT-3014`
Test data    : BRANCH_CODE and `EVT-3014` — AC-FIN-073's own values
<!-- TC:TC-FIN-119:END -->

<!-- TC:TC-FIN-120:START traces=AC-FIN-074,REQ-FIN-069,API-FIN-020 -->
### TC-FIN-120 — an event dimension value that matches no defined code is refused
Derived from : AC-FIN-074  (REQ-FIN-069)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-027 → FIN-422-UNRESOLVED-DIMENSION-VALUE (422)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the rule line of AC-FIN-066 on the PAYMENT_RECEIVED rule; BRANCH holds **no**
               value with code "BR-99", active or not; an Open period
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3015` and
                  `fields.BRANCH_CODE` = "BR-99"
Expected     : 422 with code FIN-422-UNRESOLVED-DIMENSION-VALUE —
               ar: «قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد» ·
               en: "The dimension value in the event matches no defined value of this
               dimension".
               No translation and no guess is attempted. No JournalEntry exists with
               eventReference `EVT-3015`
Test data    : "BR-99" and `EVT-3015` — AC-FIN-074's own values
<!-- TC:TC-FIN-120:END -->

<!-- TC:TC-FIN-123:START traces=AC-FIN-077,REQ-FIN-072,API-FIN-020 -->
### TC-FIN-123 — a repeated event reference is answered as a duplicate
Derived from : AC-FIN-077  (REQ-FIN-072)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-004 → FIN-409-DUPLICATE-EVENT (409) — the pre-check detection
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a POSTED entry with eventReference `EVT-4001`; its event type still carries its
               active rule
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with another event whose
                  reference is `EVT-4001`
Expected     : 409 with code FIN-409-DUPLICATE-EVENT —
               ar: «تم بالفعل ترحيل قيد لهذا المرجع» ·
               en: "An entry for this event reference has already been posted".
               Exactly 1 JournalEntry exists with eventReference `EVT-4001`. For the consumer
               this code means "already posted — acknowledge" (DOC phase)
Test data    : `EVT-4001` — AC-FIN-077's own value
<!-- TC:TC-FIN-123:END -->

<!-- TC:TC-FIN-124:START traces=AC-FIN-078,REQ-FIN-072,API-FIN-020 -->
### TC-FIN-124 — two simultaneous events with one reference post once and never answer 500
Derived from : AC-FIN-078  (REQ-FIN-072)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event, twice concurrently
Rule / code  : RULE-FIN-004 → FIN-409-DUPLICATE-EVENT (409) — the store-refusal detection
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: no entry with eventReference `EVT-4002`; an active rule for the event's type; an
               Open period; two callers under the consumer's service principal
Steps        : 1. submit two identical events with reference `EVT-4002` so that both pass the
                  duplicate pre-check before either commits — the harness holds both requests
                  past the pre-check and releases them together
               2. read back every entry carrying that reference
Expected     : step 1 → one call answers 200 with the POSTED entry; the other answers 409 with
                  code FIN-409-DUPLICATE-EVENT and the message of TC-FIN-123 —
                  ar: «تم بالفعل ترحيل قيد لهذا المرجع» ·
                  en: "An entry for this event reference has already been posted".
                  The losing call is **never** FIN-500 nor any generic data-integrity answer:
                  UQ_FIN_JOURNAL_ENTRY_EVENT_REF refuses the second insert and the refusal is
                  translated to this rule's code.
               step 2 → exactly 1 entry for `EVT-4002`
Test data    : `EVT-4002` — AC-FIN-078's own value
<!-- TC:TC-FIN-124:END -->

<!-- TC:TC-FIN-125:START traces=AC-FIN-079,REQ-FIN-073,API-FIN-020 -->
### TC-FIN-125 — an event into a soft-closed period gets the distinct period-not-open code
Derived from : AC-FIN-079  (REQ-FIN-073)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-008 → FIN-409-PERIOD-NOT-OPEN (409)
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: the period covering 2026-10-05 is SOFT_CLOSE; an active rule for the event's
               type whose accounts are leaf and active, so the period is the only failing fact
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-4003` and
                  `docDate` 2026-10-05
Expected     : 409 with code FIN-409-PERIOD-NOT-OPEN —
               ar: «الفترة المستهدفة غير مفتوحة» · en: "The target period is not open".
               The code differs from every validation and configuration code API-FIN-020 lists —
               every FIN-400-*, every FIN-422-*, and FIN-422-NO-DEFINED-PERIOD in particular — so
               the consumer can wait and retry on it alone. No JournalEntry exists with
               eventReference `EVT-4003`
Test data    : 2026-10-05 and `EVT-4003` — AC-FIN-079's own values
<!-- TC:TC-FIN-125:END -->

<!-- TC:TC-FIN-126:START traces=AC-FIN-080,REQ-FIN-074,API-FIN-020 -->
### TC-FIN-126 — an event date no period covers is a configuration error, not a retry signal
Derived from : AC-FIN-080  (REQ-FIN-074)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-028 → FIN-422-NO-DEFINED-PERIOD (422)
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: no FiscalPeriod whose `startDate` … `endDate` (DBF-FIN-080, DBF-FIN-081) covers
               2027-01-15; an active rule for the event's type
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-4004` and
                  `docDate` 2027-01-15
Expected     : 422 with code FIN-422-NO-DEFINED-PERIOD —
               ar: «لا توجد فترة محاسبية معرَّفة تغطي تاريخ الحدث» ·
               en: "No accounting period is defined for the event date".
               The code differs from FIN-409-PERIOD-NOT-OPEN, and the v1 answer FIN-404-PERIOD is
               no longer raised on this path. No JournalEntry exists with eventReference
               `EVT-4004`
Test data    : 2027-01-15 and `EVT-4004` — AC-FIN-080's own values
<!-- TC:TC-FIN-126:END -->

<!-- TC:TC-FIN-128:START traces=AC-FIN-082,REQ-FIN-076,API-FIN-020 -->
### TC-FIN-128 — a MAPPING remainder line still absorbs the rounding difference exactly
Derived from : AC-FIN-082  (REQ-FIN-076)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-010 → success path, no code · RULE-FIN-003 and RULE-FIN-006 pass
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an active rule distributing the event amount as 33% + 33% + remainder, whose one
               remainder line (`isRemainderFl` true) is MAPPING on PAYMENT_METHOD; an active
               mapping for "CHEQUE" to an active leaf account; an Open period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-4005`,
                  amount 100.00 and `fields.PAYMENT_METHOD` = "CHEQUE"
Expected     : 200 · POSTED. The two percentage lines total 66.00, the remainder line is
               exactly 34.00 **on the mapped account**, and the entry's debits equal its credits
               at 100.00. A mapped remainder line computes exactly like any other: every v1
               posting check holds on the new path unchanged
Test data    : 100.00 · 33% · 33% · 66.00 · 34.00 · "CHEQUE" · `EVT-4005` — AC-FIN-082's own
<!-- TC:TC-FIN-128:END -->

<!-- TC:TC-FIN-002:START traces=AC-FIN-002,REQ-FIN-002,API-FIN-003 -->
### TC-FIN-002 — an account with children cannot be marked as accepting direct posting
Derived from : AC-FIN-002  (REQ-FIN-002)
Exercises    : API-FIN-003 PUT /api/v1/fin/accounts/{id}
Rule / code  : RULE-FIN-001 → FIN-409-HAS-CHILDREN (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a finance administrator holding `PERM_FIN_ACCOUNTS_UPDATE` (and the
               `PERM_FIN_ACCOUNTS_VIEW` gateway); an account `<parent>` (ENT-FIN-001) that has at
               least one child row whose `parentAccountId` (DBF-FIN-007) points at it
Steps        : 1. PUT `/api/v1/fin/accounts/{parent id}` with `isLeafFl` = true
Expected     : 409 · `LocalizedException → {code, messageAr, messageEn}` with code
               FIN-409-HAS-CHILDREN —
               ar: «لا يمكن لحساب له حسابات فرعية أن يقبل ترحيلاً مباشرًا» ·
               en: "An account with sub-accounts cannot accept direct posting".
               The stored `isLeafFl` (DBF-FIN-008) is unchanged; a subsequent API-FIN-001 search
               returns the parent still non-leaf
Test data    : `<parent>` and `<child>` are placeholders — AC-FIN-002 names no code; the child
               relation is the only fact the AC states
<!-- TC:TC-FIN-002:END -->

<!-- TC:TC-FIN-003:START traces=AC-FIN-003,REQ-FIN-003,API-FIN-004 -->
### TC-FIN-003 — deactivating an account blocks every later posting to it
Derived from : AC-FIN-003  (REQ-FIN-003)
Exercises    : API-FIN-004 PUT /api/v1/fin/accounts/{id}/deactivate, then
               API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409), on the second half
Scenario     : STATE · data class VALID · language ALL
Preconditions: an active leaf account `<A>`; an Open period whose `[startDate, endDate]` covers
               the document date; a caller holding `PERM_FIN_ACCOUNTS_UPDATE` and
               `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. PUT `/api/v1/fin/accounts/{A}/deactivate`
               2. POST `/api/v1/fin/journal-entries` with a balanced two-line entry, one line
                  targeting `<A>`
Expected     : step 1 → 200 · `AccountResponse` with `isActiveFl` = false (DBF-FIN-009).
               step 2 → 409 with code FIN-409-NOT-POSTABLE-ACCOUNT —
               ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en: "The target account does not accept direct posting".
               Nothing is written: REQ-FIN-015's one-transaction rule means no DRAFT survives
Test data    : `<A>` is a placeholder; the AC names no code
<!-- TC:TC-FIN-003:END -->

<!-- TC:TC-FIN-006:START traces=AC-FIN-006,REQ-FIN-006,API-FIN-007 -->
### TC-FIN-006 — a duplicate code under the same dimension is refused
Derived from : AC-FIN-006  (REQ-FIN-006)
Exercises    : API-FIN-007 POST /api/v1/fin/dimensions/{id}/values
Rule / code  : RULE-FIN-002 → FIN-409-DIMVALUE-DUP (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a caller holding `PERM_FIN_DIMENSIONS_CREATE`; a dimension `PROJECT`
               (ENT-FIN-002) already holding a value with code `NORTH`
Steps        : 1. POST `/api/v1/fin/dimensions/{PROJECT id}/values` with code `NORTH`, both
                  names and a sortOrder
Expected     : 409 with code FIN-409-DIMVALUE-DUP —
               ar: «هذا الرمز مستخدم بالفعل ضمن هذا البُعد» ·
               en: "This code is already used within this dimension".
               No second row is stored: API-FIN-008 filtered by that dimension returns exactly
               one `NORTH` value
Test data    : `NORTH` — AC-FIN-006's own value; `PROJECT` — ENT-FIN-002's own example code
<!-- TC:TC-FIN-006:END -->

<!-- TC:TC-FIN-009:START traces=AC-FIN-009,REQ-FIN-009,API-FIN-011 -->
### TC-FIN-009 — a percentage line set with no remainder line is refused
Derived from : AC-FIN-009  (REQ-FIN-009)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-003 → FIN-409-REMAINDER-COUNT (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a caller holding `PERM_FIN_RULES_UPDATE`; an EventTypeRule (ENT-FIN-009) whose
               existing line set already carries one `PERCENTAGE`-distributed line and no line
               marked `isRemainderFl` (DBF-FIN-107)
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with a second line whose
                  `distributionTypeCode` is `PERCENTAGE` and whose `isRemainderFl` is false
Expected     : 409 with code FIN-409-REMAINDER-COUNT —
               ar: «يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي» ·
               en: "Exactly one remainder line is required when any percentage distribution is
               present".
               The rule's stored line set is unchanged, and stays unchanged until exactly one
               line is marked remainder — the AC's own wording
Test data    : `PERCENTAGE` — a seeded `DISTRIBUTION_TYPE` value (SRS A6); the two-line count is
               AC-FIN-009's own
<!-- TC:TC-FIN-009:END -->

<!-- TC:TC-FIN-011:START traces=AC-FIN-011,REQ-FIN-011,API-FIN-020 -->
### TC-FIN-011 — a repeated event reference never produces a second entry
Derived from : AC-FIN-011  (REQ-FIN-011)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-004 → FIN-409-DUPLICATE-EVENT (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a POSTED JournalEntry (ENT-FIN-004) whose `eventReference` (DBF-FIN-041) is
               `EVT-1001`; the event type still carries its active rule; the caller is the Event
               consumer's own service principal holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with a canonical event whose
                  reference is `EVT-1001`
Expected     : 409 with code FIN-409-DUPLICATE-EVENT —
               ar: «تم بالفعل ترحيل قيد لهذا المرجع» ·
               en: "An entry for this event reference has already been posted".
               API-FIN-018 filtered on that reference still returns exactly one entry; no second
               `docNo` is consumed from the fiscal-year series
Test data    : `EVT-1001` — AC-FIN-011's own value
<!-- TC:TC-FIN-011:END -->

<!-- TC:TC-FIN-012:START traces=AC-FIN-012,REQ-FIN-012,API-FIN-020 -->
### TC-FIN-012 — the remainder line absorbs the rounding difference exactly
Derived from : AC-FIN-012  (REQ-FIN-012)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-010 → (success path; no code is raised when the residue is positive)
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an active EventTypeRule whose lines distribute the event amount as 33% + 33% +
               remainder, exactly one line carrying `isRemainderFl` = true (DBF-FIN-107) and
               agreeing with its own `REMAINDER` type code; an Open period covering the document
               date; every derived account leaf and active
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with an event whose amount is
                  100.00
Expected     : 200 · `JournalEntryResponse` with `statusCode` = POSTED. The two percentage lines
               total 66.00, each rounded to the smallest currency unit, and the remainder line is
               exactly 34.00 (DBF-FIN-055, `NUMERIC(18,4)`), so total debits equal total credits
               to the smallest currency unit. The remainder amount is a difference, never a
               percentage — RULE-FIN-010's own statement
Test data    : 100.00 · 33% · 33% · 66.00 · 34.00 — all AC-FIN-012's own values
<!-- TC:TC-FIN-012:END -->

<!-- TC:TC-FIN-013:START traces=AC-FIN-013,REQ-FIN-013,API-FIN-020 -->
### TC-FIN-013 — an event whose type has no active rule is refused
Derived from : AC-FIN-013  (REQ-FIN-013)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-005 → FIN-404-NO-ACTIVE-RULE (404)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an `ACCOUNTING_EVENT_TYPE` value registered in MDL for which FIN holds no active
               EventTypeRule row — reachable either because none was ever created or because the
               only one was retired through API-FIN-034
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with an event of that type
Expected     : 404 with code FIN-404-NO-ACTIVE-RULE —
               ar: «لا توجد قاعدة نشطة لهذا النوع من الأحداث» ·
               en: "No active rule exists for this event type".
               No entry is built and none is posted; the failure is recorded for operator
               follow-up, which is AC-FIN-013's own Then
Test data    : the event type is a placeholder — `ACCOUNTING_EVENT_TYPE` seeds no value (SRS A6:
               host-specific, added as data)
<!-- TC:TC-FIN-013:END -->

<!-- TC:TC-FIN-015:START traces=AC-FIN-015,REQ-FIN-015,API-FIN-019 -->
### TC-FIN-015 — two failing validations are both returned and nothing is posted
Derived from : AC-FIN-015  (REQ-FIN-015)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-006 and RULE-FIN-007 together → FIN-409-UNBALANCED,
               FIN-409-NOT-POSTABLE-ACCOUNT
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an accountant holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`; an Open period; one
               non-leaf (rollup) account among the submitted lines; the submitted `fiscalYearId`,
               `periodId` and `docDate` are coherent, so RULE-FIN-017 does not fail first
Steps        : 1. POST `/api/v1/fin/journal-entries` with an entry that is both unbalanced and
                  targets the rollup account on one line
Expected     : the response carries **both** failures, not the first one only —
               FIN-409-UNBALANCED (ar: «القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن» ·
               en: "The entry is unbalanced — total debits do not equal total credits") and
               FIN-409-NOT-POSTABLE-ACCOUNT (ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en: "The target account does not accept direct posting").
               Nothing is written at all: the whole build-through-post sequence is one
               transaction, so no DRAFT row and no `docNo` survive the failed attempt
Test data    : the two failing conditions are AC-FIN-015's own count; account ids are placeholders
<!-- TC:TC-FIN-015:END -->

<!-- TC:TC-FIN-016:START traces=AC-FIN-016,REQ-FIN-016,API-FIN-022 -->
### TC-FIN-016 — a POSTED entry cannot be deleted; only a reversal changes its effect
Derived from : AC-FIN-016  (REQ-FIN-016)
Exercises    : API-FIN-022 GET /api/v1/fin/journal-entries/{id} (the surface assertion) ·
               API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse (the only mutation)
Rule / code  : RULE-FIN-016 → no catalog code: the rule is enforced by omission, there being no
               UPDATE and no DELETE mapping on a POSTED row anywhere in the module
Scenario     : STATE · data class EDGE · language ALL
Preconditions: a POSTED JournalEntry; a caller holding `PERM_FIN_JOURNAL_ENTRIES_VIEW` and
               `PERM_FIN_JOURNAL_ENTRIES_REVERSE`
Steps        : 1. enumerate the mappings published on `/api/v1/fin/journal-entries` and on
                  `/api/v1/fin/journal-entries/{id}`
               2. issue a DELETE against `/api/v1/fin/journal-entries/{id}`
               3. GET `/api/v1/fin/journal-entries/{id}`
Expected     : step 1 → the published set is search, create, from-event, reverse and read, and no
                  other; FIN publishes no DELETE endpoint on any screen.
               step 2 → refused by the framework's own routing before any FIN code runs, so **no
                  FIN catalog code is emitted** and none is asserted here.
               step 3 → the entry is byte-identical to before, still `statusCode` = POSTED.
               The only path that changes its effect is API-FIN-021 (REQ-FIN-028), whose own
               effect is a new linked entry, never a mutation of this row
Test data    : none beyond the existing POSTED entry
<!-- TC:TC-FIN-016:END -->

<!-- TC:TC-FIN-017:START traces=AC-FIN-017,REQ-FIN-017,API-FIN-019 -->
### TC-FIN-017 — a DRAFT that passes every check posts directly and locks
Derived from : AC-FIN-017  (REQ-FIN-017)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-016 → no code (the lock is enforced by omission) · RULE-FIN-006,
               RULE-FIN-007, RULE-FIN-008, RULE-FIN-009 all pass
Scenario     : STATE · data class VALID · language ALL
Preconditions: an Open period belonging to the submitted fiscal year and covering the document
               date; two or more balanced lines on leaf, active accounts; every cited dimension
               value active and belonging to its stated dimension
Steps        : 1. POST `/api/v1/fin/journal-entries` with that entry
               2. GET the created entry through API-FIN-022
Expected     : 201 · `statusCode` = POSTED and `postedAt` (DBF-FIN-046) set, with **no separate
               human approval step** between validation and posting — REQ-FIN-017's whole point.
               A `docNo` of the form `JV-{fiscalYearCode}-{NNNNNN}` is present in the response and
               was never present in the request. Step 2 returns the same POSTED row: the entry is
               fully locked, no edit and no delete mapping exists for it
Test data    : `JV-2026-000123` is the format example the SRS itself gives for fiscal year `2026`
<!-- TC:TC-FIN-017:END -->

<!-- TC:TC-FIN-018:START traces=AC-FIN-018,REQ-FIN-018,API-FIN-019 -->
### TC-FIN-018 — an entry out of balance by the smallest currency unit is refused
Derived from : AC-FIN-018  (REQ-FIN-018)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-006 → FIN-409-UNBALANCED (409)
Scenario     : VIOLATION · data class BOUNDARY · language ALL
Preconditions: an Open period; every line on a leaf, active account, so the imbalance is the only
               failing check
Steps        : 1. POST `/api/v1/fin/journal-entries` with debit lines totalling 100.00 and credit
                  lines totalling 99.99
Expected     : 409 with code FIN-409-UNBALANCED —
               ar: «القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن» ·
               en: "The entry is unbalanced — total debits do not equal total credits".
               The imbalance amount is shown, which is AC-FIN-018's own Then. Nothing is posted
               and nothing is stored
Test data    : 100.00 and 99.99 — AC-FIN-018's own values, one hundredth apart, which is the
               smallest currency unit RULE-FIN-006 measures to
<!-- TC:TC-FIN-018:END -->

<!-- TC:TC-FIN-019:START traces=AC-FIN-019,REQ-FIN-019,API-FIN-019 -->
### TC-FIN-019 — a line targeting a rollup account is refused, naming the line
Derived from : AC-FIN-019  (REQ-FIN-019)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an Open period; a balanced line set in which exactly one line targets a non-leaf
               (rollup) account — `isLeafFl` false (DBF-FIN-008) — every other line being leaf and
               active
Steps        : 1. POST `/api/v1/fin/journal-entries` with that entry
Expected     : 409 with code FIN-409-NOT-POSTABLE-ACCOUNT —
               ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en: "The target account does not accept direct posting".
               The offending line is named, which is AC-FIN-019's own Then. Nothing is posted.
               The published error body of API-FIN-019 carries the code in
               `fieldErrors[].field` with no line index and no account id, so this half is
               expected to fail against the build until the backend publishes the line — the
               follow-up ADR-FIN-065 records (ADR-FIN-066)
Test data    : account ids are placeholders; the rollup relation is the fact the AC states
<!-- TC:TC-FIN-019:END -->

<!-- TC:TC-FIN-020:START traces=AC-FIN-020,REQ-FIN-020,API-FIN-019 -->
### TC-FIN-020 — the period gate is read at post time, not at build time
Derived from : AC-FIN-020  (REQ-FIN-020)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries, with API-FIN-026 in between
Rule / code  : RULE-FIN-008 → FIN-409-PERIOD-NOT-OPEN (409)
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: a period that is OPEN when the entry is composed; an approver holding
               `PERM_FIN_PERIODS_CLOSE_APPROVE`, so the period can be moved to HARD_CLOSE between
               composition and submission (OPEN → SOFT_CLOSE → HARD_CLOSE, SRS A7)
Steps        : 1. compose a balanced entry against that period while it is still Open
               2. soft-close then hard-close the period (API-FIN-025, API-FIN-026)
               3. POST `/api/v1/fin/journal-entries` with the entry composed in step 1
Expected     : 409 with code FIN-409-PERIOD-NOT-OPEN —
               ar: «الفترة المستهدفة غير مفتوحة» ·
               en: "The target period is not open".
               The rejection cites the period's **current** state, which is AC-FIN-020's own Then,
               and holds even though the period was Open when the entry was built
Test data    : none invented; the state sequence is the SRS A7 lifecycle
<!-- TC:TC-FIN-020:END -->

<!-- TC:TC-FIN-021:START traces=AC-FIN-021,REQ-FIN-021,API-FIN-019 -->
### TC-FIN-021 — a line citing an inactive dimension value is refused
Derived from : AC-FIN-021  (REQ-FIN-021)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-009 → FIN-409-INVALID-DIMENSION (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an Open period and a balanced, leaf-account line set; one DimensionValue
               (ENT-FIN-003) whose `isActiveFl` (DBF-FIN-029) is false. That state is reachable
               only through API-FIN-035, the sole endpoint that clears the flag — the setup
               dependency ADR-FIN-009 records
Steps        : 1. PUT `/api/v1/fin/dimensions/values/{id}/deactivate`
               2. POST `/api/v1/fin/journal-entries` with a line citing that dimension value
Expected     : step 1 → 200 with `isActiveFl` = false.
               step 2 → 409 with code FIN-409-INVALID-DIMENSION —
               ar: «قيمة البُعد غير صالحة» · en: "The dimension value is invalid".
               The offending line and dimension are named, which is AC-FIN-021's own Then. The
               published error body names neither, so this half is expected to fail against the
               build until the backend publishes them — the follow-up ADR-FIN-065 records
               (ADR-FIN-066)
Test data    : a value under `PROJECT`; ids are placeholders
<!-- TC:TC-FIN-021:END -->

<!-- TC:TC-FIN-026:START traces=AC-FIN-026,REQ-FIN-026,API-FIN-017 -->
### TC-FIN-026 — an allocation run distributes the source balance to the last unit
Derived from : AC-FIN-026  (REQ-FIN-026)
Exercises    : API-FIN-017 POST /api/v1/fin/allocation-rules/{id}/run
Rule / code  : RULE-FIN-010 (reused for allocations) → success path, no code raised ·
               RULE-FIN-003 governs the target set
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an active AllocationRule (ENT-FIN-013) with two `PERCENTAGE` targets and one
               `REMAINDER` target, exactly one target carrying `isRemainderFl` = true
               (DBF-FIN-146) in agreement with its own `distributionTypeCode`; the source
               account's current balance is 1,000.00; an Open period; an accountant holding
               `PERM_FIN_ALLOCATION_RULES_UPDATE`
Steps        : 1. POST `/api/v1/fin/allocation-rules/{id}/run`
Expected     : 201 · one `JournalEntryResponse` with `journalTypeCode` = ALLOCATION and
               `statusCode` = POSTED, whose target lines sum **exactly** to 1,000.00, the
               remainder target absorbing the rounding difference. The entry balances, so
               RULE-FIN-006 does not fire
Test data    : 1,000.00 and the two-percentage-plus-one-remainder shape — AC-FIN-026's own
<!-- TC:TC-FIN-026:END -->

<!-- TC:TC-FIN-029:START traces=AC-FIN-029,REQ-FIN-029,API-FIN-021 -->
### TC-FIN-029 — a reversal of an entry in a closed period posts into the current open period
Derived from : AC-FIN-029  (REQ-FIN-029)
Exercises    : API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse
Rule / code  : RULE-FIN-012 → success path, no distinct code (the period substitution is the
               rule's effect, not a refusal)
Scenario     : STATE · data class EDGE · language ALL
Preconditions: a POSTED entry whose own period is now HARD_CLOSE; a different period that is
               currently OPEN; a caller holding `PERM_FIN_JOURNAL_ENTRIES_REVERSE`
Steps        : 1. POST `/api/v1/fin/journal-entries/{id}/reverse`
Expected     : 201 · the reversal entry's `periodId` (DBF-FIN-038) is the **current open**
               period, not the original's closed one — AC-FIN-029's own Then. The mirrored lines
               carry the same amounts with opposite `directionCode`, and both rows are linked
               through `originalEntryId` / `reversalEntryId`. FIN-409-PERIOD-NOT-OPEN is **not**
               raised, because the reversal does not target the closed period
Test data    : none invented
<!-- TC:TC-FIN-029:END -->

<!-- TC:TC-FIN-030:START traces=AC-FIN-030,REQ-FIN-030,API-FIN-021 -->
### TC-FIN-030 — an entry that already carries a reversal cannot be reversed again
Derived from : AC-FIN-030  (REQ-FIN-030)
Exercises    : API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse
Rule / code  : RULE-FIN-013 → FIN-409-ALREADY-REVERSED (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an entry reversed once already. Per SRS A7 it stays POSTED — classic reversal
               writes no VOID anywhere — and carries a `reversalEntryId` (DBF-FIN-043) link to
               its mirror
Steps        : 1. POST `/api/v1/fin/journal-entries/{id}/reverse` on the same original a second
                  time
Expected     : 409 with code FIN-409-ALREADY-REVERSED —
               ar: «تم عكس هذا القيد بالفعل ولا يمكن عكسه مرة أخرى» ·
               en: "This entry has already been reversed and cannot be reversed again".
               No second mirror is posted, so the pair's net ledger effect stays zero rather than
               becoming −(original)
Test data    : none invented
<!-- TC:TC-FIN-030:END -->

<!-- TC:TC-FIN-032:START traces=AC-FIN-032,REQ-FIN-032,API-FIN-024 -->
### TC-FIN-032 — a soft-closed period reopens
Derived from : AC-FIN-032  (REQ-FIN-032)
Exercises    : API-FIN-024 PATCH /api/v1/fin/fiscal-periods/{id}/open
Rule / code  : RULE-FIN-014 → not triggered here (the period is not Hard Closed)
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period whose `statusCode` (DBF-FIN-082) is SOFT_CLOSE — the one state §10.2
               calls re-openable; an administrator holding `PERM_FIN_PERIODS_UPDATE`
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/open`
Expected     : 200 · `FiscalPeriodResponse` with `statusCode` = OPEN, the SOFT_CLOSE → OPEN
               transition of SRS A7. Normal posting into it succeeds afterwards, so RULE-FIN-008
               no longer refuses it
Test data    : the `PERIOD_STATE` values OPEN and SOFT_CLOSE — seeded lookup values (SRS A6)
<!-- TC:TC-FIN-032:END -->

<!-- TC:TC-FIN-033:START traces=AC-FIN-033,REQ-FIN-033,API-FIN-025 -->
### TC-FIN-033 — soft-closing a period blocks normal posting into it
Derived from : AC-FIN-033  (REQ-FIN-033)
Exercises    : API-FIN-025 PATCH /api/v1/fin/fiscal-periods/{id}/soft-close
Rule / code  : RULE-FIN-008 → FIN-409-PERIOD-NOT-OPEN (409), on the posting attempt that follows
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period currently OPEN; an administrator holding `PERM_FIN_PERIODS_UPDATE`
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/soft-close`
               2. POST a balanced manual entry against that period (API-FIN-019)
Expected     : step 1 → 200 with `statusCode` = SOFT_CLOSE (OPEN → SOFT_CLOSE, SRS A7).
               step 2 → 409 with code FIN-409-PERIOD-NOT-OPEN —
               ar: «الفترة المستهدفة غير مفتوحة» · en: "The target period is not open" —
               which is AC-FIN-033's own "REQ-FIN-020 rejects normal postings thereafter"
Test data    : none invented
<!-- TC:TC-FIN-033:END -->

<!-- TC:TC-FIN-034:START traces=AC-FIN-034,REQ-FIN-034,API-FIN-026 -->
### TC-FIN-034 — hard-closing a period is permanent
Derived from : AC-FIN-034  (REQ-FIN-034)
Exercises    : API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close
Rule / code  : RULE-FIN-015 (the distinct permission gates the call) · RULE-FIN-014 (the
               terminal state it creates)
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period whose `statusCode` is SOFT_CLOSE; an authorized approver holding
               `PERM_FIN_PERIODS_CLOSE_APPROVE` together with the `PERM_FIN_PERIODS_VIEW` gateway
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/hard-close`
Expected     : 200 · `statusCode` = HARD_CLOSE, with `closedBy` (DBF-FIN-083) and `closedAt`
               (DBF-FIN-084) set. The state is terminal: TC-FIN-035 shows the reopen refusal, and
               posting into it is refused by RULE-FIN-008
Test data    : none invented
<!-- TC:TC-FIN-034:END -->

<!-- TC:TC-FIN-035:START traces=AC-FIN-035,REQ-FIN-035,API-FIN-024 -->
### TC-FIN-035 — a hard-closed period never reopens
Derived from : AC-FIN-035  (REQ-FIN-035)
Exercises    : API-FIN-024 PATCH /api/v1/fin/fiscal-periods/{id}/open
Rule / code  : RULE-FIN-014 → FIN-409-NOT-REOPENABLE (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a period whose `statusCode` is HARD_CLOSE; an administrator holding
               `PERM_FIN_PERIODS_UPDATE`
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/open`
Expected     : 409 with code FIN-409-NOT-REOPENABLE —
               ar: «الفترة مغلقة إغلاقًا صارمًا ولا يمكن إعادة فتحها» ·
               en: "The period is hard-closed and cannot be reopened".
               The stored `statusCode` is unchanged
Test data    : none invented
<!-- TC:TC-FIN-035:END -->

<!-- TC:TC-FIN-047:START traces=AC-FIN-009,REQ-FIN-009,API-FIN-011 -->
### TC-FIN-047 — two remainder lines are refused as surely as none
Derived from : AC-FIN-009  (REQ-FIN-009) — the "exactly one" limit its RULE states
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-003 → FIN-409-REMAINDER-COUNT (409)
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an EventTypeRule whose line set already carries one `PERCENTAGE` line and one line
               marked `isRemainderFl` = true; a caller holding `PERM_FIN_RULES_UPDATE`
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with a **second** line marked
                  `isRemainderFl` = true
Expected     : 409 with code FIN-409-REMAINDER-COUNT —
               ar: «يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي» ·
               en: "Exactly one remainder line is required when any percentage distribution is
               present".
               This is the upper side of the same numeric limit TC-FIN-009 tests from below:
               RULE-FIN-003 requires exactly one, so zero and two fail alike. Which line is the
               remainder decides every other line's amount under RULE-FIN-010, so an ambiguous set
               must never be stored
Test data    : the line counts come from RULE-FIN-003's own "exactly one"
<!-- TC:TC-FIN-047:END -->

<!-- TC:TC-FIN-048:START traces=AC-FIN-012,REQ-FIN-012,API-FIN-020 -->
### TC-FIN-048 — a remainder that computes to zero or less is refused
Derived from : AC-FIN-012  (REQ-FIN-012) — the positivity limit its RULE states
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-010 → FIN-422-REMAINDER-NOT-POSITIVE (422)
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an active EventTypeRule whose non-remainder lines on the remainder line's own side
               already equal or exceed the total carried by the opposing side, leaving no residue
               for the remainder line to absorb; exactly one line marked `isRemainderFl`
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with an event of that type
Expected     : 422 with code FIN-422-REMAINDER-NOT-POSITIVE —
               ar: «سطر الباقي يُحسب كفرق ويجب أن يكون موجبًا؛ السطور الأخرى تستهلك المبلغ بالكامل» ·
               en: "The remainder line is computed as a difference and must be positive; the
               other lines already consume the full amount".
               Nothing is posted. The refusal happens in the rule engine, before
               `CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE` could ever be reached — a negative amount is
               never offered to the database
Test data    : no figures are invented; the shape ("other lines already equal or exceed the
               opposing side") is RULE-FIN-010's own wording
<!-- TC:TC-FIN-048:END -->

<!-- TC:TC-FIN-049:START traces=AC-FIN-014,REQ-FIN-014,API-FIN-019 -->
### TC-FIN-049 — a manual entry with no lines never reaches the rules
Derived from : AC-FIN-014  (REQ-FIN-014) — the "at least two balanced lines" limit it states
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : — (no FIN catalog code: the refusal is the platform validation envelope, before
               any RULE-FIN-* is evaluated)
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an accountant holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`; a coherent
               fiscalYearId / periodId / docDate triple, so nothing else can fail first
Steps        : 1. POST `/api/v1/fin/journal-entries` with a well-formed header and an **empty**
                  `lines` array
Expected     : the request is refused at the DTO boundary — `JournalEntryCreateRequest.lines`
               carries `@NotEmpty` — and answers the platform `VALIDATION_ERROR` envelope in the
               caller's language, not a FIN code. No entry row is written and no `docNo` is drawn
               from the fiscal-year series. This is the boundary the backend plan names when it
               says the lineless entry shape "exists solely on the internal year-end path"
               (API-FIN-027) and cannot be reached through this DTO
Test data    : the empty line set is the boundary of AC-FIN-014's "at least two"
<!-- TC:TC-FIN-049:END -->
<!-- SUB:RULE-SCENARIOS:END -->
