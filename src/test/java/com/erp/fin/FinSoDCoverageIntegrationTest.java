package com.erp.fin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.fin.domain.JournalEntryDomain;
import com.erp.fin.dto.YearEndCloseResponse;
import com.erp.fin.entity.Account;
import com.erp.fin.entity.FiscalPeriod;
import com.erp.fin.entity.FiscalYear;
import com.erp.fin.exception.FinErrorCodes;
import com.erp.fin.repository.AccountRepository;
import com.erp.fin.repository.FiscalPeriodRepository;
import com.erp.fin.repository.FiscalYearRepository;
import com.erp.fin.service.FiscalPeriodService;
import com.erp.fin.service.FiscalYearService;
import com.erp.main.ErpMainApplication;
import com.erp.sec.permission.PermissionConstants;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coverage for the three FIN test-plan cases — TC-FIN-038, TC-FIN-060 and TC-FIN-061
 * ({@code governance/modules/FIN/packages/backend-test/RULE-SCENARIOS.md}) — that no HTTP-level
 * run can exercise, because each one needs a principal holding ONE FIN permission but not another
 * and the {@code api-verify} skill may not create roles or users to build one. An in-process JUnit
 * test sets the {@link SecurityContextHolder} directly, so the exact authority set each case names
 * is constructible here and nowhere else.
 *
 * <p>All three are AC-FIN-038 / RULE-FIN-015: the close-approval action is gated by a permission
 * distinct from the journal-entry-creation permission, and by nothing else. The denial therefore
 * comes from {@code @PreAuthorize(PERM_FIN_PERIODS_CLOSE_APPROVE)} on
 * {@code FiscalPeriodService.hardClose} / {@code FiscalYearService.yearEndClose}, and reaches the
 * caller as {@link LocalizedException} carrying {@code FIN-403-FORBIDDEN} because
 * {@code FinForbiddenAdvisor} re-raises every {@code AccessDeniedException} thrown inside
 * {@code com.erp.fin.service} as that catalog code. {@code FIN-403-SOD-VIOLATION} is never the
 * answer — {@code FinSeparationOfDutiesService} and
 * {@code FiscalPeriodDomain.assertCanHardClose(boolean, boolean)} were both deleted by a recorded
 * human decision, so the code has no throw site left anywhere in {@code com.erp.fin}. There is no
 * HTTP layer in this suite, so nothing here asserts a status line; the exception and the persisted
 * state are the observable outcome.
 *
 * <p>Runs against the real dev Postgres/Redis (docker/docker-compose.yml, {@code dev} profile) the
 * same way the running application does; every test wraps its writes in the outer class-level
 * {@link Transactional} and rolls back on completion, so nothing here leaves data behind.
 */
@SpringBootTest(classes = ErpMainApplication.class)
@ActiveProfiles("dev")
@Transactional
class FinSoDCoverageIntegrationTest {

    /**
     * Far enough ahead of every fiscal year the dev database carries that the fixture's two years
     * and their successor-by-adjacency lookup cannot collide with existing rows.
     */
    private static final LocalDate FIXTURE_YEAR_START = LocalDate.of(2190, 1, 1);

    @Autowired
    private FiscalPeriodService fiscalPeriodService;
    @Autowired
    private FiscalYearService fiscalYearService;

    @Autowired
    private FiscalYearRepository fiscalYearRepository;
    @Autowired
    private FiscalPeriodRepository fiscalPeriodRepository;
    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // -----------------------------------------------------------------------------------------
    // TC-FIN-038 — close-approval permission distinct from entry-creation permission (SoD)
    // -----------------------------------------------------------------------------------------

    @Test
    void hardClose_isDeniedForAPrincipalHoldingOnlyTheEntryCreationPermission() {
        // Covers: TC-FIN-038, AC-FIN-038
        //
        // "A role holding only PERM_FIN_JOURNAL_ENTRIES_CREATE (no close-approve permission)" is
        // exactly the principal no api-verify HTTP client may construct. Built here as the literal
        // authority set, so the ONLY thing that can account for the refusal is the missing
        // PERM_FIN_PERIODS_CLOSE_APPROVE on FiscalPeriodService.hardClose.
        FiscalYear year = persistYear(FIXTURE_YEAR_START, FIXTURE_YEAR_START.plusYears(1).minusDays(1));
        FiscalPeriod period = persistPeriod(year, 1, FiscalPeriod.STATUS_OPEN);

        setAuthenticatedPrincipal("tc-fin-038-" + uniqueSuffix(),
            PermissionConstants.PERM_FIN_JOURNAL_ENTRIES_CREATE);

        Throwable thrown =
            catchThrowable(() -> fiscalPeriodService.hardClose(period.getFiscalPeriodPk()));

        assertThat(thrown).isInstanceOf(LocalizedException.class);
        LocalizedException denial = (LocalizedException) thrown;
        assertThat(denial.getErrorCode()).isEqualTo(FinErrorCodes.FIN_403_FORBIDDEN);
        assertThat(denial.getStatus()).isEqualTo(Status.FORBIDDEN);

        // "the period is unchanged" — asserted, not assumed.
        FiscalPeriod reloaded =
            fiscalPeriodRepository.findById(period.getFiscalPeriodPk()).orElseThrow();
        assertThat(reloaded.getStatusCode()).isEqualTo(FiscalPeriod.STATUS_OPEN);
        assertThat(reloaded.getClosedBy()).isNull();
        assertThat(reloaded.getClosedAt()).isNull();
    }

