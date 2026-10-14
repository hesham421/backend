package com.erp.mdl.report;

import com.erp.common.print.ReportColumn;
import com.erp.common.print.ReportParams;
import com.erp.common.print.ReportProvider;
import com.erp.common.print.ReportResult;
import com.erp.mdl.dto.LookupTypeResponse;
import com.erp.mdl.dto.LookupTypeSearchRequest;
import com.erp.mdl.service.LookupTypeService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** SCR-MDL-001 type list printed: the lookup types of {@code LookupTypeService.search}, same permission. */
@Component
@RequiredArgsConstructor
public class LookupTypesReport implements ReportProvider {

    private static final List<ReportColumn> COLUMNS = List.of(
        ReportColumn.text("key", "المفتاح", "Key"),
        ReportColumn.text("ownerModule", "الوحدة المالكة", "Owner module"),
        ReportColumn.text("name", "الاسم (عربي)", "Name (English)"),
        ReportColumn.text("status", "الحالة", "Status"));

    private final LookupTypeService lookupTypeService;

    @Override
    public String code() {
        return "MDL_LOOKUP_TYPES";
    }

    @Override
    public String titleAr() {
        return "أنواع اللوكب";
    }

    @Override
    public String titleEn() {
        return "Lookup types";
    }

    @Override
    public ReportResult run(ReportParams params) {
        List<LookupTypeResponse> types = params.allPages(page -> lookupTypeService.search(
            LookupTypeSearchRequest.builder().filters(params.filters()).sortField(params.sortField())
                .sortDirection(params.sortDirection()).page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        List<Map<String, Object>> rows = types.stream().map(type -> ReportResult.row(
            "key", type.getKey(),
            "ownerModule", type.getOwnerModuleCode(),
            "name", params.localized(type.getNameAr(), type.getNameEn()),
            "status", MdlReportLabels.activeStatus(type.getIsActiveFl(), params))).toList();
        return new ReportResult(COLUMNS, rows);
    }
}
