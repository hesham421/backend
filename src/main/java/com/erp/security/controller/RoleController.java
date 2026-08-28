package com.erp.security.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.security.dto.*;
import com.erp.security.service.RoleAccessService;
import com.erp.security.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Contract: role-access.contract.md (BE-REQ-ROLEACCESS-001). */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role Access Control", description = "إدارة الأدوار والصلاحيات (Role Access Control)")
public class RoleController {

    private final RoleService roleService;
    private final RoleAccessService roleAccessService;
    private final OperationCode operationCode;

    // ROLE CRUD OPERATIONS
    // Contract: role-access.contract.md - Endpoints 1-5

    /**
     * Contract: role-access.contract.md - Endpoint 3
     */
    @PostMapping
    @Operation(summary = "Create new role", description = "Creates a new role with roleCode, roleName, description, and active status")
    public ResponseEntity<ApiResponse<RoleDto>> createRole(@RequestBody @Valid CreateRoleRequest req) {
        return operationCode.craftResponse(roleService.createRole(req));
    }

    /**
     * Contract: role-access.contract.md - Endpoint 1
     */
    @PostMapping("/search")
    @Operation(
        summary = "Search roles",
        description = "Search roles with dynamic filters, sorting, and pagination. "
                + "Allowed filter fields: roleName. "
                + "Allowed sort fields: id, roleName."
    )
    public ResponseEntity<ApiResponse<Page<RoleDto>>> searchRoles(@RequestBody RoleSearchContractRequest searchRequest) {
        return operationCode.craftResponse(roleService.searchRoles(searchRequest.toCommonSearchRequest()));
    }

    /**
     * Contract: role-access.contract.md - Endpoint 2
     */
    @GetMapping("/{roleId}")
    @Operation(summary = "Get role by ID", description = "Retrieve a single role by its ID")
    public ResponseEntity<ApiResponse<RoleDto>> getRoleById(@PathVariable Long roleId) {
        return operationCode.craftResponse(roleService.getRoleById(roleId));
    }

    /**
     * Contract: role-access.contract.md - Endpoint 4
     */
    @PutMapping("/{roleId}")
    @Operation(summary = "Update role", description = "Update an existing role's information (roleCode cannot be changed)")
    public ResponseEntity<ApiResponse<RoleDto>> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return operationCode.craftResponse(roleService.updateRole(roleId, request));
    }

    /**
     * Contract: role-access.contract.md - Endpoint 5
     */
    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete role", description = "Delete a role permanently. Returns 409 if role has user assignments.")
    public void deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
    }

    /**
     * Contract: role-access.contract.md - Endpoint 11 (activate)
     */
    @PutMapping("/{roleId}/activate")
    @Operation(summary = "Activate role", description = "تفعيل الدور")
    public ResponseEntity<ApiResponse<RoleDto>> activateRole(@PathVariable Long roleId) {
        return operationCode.craftResponse(roleService.activate(roleId));
    }

    /**
     * Contract: role-access.contract.md - Endpoint 11 (deactivate)
     */
    @PutMapping("/{roleId}/deactivate")
    @Operation(summary = "Deactivate role", description = "إلغاء تفعيل الدور")
    public ResponseEntity<ApiResponse<RoleDto>> deactivateRole(@PathVariable Long roleId) {
        return operationCode.craftResponse(roleService.deactivate(roleId));
    }

    // ROLE → PAGES PERMISSIONS (MASTER/DETAIL)
    // Contract: role-access.contract.md - Endpoints 6-10

    /**
     * Contract: role-access.contract.md - Endpoint 6
     */
    @GetMapping("/{roleId}/pages")
    @Operation(
        summary = "Get role pages matrix",
        description = "Get list of assigned pages for this role with CRUD permissions. VIEW is implicit and not included."
    )
    public ResponseEntity<ApiResponse<RolePagesMatrixResponse>> getRolePages(@PathVariable Long roleId) {
        return operationCode.craftResponse(roleAccessService.getRolePages(roleId));
    }

    /**
     * Contract: role-access.contract.md - Endpoint 7
     */
    @PostMapping("/{roleId}/pages")
    @Operation(
        summary = "Add page to role",
        description = "Add a page to a role with specific CRUD permissions. VIEW is ALWAYS added automatically. Permissions: CREATE, UPDATE, DELETE"
    )
    public ResponseEntity<ApiResponse<PageAssignmentResponse>> addPageToRole(
            @PathVariable Long roleId,
            @Valid @RequestBody AddPageToRoleRequest request
    ) {
        return operationCode.craftResponse(roleAccessService.addPageToRole(roleId, request));
    }

    /**
     * Contract: role-access.contract.md - Endpoint 8
     */
    @PutMapping("/{roleId}/pages")
    @Operation(
        summary = "Bulk update role pages (replace mode)",
        description = "FULL REPLACE: Replace all page assignments for this role. VIEW is auto-added. Empty array removes all pages."
    )
    public ResponseEntity<ApiResponse<RolePagesMatrixResponse>> syncRolePages(
            @PathVariable Long roleId,
            @Valid @RequestBody SyncRolePagesRequest request
    ) {
        return operationCode.craftResponse(roleAccessService.syncRolePages(roleId, request));
    }

    /**
     * Contract: role-access.contract.md - Endpoint 9
     */
    @DeleteMapping("/{roleId}/pages/{pageCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Remove page from role",
        description = "Remove a page from the role completely (removes VIEW + all CRUD permissions)"
    )
    public void removePageFromRole(
            @PathVariable Long roleId,
            @PathVariable String pageCode
    ) {
        roleAccessService.removePageFromRole(roleId, pageCode);
    }

    /**
     * Contract: role-access.contract.md - Endpoint 10
     */
    @PostMapping("/{roleId}/copy-from/{sourceRoleId}")
    @Operation(
        summary = "Copy page permissions from another role",
        description = "Copy source role's page-scoped permissions (PAGE_ID_FK IS NOT NULL) to this role, replacing its existing page-scoped assignments. System-level permissions on this role are left untouched."
    )
    public ResponseEntity<ApiResponse<CopyPermissionsResponse>> copyFromRole(
            @PathVariable Long roleId,
            @PathVariable Long sourceRoleId
    ) {
        return operationCode.craftResponse(roleAccessService.copyPermissionsFromRole(roleId, sourceRoleId));
    }
}
