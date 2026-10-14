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
 * The day's entries from the journal entries screen ({@code reports/fin/daily-journal.jrxml}), grouped by journal type with
 * counts and totals: the screen's own search, narrowed to today when it sets no date; the template reads them live.
 */
@Component
@RequiredArgsConstructor
public class DailyJournalReport implements TemplateReportProvider {

    private final JournalEntryService journalEntryService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_DAILY_JOURNAL";
    }

    @Override
    public String titleAr() {
        return "الحركة اليومية للقيود";
    }

    @Override
    public String titleEn() {
        return "Daily journal activity";
    }

    @Override
    public String templatePath() {
        return "fin/daily-journal";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        List<SearchFilter> filters = FinPrintTemplates.dayDefault(params.filters());
        List<JournalEntryResponse> entries = params.allPages(page -> journalEntryService.search(JournalEntrySearchRequest.builder()
            .filters(filters).sortField(params.sortField()).sortDirection(params.sortDirection()).page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        List<String> criteria = FinPrintTemplates.criteria();
        FinPrintTemplates.dateRange(criteria, filters, params);
        criteria.add(params.localized("العدد: ", "Count: ") + entries.size());
        return FinPrintTemplates.parameters(params, criteria,
            "IDS", FinPrintTemplates.ids(entries, JournalEntryResponse::getJournalEntryPk),
            "JOURNAL_TYPE_LABELS", labels.map(FinLookupValidationService.KEY_JOURNAL_TYPE, params),
            "STATUS_LABELS", labels.map(FinLookupValidationService.KEY_JOURNAL_STATUS, params));
    }
}
