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

/**
 * The account statement printed from the account ledger screen ({@code reports/fin/account-statement.jrxml}): the
 * chosen account and every account under it, each with its opening balance (posted lines before the range),
 * the range's lines with a running balance, and its closing balance. No dates means today. Same gate as the ledger.
 */
@Component
@RequiredArgsConstructor
public class AccountStatementReport implements TemplateReportProvider {

    private final FinPrintCriteriaService criteriaService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_ACCOUNT_STATEMENT";
    }

    @Override
    public String titleAr() {
        return "كشف حساب";
    }

    @Override
    public String titleEn() {
        return "Account statement";
    }

    @Override
    public String templatePath() {
        return "fin/account-statement";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        Long accountId = params.requiredLong("accountId");
        LocalDate fromDate = params.date("fromDate");
        LocalDate toDate = params.date("toDate");
        if (fromDate == null && toDate == null) {
            fromDate = LocalDate.now();
            toDate = fromDate;
        }
        Long dimensionId = params.longValue("dimensionId");
        Long dimensionValueId = params.longValue("dimensionValueId");
        FinPrintCriteria resolved = criteriaService.accountLedger(accountId, dimensionId, dimensionValueId).getData();

        List<String> criteria = FinPrintTemplates.criteria();
        criteria.add(params.localized("الحساب: ", "Account: ") + resolved.accountCode() + " — "
            + params.localized(resolved.accountNameAr(), resolved.accountNameEn())
            + params.localized(" وكل ما تحته", " and every account under it"));
        criteria.add(params.localized("من: ", "From: ") + (fromDate == null
            ? params.localized("البداية", "the beginning") : FinPrintTemplates.date(fromDate)));
        criteria.add(params.localized("إلى: ", "To: ") + FinPrintTemplates.date(toDate == null ? LocalDate.now() : toDate));
        FinPrintTemplates.dimension(criteria, resolved, params);
        return FinPrintTemplates.parameters(params, criteria,
            "ACCOUNT_ID", accountId,
            "FROM_DATE", FinPrintTemplates.sqlDate(fromDate == null ? LocalDate.of(1900, 1, 1) : fromDate),
            "TO_DATE", FinPrintTemplates.sqlDate(toDate == null ? LocalDate.now() : toDate),
            "DIMENSION_ID", dimensionId, "DIMENSION_VALUE_ID", dimensionValueId,
            "JOURNAL_TYPE_LABELS", labels.map(FinLookupValidationService.KEY_JOURNAL_TYPE, params),
            "NATURE_LABELS", labels.map(FinLookupValidationService.KEY_DEBIT_CREDIT, params));
    }
}
