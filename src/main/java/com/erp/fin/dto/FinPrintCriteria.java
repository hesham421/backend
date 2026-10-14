package com.erp.fin.dto;

import java.time.LocalDate;
import lombok.Builder;

/**
 * What a printed FIN report states about its criteria, resolved by {@code FinPrintCriteriaService}: the names
 * of the period, year, dimension, value and account the screen's ids point at, and the income statement's
 * period range as dates. A {@code null} name means "not applied".
 */
@Builder
public record FinPrintCriteria(String periodNameAr, String periodNameEn, String fiscalYearCode,
                               LocalDate fromDate, LocalDate toDate,
                               String dimensionNameAr, String dimensionNameEn,
                               String dimensionValueNameAr, String dimensionValueNameEn,
                               String accountCode, String accountNameAr, String accountNameEn,
                               String accountNatureCode) {
}
