package com.erp.fin.exception;

/**
 * Module-specific error codes for Finance / General Ledger (FIN) — one constant per row of the
 * FIN v1 Error Catalog (packages/backend-execution/_SECTIONS.md). The constant value is the
 * runtime code verbatim ({@code FIN-{http}[-{SLUG}]}, per CORE.md's error-code format), and is
 * both the wire {@code code} in the {@code ApiError} envelope and the i18n message key — same
 * convention as {@code MdlErrorCodes} and {@code SecErrorCodes}.
 *
 * <p>Registration is incremental, per sub (MDL's established pattern): only the codes actually
 * thrown by the code delivered so far are declared here. DATA-DOM-LOOKUP registered the three
 * codes its Domain layer throws — {@code DimensionValueDomain}, {@code EventTypeRuleDomain} and
 * {@code AllocationRuleDomain}; DATA-DOM-MASTER adds the five thrown by {@code AccountDomain}
 * and {@code FiscalPeriodDomain}. The SVC-API phase adds the service-layer not-found and
 * lookup-validation codes.
 */
public final class FinErrorCodes {

    private FinErrorCodes() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    /**
     * RULE-FIN-002 — a dimension value whose code already exists under the same dimension
     * (QR-FIN-010; backed by {@code UQ_FIN_DIMENSION_VALUE_DIM_CODE}).
     * API: API-FIN-007. HTTP 409.
     */
    public static final String FIN_409_DIMVALUE_DUP = "FIN-409-DIMVALUE-DUP";

    /**
     * PLATFORM-STD (§6.4, one rule per event type, QR-FIN-014) — the event type already has an
     * active rule (backed by {@code UQ_FIN_EVENT_TYPE_RULE_CODE}).
     * API: API-FIN-010. HTTP 409.
     */
    public static final String FIN_409_RULE_DUP = "FIN-409-RULE-DUP";

    /**
     * RULE-FIN-003 — wrong remainder-line/target count: exactly one remainder is required
     * whenever any sibling line (ENT-FIN-010) or target (ENT-FIN-014) uses percentage
     * distribution (QR-FIN-016).
     * APIs: API-FIN-011, API-FIN-016. HTTP 409.
     */
    public static final String FIN_409_REMAINDER_COUNT = "FIN-409-REMAINDER-COUNT";

    /**
     * RULE-FIN-001 (create trigger) — a new sub-account was placed under a parent that is still
     * marked as accepting direct posting; the parent must be demoted first (QR-FIN-006).
     * API: API-FIN-002. HTTP 409.
     */
    public static final String FIN_409_PARENT_NOT_LEAF_ELIGIBLE = "FIN-409-PARENT-NOT-LEAF-ELIGIBLE";

    /**
     * RULE-FIN-001 (update trigger) — an account that already has sub-accounts cannot be marked
     * as accepting direct posting (QR-FIN-006).
     * APIs: API-FIN-002, API-FIN-003. HTTP 409.
     */
    public static final String FIN_409_HAS_CHILDREN = "FIN-409-HAS-CHILDREN";

    /**
     * RULE-FIN-007 — a posting line targets an account that is not a leaf or not active.
     * APIs: API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017. HTTP 409.
     */
    public static final String FIN_409_NOT_POSTABLE_ACCOUNT = "FIN-409-NOT-POSTABLE-ACCOUNT";

    /**
     * RULE-FIN-014 — an attempt to reopen a Hard Closed (or Year-End Closed) period; both are
     * terminal states per SRS A7 (QR-FIN-040).
     * APIs: API-FIN-024, API-FIN-026. HTTP 409.
     */
    public static final String FIN_409_NOT_REOPENABLE = "FIN-409-NOT-REOPENABLE";

    /**
     * RULE-FIN-015 — the close-approver also holds the journal-entry-creation permission, so the
     * separation of duties is not satisfied. The two permission facts are read from SEC by the
     * service and passed into {@code FiscalPeriodDomain} as plain arguments.
     * APIs: API-FIN-026, API-FIN-027. HTTP 403 ({@code Status.FORBIDDEN}).
     */
    public static final String FIN_403_SOD_VIOLATION = "FIN-403-SOD-VIOLATION";

