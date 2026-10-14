package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.common.search.SearchFilter;
import com.erp.fin.dto.JournalEntryResponse;
import com.erp.fin.dto.JournalEntrySearchRequest;
import com.erp.fin.service.FinLookupValidationService;
import com.erp.fin.service.JournalEntryService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The day's event-generated entries from the journal entries screen ({@code reports/fin/daily-events.jrxml}), grouped by
 * source table of the event reference ({@code LEGACY:<TABLE>:<PK>:<SEQ>}) for reconciliation with the loan system.
 */
@Component
@RequiredArgsConstructor
public class DailyEventsReport implements TemplateReportProvider {

    private final JournalEntryService journalEntryService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_DAILY_EVENTS";
    }

    @Override
    public String titleAr() {
        return "أحداث نظام القروض اليومية";
    }

    @Override
    public String titleEn() {
        return "Daily loan-system events";
    }

    @Override
    public String templatePath() {
        return "fin/daily-events";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        List<SearchFilter> filters = FinPrintTemplates.dayDefault(params.filters());
        List<JournalEntryResponse> entries = params.allPages(page -> journalEntryService.search(JournalEntrySearchRequest.builder()
            .filters(filters).sortField(params.sortField()).sortDirection(params.sortDirection()).page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        List<String> criteria = FinPrintTemplates.criteria();
        FinPrintTemplates.dateRange(criteria, filters, params);
        return FinPrintTemplates.parameters(params, criteria,
            "IDS", FinPrintTemplates.ids(entries, JournalEntryResponse::getJournalEntryPk),
            "STATUS_LABELS", labels.map(FinLookupValidationService.KEY_JOURNAL_STATUS, params));
    }
}
