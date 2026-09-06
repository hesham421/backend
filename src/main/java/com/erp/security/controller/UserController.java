package com.erp.security.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.security.dto.RoleResponse;
import com.erp.security.dto.UserCreateRequest;
import com.erp.security.dto.UserResponse;
import com.erp.security.dto.UserRoleAssignRequest;
import com.erp.security.dto.UserSearchRequest;
import com.erp.security.dto.UserUpdateRequest;
import com.erp.security.service.UserRoleService;
import com.erp.security.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin controller for User Management (SCR-SEC-001, API-SEC-007/008/009/010/012). Base path taken
 * literally from the SVC-API spec ({@code /api/v1/security/users}). Search is {@code GET /users}
 * with query-param filters (@ModelAttribute) exactly as the authoritative contract specifies — a
 * deliberate, documented variance from build-create-controller A.6.6 (POST /search), mirroring the
 * RBAC PermissionController; GET and POST on {@code /users} do not collide. DELETE maps onto the
 * service's soft deactivate (204). Role assignment returns a 200 status envelope.
 */
@RestController
@RequestMapping("/api/v1/security/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Security user accounts (SCR-SEC-001) - حسابات مستخدمي الأمان")
public class UserController {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create user", description = "إنشاء مستخدم جديد")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody UserCreateRequest request) {
        return operationCode.craftResponse(userService.create(request));
    }

    @GetMapping
    @Operation(summary = "Search users", description = "البحث في المستخدمين")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> search(
            @ModelAttribute UserSearchRequest searchRequest) {
        return operationCode.craftResponse(userService.search(searchRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "تحديث مستخدم حسب المعرف")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return operationCode.craftResponse(userService.update(id, request));
    }

    /**
     * API-SEC-010 DELETE — soft deactivate (isActiveFl = false, userStatusId = INACTIVE). Mirrors
     * build-create-controller's delete() shape (204, void, no wrapped response).
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate user", description = "إلغاء تفعيل مستخدم حسب المعرف")
    public void deactivate(@PathVariable Long id) {
        userService.deactivate(id);
    }

    @PostMapping("/{id}/roles")
    @Operation(summary = "Assign role to user", description = "إسناد دور لمستخدم")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleAssignRequest request) {
        return operationCode.craftResponse(userRoleService.assign(id, request.getRoleId()));
    }

    /**
     * Revoke a single role from a user — the counterpart of assignRole. Returns a 200 status
     * envelope (not a 204), mirroring the role↔permission and role↔module revoke endpoints on
     * RoleController: a relationship mutation conveys a status envelope rather than an empty delete.
     */
    @DeleteMapping("/{id}/roles/{roleId}")
    @Operation(summary = "Revoke role from user", description = "سحب دور من مستخدم")
    public ResponseEntity<ApiResponse<Void>> revokeRole(
            @PathVariable Long id,
            @PathVariable Long roleId) {
        return operationCode.craftResponse(userRoleService.revoke(id, roleId));
    }

    @GetMapping("/{id}/roles")
    @Operation(summary = "List user's assigned roles", description = "أدوار المستخدم المُسنَدة")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getRoles(@PathVariable Long id) {
        return operationCode.craftResponse(userRoleService.getRoles(id));
    }
}