    /**
     * RULE-FIN-006 — the entry's total debits do not equal its total credits (QR-FIN-029,
     * POL-FIN-001). Thrown by {@code JournalEntryDomain.assertBalanced(...)}.
     * APIs: API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017. HTTP 409
     * ({@code Status.CONFLICT}).
     */
    public static final String FIN_409_UNBALANCED = "FIN-409-UNBALANCED";

    /**
     * RULE-FIN-008 — the target period is not Open at post time (QR-FIN-031, POL-FIN-004). Thrown
     * by {@code JournalEntryDomain.assertTargetPeriodOpen(...)} and by
     * {@code JournalEntryDomain.resolveReversalPeriodPk(...)} when RULE-FIN-012 finds no open
     * period to receive a reversal.
     * APIs: API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021. HTTP 409.
     */
    public static final String FIN_409_PERIOD_NOT_OPEN = "FIN-409-PERIOD-NOT-OPEN";

    /**
     * RULE-FIN-009 — a posting line cites a dimension value that does not belong to its stated
     * dimension, or is inactive (QR-FIN-032). Thrown by
     * {@code DimensionValueDomain.assertUsableOnLine(...)}.
     * APIs: API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017. HTTP 409.
     */
    public static final String FIN_409_INVALID_DIMENSION = "FIN-409-INVALID-DIMENSION";

    /**
     * RULE-FIN-004 — an entry has already been produced for this {@code eventReference}
     * (QR-FIN-026, POL-FIN-012; backed by {@code UQ_FIN_JOURNAL_ENTRY_EVENT_REF}). Thrown by
     * {@code JournalEntryDomain.create(...)}.
     * API: API-FIN-020. HTTP 409.
     */
    public static final String FIN_409_DUPLICATE_EVENT = "FIN-409-DUPLICATE-EVENT";

    /**
     * RULE-FIN-013 — a reverse action was requested on an entry that is not POSTED (QR-FIN-035).
     * Thrown by {@code JournalEntryDomain.assertCanReverse()}.
     * API: API-FIN-021. HTTP 409.
     */
    public static final String FIN_409_NOT_POSTED = "FIN-409-NOT-POSTED";

    /**
     * PLATFORM-STD — the row is not in the state this transition expects. The Error Catalog
     * registers this row against the period soft-close transition; it does not cover RULE-FIN-016
     * (the posting lock), which is enforced by omission and throws nothing — see
     * {@code JournalEntryDomain}'s class javadoc.
     *
     * <p>ALIGN-BE reuses the same row for API-FIN-027's re-run guard: a year-end close requested
     * on a fiscal year already CLOSED is the identical semantic ("the row is not in the state this
     * transition expects"), so no new code is invented for it. Thrown there by
     * {@code FiscalYearDomain.assertCanYearEndClose()}.
     * APIs: API-FIN-025, API-FIN-027. HTTP 409.
     */
    public static final String FIN_409_INVALID_TRANSITION = "FIN-409-INVALID-TRANSITION";

    /**
     * PLATFORM-STD — unknown journal entry id. The not-found code for the two read paths this
     * sub's repository layer serves (QR-FIN-035's load-before-reverse and QR-FIN-037's read-one);
     * raised by the service's {@code orElseThrow}, registered here with the rest of the posting
     * core's codes.
     * APIs: API-FIN-021, API-FIN-022. HTTP 404 ({@code Status.NOT_FOUND}).
     */
    public static final String FIN_404_ENTRY = "FIN-404-ENTRY";

    // ─────────────────────────────────────────────────────────────────────────────────────────
    // SVC-API-CRUD — the service-layer codes API-FIN-002/003/004/006/007/010/011/013/016/019
    // actually throw. Every constant below is a verbatim Error Catalog row (_SECTIONS.md).
    // ─────────────────────────────────────────────────────────────────────────────────────────

    /**
     * PLATFORM-STD (XM-FIN-001) — a submitted lookup-backed code is not an active value of its
     * MDL lookup type, or the type key itself is unknown/inactive (MDL's own
     * {@code MDL-404-TYPE-KEY}, translated at FIN's boundary, never surfaced raw).
     * APIs: many. HTTP 400 ({@code Status.VALIDATION_ERROR}).
     */
    public static final String FIN_400_INVALID_LOOKUP = "FIN-400-INVALID-LOOKUP";

