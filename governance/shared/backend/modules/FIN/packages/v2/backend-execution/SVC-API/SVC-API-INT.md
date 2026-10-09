<!-- source: PHASE:SVC-API / SUB:SVC-API-INT -->
<!-- context: SVC-API-HEADER.md — phase-level preamble -->
<!-- traces: DBF-FIN-034, DBF-FIN-041, DBF-FIN-042, DBF-FIN-043, DBF-FIN-063, DBF-FIN-064, DBF-FIN-066, DBF-FIN-067, DBF-FIN-068, DBF-FIN-069, DBF-FIN-077, DBF-FIN-082, DBF-FIN-083, DBF-FIN-084, DBF-FIN-090, DBF-FIN-102, DBF-FIN-112, DBF-FIN-115, DBF-FIN-133, DBF-FIN-145, DBF-FIN-146, DBF-FIN-148, DBF-FIN-153, DBF-FIN-160, DBF-FIN-161, DBF-FIN-164, REQ-FIN-010, REQ-FIN-011, REQ-FIN-012, REQ-FIN-013, REQ-FIN-017, REQ-FIN-018, REQ-FIN-019, REQ-FIN-020, REQ-FIN-021, REQ-FIN-023, REQ-FIN-024, REQ-FIN-026, REQ-FIN-028, REQ-FIN-029, REQ-FIN-030, REQ-FIN-031, REQ-FIN-032, REQ-FIN-033, REQ-FIN-034, REQ-FIN-035, REQ-FIN-036, REQ-FIN-037, REQ-FIN-038, REQ-FIN-047, REQ-FIN-050, REQ-FIN-051, REQ-FIN-052, REQ-FIN-053, REQ-FIN-063, REQ-FIN-066, REQ-FIN-067, REQ-FIN-068, REQ-FIN-069, REQ-FIN-070, REQ-FIN-072, REQ-FIN-073, REQ-FIN-074, REQ-FIN-075, REQ-FIN-076, REQ-FIN-077 -->
<!-- SUB:SVC-API-INT:START traces=REQ-FIN-010,REQ-FIN-023,REQ-FIN-024,REQ-FIN-026,REQ-FIN-028,REQ-FIN-031,REQ-FIN-034,REQ-FIN-036,REQ-FIN-037,REQ-FIN-050,REQ-FIN-063,REQ-FIN-072,REQ-FIN-073 -->
### SUB — SVC-API-INT (posting-pipeline orchestration and period-control actions)

