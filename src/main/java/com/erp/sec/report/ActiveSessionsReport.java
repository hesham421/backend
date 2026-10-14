package com.erp.sec.report;

import com.erp.common.print.ColumnType;
import com.erp.common.print.ReportColumn;
import com.erp.common.print.ReportParams;
import com.erp.common.print.ReportProvider;
import com.erp.common.print.ReportResult;
import com.erp.sec.dto.ActiveSessionResponse;
import com.erp.sec.dto.ActiveSessionSearchRequest;
import com.erp.sec.service.SessionService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** SCR-SEC-009 printed: the live sessions of {@code SessionService.search} for the screen's search body, same permission. */
@Component
@RequiredArgsConstructor
public class ActiveSessionsReport implements ReportProvider {

    private static final List<ReportColumn> COLUMNS = List.of(
        new ReportColumn("sessionId", ColumnType.INTEGER, "معرّف الجلسة", "Session ID"),
        ReportColumn.text("user", "المستخدم", "User"),
        new ReportColumn("startedAt", ColumnType.DATETIME, "بدأت في", "Started at"),
        new ReportColumn("lastActivityAt", ColumnType.DATETIME, "آخر نشاط", "Last activity"),
        ReportColumn.text("ipAddress", "عنوان IP", "IP address"));

    private final SessionService sessionService;

    @Override
    public String code() {
        return "SEC_SESSIONS";
    }

    @Override
    public String titleAr() {
        return "الجلسات النشطة";
    }

    @Override
    public String titleEn() {
        return "Active sessions";
    }

    @Override
    public ReportResult run(ReportParams params) {
        List<ActiveSessionResponse> sessions = params.allPages(page -> sessionService.search(
            ActiveSessionSearchRequest.builder().filters(params.filters()).sortField(params.sortField())
                .sortDirection(params.sortDirection()).page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        List<Map<String, Object>> rows = sessions.stream().map(session -> ReportResult.row(
            "sessionId", session.getActiveSessionPk(),
            "user", owner(session),
            "startedAt", session.getStartedAt(),
            "lastActivityAt", session.getLastActivityAt(),
            "ipAddress", session.getIpAddress())).toList();
        return new ReportResult(COLUMNS, rows);
    }

    /** The screen's owner label: the login, else the user id. */
    private static String owner(ActiveSessionResponse session) {
        String username = session.getUsername() == null ? "" : session.getUsername().trim();
        if (!username.isEmpty()) {
            return username;
        }
        return session.getUserId() == null ? null : session.getUserId().toString();
    }
}
