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

/** The dimension report printed from {@code reports/fin/dimension-report.jrxml}, read live by the template's SQL. */
@Component
@RequiredArgsConstructor
public class DimensionReport implements TemplateReportProvider {

    private final FinPrintCriteriaService criteriaService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_DIMENSION_REPORTS";
    }

    @Override
    public String titleAr() {
        return "تقارير الأبعاد";
    }

    @Override
    public String titleEn() {
        return "Dimension reports";
    }

    @Override
    public String templatePath() {
        return "fin/dimension-report";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        Long dimensionId = params.requiredLong("dimensionId");
        Long dimensionValueId = params.longValue("dimensionValueId");
        Long periodId = params.longValue("periodId");
        FinPrintCriteria resolved = criteriaService.dimensionReport(dimensionId, dimensionValueId, periodId).getData();

        List<String> criteria = FinPrintTemplates.criteria();
        FinPrintTemplates.dimension(criteria, resolved, params);
        criteria.add(params.localized("الفترة: ", "Period: ") + (periodId == null
            ? params.localized("كل الفترات", "all periods")
            : params.localized(resolved.periodNameAr(), resolved.periodNameEn())));
        return FinPrintTemplates.parameters(params, criteria,
            "DIMENSION_ID", dimensionId, "DIMENSION_VALUE_ID", dimensionValueId, "PERIOD_ID", periodId,
            "NATURE_LABELS", labels.map(FinLookupValidationService.KEY_DEBIT_CREDIT, params));
    }
}
