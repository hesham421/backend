package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.fin.dto.DimensionResponse;
import com.erp.fin.dto.DimensionSearchRequest;
import com.erp.fin.service.DimensionService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The dimensions screen printed from {@code reports/fin/dimensions.jrxml}: the records are exactly those
 * {@code DimensionService.search} returns for the screen's search body (same filters, sort and permission); the
 * template reads them live, each dimension with all its values.
 */
@Component
@RequiredArgsConstructor
public class DimensionsReport implements TemplateReportProvider {

    private final DimensionService dimensionService;

    @Override
    public String code() {
        return "FIN_DIMENSIONS";
    }

    @Override
    public String titleAr() {
        return "الأبعاد التحليلية";
    }

    @Override
    public String titleEn() {
        return "Analysis dimensions";
    }

    @Override
    public String templatePath() {
        return "fin/dimensions";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        List<DimensionResponse> records = params.allPages(page -> dimensionService.search(DimensionSearchRequest.builder()
            .filters(params.filters()).sortField(params.sortField()).sortDirection(params.sortDirection())
            .page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        return FinPrintTemplates.parameters(params,
            FinPrintTemplates.listCriteria(params, records.size(), "بُعد", "dimensions"),
            "IDS", FinPrintTemplates.ids(records, DimensionResponse::getDimensionPk));
    }
}
