package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.fin.dto.FinPrintCriteria;
import com.erp.fin.service.FinLookupValidationService;
import com.erp.fin.service.FinPrintCriteriaService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The income statement printed from {@code reports/fin/income-statement.jrxml}, read live by the template's SQL. */
@Component
@RequiredArgsConstructor
public class IncomeStatementReport implements TemplateReportProvider {

    private final FinPrintCriteriaService criteriaService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_INCOME_STATEMENT";
    }

    @Override
    public String titleAr() {
        return "قائمة الدخل";
    }

    @Override
    public String titleEn() {
        return "Income statement";
    }

    @Override
    public String templatePath() {
        return "fin/income-statement";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        Long fiscalYearId = params.requiredLong("fiscalYearId");
        Long dimensionId = params.longValue("dimensionId");
        Long dimensionValueId = params.longValue("dimensionValueId");
        FinPrintCriteria resolved = criteriaService.incomeStatement(fiscalYearId, params.longValue("fromPeriodId"),
            params.longValue("toPeriodId"), dimensionId, dimensionValueId).getData();

        List<String> criteria = FinPrintTemplates.criteria();
        criteria.add(params.localized("السنة المالية: ", "Fiscal year: ") + resolved.fiscalYearCode());
        if (resolved.fromDate() != null) {
            criteria.add(params.localized("من: ", "From: ") + FinPrintTemplates.date(resolved.fromDate()));
        }
        if (resolved.toDate() != null) {
            criteria.add(params.localized("إلى: ", "To: ") + FinPrintTemplates.date(resolved.toDate()));
        }
        FinPrintTemplates.dimension(criteria, resolved, params);
        return FinPrintTemplates.parameters(params, criteria,
            "FISCAL_YEAR_ID", fiscalYearId,
            "FROM_DATE", FinPrintTemplates.sqlDate(resolved.fromDate()), "TO_DATE", FinPrintTemplates.sqlDate(resolved.toDate()),
            "DIMENSION_ID", dimensionId, "DIMENSION_VALUE_ID", dimensionValueId,
            "TYPE_LABELS", labels.map(FinLookupValidationService.KEY_ACCOUNT_TYPE, params),
            "NATURE_LABELS", labels.map(FinLookupValidationService.KEY_DEBIT_CREDIT, params));
    }
}
