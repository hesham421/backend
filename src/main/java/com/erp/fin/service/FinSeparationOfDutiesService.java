package com.erp.fin.service;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.fin.exception.FinErrorCodes;
import com.erp.sec.crossmodule.SecUserDirectoryApi;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RULE-FIN-015 (POL-FIN-016, REQ-FIN-038) — FIN's single consumption point for SEC's user
 * directory, and the only place the separation-of-duties <i>facts</i> are resolved. API-FIN-026
 * (hard-close) and API-FIN-027 (year-end close) both read them from here and both pass them on as
 * plain booleans to {@code FiscalPeriodDomain.assertCanHardClose(...)}, which owns the decision.
 *
 * <p><b>What SEC-BE.md asks for.</b> "FIN's service layer additionally checks at
 * hard-close/year-end-close time that no single <i>user</i> holds both
 * {@code PERM_FIN_PERIODS_CLOSE_APPROVE} and {@code PERM_FIN_JOURNAL_ENTRIES_CREATE}, per role
 * union." SEC's interceptor alone proves only "this caller holds permission X", never "no user
 * holding X also holds Y" (CORE.md "Cross-cutting authorization"), so the union has to be read
 * explicitly. {@code SecUserDirectoryApi.findUserIdsHoldingPermission(String)} returns exactly
 * that: the user ids currently holding a permission through an active role (REQ-SEC-035,
 * QR-SEC-039). The two lists are compared for any intersection.
 *
 * <p><b>In-process, never HTTP.</b> SEC's published cross-module interface is injected directly,
 * exactly as CORE.md's "Cross-module contract placement" requires and as
 * {@link FinLookupValidationService} already does for MDL. Only that interface is touched — never
 * a SEC {@code @Service}, {@code Repository} or {@code @Entity}. The read is pure, so the call
 * site declares {@code @Transactional(readOnly = true)} rather than relying on the default.
 *
 * <p><b>Why the service and not a domain class.</b> A.0.6 forbids a Domain object importing or
 * calling another module; {@code FiscalPeriodDomain}'s own javadoc records that its two SoD
 * arguments are resolved by the service and passed in. This class holds the SEC reference so no
 * domain class, mapper or controller ever has to.
 *
 * <p><b>Failure posture.</b> SEC's own {@code LocalizedException} is caught here rather than
 * allowed to surface as an unhandled 500, and is translated into FIN's own
 * {@code FIN-403-SOD-VIOLATION}: when the separation cannot be proved, the close is denied. It is
 * never swallowed — the original is logged.
 *
 * <p>No caching annotations: FIN's approved cache register is empty, and an authorization fact is
 * exactly the kind of state that must never be served stale.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FinSeparationOfDutiesService {

    /**
     * The two permission codes RULE-FIN-015 separates, as plain literals.
     *
     * <p>They carry the same values {@code com.erp.sec.permission.PermissionConstants} declares
     * (appended there by this sub, verbatim from SEC-BE.md's matrix), but that class is
     * deliberately NOT imported: {@code CrossModuleBoundaryArchTest} allows a caller module to
     * reach {@code PermissionConstants} only through a {@code @PreAuthorize} SpEL
     * {@code T(...)} reference — an exact, documented exception — while a compile-time import
     * would be a real structural dependency on a SEC internal package and would fail that rule.
     * These are arguments to a cross-module read, not a security gate, so the literal is the
     * sanctioned form here.
     */
    private static final String PERM_FIN_PERIODS_CLOSE_APPROVE = "PERM_FIN_PERIODS_CLOSE_APPROVE";

    /** See {@link #PERM_FIN_PERIODS_CLOSE_APPROVE}. */
    private static final String PERM_FIN_JOURNAL_ENTRIES_CREATE = "PERM_FIN_JOURNAL_ENTRIES_CREATE";

    private final SecUserDirectoryApi secUserDirectoryApi;

    /**
     * The two facts {@code FiscalPeriodDomain.assertCanHardClose(...)} decides over.
     *
     * @param closeApprovePermissionHeld whether the close-approval permission is held at all,
     *                                   per SEC's directory
     * @param entryCreatePermissionShared whether any single user holds both the close-approval and
     *                                   the entry-creation permission
     */
    public record SeparationOfDutiesFacts(boolean closeApprovePermissionHeld,
                                          boolean entryCreatePermissionShared) {
    }

    /**
     * Reads both permissions' user sets from SEC and reports whether they meet.
     *
     * <p>{@code @PreAuthorize("isAuthenticated()")} — this service fronts no endpoint of its own
     * and is only ever reached from a FIN service whose own {@code @PreAuthorize} has already
     * enforced {@code PERM_FIN_PERIODS_CLOSE_APPROVE}. Same spec-sanctioned form
     * {@link FinLookupValidationService} uses, never an unguarded public method (A.5.2).
     */
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public SeparationOfDutiesFacts resolveFacts() {
        log.debug("Resolving RULE-FIN-015 separation-of-duties facts from SEC");

        try {
            List<Long> approvers = secUserDirectoryApi.findUserIdsHoldingPermission(
                PERM_FIN_PERIODS_CLOSE_APPROVE);
            List<Long> creators = secUserDirectoryApi.findUserIdsHoldingPermission(
                PERM_FIN_JOURNAL_ENTRIES_CREATE);

            return new SeparationOfDutiesFacts(!approvers.isEmpty(),
                !Collections.disjoint(approvers, creators));
        } catch (LocalizedException ex) {
            log.warn("SEC user-directory read failed while evaluating RULE-FIN-015: {}",
                ex.getMessage());
            throw new LocalizedException(Status.FORBIDDEN, FinErrorCodes.FIN_403_SOD_VIOLATION,
                PERM_FIN_PERIODS_CLOSE_APPROVE);
        }
    }
}
