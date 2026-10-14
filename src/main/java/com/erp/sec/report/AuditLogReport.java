package com.erp.sec.report;

import com.erp.common.print.ColumnType;
import com.erp.common.print.ReportColumn;
import com.erp.common.print.ReportParams;
import com.erp.common.print.ReportProvider;
import com.erp.common.print.ReportResult;
import com.erp.sec.dto.AuditLogEntryResponse;
import com.erp.sec.dto.AuditLogEntrySearchRequest;
import com.erp.sec.service.AuditLogService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** SCR-SEC-008 printed: the entries of {@code AuditLogService.search} for the screen's search body, same permission. */
@Component
@RequiredArgsConstructor
public class AuditLogReport implements ReportProvider {

    private static final List<ReportColumn> COLUMNS = List.of(
        new ReportColumn("entryId", ColumnType.INTEGER, "معرّف القيد", "Entry ID"),
        ReportColumn.text("eventType", "نوع الحدث", "Event type"),
        new ReportColumn("actorUserId", ColumnType.INTEGER, "الفاعل (معرّف المستخدم)", "Actor (user ID)"),
        new ReportColumn("occurredAt", ColumnType.DATETIME, "وقت الحدث", "Occurred at"),
        ReportColumn.text("target", "الهدف", "Target"),
        ReportColumn.text("detailsAr", "التفاصيل (عربي)", "Details (Arabic)"),
        ReportColumn.text("detailsEn", "التفاصيل (إنجليزي)", "Details (English)"),
        ReportColumn.text("ipAddress", "عنوان IP", "IP address"));

    private final AuditLogService auditLogService;

    @Override
    public String code() {
        return "SEC_AUDIT_LOG";
    }

    @Override
    public String titleAr() {
        return "سجل التدقيق";
    }

    @Override
    public String titleEn() {
        return "Audit log";
    }

    @Override
    public ReportResult run(ReportParams params) {
        List<AuditLogEntryResponse> entries = params.allPages(page -> auditLogService.search(
            AuditLogEntrySearchRequest.builder().filters(params.filters()).sortField(params.sortField())
                .sortDirection(params.sortDirection()).page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        List<Map<String, Object>> rows = entries.stream().map(entry -> ReportResult.row(
            "entryId", entry.getAuditLogPk(),
            "eventType", entry.getEventTypeCode(),
            "actorUserId", entry.getActorUserId(),
            "occurredAt", entry.getOccurredAt(),
            "target", entry.getTargetRef(),
            "detailsAr", entry.getDetailsAr(),
            "detailsEn", entry.getDetailsEn(),
            "ipAddress", entry.getIpAddress())).toList();
        return new ReportResult(COLUMNS, rows);
    }
}
