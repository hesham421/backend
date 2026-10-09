package com.erp.fin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.fin.FinYearEndFixtures.YearEndFixture;
import com.erp.fin.domain.AccountDomain;
import com.erp.fin.domain.JournalEntryDomain;
import com.erp.fin.dto.AccountBalanceGroupResponse;
import com.erp.fin.dto.AccountBalanceRowResponse;
import com.erp.fin.dto.BalanceSheetResponse;
import com.erp.fin.dto.IncomeStatementResponse;
import com.erp.fin.dto.JournalEntryCreateRequest;
import com.erp.fin.dto.JournalEntryResponse;
import com.erp.fin.dto.JournalLineCreateRequest;
import com.erp.fin.dto.JournalLineDimensionCreateRequest;
import com.erp.fin.dto.TrialBalanceResponse;
import com.erp.fin.exception.FinErrorCodes;
import com.erp.fin.entity.Account;
import com.erp.fin.entity.Dimension;
import com.erp.fin.entity.DimensionValue;
import com.erp.fin.entity.FiscalPeriod;
import com.erp.fin.entity.JournalEntry;
import com.erp.fin.repository.AccountBalanceView;
import com.erp.fin.repository.AccountLedgerLineView;
import com.erp.fin.repository.AccountRepository;
import com.erp.fin.repository.DimensionBalanceView;
import com.erp.fin.repository.DimensionRepository;
import com.erp.fin.repository.DimensionValueRepository;
import com.erp.fin.repository.FiscalPeriodRepository;
import com.erp.fin.repository.FiscalYearRepository;
import com.erp.fin.repository.JournalLineDimensionRepository;
import com.erp.fin.repository.JournalLineRepository;
import com.erp.fin.service.JournalEntryService;
import com.erp.fin.service.ReportService;
import com.erp.main.ErpMainApplication;
import com.erp.sec.permission.PermissionConstants;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * QR-FIN-042 / QR-FIN-043 / QR-FIN-044 exercised WITH their optional filters supplied — the case
 * API-FIN-028 / 030 / 031 returned 500 INTERNAL_ERROR on, because the untyped
 * {@code (:p IS NULL OR …)} idiom gave PostgreSQL no type to infer for a parameter that only ever
 * appeared against NULL ("could not determine data type of parameter $n").
 *
 * <p>Each filter is asserted three ways: supplied and matching (rows), omitted (the unfiltered
 * result, never an empty one), and supplied but legitimately matching nothing (empty, not an
 * error). Same conventions as {@link FinYearEndCoverageIntegrationTest}: the real dev Postgres
 * under the {@code dev} profile, and a class-level {@link Transactional} that rolls every write
 * back.
 */
@SpringBootTest(classes = ErpMainApplication.class)
@ActiveProfiles("dev")
@Transactional
class FinReportFilterIntegrationTest {

    private static final LocalDate FIXTURE_YEAR_START = LocalDate.of(2043, 1, 1);
    private static final BigDecimal AMOUNT = new BigDecimal("500.00");
    private static final BigDecimal OTHER_BRANCH_AMOUNT = new BigDecimal("300.00");

    @Autowired private AccountRepository accountRepository;
    @Autowired private DimensionRepository dimensionRepository;
    @Autowired private DimensionValueRepository dimensionValueRepository;
    @Autowired private FiscalPeriodRepository fiscalPeriodRepository;
    @Autowired private FiscalYearRepository fiscalYearRepository;
    @Autowired private JournalEntryService journalEntryService;
    @Autowired private JournalLineRepository journalLineRepository;
    @Autowired private JournalLineDimensionRepository journalLineDimensionRepository;
    @Autowired private ReportService reportService;

    private FinYearEndFixtures fixtures;
    private PostedActivity activity;