    // -----------------------------------------------------------------------------------------
    // TC-FIN-060 — the same AC-FIN-038 denial on year-end close (API-FIN-027)
    // -----------------------------------------------------------------------------------------

    @Test
    void yearEndClose_isDeniedForAPrincipalHoldingOnlyTheEntryCreationPermission() {
        // Covers: TC-FIN-060, AC-FIN-038
        //
        // The year is built FULLY ELIGIBLE on purpose — every period HARD_CLOSE, a Retained
        // Earnings account marked, an adjacent successor year — so that nothing but the missing
        // permission can account for the refusal. A refusal against an ineligible year would be
        // indistinguishable from FIN-409-PERIODS-NOT-CLOSED / FIN-404-YEAR / FIN-404-ACCOUNT and
        // would prove nothing.
        YearEndFixture fixture = eligibleYearEndFixture();

        setAuthenticatedPrincipal("tc-fin-060-" + uniqueSuffix(),
            PermissionConstants.PERM_FIN_JOURNAL_ENTRIES_CREATE);

        Throwable thrown = catchThrowable(
            () -> fiscalYearService.yearEndClose(fixture.year().getFiscalYearPk()));

        assertThat(thrown).isInstanceOf(LocalizedException.class);
        LocalizedException denial = (LocalizedException) thrown;
        assertThat(denial.getErrorCode()).isEqualTo(FinErrorCodes.FIN_403_FORBIDDEN);
        assertThat(denial.getStatus()).isEqualTo(Status.FORBIDDEN);

        // "the year stays OPEN and every period keeps its status".
        FiscalYear reloadedYear =
            fiscalYearRepository.findById(fixture.year().getFiscalYearPk()).orElseThrow();
        assertThat(reloadedYear.getStatusCode()).isEqualTo(FiscalYear.STATUS_OPEN);
        assertThat(fiscalPeriodRepository.findByFiscalYearId(reloadedYear.getFiscalYearPk()))
            .isNotEmpty()
            .allSatisfy(period ->
                assertThat(period.getStatusCode()).isEqualTo(FiscalPeriod.STATUS_HARD_CLOSE));
    }

    // -----------------------------------------------------------------------------------------
    // TC-FIN-061 — a principal holding BOTH permissions CAN close (the satisfied direction)
    // -----------------------------------------------------------------------------------------

    @Test
    void yearEndClose_succeedsForAPrincipalHoldingBothPermissions() {
        // Covers: TC-FIN-061, REQ-FIN-038 (satisfied direction)
        //
        // The post-V30 `admin`/SYS_ADMIN state: ONE principal holding BOTH
        // PERM_FIN_JOURNAL_ENTRIES_CREATE and PERM_FIN_PERIODS_CLOSE_APPROVE — the exact overlap
        // the deleted global user-set disjointness check used to refuse. Same fully-eligible
        // fixture as TC-FIN-060, so the ONLY difference between the two tests is the authority set.
        YearEndFixture fixture = eligibleYearEndFixture();

        setAuthenticatedPrincipal("tc-fin-061-" + uniqueSuffix(),
            PermissionConstants.PERM_FIN_JOURNAL_ENTRIES_CREATE,
            PermissionConstants.PERM_FIN_PERIODS_CLOSE_APPROVE);

        AtomicReference<ServiceResult<YearEndCloseResponse>> result = new AtomicReference<>();
        Throwable thrown = catchThrowable(() ->
            result.set(fiscalYearService.yearEndClose(fixture.year().getFiscalYearPk())));

        // "Assert EXPLICITLY that the response is not a 403 and carries no FIN-403-SOD-VIOLATION":
        // the call must complete without any LocalizedException at all — no FORBIDDEN status, and
        // therefore neither FIN-403-FORBIDDEN nor the (throw-site-less) FIN-403-SOD-VIOLATION.
        assertThat(thrown)
            .as("year-end close must not be denied for a principal holding BOTH permissions — "
                + "RULE-FIN-015 is satisfied by the distinct permission, not by disjoint user sets")
            .isNull();

        ServiceResult<YearEndCloseResponse> closeResult = result.get();
        assertThat(closeResult).isNotNull();
        assertThat(closeResult.getStatus()).isEqualTo(Status.CREATED);

        YearEndCloseResponse response = closeResult.getData();
        assertThat(response).isNotNull();
        assertThat(response.getClosingEntry()).isNotNull();
        assertThat(response.getClosingEntry().getJournalTypeCode())
            .isEqualTo(JournalEntryDomain.JOURNAL_TYPE_CLOSING);
        assertThat(response.getOpeningEntry()).isNotNull();
        assertThat(response.getOpeningEntry().getJournalTypeCode())
            .isEqualTo(JournalEntryDomain.JOURNAL_TYPE_OPENING);

        FiscalYear reloadedYear =
            fiscalYearRepository.findById(fixture.year().getFiscalYearPk()).orElseThrow();
        assertThat(reloadedYear.getStatusCode()).isEqualTo(FiscalYear.STATUS_CLOSED);
        assertThat(fiscalPeriodRepository.findByFiscalYearId(reloadedYear.getFiscalYearPk()))
            .isNotEmpty()
            .allSatisfy(period ->
                assertThat(period.getStatusCode()).isEqualTo(FiscalPeriod.STATUS_YEAR_END_CLOSE));
    }

