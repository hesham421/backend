package com.erp.mdl.report;

import com.erp.common.print.ColumnType;
import com.erp.common.print.ReportColumn;
import com.erp.common.print.ReportParams;
import com.erp.common.print.ReportProvider;
import com.erp.common.print.ReportResult;
import com.erp.mdl.dto.LookupValueResponse;
import com.erp.mdl.dto.LookupValueSearchRequest;
import com.erp.mdl.service.LookupValueService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * SCR-MDL-001 value pane printed: the values of {@code LookupValueService.search}, same permission. The
 * selected type travels as the screen's own {@code lookupTypeId} filter; without it the service answers 404.
 */
@Component
@RequiredArgsConstructor
public class LookupValuesReport implements ReportProvider {

    private static final List<ReportColumn> COLUMNS = List.of(
        new ReportColumn("sortOrder", ColumnType.INTEGER, "الترتيب", "Sort order"),
        ReportColumn.text("code", "الرمز", "Code"),
        ReportColumn.text("nameAr", "الاسم (عربي)", "Name (Arabic)"),
        ReportColumn.text("nameEn", "الاسم (إنجليزي)", "Name (English)"),
        ReportColumn.text("status", "الحالة", "Status"));

    private final LookupValueService lookupValueService;

    @Override
    public String code() {
        return "MDL_LOOKUP_VALUES";
    }

    @Override
    public String titleAr() {
        return "القيم";
    }

    @Override
    public String titleEn() {
        return "Values";
    }

    @Override
    public ReportResult run(ReportParams params) {
        List<LookupValueResponse> values = params.allPages(page -> lookupValueService.search(
            LookupValueSearchRequest.builder().filters(params.filters()).sortField(params.sortField())
                .sortDirection(params.sortDirection()).page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        List<Map<String, Object>> rows = values.stream().map(value -> ReportResult.row(
            "sortOrder", value.getSortOrder(),
            "code", value.getCode(),
            "nameAr", value.getNameAr(),
            "nameEn", value.getNameEn(),
            "status", MdlReportLabels.activeStatus(value.getIsActiveFl(), params))).toList();
        return new ReportResult(COLUMNS, rows);
    }
}
