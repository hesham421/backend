package com.erp.fin.domain;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.fin.entity.EventTypeRule;
import com.erp.fin.entity.RuleLine;
import com.erp.fin.exception.FinErrorCodes;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Domain companion for ENT-FIN-009 (EventTypeRule), covering the two decisions that belong to
 * the rule aggregate:
 *
 * <ul>
 *   <li><b>One active rule per event type</b> (§6.4, QR-FIN-014) — {@code FIN-409-RULE-DUP},
 *       backed by {@code UQ_FIN_EVENT_TYPE_RULE_CODE}.</li>
 *   <li><b>RULE-FIN-003</b> — exactly one line marked as the remainder whenever the line set is a
 *       compound or percentage distribution — any sibling line uses {@code PERCENTAGE}
 *       distribution, or any line is already marked as the remainder ({@code
 *       FIN-409-REMAINDER-COUNT}, QR-FIN-016) — and that marker, {@code isRemainderFl}, must agree
 *       with the line's own REMAINDER type codes ({@code FIN-422-REMAINDER-MARKER}). The rule
 *       spans the whole line set of one rule, so it sits on the aggregate root rather than on
 *       {@code RuleLine}; A.0.7 allows at most one Domain object per entity and this is it.</li>
 * </ul>
 *
 * <p>Placement note: DATA-DOM-LOOKUP.md annotates RULE-FIN-003 with "— service", but CORE.md
 * mandates a domain class for every rule answering "is this operation allowed?", and A.5.18
 * makes a business-rule {@code if} inlined in a service an automatic rejection. CORE.md wins.
 *
 * <p>The service resolves every fact (uniqueness via QR-FIN-014, the prospective line set from
 * the request plus already-persisted siblings) and passes it in — this class never touches a
 * repository (A.0.3) and never calls another module (A.0.6).
 */
public final class EventTypeRuleDomain {

    /**
     * The {@code DISTRIBUTION_TYPE} lookup value that triggers RULE-FIN-003, per srs-fin.md §A6
     * ({@code DISTRIBUTION_TYPE: FIXED, PERCENTAGE, REMAINDER}). Compared here as the plain
     * code the column stores (XM-FIN-001); membership of the code in MDL is validated earlier,
     * in the service layer.
     */
    public static final String DISTRIBUTION_TYPE_PERCENTAGE = "PERCENTAGE";

    /** ACCOUNT_DERIVATION_TYPE (SRS A6) — the derivation value IS the account code. */
    public static final String ACCOUNT_DERIVATION_TYPE_CONSTANT = "CONSTANT";

    /** ACCOUNT_DERIVATION_TYPE (SRS A6) — the value names the event field carrying the code. */
    public static final String ACCOUNT_DERIVATION_TYPE_DIRECT = "DIRECT";

    /**
     * ACCOUNT_DERIVATION_TYPE (SRS A6) — the value is a mapping-set key. No mapping store exists
     * anywhere in db-script-fin.md, so this derivation cannot be executed; see
     * {@link #resolveAccountCode(String, String, Map)}.
     */
    public static final String ACCOUNT_DERIVATION_TYPE_MAPPING = "MAPPING";

    /** AMOUNT_SOURCE_TYPE (SRS A6) — the amount is a percentage of the event's base amount. */
    public static final String AMOUNT_SOURCE_TYPE_PERCENTAGE = "PERCENTAGE";

    /** AMOUNT_SOURCE_TYPE (SRS A6) — the amount is the rounding remainder (RULE-FIN-010). */
    public static final String AMOUNT_SOURCE_TYPE_REMAINDER = "REMAINDER";

    /** {@code NUMERIC(18,4)} — the smallest stored unit every computed amount rounds to. */
    public static final int AMOUNT_SCALE = 4;

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final String eventTypeCode;
    private final boolean active;

    private EventTypeRuleDomain(String eventTypeCode, boolean active) {
        this.eventTypeCode = eventTypeCode;
        this.active = active;
    }

