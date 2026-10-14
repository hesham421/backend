package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.fin.dto.FinPrintCriteria;
import com.erp.fin.service.FinLookupValidationService;
import com.erp.fin.service.FinPrintCriteriaService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The balance sheet printed from {@code reports/fin/balance-sheet.jrxml}, read live by the template's SQL. */
@Component
@RequiredArgsConstructor
public class BalanceSheetReport implements TemplateReportProvider {

    private final FinPrintCriteriaService criteriaService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_BALANCE_SHEET";
    }

    @Override
    public String titleAr() {
        return "الميزانية العمومية";
    }

    @Override
    public String titleEn() {
        return "Balance sheet";
    }

    @Override
    public String templatePath() {
        return "fin/balance-sheet";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        Long fiscalYearId = params.requiredLong("fiscalYearId");
        LocalDate asOfDate = params.date("asOfDate");
        Long dimensionId = params.longValue("dimensionId");
        Long dimensionValueId = params.longValue("dimensionValueId");
        FinPrintCriteria resolved = criteriaService.balanceSheet(fiscalYearId, dimensionId, dimensionValueId).getData();

        List<String> criteria = FinPrintTemplates.criteria();
        criteria.add(params.localized("السنة المالية: ", "Fiscal year: ") + resolved.fiscalYearCode());
        if (asOfDate != null) {
            criteria.add(params.localized("حتى تاريخ: ", "As of: ") + FinPrintTemplates.date(asOfDate));
        }
        FinPrintTemplates.dimension(criteria, resolved, params);
        return FinPrintTemplates.parameters(params, criteria,
            "FISCAL_YEAR_ID", fiscalYearId, "AS_OF_DATE", FinPrintTemplates.sqlDate(asOfDate),
            "DIMENSION_ID", dimensionId, "DIMENSION_VALUE_ID", dimensionValueId,
            "TYPE_LABELS", labels.map(FinLookupValidationService.KEY_ACCOUNT_TYPE, params),
            "NATURE_LABELS", labels.map(FinLookupValidationService.KEY_DEBIT_CREDIT, params));
    }
}
