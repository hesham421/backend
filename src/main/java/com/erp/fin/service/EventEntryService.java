package com.erp.fin.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.fin.domain.EventTypeRuleDomain;
import com.erp.fin.domain.JournalEntryDomain;
import com.erp.fin.dto.EventEntryBuildRequest;
import com.erp.fin.dto.JournalEntryResponse;
import com.erp.fin.dto.JournalLineResponse;
import com.erp.fin.entity.Account;
import com.erp.fin.entity.EventTypeRule;
import com.erp.fin.entity.FiscalPeriod;
import com.erp.fin.entity.JournalEntry;
import com.erp.fin.entity.RuleLine;
import com.erp.fin.exception.FinErrorCodes;
import com.erp.fin.mapper.JournalEntryMapper;
import com.erp.fin.mapper.JournalEntryMapper.BuiltLine;
import com.erp.fin.repository.AccountRepository;
import com.erp.fin.repository.EventTypeRuleRepository;
import com.erp.fin.repository.JournalEntryRepository;
import com.erp.fin.repository.RuleLineRepository;
import com.erp.fin.service.JournalPostingService.PostingRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration layer for API-FIN-020 — build and post a journal entry from a canonical
 * accounting event (SVC-API-INT.md, REQ-FIN-010..013 and REQ-FIN-017..021).
 *
 * <p>No caching annotations — FIN's approved cache register is empty. No
 * {@code ALLOWED_SORT_FIELDS}: this service exposes no search.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final EventTypeRuleRepository eventTypeRuleRepository;
    private final RuleLineRepository ruleLineRepository;
    private final AccountRepository accountRepository;
    private final JournalPostingService postingService;
    private final JournalEntryMapper mapper;

    /**
     * API-FIN-020 — check RULE-FIN-004 (QR-FIN-026) → resolve the active rule (QR-FIN-027) →
     * generate {@code docNo} → build the lines from the rule against the event's fields, the
     * remainder line last (QR-FIN-028, QR-FIN-025) → validate (QR-FIN-029..032) → post
     * (QR-FIN-033), all inside ONE transaction (CORE.md "Transaction scope").
     *
     * <p><b>Atomic unit: the entry</b> — one event produces exactly one entry, so "per entry" and
     * "per request" coincide. Nothing is written before validation succeeds.
     *
     * <p><b>Fail-fast vs aggregated.</b> The two preconditions are fail-fast, because an
     * unresolvable reference leaves nothing further to validate: RULE-FIN-004 (a duplicate
     * {@code eventReference} means the entry already exists) and RULE-FIN-005 (no active rule
     * means there is nothing to build). The four post-time rules — RULE-FIN-006/007/008/009 — are
     * aggregated by {@link JournalPostingService}, per REQ-FIN-015 / AC-FIN-015.
     *
     * <p><b>Rules delegated, none inlined</b> (A.5.18): RULE-FIN-004 →
     * {@code JournalEntryDomain.create(...)}; RULE-FIN-003 →
     * {@code EventTypeRuleDomain.assertRemainderLineSetValid(...)}, the precondition RULE-FIN-010
     * depends on; the account derivation, the amount sourcing and the remainder computation
     * (RULE-FIN-010) → {@code EventTypeRuleDomain}'s three derivations; RULE-FIN-006/007/008/009 →
     * the shared pipeline's own delegations.
     *
     * <p><b>Lookup validation.</b> Nothing lookup-backed is taken from the request:
     * {@code journalTypeCode} is the system-assigned SRS A6 value {@code EVENT_GENERATED}, each
     * line's {@code directionCode} comes from a {@code RuleLine} already validated against MDL at
     * API-FIN-011 time, and {@code eventTypeCode} is never persisted by this path — it only
     * selects the rule, and an unknown value surfaces as {@code FIN-404-NO-ACTIVE-RULE}. There is
     * therefore no XM-FIN-001 call to make here.
     *
     * <p><b>Security.</b> A system-to-system call from the Event consumer's own service principal,
     * gated by the same interceptor as any authenticated caller and by
     * {@code PERM_FIN_JOURNAL_ENTRIES_CREATE} (SVC-API-INT.md, API-FIN-020's Security line).
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants)"
        + ".PERM_FIN_JOURNAL_ENTRIES_CREATE)")
    public ServiceResult<JournalEntryResponse> build(EventEntryBuildRequest request) {
        log.info("Building event entry for event reference: {} type: {}",
            request.getEventReference(), request.getEventTypeCode());

        boolean alreadyProcessed =
            journalEntryRepository.existsByEventReference(request.getEventReference());
        JournalEntryDomain.create(request.getEventReference(), alreadyProcessed);

        EventTypeRule rule = eventTypeRuleRepository
            .findByEventTypeCodeAndIsActiveFl(request.getEventTypeCode(), Boolean.TRUE)
            .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND,
                FinErrorCodes.FIN_404_NO_ACTIVE_RULE, request.getEventTypeCode()));

        List<RuleLine> ruleLines =
            ruleLineRepository.findByEventTypeRulePk(rule.getEventTypeRulePk());
        EventTypeRuleDomain.from(rule).assertRemainderLineSetValid(ruleLines);

        FiscalPeriod period = postingService.resolvePeriodContaining(request.getDocDate());

        JournalEntry posted = postingService.buildValidateAndPost(new PostingRequest(
            period.getFiscalYear(), period, request.getDocDate(),
            JournalPostingService.JOURNAL_TYPE_EVENT_GENERATED, request.getEventReference(),
            request.getDescriptionAr(), request.getDescriptionEn(),
            buildLines(ruleLines, request)));

        List<JournalLineResponse> lineResponses =
            posted.getLines().stream().map(mapper::toLineResponse).toList();

        return ServiceResult.success(mapper.toResponse(posted, lineResponses), Status.CREATED);
    }

    /**
     * QR-FIN-025 / QR-FIN-028 — the rule's lines turned into posting lines against this event,
     * the remainder line computed last as RULE-FIN-010 requires ("the total minus the sum of every
     * other line, after every percentage line rounds"; "never itself computed as a percentage").
     *
     * <p>Pure fact-gathering and assembly: every branch on a business code lives in
     * {@code EventTypeRuleDomain} (the account derivation, the amount source, the remainder
     * difference) or in {@code JournalEntryDomain} (which side an amount lands on), and an account
     * code that resolves to nothing is the catalog's own {@code FIN-404-ACCOUNT}. Rule-driven lines
     * carry no dimension tags — ENT-FIN-010 declares no dimension column (DBF-FIN-098..108), so
     * RULE-FIN-009 has nothing to judge on this path.
     *
     * <p><b>Per side, and one marker (ALIGN-BE).</b> The running totals are kept per posting side
     * ({@code JournalEntryDomain.SideTotals}) and the remainder line takes the difference between
     * its own side and the opposing one, which is what RULE-FIN-010 means by "the balancing
     * amount": with base 1000 and lines DEBIT/FIELD 1000, CREDIT/PERCENTAGE 60% and
     * CREDIT/REMAINDER, the remainder is {@code 1000 − 600 = 400}, not the {@code 1000 − 1600 =
     * −600} a side-blind total produced. Which line is the remainder is read from
     * {@code isRemainderFl} alone (DBF-FIN-103) through
     * {@code EventTypeRuleDomain.isRemainderLine(...)} — the same marker RULE-FIN-003's guard
     * counts, so guard and builder can no longer disagree about it.
     */
    private List<BuiltLine> buildLines(List<RuleLine> ruleLines,
                                       EventEntryBuildRequest request) {
        List<BuiltLine> built = new ArrayList<>(ruleLines.size());
        List<RuleLine> remainderLines = new ArrayList<>();
        JournalEntryDomain.SideTotals totals = JournalEntryDomain.SideTotals.zero();

        for (RuleLine ruleLine : ruleLines) {
            if (EventTypeRuleDomain.isRemainderLine(ruleLine.getIsRemainderFl())) {
                remainderLines.add(ruleLine);
                continue;
            }
            BigDecimal amount = EventTypeRuleDomain.sourcedAmount(
                ruleLine.getAmountSourceTypeCode(), ruleLine.getAmountSourceValue(),
                request.getBaseAmount(), request.getAmounts());
            built.add(toBuiltLine(ruleLine, amount, request));
            totals = totals.plus(ruleLine.getDirectionCode(), amount);
        }

        for (RuleLine remainderLine : remainderLines) {
            String direction = remainderLine.getDirectionCode();
            BigDecimal remainder = EventTypeRuleDomain.remainderAmount(
                totals.opposingTotalOf(direction), totals.ownTotalOf(direction));
            built.add(toBuiltLine(remainderLine, remainder, request));
            totals = totals.plus(direction, remainder);
        }
        return built;
    }

    private BuiltLine toBuiltLine(RuleLine ruleLine, BigDecimal amount,
                                  EventEntryBuildRequest request) {
        String accountCode = EventTypeRuleDomain.resolveAccountCode(
            ruleLine.getAccountDerivationTypeCode(), ruleLine.getAccountDerivationValue(),
            request.getFields());
        return new BuiltLine(resolveAccountByCode(accountCode), amount,
            ruleLine.getDirectionCode(),
            EventTypeRuleDomain.isRemainderLine(ruleLine.getIsRemainderFl()),
            request.getDescriptionAr(), request.getDescriptionEn(), List.of());
    }

    /**
     * FK resolution by natural key, not a rule: a derived account code that matches no account is
     * {@code FIN-404-ACCOUNT}. Read through the existing repository layer with no new query
     * method — an explicit {@code Specification} plus the fluent {@code first()}, the same
     * A.5.17-sanctioned technique the posting pipeline uses for its own scoped reads.
     */
    private Account resolveAccountByCode(String accountCode) {
        Specification<Account> ofCode = (root, query, cb) -> cb.equal(root.get("code"),
            accountCode);

        return accountRepository.findBy(ofCode, fluent -> fluent.first())
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, FinErrorCodes.FIN_404_ACCOUNT, accountCode));
    }
}