    /**
     * API-FIN-010 (create event-type rule): one active rule per event type.
     *
     * @param eventTypeCode     the submitted ACCOUNTING_EVENT_TYPE code
     * @param ruleAlreadyExists QR-FIN-014's answer, resolved by the service
     * @throws LocalizedException {@code FIN-409-RULE-DUP} when the event type already has a rule
     */
    public static EventTypeRuleDomain create(String eventTypeCode, boolean ruleAlreadyExists) {
        if (ruleAlreadyExists) {
            throw new LocalizedException(Status.ALREADY_EXISTS,
                FinErrorCodes.FIN_409_RULE_DUP, eventTypeCode);
        }
        return new EventTypeRuleDomain(eventTypeCode, true);
    }

    /** Reconstructs a Domain view over a persisted row — no validation. */
    public static EventTypeRuleDomain from(EventTypeRule entity) {
        return new EventTypeRuleDomain(entity.getEventTypeCode(),
            Boolean.TRUE.equals(entity.getIsActiveFl()));
    }

    /**
     * Is this line the remainder line? {@code isRemainderFl} (DBF-FIN-103) is the schema's own
     * marker and the SINGLE one both this class's RULE-FIN-003 guard and the API-FIN-020 builder
     * read. Which column carries the marker is rule knowledge, so the predicate lives here rather
     * than being re-expressed at each call site.
     *
     * <p>Before ALIGN-BE the guard counted {@code isRemainderFl} while the builder keyed off
     * {@code amountSourceTypeCode = REMAINDER}; two lines could therefore satisfy the guard and
     * both behave as the remainder at run time. {@link #assertRemainderLineSetValid(List)} now
     * rejects any disagreement between the two columns ({@code FIN-422-REMAINDER-MARKER}), so the
     * single marker is well defined for every line this predicate ever sees.
     */
    public static boolean isRemainderLine(Boolean isRemainderFl) {
        return Boolean.TRUE.equals(isRemainderFl);
    }

    /**
     * Does this line's own type code declare a remainder, on either of the two code columns
     * ENT-FIN-010 carries? {@code amountSourceTypeCode = REMAINDER} (DBF-FIN-103's sibling
     * DBF-FIN-103/104) and {@code distributionTypeCode = REMAINDER} (DBF-FIN-105) both say so.
     */
    private static boolean declaresRemainder(RuleLine line) {
        return AMOUNT_SOURCE_TYPE_REMAINDER.equalsIgnoreCase(line.getAmountSourceTypeCode())
            || AllocationRuleDomain.DISTRIBUTION_TYPE_REMAINDER
                .equalsIgnoreCase(line.getDistributionTypeCode());
    }

    /**
     * API-FIN-011 (add rule line): RULE-FIN-003, in the amended form ALIGN-BE gives it. Decision
     * only — the service persists the line set after this returns.
     *
     * <p>Two checks, in order:
     *
     * <ol>
     *   <li><b>Marker agreement</b> — a line marked {@code isRemainderFl} must declare a REMAINDER
     *       type code, and a line declaring one must be marked. The rule's own marker is
     *       {@code isRemainderFl} (DBF-FIN-103); the disagreement is rejected rather than silently
     *       resolved, because "which line is the remainder" decides every other line's amount
     *       (RULE-FIN-010) ({@code FIN-422-REMAINDER-MARKER}).</li>
     *   <li><b>Exactly one remainder</b> — whenever the line set is a compound or percentage
     *       distribution (POL-FIN-006's own trigger: any sibling uses PERCENTAGE distribution, OR
     *       any line is already marked as the remainder). The second half is what ALIGN-BE adds:
     *       two FIXED-distribution lines both marked remainder used to pass unchallenged here and
     *       then die on {@code CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE} at run time
     *       ({@code FIN-409-REMAINDER-COUNT}).</li>
     * </ol>
     *
     * @param prospectiveLines the rule's complete line set as it would stand after the write
     *                         (already-persisted siblings plus the submitted line), resolved by
     *                         the service
     * @throws LocalizedException {@code FIN-422-REMAINDER-MARKER} or
     *                            {@code FIN-409-REMAINDER-COUNT}
     */
    public void assertRemainderLineSetValid(List<RuleLine> prospectiveLines) {
        if (prospectiveLines == null || prospectiveLines.isEmpty()) {
            return;
        }
        for (RuleLine line : prospectiveLines) {
            if (isRemainderLine(line.getIsRemainderFl()) != declaresRemainder(line)) {
                throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION,
                    FinErrorCodes.FIN_422_REMAINDER_MARKER, line.getLineNo());
            }
        }

