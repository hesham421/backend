package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.fin.dto.AccountResponse;
import com.erp.fin.dto.AccountSearchRequest;
import com.erp.fin.service.AccountService;
import com.erp.fin.service.FinLookupValidationService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * SCR-FIN-001 printed from {@code reports/fin/chart-of-accounts.jrxml}: the accounts are exactly those
 * {@code AccountService.search} returns for the screen's search body (same filters, sort and permission), in
 * the screen's tree order (each parent followed by its children) and indented by level; the template reads
 * each account live.
 */
@Component
@RequiredArgsConstructor
public class ChartOfAccountsReport implements TemplateReportProvider {

    private final AccountService accountService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_ACCOUNTS";
    }

    @Override
    public String titleAr() {
        return "دليل الحسابات";
    }

    @Override
    public String titleEn() {
        return "Chart of accounts";
    }

    @Override
    public String templatePath() {
        return "fin/chart-of-accounts";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        List<AccountResponse> accounts = params.allPages(page -> accountService.search(AccountSearchRequest.builder()
            .filters(params.filters()).sortField(params.sortField()).sortDirection(params.sortDirection())
            .page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        Map<Long, List<AccountResponse>> childrenOf = new LinkedHashMap<>();
        List<AccountResponse> roots = new ArrayList<>();
        Map<Long, AccountResponse> byPk = new LinkedHashMap<>();
        accounts.forEach(account -> byPk.put(account.getAccountPk(), account));
        for (AccountResponse account : accounts) {
            Long parent = account.getParentAccountId();
            if (parent != null && byPk.containsKey(parent)) {
                childrenOf.computeIfAbsent(parent, key -> new ArrayList<>()).add(account);
            } else {
                roots.add(account);
            }
        }
        List<String> ordered = new ArrayList<>(accounts.size());
        roots.forEach(root -> walk(root, 0, childrenOf, ordered));

        List<String> criteria = FinPrintTemplates.criteria();
        criteria.add(params.localized("عدد الحسابات: ", "Accounts: ") + accounts.size());
        return FinPrintTemplates.parameters(params, criteria,
            "ACCOUNTS", String.join(",", ordered),
            "TYPE_LABELS", labels.map(FinLookupValidationService.KEY_ACCOUNT_TYPE, params),
            "NATURE_LABELS", labels.map(FinLookupValidationService.KEY_DEBIT_CREDIT, params));
    }

    /** The screen's {@code buildAccountTree}: depth-first, as {@code pk:depth}; a parent outside the result counts as none. */
    private static void walk(AccountResponse account, int depth, Map<Long, List<AccountResponse>> childrenOf,
                             List<String> ordered) {
        ordered.add(account.getAccountPk() + ":" + depth);
        childrenOf.getOrDefault(account.getAccountPk(), List.of())
            .forEach(child -> walk(child, depth + 1, childrenOf, ordered));
    }
}
