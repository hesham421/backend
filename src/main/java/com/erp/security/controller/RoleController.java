package com.erp.security.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.security.dto.RoleCreateRequest;
import com.erp.security.dto.RoleModuleAssignRequest;
import com.erp.security.dto.RolePermissionGrantRequest;
import com.erp.security.dto.RoleResponse;
import com.erp.security.dto.RoleSearchRequest;
import com.erp.security.dto.RoleUpdateRequest;
import com.erp.security.service.RoleModuleService;
import com.erp.security.service.RolePermissionService;
import com.erp.security.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the SCR-SEC-002 Roles resource ({@code /api/v1/security/roles}). The
 * SVC-API-MODULES sub created it with the Tier-1 module grant/revoke endpoints (API-SEC-017/018);
 * the SVC-API-RBAC sub ADDS Roles CRUD (API-SEC-011) and the Tier-2 permission grant/revoke
 * endpoints (API-SEC-015) to this SAME controller.
 *
 * <p>Module and permission grant/revoke return a 200 envelope (not a 204 delete): a relationship
 * mutation can legitimately fail with a business status (ERR-0013/0014) and both convey a status
 * envelope — a documented, deliberate variance from build-create-controller's void-delete shape.
 * The Roles CRUD delete IS a soft deactivate returning 204 (the standard entity-delete shape).
 */
@RestController
@RequestMapping("/api/v1/security/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "RBAC roles, Tier-1 module grants and Tier-2 permission grants - أدوار الصلاحيات")
public class RoleController {

    private final RoleService roleService;
    private final RoleModuleService roleModuleService;
    private final RolePermissionService rolePermissionService;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create role", description = "إنشاء دور جديد")
    public ResponseEntity<ApiResponse<RoleResponse>> create(
            @Valid @RequestBody RoleCreateRequest request) {
        return operationCode.craftResponse(roleService.create(request));
    }

    @PostMapping("/search")
    @Operation(summary = "Search roles", description = "البحث في الأدوار")
    public ResponseEntity<ApiResponse<Page<RoleResponse>>> search(
            @Valid @RequestBody RoleSearchRequest searchRequest) {
        return operationCode.craftResponse(roleService.search(searchRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role", description = "تحديث دور حسب المعرف")
    public ResponseEntity<ApiResponse<RoleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {
        return operationCode.craftResponse(roleService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID", description = "جلب دور حسب المعرف")
    public ResponseEntity<ApiResponse<RoleResponse>> getById(@PathVariable Long id) {
        return operationCode.craftResponse(roleService.getById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate role", description = "إلغاء تفعيل دور حسب المعرف")
    public void deactivate(@PathVariable Long id) {
        roleService.deactivate(id);
    }

    @PostMapping("/{id}/modules")
    @Operation(summary = "Assign module to role", description = "منح موديل لدور (Tier-1)")
    public ResponseEntity<ApiResponse<Void>> assignModule(
            @PathVariable Long id,
            @Valid @RequestBody RoleModuleAssignRequest request) {
        return operationCode.craftResponse(roleModuleService.assignModule(id, request.getModuleId()));
    }

    @DeleteMapping("/{id}/modules/{moduleId}")
    @Operation(summary = "Revoke module from role", description = "سحب موديل من دور")
    public ResponseEntity<ApiResponse<Void>> revokeModule(
            @PathVariable Long id,
            @PathVariable Long moduleId) {
        return operationCode.craftResponse(roleModuleService.revokeModule(id, moduleId));
    }

    @PostMapping("/{id}/permissions")
    @Operation(summary = "Grant permission to role", description = "منح صلاحية شاشة لدور (Tier-2)")
    public ResponseEntity<ApiResponse<Void>> grantPermission(
            @PathVariable Long id,
            @Valid @RequestBody RolePermissionGrantRequest request) {
        return operationCode.craftResponse(rolePermissionService.grant(id, request.getPermissionId()));
    }

    @DeleteMapping("/{id}/permissions/{permissionId}")
    @Operation(summary = "Revoke permission from role", description = "سحب صلاحية شاشة من دور")
    public ResponseEntity<ApiResponse<Void>> revokePermission(
            @PathVariable Long id,
            @PathVariable Long permissionId) {
        return operationCode.craftResponse(rolePermissionService.revoke(id, permissionId));
    }
}