        boolean anyPercentage = prospectiveLines.stream()
            .anyMatch(line -> DISTRIBUTION_TYPE_PERCENTAGE
                .equalsIgnoreCase(line.getDistributionTypeCode()));
        boolean anyRemainder = prospectiveLines.stream()
            .anyMatch(line -> isRemainderLine(line.getIsRemainderFl()));
        if (!anyPercentage && !anyRemainder) {
            return;
        }
        long remainderCount = prospectiveLines.stream()
            .filter(line -> isRemainderLine(line.getIsRemainderFl()))
            .count();
        if (remainderCount != 1L) {
            throw new LocalizedException(Status.CONFLICT,
                FinErrorCodes.FIN_409_REMAINDER_COUNT, remainderCount);
        }
    }

    /**
     * §6.4 / REQ-FIN-010 — the account a rule line targets for a given event, per its
     * ACCOUNT_DERIVATION_TYPE (SRS A6: {@code CONSTANT, DIRECT, MAPPING}):
     *
     * <ul>
     *   <li>{@code CONSTANT} — {@code accountDerivationValue} (DBF-FIN-102) IS the account
     *       code;</li>
     *   <li>{@code DIRECT} — it names the event field carrying the account code;</li>
     *   <li>{@code MAPPING} — <b>rejected, loudly</b> ({@code FIN-422-MAPPING-UNSUPPORTED}). The
     *       value is a mapping-set key, and db-script-fin.md declares no mapping store anywhere:
     *       ENT-FIN-010 carries only the type/value pair (DBF-FIN-101/102) and none of the 14 FIN
     *       tables is a mapping set. Treating the key as a {@code DIRECT} event-field name — as an
     *       earlier revision did — silently resolves the line to the wrong account or to none at
     *       all, which is the one outcome a ledger must never produce. The line therefore fails
     *       until the mapping store is specified.</li>
     * </ul>
     *
     * <p>Added by SVC-API-INT: the derivation branches on a business code, so it belongs to the
     * rule aggregate rather than to an {@code if} in the service body (A.5.18). {@code CONSTANT}
     * and {@code DIRECT} are pure derivations with no catalog code of their own — an unresolvable
     * account code surfaces as the service's own {@code FIN-404-ACCOUNT}.
     *
     * @param accountDerivationTypeCode  DBF-FIN-101
     * @param accountDerivationValue     DBF-FIN-102
     * @param eventFields                the event payload's plain string fields, passed in by the
     *                                   service ({@code null}-tolerant)
     * @return the account code to post to, or {@code null} when the event carries no such field
     * @throws LocalizedException {@code FIN-422-MAPPING-UNSUPPORTED} for a {@code MAPPING} line
     */
    public static String resolveAccountCode(String accountDerivationTypeCode,
                                            String accountDerivationValue,
                                            Map<String, String> eventFields) {
        if (ACCOUNT_DERIVATION_TYPE_CONSTANT.equalsIgnoreCase(accountDerivationTypeCode)) {
            return accountDerivationValue;
        }
        if (ACCOUNT_DERIVATION_TYPE_MAPPING.equalsIgnoreCase(accountDerivationTypeCode)) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION,
                FinErrorCodes.FIN_422_MAPPING_UNSUPPORTED, accountDerivationValue);
        }
        if (eventFields == null || accountDerivationValue == null) {
            return null;
        }
        return eventFields.get(accountDerivationValue);
    }

    /**
     * RULE-FIN-010 (build half) — a rule line's amount for a given event, per its
     * AMOUNT_SOURCE_TYPE (SRS A6: {@code FIELD, PERCENTAGE, REMAINDER}):
     *
     * <ul>
     *   <li>{@code FIELD} — {@code amountSourceValue} (DBF-FIN-104) names the event amount
     *       field;</li>
     *   <li>{@code PERCENTAGE} — {@code amountSourceValue} is a percentage of the event's base
     *       amount, rounded to the smallest stored unit ({@code NUMERIC(18,4)},
     *       {@code HALF_UP}) exactly as the rule's own text requires;</li>
     *   <li>{@code REMAINDER} — <b>never reached</b>: the remainder line is identified by
     *       {@link #isRemainderLine(Boolean)} and computed last, as a difference, by
     *       {@link #remainderAmount(BigDecimal, BigDecimal)}; it is "never itself computed as a
     *       percentage". Reaching this method with a REMAINDER source means the line's marker and
     *       its type code disagree, which {@link #assertRemainderLineSetValid(List)} rejects —
     *       raised here too, so the derivation can never silently produce a normal amount for a
     *       remainder line ({@code FIN-422-REMAINDER-MARKER}).</li>
     * </ul>
     *
     * @return the line's amount
     * @throws LocalizedException {@code FIN-422-REMAINDER-MARKER} for a REMAINDER-sourced line
     */
    public static BigDecimal sourcedAmount(String amountSourceTypeCode,
                                           String amountSourceValue,
                                           BigDecimal baseAmount,
                                           Map<String, BigDecimal> eventAmounts) {
        if (AMOUNT_SOURCE_TYPE_REMAINDER.equalsIgnoreCase(amountSourceTypeCode)) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION,
                FinErrorCodes.FIN_422_REMAINDER_MARKER, amountSourceTypeCode);
        }
        if (AMOUNT_SOURCE_TYPE_PERCENTAGE.equalsIgnoreCase(amountSourceTypeCode)) {
            BigDecimal percentage = amountSourceValue == null
                ? BigDecimal.ZERO : new BigDecimal(amountSourceValue);
            BigDecimal base = baseAmount == null ? BigDecimal.ZERO : baseAmount;
            return base.multiply(percentage)
                .divide(ONE_HUNDRED, AMOUNT_SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal fieldAmount = eventAmounts == null || amountSourceValue == null
            ? null : eventAmounts.get(amountSourceValue);
        return fieldAmount == null ? BigDecimal.ZERO : fieldAmount;
    }

    /**
     * RULE-FIN-010 — the remainder line's amount, computed <b>per side</b>: the opposing side's
     * running total minus its own side's running total, after every percentage line has rounded.
     * It is never itself a percentage.
     *
     * <p><b>Why per side (ALIGN-BE).</b> A journal entry's remainder line exists to make one side
     * equal the other (POL-FIN-006, "the balancing guarantor"), so the difference it absorbs is a
     * difference between the two sides — not between a distributed total and the sum of every line
     * regardless of direction. The earlier side-blind form subtracted every line, debit and credit
     * alike, from the base amount: base 1000 with DEBIT 1000, CREDIT 60% = 600 and a CREDIT
     * remainder produced {@code 1000 − 1600 = −600}, a negative amount that failed
     * {@code CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE} as an unlocalized data-integrity 409. Per side
     * the same case gives {@code 1000 − 600 = 400} and the entry balances exactly.
     *
     * <p>This remains the rule's single implementation: the event-build path (API-FIN-020, this
     * class) and the allocation path (API-FIN-017, {@code AllocationRuleDomain}) both reach it, so
     * the rounding difference is absorbed identically wherever a compound distribution is built.
     * The allocation path's arguments are the same two facts under different names — the source
     * line's amount is the opposing side's whole total, and the already-assigned targets are the
     * remainder's own side.
     *
     * @param opposingSideTotal   the total already carried by the side opposite the remainder line
     * @param ownSideTotalSoFar   the sum of every already-computed line on the remainder's own side
     * @throws LocalizedException {@code FIN-422-REMAINDER-NOT-POSITIVE} when no positive residue
     *                            is left for the remainder line to absorb (POL-FIN-005)
     */
    public static BigDecimal remainderAmount(BigDecimal opposingSideTotal,
                                             BigDecimal ownSideTotalSoFar) {
        BigDecimal safeOpposing =
            opposingSideTotal == null ? BigDecimal.ZERO : opposingSideTotal;
        BigDecimal safeOwn = ownSideTotalSoFar == null ? BigDecimal.ZERO : ownSideTotalSoFar;
        BigDecimal remainder =
            safeOpposing.subtract(safeOwn).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        if (remainder.signum() <= 0) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION,
                FinErrorCodes.FIN_422_REMAINDER_NOT_POSITIVE, safeOpposing, safeOwn);
        }
        return remainder;
    }

    public String getEventTypeCode() {
        return eventTypeCode;
    }

    public boolean isActive() {
        return active;
    }
}