    /**
     * PLATFORM-STD — duplicate account code (QR-FIN-005, backed by {@code UQ_FIN_ACCOUNT_CODE}).
     * API: API-FIN-002. HTTP 409 ({@code Status.ALREADY_EXISTS}).
     */
    public static final String FIN_409_ACCOUNT_DUP = "FIN-409-ACCOUNT-DUP";

    /**
     * PLATFORM-STD — unknown account id.
     * APIs: API-FIN-003, API-FIN-004, API-FIN-013, API-FIN-016, API-FIN-019, API-FIN-028.
     * HTTP 404 ({@code Status.NOT_FOUND}).
     */
    public static final String FIN_404_ACCOUNT = "FIN-404-ACCOUNT";

    /**
     * PLATFORM-STD — duplicate dimension code (backed by {@code UQ_FIN_DIMENSION_CODE}).
     * API: API-FIN-006. HTTP 409 ({@code Status.ALREADY_EXISTS}).
     */
    public static final String FIN_409_DIMENSION_DUP = "FIN-409-DIMENSION-DUP";

    /**
     * PLATFORM-STD — unknown dimension id.
     * APIs: API-FIN-007, API-FIN-008, API-FIN-019, API-FIN-032. HTTP 404.
     */
    public static final String FIN_404_DIMENSION = "FIN-404-DIMENSION";

    /**
     * PLATFORM-STD — unknown event-type rule id.
     * API: API-FIN-011. HTTP 404.
     */
    public static final String FIN_404_RULE = "FIN-404-RULE";

    /**
     * PLATFORM-STD — a recurring template was submitted without a {@code frequencyCode} while its
     * {@code scheduleTypeCode} is not {@code REVERSING} (SRS ENT-FIN-011: "yes (when
     * scheduleTypeCode=RECURRING)"). A conditional-requiredness input check, not an SRS RULE.
     * API: API-FIN-013. HTTP 400 ({@code Status.VALIDATION_ERROR}).
     */
    public static final String FIN_400_MISSING_FREQUENCY = "FIN-400-MISSING-FREQUENCY";

    /**
     * PLATFORM-STD — unknown fiscal year id. Registered here because API-FIN-019 resolves
     * {@code fiscalYearId} before it can build an entry; the catalog row is shared with
     * API-FIN-027. HTTP 404.
     */
    public static final String FIN_404_YEAR = "FIN-404-YEAR";

    /**
     * PLATFORM-STD — unknown period id. Registered here because API-FIN-019 resolves
     * {@code periodId} before it can build an entry; the catalog row is shared with
     * API-FIN-024/025/026. HTTP 404.
     */
    public static final String FIN_404_PERIOD = "FIN-404-PERIOD";

    // ─────────────────────────────────────────────────────────────────────────────────────────
    // SVC-API-INT — the codes API-FIN-014/017/020/021/023/024/025/026/027 add on top of the
    // posting-core set above. Every constant below is a verbatim Error Catalog row
    // (_SECTIONS.md, "Error Catalog — FIN v1").
    // ─────────────────────────────────────────────────────────────────────────────────────────

    /**
     * RULE-FIN-005 — the submitted event's type has no active {@code EventTypeRule} (QR-FIN-027).
     * A not-found precondition: without a rule there is nothing to build an entry from, so it is
     * raised fail-fast by the service's {@code orElseThrow} before any other check runs.
     * API: API-FIN-020. HTTP 404 ({@code Status.NOT_FOUND}).
     */
    public static final String FIN_404_NO_ACTIVE_RULE = "FIN-404-NO-ACTIVE-RULE";

    /**
     * PLATFORM-STD — unknown allocation rule id (QR-FIN-022).
     * API: API-FIN-017. HTTP 404 ({@code Status.NOT_FOUND}).
     */
    public static final String FIN_404_ALLOCATION_RULE = "FIN-404-ALLOCATION-RULE";

