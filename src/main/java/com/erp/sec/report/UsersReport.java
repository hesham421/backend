package com.erp.sec.report;

import com.erp.common.print.ColumnType;
import com.erp.common.print.ReportColumn;
import com.erp.common.print.ReportParams;
import com.erp.common.print.ReportProvider;
import com.erp.common.print.ReportResult;
import com.erp.sec.dto.RoleSummaryResponse;
import com.erp.sec.dto.UserResponse;
import com.erp.sec.dto.UserSearchRequest;
import com.erp.sec.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** SCR-SEC-004 printed: every user of {@code UserService.search} for the screen's search body, same permission. */
@Component
@RequiredArgsConstructor
public class UsersReport implements ReportProvider {

    private static final List<ReportColumn> COLUMNS = List.of(
        ReportColumn.text("user", "المستخدم", "User"),
        ReportColumn.text("username", "اسم الحساب", "Username"),
        ReportColumn.text("email", "البريد الإلكتروني", "Email"),
        ReportColumn.text("roles", "الأدوار الممنوحة", "Assigned Roles"),
        ReportColumn.text("status", "الحالة", "Status"),
        new ReportColumn("lastLoginAt", ColumnType.DATETIME, "آخر دخول", "Last Login"));

    private final UserService userService;

    @Override
    public String code() {
        return "SEC_USERS";
    }

    @Override
    public String titleAr() {
        return "إدارة المستخدمين";
    }

    @Override
    public String titleEn() {
        return "User Management";
    }

    @Override
    public ReportResult run(ReportParams params) {
        List<UserResponse> users = params.allPages(page -> userService.search(UserSearchRequest.builder()
            .filters(params.filters()).sortField(params.sortField()).sortDirection(params.sortDirection())
            .page(page).size(ReportParams.PAGE_SIZE).build()).getData());
        List<Map<String, Object>> rows = users.stream().map(user -> ReportResult.row(
            "user", displayName(user, params),
            "username", user.getUsername() == null || user.getUsername().isBlank() ? null : "@" + user.getUsername(),
            "email", user.getEmail(),
            "roles", roles(user.getRoles(), params),
            "status", user.getStatusCode(),
            "lastLoginAt", user.getLastLoginAt())).toList();
        return new ReportResult(COLUMNS, rows);
    }

    /** The screen's name cell: the name in the print language, else the other one, else the login. */
    private static String displayName(UserResponse user, ReportParams params) {
        String name = params.localized(user.getFullNameAr(), user.getFullNameEn());
        return name == null || name.isBlank() ? user.getUsername() : name;
    }

    private static String roles(List<RoleSummaryResponse> roles, ReportParams params) {
        if (roles == null || roles.isEmpty()) {
            return params.localized("لا يوجد دور معين", "No role assigned");
        }
        return roles.stream()
            .map(role -> {
                String name = params.localized(role.getNameAr(), role.getNameEn());
                return name == null || name.isBlank() ? role.getCode() : name;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.joining(", "));
    }
}
