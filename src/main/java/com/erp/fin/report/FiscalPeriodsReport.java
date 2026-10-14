package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.fin.dto.FiscalPeriodResponse;
import com.erp.fin.dto.FiscalPeriodSearchRequest;
import com.erp.fin.service.FinLookupValidationService;
import com.erp.fin.service.FiscalPeriodService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The fiscal periods pane (one year) printed from {@code reports/fin/fiscal-periods.jrxml}: the records are exactly those
 * {@code FiscalPeriodService.search} returns for the screen's search body (same filters, sort and permission); the
 * template reads them live.
 */
@Component
@RequiredArgsConstructor
public class FiscalPeriodsReport implements TemplateReportProvider {

    private final FiscalPeriodService fiscalPeriodService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_FISCAL_PERIODS";
    }

    @Override
    public String titleAr() {
        return "الفترات المالية";
    }

    @Override
    public String titleEn() {
        return "Fiscal periods";
    }

    @Override
    public String templatePath() {
        return "fin/fiscal-periods";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        List<FiscalPeriodResponse> records = params.allPages(page -> fiscalPeriodService.search(FiscalPeriodSearchRequest.builder()
            .filters(params.filters()).sortField(params.sortField()).sortDirection(params.sortDirection())
            .page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        return FinPrintTemplates.parameters(params,
            FinPrintTemplates.listCriteria(params, records.size(), "فترة", "periods"),
            "IDS", FinPrintTemplates.ids(records, FiscalPeriodResponse::getFiscalPeriodPk),
            "PERIOD_STATUS_LABELS", labels.map(FinLookupValidationService.KEY_PERIOD_STATE, params));
    }
}