<!-- API:API-FIN-020:START traces=REQ-FIN-010,REQ-FIN-011,REQ-FIN-012,REQ-FIN-013,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-047,REQ-FIN-050,REQ-FIN-051,REQ-FIN-052,REQ-FIN-053,REQ-FIN-063,REQ-FIN-066,REQ-FIN-067,REQ-FIN-068,REQ-FIN-069,REQ-FIN-070,REQ-FIN-072,REQ-FIN-073,REQ-FIN-074,REQ-FIN-075,REQ-FIN-076,DBF-FIN-041,DBF-FIN-090,DBF-FIN-102,DBF-FIN-148,DBF-FIN-153,DBF-FIN-160,DBF-FIN-161,DBF-FIN-164,DBF-FIN-063,DBF-FIN-064 -->
### API-FIN-020 — build event entry (system; MODIFIED v2 — mapped accounts, event dimensions, distinct rejections)
Entity       : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 · operation CREATE (build and post); reads ENT-FIN-009, ENT-FIN-010, ENT-FIN-015, ENT-FIN-016, ENT-FIN-003, ENT-FIN-008
Endpoint     : POST /api/v1/fin/journal-entries/from-event   verb: POST — a system call with no screen affordance (SRS SCR-REQ-FIN-006)
Layers       : `JournalEntryController.buildFromEvent` → `EventEntryService.build` → `JournalPostingService.buildValidateAndPost`
Request      : body `EventEntryBuildRequest` (as published in api-docs-fin.md) — `eventReference` → DBF-FIN-041 (required, ≤ 100) · `eventTypeCode` (required; selects the active rule by DBF-FIN-090) · `docDate` → DBF-FIN-036 (required; selects the period by DBF-FIN-080/081) · `baseAmount` and `amounts{}` (the amount sources the rule lines name) · `fields{}` — the event's business facts, keyed by FIN_EVENT_BUSINESS_FIELD code (PAYMENT_METHOD, EXPENSE_TYPE_CODE, ORGANISATION_CODE, BRANCH_CODE), string values (ADR-FIN-031) · `descriptionAr` → DBF-FIN-044 · `descriptionEn` → DBF-FIN-045. No payload value is ever read as an account code: an extra `fields.accountCode` is simply never looked at (REQ-FIN-047, AC-FIN-047)
Response     : 200 · `ApiResponse<JournalEntryResponse>` (statusCode POSTED, lines with their dimensions) — as published; a rejection writes no row (SRS A7)
Validations  : in build order; the first failure rejects the whole event and nothing is written
- RULE-FIN-004 (MODIFIED v2) — trigger: on create (event-sourced entry), by the pre-check before building and again when the store's uniqueness guarantee on eventReference refuses a concurrent second write. Statement: reject building an entry for an event reference that has already produced a posted entry, and answer with this rule's duplicate-event error whichever detection catches the repeat; a uniqueness refusal from the store is never surfaced as a generic data-integrity or server error. Message: ar "تم بالفعل ترحيل قيد لهذا المرجع" · en "An entry for this event reference has already been posted" → FIN-409-DUPLICATE-EVENT (pre-check QR-FIN-026; the store's refusal — see Concurrency)
- RULE-FIN-005 (v1, SRS §A5) — no active rule for the event type → FIN-404-NO-ACTIVE-RULE (QR-FIN-027)
- RULE-FIN-028 — trigger: on build (event-sourced entry). Statement: reject the event with the no-defined-period configuration error when no accounting period's start and end dates cover the event's date, and never answer this case with the period-not-open error. Message: ar "لا توجد فترة محاسبية معرَّفة تغطي تاريخ الحدث" · en "No accounting period is defined for the event date" → FIN-422-NO-DEFINED-PERIOD (QR-FIN-059 finds no covering period; AC-FIN-080)
- RULE-FIN-003 (v1, SRS §A5) — single remainder marker over the stored line set (QR-FIN-016) → FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER
- RULE-FIN-021 — trigger: on build (event-sourced entry, per MAPPING rule line and per BUSINESS_FIELD dimension tag). Statement: reject the event with the missing-business-field error when the event does not carry the business field named by a MAPPING rule line or by a BUSINESS_FIELD dimension tag. Message: ar "الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة" · en "The event does not carry the business field the rule requires" → FIN-422-MISSING-BUSINESS-FIELD (the named key is absent from `fields{}` or blank; AC-FIN-053, AC-FIN-073)
- RULE-FIN-020 — trigger: on build (event-sourced entry, per MAPPING rule line). Statement: reject the event with the dedicated unmapped-value error when no active account mapping exists for the rule's event type, the rule line's business field and the event's value for that field; never substitute a default account. Message: ar "لا يوجد ربط حساب نشط للقيمة التجارية في الحدث" · en "No active account mapping exists for the business value in the event" → FIN-422-UNMAPPED-VALUE (QR-FIN-055 finds no active row; an inactive mapping counts as none — AC-FIN-056, AC-FIN-057)
- RULE-FIN-027 — trigger: on build (event-sourced entry, per BUSINESS_FIELD dimension tag). Statement: reject the event with the unresolvable-dimension-value error when the event's value for a tag's business field equals the code of no dimension value of the tagged dimension. Message: ar "قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد" · en "The dimension value in the event matches no defined value of this dimension" → FIN-422-UNRESOLVED-DIMENSION-VALUE (QR-FIN-058, exact code within the dimension — ADR-FIN-026; AC-FIN-074). A match that is inactive is RULE-FIN-009's rejection, below
- RULE-FIN-010 (v1, SRS §A5) — the remainder line, computed last per posting side, must come out positive → FIN-422-REMAINDER-NOT-POSITIVE (QR-FIN-028); a MAPPING remainder line computes exactly like any other (AC-FIN-082)
- RULE-FIN-006, RULE-FIN-007, RULE-FIN-008, RULE-FIN-009 (v1, SRS §A5 — applied unchanged to the new path, REQ-FIN-076): debits equal credits (QR-FIN-029) → FIN-409-UNBALANCED; every line's account — constant or mapped — is a leaf, active and postable (QR-FIN-030) → FIN-409-NOT-POSTABLE-ACCOUNT, ar "الحساب المستهدف لا يقبل ترحيلاً مباشرًا" · en "The target account does not accept direct posting" (REQ-FIN-052, AC-FIN-054, AC-FIN-055); the covering period is Open (QR-FIN-031) → FIN-409-PERIOD-NOT-OPEN, ar "الفترة المستهدفة غير مفتوحة" · en "The target period is not open" — the retryable rejection (REQ-FIN-073, AC-FIN-079); every dimension value — the tag's constant one or the one resolved from the event — belongs to its dimension and is active (QR-FIN-032) → FIN-409-INVALID-DIMENSION, ar "قيمة البُعد غير صالحة" · en "The dimension value is invalid" (REQ-FIN-066, REQ-FIN-067, AC-FIN-072)
Errors       : FIN-409-DUPLICATE-EVENT, FIN-404-NO-ACTIVE-RULE, FIN-422-NO-DEFINED-PERIOD, FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER, FIN-422-MISSING-BUSINESS-FIELD, FIN-422-UNMAPPED-VALUE, FIN-422-UNRESOLVED-DIMENSION-VALUE, FIN-404-ACCOUNT (a CONSTANT line's account code resolves to no account), FIN-422-REMAINDER-NOT-POSITIVE, FIN-409-UNBALANCED, FIN-409-NOT-POSTABLE-ACCOUNT, FIN-409-PERIOD-NOT-OPEN, FIN-409-INVALID-DIMENSION, FIN-404-YEAR, FIN-403-FORBIDDEN, FIN-500. Retired: FIN-422-MAPPING-UNSUPPORTED — MAPPING is implemented (ADR-FIN-027). No longer raised here: FIN-404-PERIOD for a date no period covers, which is FIN-422-NO-DEFINED-PERIOD now (ADR-FIN-030)
Orchestration: (sets DBF-FIN-035 docNo · DBF-FIN-039 journalTypeCode=EVENT_GENERATED · DBF-FIN-040 statusCode DRAFT→POSTED · DBF-FIN-046 postedAt · DBF-FIN-037 fiscalYearId and DBF-FIN-038 periodId from the covering period · per line DBF-FIN-052, DBF-FIN-053, DBF-FIN-054 accountId (constant or mapped), DBF-FIN-055, DBF-FIN-056, DBF-FIN-057 · per resolved tag one JournalLineDimension row DBF-FIN-062, DBF-FIN-063, DBF-FIN-064 — the shape a manual line writes, REQ-FIN-070)
  1. pre-check RULE-FIN-004 (QR-FIN-026) → 2. resolve the active rule (QR-FIN-027) → 3. resolve the period covering docDate (QR-FIN-059); none → `FiscalPeriodDomain.assertDateCovered` answers RULE-FIN-028. This event path resolves its period itself, so the shared pipeline's FIN-404-PERIOD stays the answer only for API-FIN-014 and API-FIN-017 → 4. load the rule's lines (QR-FIN-025, read half) and their tags (QR-FIN-057); re-check RULE-FIN-003 (QR-FIN-016)
  5. per line, the account: CONSTANT → the account whose code is the line's accountDerivationValue; MAPPING → the value `fields[accountBusinessFieldCode]` (`EventTypeRuleDomain.assertFieldPresent`, RULE-FIN-021) → the active mapping for (the rule's eventTypeCode, the line's business field, that value) (QR-FIN-055, `AccountMappingDomain.assertResolved`, RULE-FIN-020) → its accountId
  6. per line, the amount from its amount source; the remainder line last, per side (QR-FIN-028) → 7. per tag, the dimension value: CONSTANT → the tag's dimensionValueId; BUSINESS_FIELD → the value `fields[businessFieldCode]` (RULE-FIN-021) → the DimensionValue with that code in the tagged dimension (QR-FIN-058, `DimensionValueDomain.assertResolvedByCode`, RULE-FIN-027)
  8. validate RULE-FIN-006 / 007 / 008 / 009 (QR-FIN-029 … QR-FIN-032) → 9. allocate docNo under the fiscal-year row lock (as API-FIN-019) → 10. persist header, lines and line dimensions with the header flushed (QR-FIN-025) → post (QR-FIN-033) → return
Repository   : QR-FIN-026, QR-FIN-027, QR-FIN-059, QR-FIN-025, QR-FIN-057, QR-FIN-016, QR-FIN-055, QR-FIN-028, QR-FIN-058, QR-FIN-029, QR-FIN-030, QR-FIN-031, QR-FIN-032, QR-FIN-033 · join NONE · READ_WRITE (one transaction, build through post; a rejection rolls back every row)
Concurrency  : two simultaneous events with one reference (AC-FIN-078) can both pass the QR-FIN-026 pre-check. UQ_FIN_JOURNAL_ENTRY_EVENT_REF is the guarantee: the header insert is flushed inside the transaction, the store refuses the second, and FinPersistenceRefusalAdvisor (CORE, ADR-FIN-030) translates a refusal naming that constraint into FIN-409-DUPLICATE-EVENT with RULE-FIN-004's message. Exactly one entry posts and the other caller gets the duplicate error, never FIN-500 (REQ-FIN-072). The docNo series stays guarded by the fiscal-year row lock
Security     : system-to-system call under the event consumer's own service principal, gated like any authenticated caller · `PERM_FIN_JOURNAL_ENTRIES_CREATE`. Machine-caller authentication is a separate SEC change set (CS-FIN-001, out of scope)
Localization : every rejection carries its ar + en message; descriptionAr/descriptionEn are stored as sent
<!-- API:API-FIN-020:END -->

<!-- API:API-FIN-014:START traces=REQ-FIN-023,REQ-FIN-024,DBF-FIN-115,DBF-FIN-112 -->
### API-FIN-014 — run template
Entity       : ENT-FIN-011, ENT-FIN-004, ENT-FIN-005 · operation UPDATE (run — posts a RECURRING entry)
Endpoint: POST /api/v1/fin/recurring-templates/{id}/run · Layers: `RecurringTemplateController.run`→`RecurringTemplateService.run`
Request: path `id` (or invoked by an internal scheduler with no path — the endpoint is the
same either way) · Response: 201 · `JournalEntryResponse`
Validations: the template must still be ACTIVE — `RecurringTemplateDomain.assertCanRun()`
(the NEW Domain companion for ENT-FIN-011) runs FIRST, immediately after the row is loaded and
before the period is resolved, and refuses a deactivated template with `FIN-409-NOT-ACTIVE`;
**no RULE-FIN-* states this gate — it rests on a recorded human decision of 2026-09-12, not on
stated requirement.** Then the same RULE-FIN-006/007/008/009 checks as API-FIN-019 (the
template's own lines were already balance-checked at API-FIN-013 create time, but re-validated
here since accounts/periods may have changed since)
Errors: same as API-FIN-019, plus `FIN-404-TEMPLATE` (unknown recurring template id) and
`FIN-409-NOT-ACTIVE` (the template exists but is deactivated)
Orchestration: (sets DBF-FIN-115 nextRunDate, advanced per frequency; the posted entry's DBF-FIN-035 docNo, DBF-FIN-039 journalTypeCode=RECURRING and DBF-FIN-040 statusCode as in API-FIN-019) load template (QR-FIN-019) → assert it is active → build entry from its lines (journalTypeCode=
RECURRING) → validate + post (QR-FIN-029..033) → advance nextRunDate per frequencyCode →
if scheduleTypeCode=REVERSING: also build and post the linked reversal in the next period
(REQ-FIN-024, reusing RULE-FIN-011/012 via QR-FIN-034/036) → return
Repository: QR-FIN-019, QR-FIN-024(-style build), QR-FIN-029..033, QR-FIN-034, QR-FIN-036
· join NONE · READ_WRITE
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_UPDATE` (running
a template is modeled as an update-class custom action) · Localization: n/a
Concurrency  : the template row is loaded PESSIMISTIC_WRITE before nextRunDate is read, so two concurrent runs of one template serialize instead of both reading the same predecessor date and both posting (ADR-FIN-041); the posted entry's docNo is separately allocated under the fiscal-year row lock as in API-FIN-019
<!-- API:API-FIN-014:END -->

<!-- API:API-FIN-017:START traces=REQ-FIN-026,DBF-FIN-133,DBF-FIN-145,DBF-FIN-146 -->
### API-FIN-017 — run allocation rule
Entity       : ENT-FIN-013, ENT-FIN-004, ENT-FIN-005 · operation UPDATE (run — posts an ALLOCATION entry)
Endpoint: POST /api/v1/fin/allocation-rules/{id}/run · Layers: `AllocationRuleController.run`→`AllocationRuleService.run`
Request: path `id` · Response: 201 · `JournalEntryResponse`
Validations: the rule must still be ACTIVE — `AllocationRuleDomain.assertCanRun()` runs FIRST,
immediately after the row is loaded and before the target set is even fetched, and refuses a
deactivated rule with `FIN-409-NOT-ACTIVE`; **no RULE-FIN-* states this gate — it rests on a
recorded human decision of 2026-09-12, not on stated requirement.** Then RULE-FIN-003 (single
remainder marker over the stored targets, QR-FIN-016) and RULE-FIN-010 (per-side remainder
guarantee, QR-FIN-028, reused) then RULE-FIN-006/007/008/009
Errors: same family as API-FIN-019, plus `FIN-404-ALLOCATION-RULE`, `FIN-409-NOT-ACTIVE`,
`FIN-409-REMAINDER-COUNT`, `FIN-422-REMAINDER-MARKER`, `FIN-422-REMAINDER-NOT-POSITIVE`
Orchestration: (sets the posted entry's DBF-FIN-035 docNo, DBF-FIN-039 journalTypeCode=ALLOCATION and DBF-FIN-040 statusCode as in API-FIN-019) load rule → assert it is active → load targets + current source-account balance (QR-FIN-022) →
distribute per target's distributionTypeCode, the target marked `isRemainderFl`
(DBF-FIN-146, the single marker — not `distributionTypeCode`) absorbing the rounding
difference between the source line's side and the targets' own side (QR-FIN-028) → build
entry (journalTypeCode=ALLOCATION) → validate + post (QR-FIN-029..033) → return
Repository: QR-FIN-022, QR-FIN-028, QR-FIN-029..033 · join NONE · READ_WRITE
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_UPDATE` · Localization: n/a
Concurrency  : the posted entry's docNo is allocated under the same fiscal-year row lock as API-FIN-019 (JournalPostingService); nothing else is allocated. The source balance this run reads (QR-FIN-022, QR-FIN-043) is read without a lock and the entry is posted for that amount, so two concurrent runs of one rule both read the same balance and both post — a source of 1,000.00 can end at −1,000.00. v1 as built; stated here, not changed by v2 (ADR-FIN-042)
<!-- API:API-FIN-017:END -->

<!-- API:API-FIN-021:START traces=REQ-FIN-028,REQ-FIN-029,REQ-FIN-030,DBF-FIN-042,DBF-FIN-043,REQ-FIN-077 -->
### API-FIN-021 — reverse entry
Entity       : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 · operation CREATE (reverse — posts the mirror entry; custom action)
Endpoint: POST /api/v1/fin/journal-entries/{id}/reverse · Layers: `JournalEntryController.reverse`→`JournalEntryService.reverse`
Request: path `id` · Response: 201 · `JournalEntryResponse` (the new reversal entry)
Validations: RULE-FIN-013 (must be POSTED and not already reversed, QR-FIN-035); RULE-FIN-012
(period substitution if original's is closed, QR-FIN-036)
Errors: `FIN-409-NOT-POSTED`, `FIN-409-ALREADY-REVERSED`, `FIN-404-ENTRY`
Orchestration: (sets DBF-FIN-042 originalEntryId on the reversal · DBF-FIN-043 reversalEntryId on the original · the reversal's DBF-FIN-039 journalTypeCode=REVERSAL) load original UNDER A ROW LOCK (`SELECT ... FOR UPDATE` on the
FIN_JOURNAL_ENTRY header — no FIN entity carries `@Version`, so without it two concurrent
reversals both read reversalEntryId as null, both post a mirror, and the second save
overwrites the first link, leaving one orphaned mirror and a net effect of −(original)) →
check RULE-FIN-013 (QR-FIN-035) → resolve posting period
(original's if Open, else the current open period per RULE-FIN-012, QR-FIN-036) → build
mirrored lines with opposite directions, same amounts (RULE-FIN-011, QR-FIN-034) →
validate + post (QR-FIN-029..033, journalTypeCode=REVERSAL) → set originalEntryId/
reversalEntryId on both rows (bidirectional link) → return. Classic reversal: the original
STAYS POSTED — the only write back to it is the reversalEntryId link (DBF-FIN-043); its
statusCode is never modified, so both entries are seen by the POSTED-only report queries and
the pair nets to zero (SRS A7). Because the original stays POSTED, the double-reversal half of
RULE-FIN-013 is an explicit guard on the reversal link: a second reverse on the same entry is
rejected with `FIN-409-ALREADY-REVERSED` (AC-FIN-030), which would otherwise post a second
mirror and leave a net effect of −(original).
Repository: QR-FIN-034, QR-FIN-035, QR-FIN-036, QR-FIN-029..033 · join NONE · READ_WRITE
Security: screen FIN_JOURNAL_ENTRIES · `PERM_FIN_JOURNAL_ENTRIES_REVERSE` (custom action) · Localization: n/a
Concurrency  : the already-reversed guard is a read-then-write, made atomic by the PESSIMISTIC_WRITE load of the original's header in the Orchestration line; the mirror's docNo is allocated under the fiscal-year row lock
v2: an entry posted through an account mapping or dimension tags reverses exactly like any other. The mirror copies each line's stored accountId and its JournalLineDimension rows with the opposite direction, the original stays POSTED, and every account and dimension balance nets to zero (REQ-FIN-077, AC-FIN-083). No mapping is consulted: a reversal reads the posted lines, never the rules or mappings that built them.
<!-- API:API-FIN-021:END -->

<!-- API:API-FIN-023:START traces=REQ-FIN-031,DBF-FIN-066,DBF-FIN-067,DBF-FIN-068,DBF-FIN-077 -->
### API-FIN-023 — create fiscal year
Entity       : ENT-FIN-007, ENT-FIN-008 · operation CREATE
Endpoint: POST /api/v1/fin/fiscal-years · Layers: `FiscalYearController.create`→`FiscalYearService.create`
Request: (writes DBF-FIN-066 code · DBF-FIN-067 startDate · DBF-FIN-068 endDate) `{code, startDate, endDate, periodCount}` · Response: 201 · `FiscalYearResponse` with its generated periods
Validations: uniqueness of code (QR-FIN-047)
Errors: `FIN-409-YEAR-DUP`
Orchestration: (sets DBF-FIN-069 statusCode=OPEN · DBF-FIN-070 isActiveFl=TRUE; per generated period DBF-FIN-076 fiscalYearId · DBF-FIN-077 periodNo · DBF-FIN-078 nameAr · DBF-FIN-079 nameEn · DBF-FIN-080 startDate · DBF-FIN-081 endDate · DBF-FIN-082 statusCode=OPEN) validate uniqueness → persist year (statusCode=OPEN) + generate periodCount periods,
each statusCode=OPEN (QR-FIN-038) → return
Period spans and names (governed, not implicit): when `periodCount` = 12 and
[startDate, endDate] spans one whole calendar year, each generated period IS a calendar
month — period N runs from the 1st to the last day of the Nth month of the span — and its
NAME_AR / NAME_EN (DBF-FIN-078/079, both NOT NULL) are that month's own name in Arabic and
English, taken from the platform's locale data (`java.time.Month` + CLDR), never a
hardcoded string table. For any other `periodCount`, or a span that is not a whole year,
the fallback is the even split: the days of [startDate, endDate] divided into periodCount
contiguous blocks, the first (totalDays mod periodCount) blocks one day longer, so the last
period always ends on the year's endDate and no day belongs to two periods; those periods
are named "الفترة N" / "Period N".
Repository: QR-FIN-038 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_CREATE` · Localization: n/a
Concurrency  : the code pre-check is the friendly path; UQ_FIN_FISCAL_YEAR_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_FISCAL_YEAR_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-YEAR-DUP — v1 behaviour as built, recorded here and not changed by v2.
<!-- API:API-FIN-023:END -->

<!-- API:API-FIN-024:START traces=REQ-FIN-032,DBF-FIN-082 -->
### API-FIN-024 — open period
Entity       : ENT-FIN-008 · operation UPDATE (open)
Endpoint: PATCH /api/v1/fin/fiscal-periods/{id}/open · Layers: `FiscalPeriodController.open`→`FiscalPeriodService.open`
Request: path `id` · Response: 200 · `FiscalPeriodResponse`
Validations: current state must be SOFT_CLOSE (§10.2 "re-openable" — Hard Closed is not, RULE-FIN-014)
Errors: `FIN-409-NOT-REOPENABLE`, `FIN-404-PERIOD`
Orchestration: (sets DBF-FIN-082 statusCode=OPEN) load the period PESSIMISTIC_WRITE → check RULE-FIN-014 (QR-FIN-040) → transition (QR-FIN-039) → return
Repository: QR-FIN-039, QR-FIN-040 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_UPDATE` · Localization: n/a
Concurrency  : the period row is loaded PESSIMISTIC_WRITE before the current-status check, so two concurrent transitions of one period serialize instead of racing last-writer-wins (ADR-FIN-041)
<!-- API:API-FIN-024:END -->

<!-- API:API-FIN-025:START traces=REQ-FIN-033,DBF-FIN-082 -->
### API-FIN-025 — soft-close period
Entity       : ENT-FIN-008 · operation UPDATE (soft-close)
Endpoint: PATCH /api/v1/fin/fiscal-periods/{id}/soft-close · Layers: `FiscalPeriodController.softClose`→`FiscalPeriodService.softClose`
Request: path `id` · Response: 200 · `FiscalPeriodResponse`
Validations: current state must be OPEN · Errors: `FIN-409-INVALID-TRANSITION`, `FIN-404-PERIOD`
Orchestration: (sets DBF-FIN-082 statusCode=SOFT_CLOSE) load the period PESSIMISTIC_WRITE → transition (QR-FIN-039) → return · Repository: QR-FIN-039 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_UPDATE` · Localization: n/a
Concurrency  : the period row is loaded PESSIMISTIC_WRITE before the current-status check, so two concurrent transitions of one period serialize instead of racing last-writer-wins (ADR-FIN-041)
<!-- API:API-FIN-025:END -->

<!-- API:API-FIN-026:START traces=REQ-FIN-034,REQ-FIN-035,REQ-FIN-037,REQ-FIN-038,DBF-FIN-082,DBF-FIN-083,DBF-FIN-084 -->
### API-FIN-026 — hard-close period (approval)
Entity       : ENT-FIN-008 · operation UPDATE (hard-close — custom close-approve action)
Endpoint: PATCH /api/v1/fin/fiscal-periods/{id}/hard-close · Layers: `FiscalPeriodController.hardClose`→`FiscalPeriodService.hardClose`
Request: path `id` · Response: 200 · `FiscalPeriodResponse`
Validations: RULE-FIN-014 (not already Hard Closed, QR-FIN-040); RULE-FIN-015 (full text:
DATA-DOM §ENT-FIN-008) — caller must hold `PERM_FIN_PERIODS_CLOSE_APPROVE`, a permission
distinct from `PERM_FIN_JOURNAL_ENTRIES_CREATE`. **That gate is the WHOLE of the rule**: the
`@PreAuthorize` on `FiscalPeriodService.hardClose`, enforced by SEC's own mechanism, exactly as
RULE-FIN-015 states it and AC-FIN-038 describes it. There is NO service-layer SoD read — the
one that existed was deleted on 2026-09-12 as an over-implementation (it refused the close for
every caller whenever any single user held both codes); see the Error Catalog's struck
`FIN-403-SOD-VIOLATION` row and retired XM-FIN-002
Errors: `FIN-409-NOT-REOPENABLE`(reused message context), `FIN-404-PERIOD`
Orchestration: (sets DBF-FIN-082 statusCode=HARD_CLOSE · DBF-FIN-083 closedBy · DBF-FIN-084 closedAt) load the period PESSIMISTIC_WRITE → check RULE-FIN-014 (QR-FIN-040)
→ transition, set closedBy/closedAt to the approving principal (QR-FIN-039) → return
Repository: QR-FIN-039, QR-FIN-040 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_CLOSE_APPROVE` (custom — the distinct permission RULE-FIN-015 requires; since V30 it is also granted to SYS_ADMIN) · Localization: n/a
Concurrency  : the period row is loaded PESSIMISTIC_WRITE before the current-status check, so two concurrent transitions of one period serialize instead of racing last-writer-wins (ADR-FIN-041)
<!-- API:API-FIN-026:END -->

<!-- API:API-FIN-027:START traces=REQ-FIN-036,DBF-FIN-069,DBF-FIN-034 -->
### API-FIN-027 — run year-end close
Entity       : ENT-FIN-007, ENT-FIN-008, ENT-FIN-004 · operation UPDATE (year-end close; creates the CLOSING and OPENING entries)
Endpoint: POST /api/v1/fin/fiscal-years/{id}/year-end-close · Layers: `FiscalYearController.yearEndClose`→`FiscalYearService.yearEndClose`
Request: path `id` · Response: 201 · `{closingEntry: JournalEntryResponse, openingEntry: JournalEntryResponse}`
Validations, IN THIS ORDER (the order is part of the contract — a test asserting 409 on this
endpoint must not be defeated by a missing successor year or Retained Earnings
account): (1) the fiscal year's own statusCode must
still be OPEN — a re-run against an already-CLOSED year is rejected with
`FIN-409-INVALID-TRANSITION`, a stated rule on ENT-FIN-007's state machine rather than the
incidental `FIN-409-PERIODS-NOT-CLOSED` the first run's own YEAR_END_CLOSE transitions would
otherwise raise; (2) every period of the year must be HARD_CLOSE (§10.4 precondition), 409.
Only then are the successor year and the Retained Earnings account resolved.
RULE-FIN-015 is no longer a numbered step here: it is enforced entirely by this endpoint's
`@PreAuthorize(PERM_FIN_PERIODS_CLOSE_APPROVE)` gate, which rejects before the method body
runs. The step that used to come first — a service-layer SoD read answering 403 — was deleted
on 2026-09-12 as an over-implementation; see the Error Catalog's struck `FIN-403-SOD-VIOLATION`
row. **A test written against the old contract and asserting 403 from inside this method will
now fail, and should be retargeted at the `@PreAuthorize` gate.**
Errors: `FIN-409-INVALID-TRANSITION` (year already closed), `FIN-409-PERIODS-NOT-CLOSED`, `FIN-404-YEAR` (unknown year id, or no adjacent successor year), `FIN-404-ACCOUNT` (no account marked as Retained Earnings)
Orchestration: (sets DBF-FIN-069 statusCode=CLOSED · DBF-FIN-082 statusCode=YEAR_END_CLOSE on every period · the two entries' DBF-FIN-039 journalTypeCode=CLOSING/OPENING) load the fiscal year PESSIMISTIC_WRITE → verify the year is still OPEN → verify all periods Hard
Closed → resolve the successor year (QR-FIN-048) and the Retained Earnings account
(QR-FIN-045) → compute closing
balances (QR-FIN-041) →
build + post a closing entry into the year's LAST period (result accounts → Retained
Earnings, journalTypeCode=CLOSING). The Retained Earnings account is DERIVED BY THE SYSTEM
from FIN_ACCOUNT.is_retained_earnings_fl (DBF-FIN-147, at most one row TRUE — partial unique
index UQ_FIN_ACCOUNT_RETAINED_EARNINGS) and is never supplied by the caller; no marked
account is `FIN-404-ACCOUNT`
→ build + post the next year's opening entry from the resulting balance-sheet balances
(journalTypeCode=OPENING, QR-FIN-041, POL-FIN-010) — CLOSING/OPENING are two values added
to the JOURNAL_TYPE lookup this stage (see Decisions Applied — a data-only, non-breaking
extension, not a new REQ/RULE) → mark FiscalYear statusCode=CLOSED, periods
statusCode=YEAR_END_CLOSE → return
Repository: QR-FIN-041, QR-FIN-029, QR-FIN-030, QR-FIN-032, QR-FIN-033 (both generated
entries go through the same validated posting pipeline) · QR-FIN-031 is deliberately NOT
applied to either of them: RULE-FIN-008 itself exempts the year-end closing and opening
entries, since this API's own all-periods-Hard-Closed precondition leaves no Open period for
the closing entry to post into. RULE-FIN-006/007/009 apply to both in full · the successor
fiscal year receiving the opening entry is resolved by DATE ADJACENCY (the year whose
startDate is the day after this year's endDate) — a DERIVED decision, recorded as such in
execution-state.json because ENT-FIN-007 declares no successor column; no such year is
`FIN-404-YEAR` · join intra-module · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_CLOSE_APPROVE` · Localization: n/a
DORMANT YEAR — a reviewed decision, not an accident. For a fiscal year in which no result account
carries a non-zero balance, `FiscalYearService.closingLines` contributes no line at all (each
result account with `net().signum() == 0` is skipped, and the Retained Earnings absorbing line is
added only when the running `resultTotal` is itself non-zero), and symmetrically
`openingLines` contributes none when no balance-sheet account carries a non-zero balance. The run
therefore posts a CLOSING (and OPENING) journal entry with an EMPTY line set, and that entry still
consumes a `docNo`: `JournalPostingService.buildValidateAndPost` allocates the number from the
locked fiscal-year series before it validates, and writes the entry unconditionally.
This was reviewed and deliberately KEPT. `JournalEntryDomain.checkBalanced` sums debits and
credits and returns empty when they compare equal, so 0 = 0 passes — a lineless entry literally
satisfies AC-FIN-036's "both entries individually balanced". Omitting the entry would make
AC-FIN-036's "posts a closing entry" false, and refusing the close outright would invent a rule no
artifact states.
Reachable only from here. `JournalEntryCreateRequest.lines` carries `@NotEmpty`, so API-FIN-019
rejects a lineless entry at the DTO boundary. This shape exists solely on the internal year-end
path, which builds its lines itself and never passes through that DTO.
Concurrency  : the fiscal year row is loaded PESSIMISTIC_WRITE at the top of the transaction, before the OPEN-status check, so two concurrent runs of year-end close on one year serialize instead of both passing the OPEN check and each posting a CLOSING/OPENING pair (ADR-FIN-041); the same lock backs the docNo allocation later in the same method
<!-- API:API-FIN-027:END -->
<!-- SUB:SVC-API-INT:END -->