    @BeforeEach
    void setUp() {
        fixtures = new FinYearEndFixtures(
            fiscalYearRepository, fiscalPeriodRepository, accountRepository);
        activity = postActivity();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ---------------------------------------------------------------------------------------
    // QR-FIN-042 — API-FIN-028's account ledger
    // ---------------------------------------------------------------------------------------

    @Test
    void accountLedgerHonoursItsOptionalDateRangeInsteadOfFailing() {
        List<AccountLedgerLineView> unfiltered = ledger(null, null, null, null);
        assertThat(unfiltered).as("the unfiltered ledger is the baseline").isNotEmpty();

        List<AccountLedgerLineView> filtered =
            ledger(activity.docDate(), activity.docDate(), null, null);

        assertThat(filtered).as("a date range that spans the activity must return rows")
            .isNotEmpty();
        assertThat(filtered).hasSameSizeAs(unfiltered);
    }

    @Test
    void accountLedgerHonoursItsOptionalDimensionFiltersInsteadOfFailing() {
        assertThat(ledger(null, null, activity.dimensionId(), null))
            .as("a dimension filter naming the tagged dimension must return rows").isNotEmpty();
        assertThat(ledger(null, null, null, activity.dimensionValueId()))
            .as("a dimension-value filter naming the tagged value must return rows").isNotEmpty();
        assertThat(ledger(activity.docDate(), activity.docDate(),
            activity.dimensionId(), activity.dimensionValueId()))
            .as("all four filters together must still return rows").isNotEmpty();
    }

    @Test
    void omittingTheLedgerFiltersReturnsTheUnfilteredResultNotAnEmptyOne() {
        assertThat(ledger(null, null, null, null))
            .as("a null bound drops its predicate — it must not match nothing")
            .isNotEmpty();
    }

    @Test
    void aLedgerFilterThatMatchesNothingReturnsAnEmptyResultNotAnError() {
        assertThat(ledger(FIXTURE_YEAR_START.minusYears(5),
            FIXTURE_YEAR_START.minusYears(5), null, null)).isEmpty();
        assertThat(ledger(null, null, activity.dimensionId(), -1L)).isEmpty();
    }

    // ---------------------------------------------------------------------------------------
    // QR-FIN-043 — API-FIN-029 / 030 / 031's shared aggregation
    // ---------------------------------------------------------------------------------------

    @Test
    void balanceAggregationHonoursItsOptionalAsOfDateInsteadOfFailing() {
        // API-FIN-030's balance sheet: fiscalYearId + asOfDate, which maps to (fiscalYearId, toDate).
        List<AccountBalanceView> filtered =
            balances(null, activity.fiscalYearId(), null, activity.docDate());

        assertThat(filtered).as("fiscalYearId + asOfDate must return rows").isNotEmpty();
        assertThat(rowFor(filtered, activity.assetAccount())).isNotNull();
    }

    @Test
    void balanceAggregationHonoursItsOptionalPeriodBoundsInsteadOfFailing() {
        // API-FIN-031's income statement: fiscalYearId + fromPeriodId/toPeriodId, which the
        // service resolves to a docDate range; and API-FIN-029's single periodId.
        assertThat(balances(activity.periodId(), null, null, null))
            .as("a periodId filter must return rows").isNotEmpty();
        assertThat(balances(activity.periodId(), activity.fiscalYearId(),
            activity.periodStart(), activity.periodEnd()))
            .as("every bound supplied at once must still return rows").isNotEmpty();
    }

    @Test
    void omittingTheAggregationFiltersReturnsTheUnfilteredResultNotAnEmptyOne() {
        List<AccountBalanceView> unfiltered = balances(null, null, null, null);
        assertThat(unfiltered).isNotEmpty();
        assertThat(rowFor(unfiltered, activity.assetAccount()))
            .as("the fixture's account is in the unfiltered aggregation").isNotNull();
    }

    @Test
    void anAggregationFilterThatMatchesNothingReturnsAnEmptyResultNotAnError() {
        assertThat(balances(-1L, null, null, null)).isEmpty();
        assertThat(balances(null, null, FIXTURE_YEAR_START.minusYears(5),
            FIXTURE_YEAR_START.minusYears(5))).isEmpty();
    }

    // ---------------------------------------------------------------------------------------
    // API-FIN-029 / 030 / 031 — the branch (dimension) filter on the three statements
    // ---------------------------------------------------------------------------------------

    @Test
    void trialBalanceWithoutADimensionFilterKeepsEveryBranch() {
        postOtherBranchActivity();
        TrialBalanceResponse result = trialBalance(null, null);

        assertThat(debitOf(result.getRows(), activity.assetAccount()))
            .isEqualByComparingTo(AMOUNT.add(OTHER_BRANCH_AMOUNT));
        assertThat(result.getDimensionValueId()).isNull();
        assertThat(result.getBalanced()).isTrue();
    }

    @Test
    void trialBalanceFilteredToOneBranchKeepsOnlyThatBranchsLines() {
        Long otherValue = postOtherBranchActivity();

        TrialBalanceResponse branch = trialBalance(null, activity.dimensionValueId());
        assertThat(debitOf(branch.getRows(), activity.assetAccount())).isEqualByComparingTo(AMOUNT);
        assertThat(branch.getDimensionValueId()).isEqualTo(activity.dimensionValueId());
        assertThat(branch.getBalanced()).as("single-branch entries balance per branch").isTrue();

        TrialBalanceResponse other = trialBalance(activity.dimensionId(), otherValue);
        assertThat(debitOf(other.getRows(), activity.assetAccount()))
            .isEqualByComparingTo(OTHER_BRANCH_AMOUNT);
        assertThat(other.getDimensionId()).isEqualTo(activity.dimensionId());

        TrialBalanceResponse all = trialBalance(null, null);
        assertThat(debitOf(branch.getRows(), activity.assetAccount())
            .add(debitOf(other.getRows(), activity.assetAccount())))
            .as("the branches add up to the unfiltered result")
            .isEqualByComparingTo(debitOf(all.getRows(), activity.assetAccount()));
    }

    @Test
    void trialBalanceRejectsAnUnknownDimension() {
        assertNotFound(() -> trialBalance(-1L, null), FinErrorCodes.FIN_404_DIMENSION);
    }

    @Test
    void balanceSheetWithoutADimensionFilterKeepsEveryBranch() {
        postOtherBranchActivity();
        assertThat(debitOf(groupRows(balanceSheet(null).getGroups()), activity.assetAccount()))
            .isEqualByComparingTo(AMOUNT.add(OTHER_BRANCH_AMOUNT));
    }

    @Test
    void balanceSheetFilteredToOneBranchKeepsOnlyThatBranchsLines() {
        postOtherBranchActivity();
        BalanceSheetResponse branch = balanceSheet(activity.dimensionValueId());

        assertThat(debitOf(groupRows(branch.getGroups()), activity.assetAccount()))
            .isEqualByComparingTo(AMOUNT);
        assertThat(branch.getDimensionValueId()).isEqualTo(activity.dimensionValueId());
    }

    @Test
    void balanceSheetRejectsAnUnknownDimension() {
        FinYearEndFixtures.setAuthenticatedPrincipal(
            "fin-branch-bs", PermissionConstants.PERM_FIN_BALANCE_SHEET_VIEW);
        assertNotFound(() -> reportService.balanceSheet(activity.fiscalYearId(), null, -1L, null),
            FinErrorCodes.FIN_404_DIMENSION);
    }

    @Test
    void incomeStatementWithoutADimensionFilterKeepsEveryBranch() {
        postOtherBranchActivity();
        assertThat(creditOf(groupRows(incomeStatement(null).getGroups()), activity.revenueAccount()))
            .isEqualByComparingTo(AMOUNT.add(OTHER_BRANCH_AMOUNT));
    }

    @Test
    void incomeStatementFilteredToOneBranchKeepsOnlyThatBranchsLines() {
        postOtherBranchActivity();
        IncomeStatementResponse branch = incomeStatement(activity.dimensionValueId());

        assertThat(creditOf(groupRows(branch.getGroups()), activity.revenueAccount()))
            .isEqualByComparingTo(AMOUNT);
        assertThat(branch.getDimensionValueId()).isEqualTo(activity.dimensionValueId());
    }

    @Test
    void incomeStatementRejectsAnUnknownDimension() {
        FinYearEndFixtures.setAuthenticatedPrincipal(
            "fin-branch-is", PermissionConstants.PERM_FIN_INCOME_STATEMENT_VIEW);
        assertNotFound(() -> reportService.incomeStatement(
            activity.fiscalYearId(), null, null, -1L, null), FinErrorCodes.FIN_404_DIMENSION);
    }

    @Test
    void aStatementRejectsAnUnknownValueAndAValueOfAnotherDimension() {
        assertNotFound(() -> trialBalance(null, -1L), FinErrorCodes.FIN_404_DIMVALUE);

        Dimension otherDimension = dimensionRepository.save(Dimension.builder()
            .code("D" + FinYearEndFixtures.uniqueSuffix())
            .nameAr("بُعد آخر").nameEn("Other dimension")
            .isActiveFl(Boolean.TRUE).build());
        LocalizedException thrown = catchThrowableOfType(LocalizedException.class,
            () -> trialBalance(otherDimension.getDimensionPk(), activity.dimensionValueId()));
        assertThat(thrown).isNotNull();
        assertThat(thrown.getErrorCode()).isEqualTo(FinErrorCodes.FIN_409_INVALID_DIMENSION);
    }

    // ---------------------------------------------------------------------------------------
    // QR-FIN-044 — API-FIN-032's dimension report
    // ---------------------------------------------------------------------------------------

    @Test
    void dimensionAggregationHonoursItsOptionalFiltersInsteadOfFailing() {
        List<DimensionBalanceView> unfiltered = dimensionBalances(null, null);
        assertThat(unfiltered).as("the unfiltered dimension report is the baseline").isNotEmpty();

        assertThat(dimensionBalances(activity.dimensionValueId(), null))
            .as("a dimension-value filter must return rows").isNotEmpty();
        assertThat(dimensionBalances(activity.dimensionValueId(), activity.periodId()))
            .as("value + period together must still return rows").isNotEmpty();
        assertThat(dimensionBalances(-1L, null))
            .as("a value that matches nothing is empty, not an error").isEmpty();
    }

    // ---------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------

    private List<AccountLedgerLineView> ledger(LocalDate fromDate, LocalDate toDate,
                                               Long dimensionId, Long dimensionValueId) {
        return journalLineRepository.findAccountLedgerLines(
            activity.assetAccount(), JournalEntry.STATUS_POSTED,
            fromDate, toDate, dimensionId, dimensionValueId);
    }

    private List<AccountBalanceView> balances(Long periodId, Long fiscalYearId,
                                              LocalDate fromDate, LocalDate toDate) {
        return journalLineRepository.aggregateAccountBalances(
            JournalEntry.STATUS_POSTED, JournalEntryDomain.DIRECTION_DEBIT,
            periodId, fiscalYearId, fromDate, toDate, null, null);
    }

    private TrialBalanceResponse trialBalance(Long dimensionId, Long dimensionValueId) {
        FinYearEndFixtures.setAuthenticatedPrincipal(
            "fin-branch-tb", PermissionConstants.PERM_FIN_TRIAL_BALANCE_VIEW);
        return reportService.trialBalance(activity.periodId(), null, dimensionId, dimensionValueId)
            .getData();
    }

    private BalanceSheetResponse balanceSheet(Long dimensionValueId) {
        FinYearEndFixtures.setAuthenticatedPrincipal(
            "fin-branch-bs", PermissionConstants.PERM_FIN_BALANCE_SHEET_VIEW);
        return reportService.balanceSheet(activity.fiscalYearId(), null, null, dimensionValueId)
            .getData();
    }

    private IncomeStatementResponse incomeStatement(Long dimensionValueId) {
        FinYearEndFixtures.setAuthenticatedPrincipal(
            "fin-branch-is", PermissionConstants.PERM_FIN_INCOME_STATEMENT_VIEW);
        return reportService.incomeStatement(
            activity.fiscalYearId(), null, null, null, dimensionValueId).getData();
    }

    private static void assertNotFound(Runnable call, String errorCode) {
        LocalizedException thrown = catchThrowableOfType(LocalizedException.class, call::run);
        assertThat(thrown).as("expected %s", errorCode).isNotNull();
        assertThat(thrown.getStatus()).isEqualTo(Status.NOT_FOUND);
        assertThat(thrown.getErrorCode()).isEqualTo(errorCode);
    }

    private static List<AccountBalanceRowResponse> groupRows(
            List<AccountBalanceGroupResponse> groups) {
        return groups.stream().flatMap(group -> group.getRows().stream()).toList();
    }

    private static BigDecimal debitOf(List<AccountBalanceRowResponse> rows, Long accountId) {
        return rows.stream().filter(row -> accountId.equals(row.getAccountId()))
            .map(AccountBalanceRowResponse::getDebitTotal).findFirst().orElse(BigDecimal.ZERO);
    }

    private static BigDecimal creditOf(List<AccountBalanceRowResponse> rows, Long accountId) {
        return rows.stream().filter(row -> accountId.equals(row.getAccountId()))
            .map(AccountBalanceRowResponse::getCreditTotal).findFirst().orElse(BigDecimal.ZERO);
    }

    private List<DimensionBalanceView> dimensionBalances(Long dimensionValueId, Long periodId) {
        return journalLineDimensionRepository.aggregateByAccountAndDimensionValue(
            JournalEntry.STATUS_POSTED, JournalEntryDomain.DIRECTION_DEBIT,
            activity.dimensionId(), dimensionValueId, periodId);
    }

    private static AccountBalanceView rowFor(List<AccountBalanceView> rows, Long accountId) {
        return rows.stream()
            .filter(row -> accountId.equals(row.getAccountId()))
            .findFirst().orElse(null);
    }

    /** One balanced, POSTED, dimension-tagged entry in an OPEN period — the whole fixture. */
    private PostedActivity postActivity() {
        YearEndFixture fixture = fixtures.openYearEndFixture(FIXTURE_YEAR_START);
        FiscalPeriod period = fixture.periods().get(0);

        Account asset = fixtures.persistAccount("ASSET", JournalEntryDomain.DIRECTION_DEBIT);
        Account revenue = fixtures.persistAccount(
            AccountDomain.ACCOUNT_TYPE_REVENUE, JournalEntryDomain.DIRECTION_CREDIT);

        Dimension dimension = dimensionRepository.save(Dimension.builder()
            .code("D" + FinYearEndFixtures.uniqueSuffix())
            .nameAr("بُعد اختبار").nameEn("Test dimension")
            .isActiveFl(Boolean.TRUE).build());
        DimensionValue value = dimensionValueRepository.save(DimensionValue.builder()
            .dimension(dimension)
            .code("V" + FinYearEndFixtures.uniqueSuffix())
            .nameAr("قيمة اختبار").nameEn("Test value")
            .isActiveFl(Boolean.TRUE).build());

        PostedActivity posted = new PostedActivity(asset.getAccountPk(), revenue.getAccountPk(),
            fixture.year().getFiscalYearPk(),
            period.getFiscalPeriodPk(), period.getStartDate(), period.getEndDate(),
            period.getStartDate(), dimension.getDimensionPk(), value.getDimensionValuePk());
        postTaggedEntry(posted, posted.dimensionValueId(), AMOUNT);
        return posted;
    }

    /**
     * A second branch: another value of the fixture's dimension and one entry, of a different
     * amount, tagged with it — so a filter to the first branch has something to exclude.
     */
    private Long postOtherBranchActivity() {
        DimensionValue other = dimensionValueRepository.save(DimensionValue.builder()
            .dimension(dimensionRepository.getReferenceById(activity.dimensionId()))
            .code("V" + FinYearEndFixtures.uniqueSuffix())
            .nameAr("فرع آخر").nameEn("Other branch")
            .isActiveFl(Boolean.TRUE).build());
        postTaggedEntry(activity, other.getDimensionValuePk(), OTHER_BRANCH_AMOUNT);
        return other.getDimensionValuePk();
    }

    /** One balanced, POSTED manual entry, both lines tagged with {@code dimensionValueId}. */
    private void postTaggedEntry(PostedActivity target, Long dimensionValueId, BigDecimal amount) {
        FinYearEndFixtures.setAuthenticatedPrincipal(
            "fin-report-filter-" + FinYearEndFixtures.uniqueSuffix(), PermissionConstants.PERM_FIN_JOURNAL_ENTRIES_CREATE);

        JournalLineDimensionCreateRequest tag = JournalLineDimensionCreateRequest.builder()
            .dimensionId(target.dimensionId())
            .dimensionValueId(dimensionValueId)
            .build();

        ServiceResult<JournalEntryResponse> posted =
            journalEntryService.createManual(JournalEntryCreateRequest.builder()
                .docDate(target.docDate())
                .fiscalYearId(target.fiscalYearId())
                .periodId(target.periodId())
                .journalTypeCode("MANUAL")
                .descriptionEn("Report filter fixture activity")
                .lines(List.of(
                    JournalLineCreateRequest.builder()
                        .accountId(target.assetAccount()).amount(amount)
                        .directionCode(JournalEntryDomain.DIRECTION_DEBIT)
                        .dimensions(List.of(tag)).build(),
                    JournalLineCreateRequest.builder()
                        .accountId(target.revenueAccount()).amount(amount)
                        .directionCode(JournalEntryDomain.DIRECTION_CREDIT)
                        .dimensions(List.of(tag)).build()))
                .build());

        assertThat(posted.getStatus()).isEqualTo(Status.CREATED);
        SecurityContextHolder.clearContext();
    }

    private record PostedActivity(Long assetAccount, Long revenueAccount, Long fiscalYearId,
                                  Long periodId, LocalDate periodStart, LocalDate periodEnd,
                                  LocalDate docDate, Long dimensionId, Long dimensionValueId) {
    }
}
