package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.fin.dto.FiscalYearResponse;
import com.erp.fin.dto.FiscalYearSearchRequest;
import com.erp.fin.service.FinLookupValidationService;
import com.erp.fin.service.FiscalYearService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The fiscal years screen printed from {@code reports/fin/fiscal-years.jrxml}: the records are exactly those
 * {@code FiscalYearService.search} returns for the screen's search body (same filters, sort and permission); the
 * template reads them live, each year with all its periods.
 */
@Component
@RequiredArgsConstructor
public class FiscalYearsReport implements TemplateReportProvider {

    private final FiscalYearService fiscalYearService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_PERIODS";
    }

    @Override
    public String titleAr() {
        return "السنوات والفترات المالية";
    }

    @Override
    public String titleEn() {
        return "Fiscal years and periods";
    }

    @Override
    public String templatePath() {
        return "fin/fiscal-years";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        List<FiscalYearResponse> records = params.allPages(page -> fiscalYearService.search(FiscalYearSearchRequest.builder()
            .filters(params.filters()).sortField(params.sortField()).sortDirection(params.sortDirection())
            .page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        return FinPrintTemplates.parameters(params,
            FinPrintTemplates.listCriteria(params, records.size(), "سنة", "years"),
            "IDS", FinPrintTemplates.ids(records, FiscalYearResponse::getFiscalYearPk),
            "YEAR_STATUS_LABELS", labels.map(FinLookupValidationService.KEY_FISCAL_YEAR_STATUS, params),
            "PERIOD_STATUS_LABELS", labels.map(FinLookupValidationService.KEY_PERIOD_STATE, params));
    }
}
