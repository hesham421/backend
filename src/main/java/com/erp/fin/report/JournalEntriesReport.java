package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.common.search.SearchFilter;
import com.erp.fin.dto.FinPrintCriteria;
import com.erp.fin.dto.JournalEntryResponse;
import com.erp.fin.dto.JournalEntrySearchRequest;
import com.erp.fin.service.FinLookupValidationService;
import com.erp.fin.service.FinPrintCriteriaService;
import com.erp.fin.service.JournalEntryService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * SCR-FIN-006 printed as a journal book from {@code reports/fin/journal-entries.jrxml}: the entries are exactly
 * those {@code JournalEntryService.search} returns for the screen's search body (same filters, sort and
 * permission); the template then reads each entry and all its lines live.
 */
@Component
@RequiredArgsConstructor
public class JournalEntriesReport implements TemplateReportProvider {

    private final JournalEntryService journalEntryService;
    private final FinPrintCriteriaService criteriaService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_JOURNAL_ENTRIES";
    }

    @Override
    public String titleAr() {
        return "دفتر اليومية";
    }

    @Override
    public String titleEn() {
        return "Journal book";
    }

    @Override
    public String templatePath() {
        return "fin/journal-entries";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        List<SearchFilter> filters = params.filters();
        List<JournalEntryResponse> entries = params.allPages(page -> journalEntryService.search(
            JournalEntrySearchRequest.builder().filters(filters).sortField(params.sortField())
                .sortDirection(params.sortDirection()).page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        FinPrintCriteria resolved = criteriaService.journalEntries(
            longFilter(filters, "fiscalYearId"), longFilter(filters, "periodId")).getData();

        Map<String, String> typeLabels = labels.map(FinLookupValidationService.KEY_JOURNAL_TYPE, params);
        Map<String, String> statusLabels = labels.map(FinLookupValidationService.KEY_JOURNAL_STATUS, params);
        List<String> criteria = FinPrintTemplates.criteria();
        if (resolved.fiscalYearCode() != null) {
            criteria.add(params.localized("السنة المالية: ", "Fiscal year: ") + resolved.fiscalYearCode());
        }
        String period = params.localized(resolved.periodNameAr(), resolved.periodNameEn());
        if (period != null) {
            criteria.add(params.localized("الفترة: ", "Period: ") + period);
        }
        for (SearchFilter filter : filters) {
            String text = describe(filter, params, typeLabels, statusLabels);
            if (text != null) {
                criteria.add(text);
            }
        }
        if (criteria.isEmpty()) {
            criteria.add(params.localized("كل القيود", "All entries"));
        }
        String ids = entries.stream().map(entry -> String.valueOf(entry.getJournalEntryPk())).collect(Collectors.joining(","));
        return FinPrintTemplates.parameters(params, criteria,
            "ENTRY_IDS", ids, "JOURNAL_TYPE_LABELS", typeLabels, "STATUS_LABELS", statusLabels);
    }

    /** One screen filter as the printout states it; the year and period are stated by name above. */
    private static String describe(SearchFilter filter, ReportParams params, Map<String, String> typeLabels,
                                   Map<String, String> statusLabels) {
        String value = filter.getValue() == null ? "" : filter.getValue().toString();
        String operator = filter.getOperator() == null ? "EQUALS" : filter.getOperator().name();
        return switch (filter.getField()) {
            case "docNo" -> params.localized("رقم القيد: ", "Entry no.: ") + value;
            case "docDate" -> switch (operator) {
                case "GREATER_THAN_OR_EQUAL", "GREATER_THAN" -> params.localized("من تاريخ: ", "From: ") + value;
                case "LESS_THAN_OR_EQUAL", "LESS_THAN" -> params.localized("إلى تاريخ: ", "To: ") + value;
                default -> params.localized("التاريخ: ", "Date: ") + value;
            };
            case "statusCode" -> params.localized("الحالة: ", "Status: ") + statusLabels.getOrDefault(value, value);
            case "journalTypeCode" -> params.localized("النوع: ", "Type: ") + typeLabels.getOrDefault(value, value);
            case "eventReference" -> params.localized("المرجع: ", "Reference: ") + value;
            default -> null;
        };
    }

    private static Long longFilter(List<SearchFilter> filters, String field) {
        return filters.stream().filter(filter -> field.equals(filter.getField()) && filter.getValue() instanceof Number)
            .map(filter -> ((Number) filter.getValue()).longValue()).findFirst().orElse(null);
    }
}
