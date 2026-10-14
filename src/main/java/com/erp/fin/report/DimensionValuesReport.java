package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.fin.dto.DimensionValueResponse;
import com.erp.fin.dto.DimensionValueSearchRequest;
import com.erp.fin.service.DimensionValueService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The dimension values pane (one dimension) printed from {@code reports/fin/dimension-values.jrxml}: the records are exactly those
 * {@code DimensionValueService.search} returns for the screen's search body (same filters, sort and permission); the
 * template reads them live.
 */
@Component
@RequiredArgsConstructor
public class DimensionValuesReport implements TemplateReportProvider {

    private final DimensionValueService dimensionValueService;

    @Override
    public String code() {
        return "FIN_DIMENSION_VALUES";
    }

    @Override
    public String titleAr() {
        return "قيم البُعد";
    }

    @Override
    public String titleEn() {
        return "Dimension values";
    }

    @Override
    public String templatePath() {
        return "fin/dimension-values";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        List<DimensionValueResponse> records = params.allPages(page -> dimensionValueService.search(DimensionValueSearchRequest.builder()
            .filters(params.filters()).sortField(params.sortField()).sortDirection(params.sortDirection())
            .page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        return FinPrintTemplates.parameters(params,
            FinPrintTemplates.listCriteria(params, records.size(), "قيمة", "values"),
            "IDS", FinPrintTemplates.ids(records, DimensionValueResponse::getDimensionValuePk));
    }
}
