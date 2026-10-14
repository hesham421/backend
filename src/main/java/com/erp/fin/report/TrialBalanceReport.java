package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.fin.dto.FinPrintCriteria;
import com.erp.fin.service.FinLookupValidationService;
import com.erp.fin.service.FinPrintCriteriaService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** SCR-FIN-009 printed from {@code reports/fin/trial-balance.jrxml}: the screen's criteria, read live by the template's SQL. */
@Component
@RequiredArgsConstructor
public class TrialBalanceReport implements TemplateReportProvider {

    private final FinPrintCriteriaService criteriaService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_TRIAL_BALANCE";
    }

    @Override
    public String titleAr() {
        return "ميزان المراجعة";
    }

    @Override
    public String titleEn() {
        return "Trial balance";
    }

    @Override
    public String templatePath() {
        return "fin/trial-balance";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        Long periodId = params.longValue("periodId");
        String accountType = params.text("accountTypeCode");
        Long dimensionId = params.longValue("dimensionId");
        Long dimensionValueId = params.longValue("dimensionValueId");
        FinPrintCriteria resolved = criteriaService.trialBalance(periodId, dimensionId, dimensionValueId).getData();

        Map<String, String> typeLabels = labels.map(FinLookupValidationService.KEY_ACCOUNT_TYPE, params);
        List<String> criteria = FinPrintTemplates.criteria();
        criteria.add(params.localized("الفترة: ", "Period: ") + (periodId == null
            ? params.localized("كل الفترات", "all periods")
            : params.localized(resolved.periodNameAr(), resolved.periodNameEn())));
        criteria.add(params.localized("نوع الحساب: ", "Account type: ") + (accountType == null
            ? params.localized("كل الأنواع", "all types") : typeLabels.getOrDefault(accountType, accountType)));
        FinPrintTemplates.dimension(criteria, resolved, params);
        return FinPrintTemplates.parameters(params, criteria,
            "PERIOD_ID", periodId, "ACCOUNT_TYPE", accountType,
            "DIMENSION_ID", dimensionId, "DIMENSION_VALUE_ID", dimensionValueId,
            "TYPE_LABELS", typeLabels,
            "NATURE_LABELS", labels.map(FinLookupValidationService.KEY_DEBIT_CREDIT, params));
    }
}