    /**
     * PLATFORM-STD — duplicate fiscal year code (backed by {@code UQ_FIN_FISCAL_YEAR_CODE}).
     * API: API-FIN-023. HTTP 409 ({@code Status.ALREADY_EXISTS}).
     */
    public static final String FIN_409_YEAR_DUP = "FIN-409-YEAR-DUP";

    /**
     * PLATFORM-STD (§10.4 precondition) — year-end close was requested while at least one period
     * of the fiscal year is not Hard Closed. Thrown by
     * {@code FiscalPeriodDomain.assertHardClosedForYearEnd()}.
     * API: API-FIN-027. HTTP 409 ({@code Status.CONFLICT}).
     */
    public static final String FIN_409_PERIODS_NOT_CLOSED = "FIN-409-PERIODS-NOT-CLOSED";

    /**
     * PLATFORM-STD — a rule line configured with {@code accountDerivationTypeCode = MAPPING}
     * cannot be executed: SRS ENT-FIN-010 defines its {@code accountDerivationValue} as a
     * mapping-set key, and db-script-fin.md declares no mapping store among FIN's 14 tables.
     * {@code CONSTANT} and {@code DIRECT} are unaffected. Thrown by
     * {@code EventTypeRuleDomain.resolveAccountCode(...)}; removed again once the mapping store is
     * specified.
     * API: API-FIN-020. HTTP 422 ({@code Status.BUSINESS_RULE_VIOLATION}).
     */
    public static final String FIN_422_MAPPING_UNSUPPORTED = "FIN-422-MAPPING-UNSUPPORTED";

    /**
     * PLATFORM-STD — unknown recurring template id (ENT-FIN-011, QR-FIN-019). Its own row: the
     * catalog assigns a distinct 404 per entity, and raising {@code FIN-404-RULE} here told the
     * client that an event-type rule was missing.
     * API: API-FIN-014. HTTP 404 ({@code Status.NOT_FOUND}).
     */
    public static final String FIN_404_TEMPLATE = "FIN-404-TEMPLATE";
    // ─────────────────────────────────────────────────────────────────────────────────────────
    // SVC-API-SEARCH — the one code this sub's read-only endpoints add. Every other code the
    // seven searches, the entry read and the five reports raise (FIN-404-ACCOUNT,
    // FIN-404-DIMENSION, FIN-404-ENTRY, FIN-404-PERIOD) is already registered above. FIN-500 is
    // the infrastructure fall-through and is produced by the global handler, not thrown here.
    // ─────────────────────────────────────────────────────────────────────────────────────────

    /**
     * PLATFORM-STD — the caller asked to sort by a field that is not on the searched entity's
     * {@code ALLOWED_SORT_FIELDS} whitelist. Registered verbatim from the Error Catalog row
     * ({@code every search API | 400 | unrecognized sort field}).
     *
     * <p>The check is explicit because the shared {@code PageableBuilder} silently DROPS an
     * unrecognised sort field instead of signalling, which would return a differently ordered page
     * than the client asked for with no indication that anything was ignored. Same reasoning and
     * same shape as SEC's delivered {@code SecSearchSupport.assertSortAllowed}.
     * APIs: API-FIN-001, 005, 008, 009, 012, 015, 018. HTTP 400
     * ({@code Status.VALIDATION_ERROR}).
     */
    public static final String FIN_400_INVALID_SORT = "FIN-400-INVALID-SORT";

    /**
     * RULE-FIN-013 — a reverse action was requested on a POSTED entry that already carries a
     * reversal link ({@code REVERSAL_ENTRY_ID}, DBF-FIN-043). The second half of REQ-FIN-030's
     * rationale ("prevents double-reversal", srs-fin.md line 682), which used to be enforced
     * incidentally by the pre-classic VOID write and now needs its own code. Thrown by
     * {@code JournalEntryDomain.assertCanReverse(boolean)}.
     *
     * <p>409 rather than 422: the request is blocked by the existence of a specific referencing
     * record — the reversal entry already pointing at this one — which is the taxonomy's
     * {@code CONFLICT} row, not the "invariant violation that is NOT about a specific referencing
     * record" row.
     * APIs: API-FIN-021, API-FIN-014. HTTP 409 ({@code Status.CONFLICT}).
     */
    public static final String FIN_409_ALREADY_REVERSED = "FIN-409-ALREADY-REVERSED";

