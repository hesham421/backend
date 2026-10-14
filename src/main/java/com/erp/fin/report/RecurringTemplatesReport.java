package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.fin.dto.RecurringTemplateResponse;
import com.erp.fin.dto.RecurringTemplateSearchRequest;
import com.erp.fin.service.FinLookupValidationService;
import com.erp.fin.service.RecurringTemplateService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The recurring templates screen printed from {@code reports/fin/recurring-templates.jrxml}: the records are exactly those
 * {@code RecurringTemplateService.search} returns for the screen's search body (same filters, sort and permission); the
 * template reads them live, each template with its lines and totals.
 */
@Component
@RequiredArgsConstructor
public class RecurringTemplatesReport implements TemplateReportProvider {

    private final RecurringTemplateService recurringTemplateService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_RECURRING_TEMPLATES";
    }

    @Override
    public String titleAr() {
        return "القوالب المتكررة والعكسية";
    }

    @Override
    public String titleEn() {
        return "Recurring / reversing templates";
    }

    @Override
    public String templatePath() {
        return "fin/recurring-templates";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        List<RecurringTemplateResponse> records = params.allPages(page -> recurringTemplateService.search(RecurringTemplateSearchRequest.builder()
            .filters(params.filters()).sortField(params.sortField()).sortDirection(params.sortDirection())
            .page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        return FinPrintTemplates.parameters(params,
            FinPrintTemplates.listCriteria(params, records.size(), "قالب", "templates"),
            "IDS", FinPrintTemplates.ids(records, RecurringTemplateResponse::getRecurringTemplatePk),
            "SCHEDULE_LABELS", labels.map(FinLookupValidationService.KEY_RECURRING_SCHEDULE_TYPE, params),
            "FREQUENCY_LABELS", labels.map(FinLookupValidationService.KEY_RECURRING_FREQUENCY, params));
    }
}
