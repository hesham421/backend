package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.fin.dto.EventTypeRuleResponse;
import com.erp.fin.dto.EventTypeRuleSearchRequest;
import com.erp.fin.service.FinLookupValidationService;
import com.erp.fin.service.EventTypeRuleService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The posting engine rules screen printed from {@code reports/fin/engine-rules.jrxml}: the records are exactly those
 * {@code EventTypeRuleService.search} returns for the screen's search body (same filters, sort and permission); the
 * template reads them live, each rule with its lines.
 */
@Component
@RequiredArgsConstructor
public class EngineRulesReport implements TemplateReportProvider {

    private final EventTypeRuleService eventTypeRuleService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_RULES";
    }

    @Override
    public String titleAr() {
        return "قواعد محرك الترحيل";
    }

    @Override
    public String titleEn() {
        return "Posting engine rules";
    }

    @Override
    public String templatePath() {
        return "fin/engine-rules";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        List<EventTypeRuleResponse> records = params.allPages(page -> eventTypeRuleService.search(EventTypeRuleSearchRequest.builder()
            .filters(params.filters()).sortField(params.sortField()).sortDirection(params.sortDirection())
            .page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        return FinPrintTemplates.parameters(params,
            FinPrintTemplates.listCriteria(params, records.size(), "قاعدة", "rules"),
            "IDS", FinPrintTemplates.ids(records, EventTypeRuleResponse::getEventTypeRulePk),
            "EVENT_TYPE_LABELS", labels.map(FinLookupValidationService.KEY_ACCOUNTING_EVENT_TYPE, params),
            "DIRECTION_LABELS", labels.map(FinLookupValidationService.KEY_DEBIT_CREDIT, params),
            "DERIVATION_LABELS", labels.map(FinLookupValidationService.KEY_ACCOUNT_DERIVATION_TYPE, params),
            "AMOUNT_SOURCE_LABELS", labels.map(FinLookupValidationService.KEY_AMOUNT_SOURCE_TYPE, params),
            "DISTRIBUTION_LABELS", labels.map(FinLookupValidationService.KEY_DISTRIBUTION_TYPE, params),
            "BUSINESS_FIELD_LABELS", labels.map(FinLookupValidationService.KEY_FIN_EVENT_BUSINESS_FIELD, params));
    }
}