    // ─────────────────────────────────────────────────────────────────────────────────────────
    // ALIGN-BE — the four codes the accounting-correctness alignment adds. Every constant below
    // is a verbatim Error Catalog row (_SECTIONS.md, "Error Catalog — FIN v1").
    // ─────────────────────────────────────────────────────────────────────────────────────────

    /**
     * RULE-FIN-003 / RULE-FIN-010 — a rule line (ENT-FIN-010) or allocation target (ENT-FIN-014)
     * whose {@code isRemainderFl} marker disagrees with its own {@code amountSourceTypeCode} /
     * {@code distributionTypeCode}: either the line declares a REMAINDER source without being
     * marked, or it is marked without declaring one. {@code isRemainderFl} (DBF-FIN-103 /
     * DBF-FIN-141) is the schema's own marker and the single one both the RULE-FIN-003 guard and
     * the distribution builder read; a disagreement makes "which line is the remainder" ambiguous,
     * which is exactly what RULE-FIN-010 must never be. Thrown by
     * {@code EventTypeRuleDomain.assertRemainderLineSetValid(...)} /
     * {@code AllocationRuleDomain.assertRemainderTargetSetValid(...)} and, defensively, by their
     * amount derivations.
     *
     * <p>422 rather than 409: an invariant violation inside the submitted/stored rule definition
     * itself, not a clash with a specific referencing record — the taxonomy's
     * {@code BUSINESS_RULE_VIOLATION} row.
     * APIs: API-FIN-011, API-FIN-016, API-FIN-017, API-FIN-020. HTTP 422.
     */
    public static final String FIN_422_REMAINDER_MARKER = "FIN-422-REMAINDER-MARKER";

    /**
     * RULE-FIN-010 — the remainder line's computed amount is not positive: the non-remainder lines
     * on its own side already equal or exceed the opposing side's total, so there is no balancing
     * residue for it to absorb. Previously this produced a negative or zero amount that died on
     * {@code CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE} as an unlocalized {@code
     * DATA_INTEGRITY_VIOLATION}. Thrown by {@code EventTypeRuleDomain.remainderAmount(...)}.
     *
     * <p>422 rather than 409: POL-FIN-005 ("amounts are always positive") is an invariant of the
     * entry being built, not a clash with an existing record.
     * APIs: API-FIN-017, API-FIN-020. HTTP 422 ({@code Status.BUSINESS_RULE_VIOLATION}).
     */
    public static final String FIN_422_REMAINDER_NOT_POSITIVE = "FIN-422-REMAINDER-NOT-POSITIVE";

    /**
     * RULE-FIN-017 — the submitted {@code periodId} belongs to a different fiscal year than the
     * submitted {@code fiscalYearId} (DBF-FIN-076 makes the period's year a stored fact). Thrown
     * by {@code JournalEntryDomain.assertHeaderCoherent(...)}.
     *
     * <p>400 rather than 409/422: nothing about the stored ledger is in conflict — both rows exist
     * and are individually valid; the request's own three header fields are mutually inconsistent,
     * which is the taxonomy's structural {@code VALIDATION_ERROR} row.
     * API: API-FIN-019. HTTP 400 ({@code Status.VALIDATION_ERROR}).
     */
    public static final String FIN_400_PERIOD_NOT_IN_YEAR = "FIN-400-PERIOD-NOT-IN-YEAR";

    /**
     * RULE-FIN-017 — the submitted {@code docDate} falls outside the submitted period's
     * {@code [startDate, endDate]} span (DBF-FIN-080/081, both NOT NULL). Thrown by
     * {@code JournalEntryDomain.assertHeaderCoherent(...)}.
     *
     * <p>400 for the same reason as {@link #FIN_400_PERIOD_NOT_IN_YEAR}.
     * API: API-FIN-019. HTTP 400 ({@code Status.VALIDATION_ERROR}).
     */
    public static final String FIN_400_DOCDATE_OUTSIDE_PERIOD = "FIN-400-DOCDATE-OUTSIDE-PERIOD";
}
