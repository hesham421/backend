package com.erp.common.print;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

/** Body of a report export: the screen's current criteria (report parameters, or its search body). */
@Schema(description = "Report export criteria - معايير طباعة التقرير")
public record ReportExportRequest(
    @Schema(description = "Report parameters or the screen's search body (filters, sortField, sortDirection)")
    Map<String, Object> params) {
}
