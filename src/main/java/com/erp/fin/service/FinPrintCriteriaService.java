package com.erp.fin.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.fin.dto.FinPrintCriteria;
import com.erp.fin.entity.Account;
import com.erp.fin.entity.Dimension;
import com.erp.fin.entity.DimensionValue;
import com.erp.fin.entity.FiscalPeriod;
import com.erp.fin.entity.FiscalYear;
import com.erp.fin.exception.FinErrorCodes;
import com.erp.fin.repository.AccountRepository;
import com.erp.fin.repository.DimensionRepository;
import com.erp.fin.repository.DimensionValueRepository;
import com.erp.fin.repository.FiscalPeriodRepository;
import com.erp.fin.repository.FiscalYearRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The printed FIN reports' gate: the same permission and the same criteria validation (same error codes, same
 * order) as the matching {@link ReportService} method, before the report's template reads the posted lines
 * with its own SQL. Also resolves the names the printout states. Reads nothing else, caches nothing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FinPrintCriteriaService {

    private final AccountRepository accountRepository;
    private final DimensionRepository dimensionRepository;
    private final DimensionValueRepository dimensionValueRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final FiscalYearRepository fiscalYearRepository;

    /** As {@link ReportService#trialBalance}. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants)"
        + ".PERM_FIN_TRIAL_BALANCE_VIEW)")
    public ServiceResult<FinPrintCriteria> trialBalance(Long periodId, Long dimensionId, Long dimensionValueId) {
        log.debug("Validating trial balance print criteria for Period ID: {}", periodId);
        FiscalPeriod period = periodId == null ? null : periodOf(periodId);
        FinPrintCriteria.FinPrintCriteriaBuilder criteria = dimensionFilter(dimensionId, dimensionValueId);
        if (period != null) {
            criteria.periodNameAr(period.getNameAr()).periodNameEn(period.getNameEn());
        }
        return ServiceResult.success(criteria.build());
    }

    /** As {@link ReportService#balanceSheet}. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants)"
        + ".PERM_FIN_BALANCE_SHEET_VIEW)")
    public ServiceResult<FinPrintCriteria> balanceSheet(Long fiscalYearId, Long dimensionId, Long dimensionValueId) {
        log.debug("Validating balance sheet print criteria for FiscalYear ID: {}", fiscalYearId);
        FiscalYear year = yearOf(fiscalYearId);
        return ServiceResult.success(dimensionFilter(dimensionId, dimensionValueId)
            .fiscalYearCode(year.getCode()).build());
    }

    /** As {@link ReportService#incomeStatement}: the period range becomes the dates its periods span. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants)"
        + ".PERM_FIN_INCOME_STATEMENT_VIEW)")
    public ServiceResult<FinPrintCriteria> incomeStatement(Long fiscalYearId, Long fromPeriodId, Long toPeriodId,
                                                           Long dimensionId, Long dimensionValueId) {
        log.debug("Validating income statement print criteria for FiscalYear ID: {}", fiscalYearId);
        FiscalYear year = yearOf(fiscalYearId);
        FinPrintCriteria.FinPrintCriteriaBuilder criteria = dimensionFilter(dimensionId, dimensionValueId);
        LocalDate fromDate = fromPeriodId == null ? null : periodOf(fromPeriodId).getStartDate();
        LocalDate toDate = toPeriodId == null ? null : periodOf(toPeriodId).getEndDate();
        return ServiceResult.success(criteria.fiscalYearCode(year.getCode()).fromDate(fromDate).toDate(toDate).build());
    }

    /** As {@link ReportService#accountLedger}. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants)"
        + ".PERM_FIN_ACCOUNT_LEDGER_VIEW)")
    public ServiceResult<FinPrintCriteria> accountLedger(Long accountId, Long dimensionId, Long dimensionValueId) {
        log.debug("Validating account ledger print criteria for Account ID: {}", accountId);
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, FinErrorCodes.FIN_404_ACCOUNT, accountId));
        FinPrintCriteria.FinPrintCriteriaBuilder criteria = FinPrintCriteria.builder();
        names(criteria, dimensionId, dimensionValueId);
        return ServiceResult.success(criteria.accountCode(account.getCode()).accountNameAr(account.getNameAr())
            .accountNameEn(account.getNameEn()).accountNatureCode(account.getNatureCode()).build());
    }

    /** As {@link ReportService#dimensionReport}: only the dimension must exist; the value and period narrow. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants)"
        + ".PERM_FIN_DIMENSION_REPORTS_VIEW)")
    public ServiceResult<FinPrintCriteria> dimensionReport(Long dimensionId, Long dimensionValueId, Long periodId) {
        log.debug("Validating dimension report print criteria for Dimension ID: {}", dimensionId);
        if (!dimensionRepository.existsById(dimensionId)) {
            throw new LocalizedException(Status.NOT_FOUND, FinErrorCodes.FIN_404_DIMENSION, dimensionId);
        }
        FinPrintCriteria.FinPrintCriteriaBuilder criteria = FinPrintCriteria.builder();
        names(criteria, dimensionId, dimensionValueId);
        if (periodId != null) {
            fiscalPeriodRepository.findById(periodId)
                .ifPresent(period -> criteria.periodNameAr(period.getNameAr()).periodNameEn(period.getNameEn()));
        }
        return ServiceResult.success(criteria.build());
    }

    /**
     * The names a printed journal states for its year and period filters. The entries themselves come from
     * {@link JournalEntryService#search}, which validates the filters; an unknown id here simply names nothing.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants)"
        + ".PERM_FIN_JOURNAL_ENTRIES_VIEW)")
    public ServiceResult<FinPrintCriteria> journalEntries(Long fiscalYearId, Long periodId) {
        log.debug("Resolving journal print criteria for FiscalYear ID: {}, Period ID: {}", fiscalYearId, periodId);
        FinPrintCriteria.FinPrintCriteriaBuilder criteria = FinPrintCriteria.builder();
        if (fiscalYearId != null) {
            fiscalYearRepository.findById(fiscalYearId).ifPresent(year -> criteria.fiscalYearCode(year.getCode()));
        }
        if (periodId != null) {
            fiscalPeriodRepository.findById(periodId)
                .ifPresent(period -> criteria.periodNameAr(period.getNameAr()).periodNameEn(period.getNameEn()));
        }
        return ServiceResult.success(criteria.build());
    }

    /** {@code ReportService.assertDimensionFilter}: same codes, same order; plus the names it resolved. */
    private FinPrintCriteria.FinPrintCriteriaBuilder dimensionFilter(Long dimensionId, Long dimensionValueId) {
        if (dimensionId != null && !dimensionRepository.existsById(dimensionId)) {
            throw new LocalizedException(Status.NOT_FOUND, FinErrorCodes.FIN_404_DIMENSION, dimensionId);
        }
        if (dimensionValueId != null) {
            DimensionValue value = dimensionValueRepository.findById(dimensionValueId)
                .orElseThrow(() -> new LocalizedException(
                    Status.NOT_FOUND, FinErrorCodes.FIN_404_DIMVALUE, dimensionValueId));
            if (dimensionId != null && !dimensionId.equals(value.getDimension().getDimensionPk())) {
                throw new LocalizedException(Status.CONFLICT, FinErrorCodes.FIN_409_INVALID_DIMENSION, dimensionValueId);
            }
        }
        FinPrintCriteria.FinPrintCriteriaBuilder criteria = FinPrintCriteria.builder();
        names(criteria, dimensionId, dimensionValueId);
        return criteria;
    }

    /** The names of the dimension and value, when given and found; the value's dimension when only it is given. */
    private void names(FinPrintCriteria.FinPrintCriteriaBuilder criteria, Long dimensionId, Long dimensionValueId) {
        DimensionValue value = dimensionValueId == null ? null : dimensionValueRepository.findById(dimensionValueId).orElse(null);
        Dimension dimension = dimensionId == null
            ? (value == null ? null : value.getDimension())
            : dimensionRepository.findById(dimensionId).orElse(null);
        if (dimension != null) {
            criteria.dimensionNameAr(dimension.getNameAr()).dimensionNameEn(dimension.getNameEn());
        }
        if (value != null) {
            criteria.dimensionValueNameAr(value.getNameAr()).dimensionValueNameEn(value.getNameEn());
        }
    }

    private FiscalYear yearOf(Long fiscalYearId) {
        return fiscalYearRepository.findById(fiscalYearId)
            .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, FinErrorCodes.FIN_404_YEAR, fiscalYearId));
    }

    private FiscalPeriod periodOf(Long periodId) {
        return fiscalPeriodRepository.findById(periodId)
            .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, FinErrorCodes.FIN_404_PERIOD, periodId));
    }
}