    // -----------------------------------------------------------------------------------------
    // Fixture helpers
    // -----------------------------------------------------------------------------------------

    /**
     * The one year-end fixture TC-FIN-060 and TC-FIN-061 share: a fiscal year every one of whose
     * periods is HARD_CLOSE, an account carrying {@code IS_RETAINED_EARNINGS_FL}, and an adjacent
     * successor year (its {@code startDate} the day after this year's {@code endDate}) holding a
     * period for the opening entry. Every precondition {@code FiscalYearService.yearEndClose}
     * checks is therefore satisfied before either test calls it.
     */
    private YearEndFixture eligibleYearEndFixture() {
        LocalDate yearStart = FIXTURE_YEAR_START;
        LocalDate yearEnd = yearStart.plusYears(1).minusDays(1);

        FiscalYear year = persistYear(yearStart, yearEnd);
        List<FiscalPeriod> periods = List.of(
            persistPeriod(year, 1, FiscalPeriod.STATUS_HARD_CLOSE),
            persistPeriod(year, 2, FiscalPeriod.STATUS_HARD_CLOSE));

        LocalDate successorStart = yearEnd.plusDays(1);
        FiscalYear successor =
            persistYear(successorStart, successorStart.plusYears(1).minusDays(1));
        persistPeriod(successor, 1, FiscalPeriod.STATUS_OPEN);

        retainedEarningsAccount();

        return new YearEndFixture(year, successor, periods);
    }

    private FiscalYear persistYear(LocalDate startDate, LocalDate endDate) {
        return fiscalYearRepository.save(FiscalYear.builder()
            .code("Y" + uniqueSuffix())
            .startDate(startDate)
            .endDate(endDate)
            .statusCode(FiscalYear.STATUS_OPEN)
            .build());
    }

    private FiscalPeriod persistPeriod(FiscalYear year, int periodNo, String statusCode) {
        LocalDate start = year.getStartDate().plusMonths(periodNo - 1L);
        return fiscalPeriodRepository.save(FiscalPeriod.builder()
            .fiscalYear(year)
            .periodNo(periodNo)
            .nameAr("فترة اختبار " + periodNo)
            .nameEn("Test period " + periodNo)
            .startDate(start)
            .endDate(start.plusMonths(1).minusDays(1))
            .statusCode(statusCode)
            .build());
    }

    /**
     * {@code UQ_FIN_ACCOUNT_RETAINED_EARNINGS} is a partial unique index over the marked rows, so
     * at most one account may carry the flag: reuse the marked account when the database already
     * has one, and mark a new EQUITY account only when it does not.
     */
    private Account retainedEarningsAccount() {
        return accountRepository.findFirstByIsRetainedEarningsFlTrueOrderByAccountPkAsc()
            .orElseGet(() -> accountRepository.save(Account.builder()
                .code("RE" + uniqueSuffix())
                .nameAr("الأرباح المحتجزة")
                .nameEn("Retained earnings")
                // ACCOUNT_TYPE / DEBIT_CREDIT values seeded by
                // V26__fin_mdl_lookup_seed.sql. AccountDomain publishes constants only for the
                // two RESULT types (REVENUE/EXPENSE) it classifies, so EQUITY is the seeded code
                // itself; the nature reuses the shared DEBIT_CREDIT lookup constant.
                .accountTypeCode("EQUITY")
                .natureCode(JournalEntryDomain.DIRECTION_CREDIT)
                .isLeafFl(Boolean.TRUE)
                .isActiveFl(Boolean.TRUE)
                .isRetainedEarningsFl(Boolean.TRUE)
                .build()));
    }

    private void setAuthenticatedPrincipal(String username, String... authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities =
            Stream.of(authorities).map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(username, "N/A", grantedAuthorities));
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /** The fully-eligible year-end setup TC-FIN-060 and TC-FIN-061 both run against. */
    private record YearEndFixture(FiscalYear year, FiscalYear successor,
                                  List<FiscalPeriod> periods) {
    }
}
