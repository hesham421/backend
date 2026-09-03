package com.erp.security.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.security.dto.PermissionResponse;
import com.erp.security.dto.PermissionSearchRequest;
import com.erp.security.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin controller for API-SEC-014 (read-only permission listing, SCR-SEC-002 VIEW). The authoritative
 * SVC-API-RBAC contract specifies {@code GET /api/v1/security/permissions} with query-param filters
 * (pageFk / permissionType / moduleFk / page / size) — the Tier-2 permission picker consumes it as a
 * GET. This is a deliberate, documented variance from build-create-controller A.6.6 (POST /search):
 * the resource is read-only and system-generated, so a GET with @ModelAttribute binding matches both
 * REST semantics and the spec's stated contract. No write endpoints (permissions are auto-generated).
 */
@RestController
@RequestMapping("/api/v1/security/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Registry", description = "Read-only screen permissions (Tier-2 picker) - صلاحيات الشاشات")
public class PermissionController {

    private final PermissionService permissionService;
    private final OperationCode operationCode;

    @GetMapping
    @Operation(summary = "List permissions", description = "قائمة الصلاحيات مع التصفية")
    public ResponseEntity<ApiResponse<Page<PermissionResponse>>> search(
            @ModelAttribute PermissionSearchRequest searchRequest) {
        return operationCode.craftResponse(permissionService.search(searchRequest));
    }
}
