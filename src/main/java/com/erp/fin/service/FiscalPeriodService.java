package com.erp.fin.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.util.SecurityContextHelper;
import com.erp.fin.domain.FiscalPeriodDomain;
import com.erp.fin.dto.FiscalPeriodResponse;
import com.erp.fin.entity.FiscalPeriod;
import com.erp.fin.exception.FinErrorCodes;
import com.erp.fin.mapper.FiscalPeriodMapper;
import com.erp.fin.repository.FiscalPeriodRepository;
import com.erp.fin.service.FinSeparationOfDutiesService.SeparationOfDutiesFacts;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration layer for ENT-FIN-008's three guarded status transitions (QR-FIN-039) —
 * API-FIN-024 (open), API-FIN-025 (soft-close) and API-FIN-026 (hard-close), SVC-API-INT.md.
 * SRS A7's PERIOD_STATE machine is a plain guarded transition, never a workflow definition
 * (CORE.md "Workflow engine": forbidden).
 *
 * <p>Every decision belongs to {@link FiscalPeriodDomain}; each method here only loads the row,
 * gathers whatever facts the guard needs, calls it, then calls the entity's own plain mutator and
 * saves — no business-rule {@code if} appears in this class (A.5.18).
 *
 * <p>No caching annotations — FIN's approved cache register is empty. No
 * {@code ALLOWED_SORT_FIELDS}: period search belongs to SVC-API-SEARCH.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FiscalPeriodService {

    private final FiscalPeriodRepository repository;
    private final FiscalPeriodMapper mapper;
    private final FinSeparationOfDutiesService separationOfDuties;

    /**
     * API-FIN-024 — load (QR-FIN-039) → check RULE-FIN-014 (QR-FIN-040) → transition → return.
     * Fail-fast: the endpoint carries a single decision, so there is nothing to aggregate.
     *
     * <p>{@code FiscalPeriodDomain.assertCanReopen()} is RULE-FIN-014's single implementation and
     * rejects exactly the two terminal states SRS A7 declares ({@code HARD_CLOSE},
     * {@code YEAR_END_CLOSE}) with the catalog's own {@code FIN-409-NOT-REOPENABLE}. Reopening a
     * period that is already OPEN is therefore idempotent rather than an error — the Error Catalog
     * registers no code for it, and API-FIN-024's Errors line names only
     * {@code FIN-409-NOT-REOPENABLE} and {@code FIN-404-PERIOD}.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants)"
        + ".PERM_FIN_PERIODS_UPDATE)")
    public ServiceResult<FiscalPeriodResponse> open(Long id) {
        log.info("Opening FiscalPeriod ID: {}", id);

        FiscalPeriod period = findOrThrow(id);
        FiscalPeriodDomain.from(period).assertCanReopen();
        period.open();

        return ServiceResult.success(mapper.toResponse(repository.save(period)), Status.UPDATED);
    }

    /**
     * API-FIN-025 — load (QR-FIN-039) → check the transition → soft-close → return. Fail-fast, for
     * the same reason as {@link #open(Long)}. The "current state must be OPEN" requirement is
     * {@code FiscalPeriodDomain.assertCanSoftClose()} ({@code FIN-409-INVALID-TRANSITION}), added
     * by this sub because the guard did not exist and an {@code if} here would be an A.5.18
     * violation.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants)"
        + ".PERM_FIN_PERIODS_UPDATE)")
    public ServiceResult<FiscalPeriodResponse> softClose(Long id) {
        log.info("Soft-closing FiscalPeriod ID: {}", id);

        FiscalPeriod period = findOrThrow(id);
        FiscalPeriodDomain.from(period).assertCanSoftClose();
        period.softClose();

        return ServiceResult.success(mapper.toResponse(repository.save(period)), Status.UPDATED);
    }

    /**
     * API-FIN-026 — load (QR-FIN-039) → check RULE-FIN-014 (QR-FIN-040) → check RULE-FIN-015 (the
     * SEC role-union read) → transition, recording {@code closedBy}/{@code closedAt} as the
     * approving principal and moment (REQ-FIN-037, DBF-FIN-083/084) → return.
     *
     * <p>Fail-fast, in the spec's own order: RULE-FIN-014 first, then RULE-FIN-015. The two are
     * independent denials with different HTTP semantics (409 versus 403) and the aggregating
     * envelope carries one {@code Status}, so combining them would have to mis-state one of them.
     *
     * <p><b>RULE-FIN-014 reuses {@code assertCanReopen()} deliberately:</b> SVC-API-INT.md directs
     * API-FIN-026 to raise {@code FIN-409-NOT-REOPENABLE} ("reused message context") when the
     * period is already Hard Closed, and that method is the rule's single implementation. Adding a
     * second guard with the same condition and the same code would duplicate the rule.
     *
     * <p><b>RULE-FIN-015</b> facts come from {@link FinSeparationOfDutiesService} — the only class
     * in FIN holding a SEC reference — and are handed to
     * {@code FiscalPeriodDomain.assertCanHardClose(...)} as two plain booleans, so the domain
     * class stays free of SEC (A.0.6).
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants)"
        + ".PERM_FIN_PERIODS_CLOSE_APPROVE)")
    public ServiceResult<FiscalPeriodResponse> hardClose(Long id) {
        log.info("Hard-closing FiscalPeriod ID: {}", id);

        FiscalPeriod period = findOrThrow(id);
        FiscalPeriodDomain domain = FiscalPeriodDomain.from(period);
        domain.assertCanReopen();

        SeparationOfDutiesFacts facts = separationOfDuties.resolveFacts();
        domain.assertCanHardClose(facts.closeApprovePermissionHeld(),
            facts.entryCreatePermissionShared());

        period.hardClose(SecurityContextHelper.getCurrentUsername(), Instant.now());

        return ServiceResult.success(mapper.toResponse(repository.save(period)), Status.UPDATED);
    }

    /** FK resolution, not a rule: an unknown period id is {@code FIN-404-PERIOD}. */
    private FiscalPeriod findOrThrow(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, FinErrorCodes.FIN_404_PERIOD, id));
    }
}
