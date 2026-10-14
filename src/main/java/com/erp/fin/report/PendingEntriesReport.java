package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.common.search.SearchFilter;
import com.erp.common.search.SearchOperator;
import com.erp.fin.dto.JournalEntryResponse;
import com.erp.fin.dto.JournalEntrySearchRequest;
import com.erp.fin.service.FinLookupValidationService;
import com.erp.fin.service.JournalEntryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Every draft entry not yet posted, oldest first, with its age in days ({@code reports/fin/pending-entries.jrxml}), read
 * through {@code JournalEntryService.search} (same permission) and live by the template.
 */
@Component
@RequiredArgsConstructor
public class PendingEntriesReport implements TemplateReportProvider {

    private final JournalEntryService journalEntryService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_PENDING_ENTRIES";
    }

    @Override
    public String titleAr() {
        return "القيود المعلّقة غير المرحّلة";
    }

    @Override
    public String titleEn() {
        return "Pending (unposted) entries";
    }

    @Override
    public String templatePath() {
        return "fin/pending-entries";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        List<SearchFilter> filters = List.of(SearchFilter.builder().field("statusCode").operator(SearchOperator.EQUALS)
            .value("DRAFT").build());
        List<JournalEntryResponse> entries = params.allPages(page -> journalEntryService.search(JournalEntrySearchRequest.builder()
            .filters(filters).sortField("createdAt").sortDirection(Sort.Direction.ASC).page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        List<String> criteria = new ArrayList<>();
        criteria.add(params.localized("الحالة: مسودة — العدد: ", "Status: draft — count: ") + entries.size());
        return FinPrintTemplates.parameters(params, criteria,
            "IDS", FinPrintTemplates.ids(entries, JournalEntryResponse::getJournalEntryPk),
            "JOURNAL_TYPE_LABELS", labels.map(FinLookupValidationService.KEY_JOURNAL_TYPE, params));
    }
}
