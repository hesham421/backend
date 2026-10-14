package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.fin.dto.AccountMappingResponse;
import com.erp.fin.dto.AccountMappingSearchRequest;
import com.erp.fin.service.FinLookupValidationService;
import com.erp.fin.service.AccountMappingService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The account mappings screen printed from {@code reports/fin/account-mappings.jrxml}: the records are exactly those
 * {@code AccountMappingService.search} returns for the screen's search body (same filters, sort and permission); the
 * template reads them live with each mapped account's code and name.
 */
@Component
@RequiredArgsConstructor
public class AccountMappingsReport implements TemplateReportProvider {

    private final AccountMappingService accountMappingService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_ACCOUNT_MAPPINGS";
    }

    @Override
    public String titleAr() {
        return "روابط الحسابات";
    }

    @Override
    public String titleEn() {
        return "Account mappings";
    }

    @Override
    public String templatePath() {
        return "fin/account-mappings";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        List<AccountMappingResponse> records = params.allPages(page -> accountMappingService.search(AccountMappingSearchRequest.builder()
            .filters(params.filters()).sortField(params.sortField()).sortDirection(params.sortDirection())
            .page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        return FinPrintTemplates.parameters(params,
            FinPrintTemplates.listCriteria(params, records.size(), "ربط", "mappings"),
            "IDS", FinPrintTemplates.ids(records, AccountMappingResponse::getAccountMappingPk),
            "EVENT_TYPE_LABELS", labels.map(FinLookupValidationService.KEY_ACCOUNTING_EVENT_TYPE, params),
            "BUSINESS_FIELD_LABELS", labels.map(FinLookupValidationService.KEY_FIN_EVENT_BUSINESS_FIELD, params),
            "PAYMENT_METHOD_LABELS", labels.map(FinLookupValidationService.KEY_PAYMENT_METHOD, params));
    }
}
