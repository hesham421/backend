package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.fin.dto.FinPrintCriteria;
import com.erp.fin.service.FinPrintCriteriaService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * One day's posted movement per value of the dimension chosen on the dimension reports screen (e.g. per branch):
 * entries, debit, credit, revenue, expense and net ({@code reports/fin/daily-branches.jrxml}). The day is today
 * unless a {@code date} is given. Same gate as the dimension report.
 */
@Component
@RequiredArgsConstructor
public class DailyBranchesReport implements TemplateReportProvider {

    private final FinPrintCriteriaService criteriaService;

    @Override
    public String code() {
        return "FIN_DAILY_BRANCHES";
    }

    @Override
    public String titleAr() {
        return "حركة اليوم حسب الفرع";
    }

    @Override
    public String titleEn() {
        return "Today's movement by branch";
    }

    @Override
    public String templatePath() {
        return "fin/daily-branches";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        Long dimensionId = params.requiredLong("dimensionId");
        Long dimensionValueId = params.longValue("dimensionValueId");
        LocalDate day = params.date("date") == null ? LocalDate.now() : params.date("date");
        FinPrintCriteria resolved = criteriaService.dimensionReport(dimensionId, dimensionValueId, null).getData();

        List<String> criteria = FinPrintTemplates.criteria();
        FinPrintTemplates.dimension(criteria, resolved, params);
        criteria.add(params.localized("اليوم: ", "Day: ") + FinPrintTemplates.date(day));
        return FinPrintTemplates.parameters(params, criteria,
            "DIMENSION_ID", dimensionId, "DIMENSION_VALUE_ID", dimensionValueId, "DAY", FinPrintTemplates.sqlDate(day));
    }
}
