package com.erp.sec.report;

import com.erp.common.print.ColumnType;
import com.erp.common.print.ReportColumn;
import com.erp.common.print.ReportParams;
import com.erp.common.print.ReportProvider;
import com.erp.common.print.ReportResult;
import com.erp.sec.dto.SignupRequestResponse;
import com.erp.sec.dto.SignupRequestSearchRequest;
import com.erp.sec.service.SignupRequestService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** SCR-SEC-004 pending sub-view printed: the sign-up requests of {@code SignupRequestService.search}, same permission. */
@Component
@RequiredArgsConstructor
public class PendingSignupsReport implements ReportProvider {

    private static final List<ReportColumn> COLUMNS = List.of(
        ReportColumn.text("fullName", "الاسم", "Name"),
        ReportColumn.text("email", "البريد الإلكتروني", "Email"),
        new ReportColumn("submittedAt", ColumnType.DATETIME, "تاريخ التقديم", "Submitted"));

    private final SignupRequestService signupRequestService;

    @Override
    public String code() {
        return "SEC_PENDING_SIGNUPS";
    }

    @Override
    public String titleAr() {
        return "طلبات التسجيل المعلّقة";
    }

    @Override
    public String titleEn() {
        return "Pending sign-ups";
    }

    @Override
    public ReportResult run(ReportParams params) {
        List<SignupRequestResponse> requests = params.allPages(page -> signupRequestService.search(
            SignupRequestSearchRequest.builder().filters(params.filters()).sortField(params.sortField())
                .sortDirection(params.sortDirection()).page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        List<Map<String, Object>> rows = requests.stream().map(request -> ReportResult.row(
            "fullName", params.localized(request.getFullNameAr(), request.getFullNameEn()),
            "email", request.getEmail(),
            "submittedAt", request.getSubmittedAt())).toList();
        return new ReportResult(COLUMNS, rows);
    }
}
