package com.erp.security.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.security.dto.ModuleResponse;
import com.erp.security.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin controller for API-SEC-019 (dashboard modules for the current user). Self-scoped: the
 * endpoint resolves the caller from the JWT principal and returns only that caller's granted active
 * modules, so it carries no {@code @PreAuthorize} authority (see DashboardService's javadoc);
 * SecurityConfig's JWT filter gates the path since it is not on the public allow-list.
 */
@RestController
@RequestMapping("/api/v1/security/me")
@RequiredArgsConstructor
@Tag(name = "Current User Dashboard", description = "Self-scoped dashboard for the authenticated user - داشبورد المستخدم الحالي")
public class MeController {

    private final DashboardService dashboardService;
    private final OperationCode operationCode;

    @GetMapping("/modules")
    @Operation(summary = "List my dashboard modules", description = "قائمة موديولات الداشبورد للمستخدم الحالي")
    public ResponseEntity<ApiResponse<List<ModuleResponse>>> modules() {
        return operationCode.craftResponse(dashboardService.grantedModules());
    }
}
