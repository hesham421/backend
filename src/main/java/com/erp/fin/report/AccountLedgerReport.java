package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.fin.dto.FinPrintCriteria;
import com.erp.fin.service.FinLookupValidationService;
import com.erp.fin.service.FinPrintCriteriaService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The account ledger printed from {@code reports/fin/account-ledger.jrxml}, read live by the template's SQL. */
@Component
@RequiredArgsConstructor
public class AccountLedgerReport implements TemplateReportProvider {

    private final FinPrintCriteriaService criteriaService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_ACCOUNT_LEDGER";
    }

    @Override
    public String titleAr() {
        return "دفتر الحساب";
    }

    @Override
    public String titleEn() {
        return "Account ledger";
    }

    @Override
    public String templatePath() {
        return "fin/account-ledger";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        Long accountId = params.requiredLong("accountId");
        LocalDate fromDate = params.date("fromDate");
        LocalDate toDate = params.date("toDate");
        Long dimensionId = params.longValue("dimensionId");
        Long dimensionValueId = params.longValue("dimensionValueId");
        FinPrintCriteria resolved = criteriaService.accountLedger(accountId, dimensionId, dimensionValueId).getData();

        List<String> criteria = FinPrintTemplates.criteria();
        criteria.add(params.localized("الحساب: ", "Account: ") + resolved.accountCode() + " — "
            + params.localized(resolved.accountNameAr(), resolved.accountNameEn()));
        criteria.add(params.localized("من: ", "From: ") + (fromDate == null
            ? params.localized("البداية", "the beginning") : FinPrintTemplates.date(fromDate)));
        criteria.add(params.localized("إلى: ", "To: ") + (toDate == null
            ? params.localized("اليوم", "today") : FinPrintTemplates.date(toDate)));
        FinPrintTemplates.dimension(criteria, resolved, params);
        return FinPrintTemplates.parameters(params, criteria,
            "ACCOUNT_ID", accountId,
            "FROM_DATE", FinPrintTemplates.sqlDate(fromDate), "TO_DATE", FinPrintTemplates.sqlDate(toDate),
            "DIMENSION_ID", dimensionId, "DIMENSION_VALUE_ID", dimensionValueId,
            "JOURNAL_TYPE_LABELS", labels.map(FinLookupValidationService.KEY_JOURNAL_TYPE, params));
    }
}
