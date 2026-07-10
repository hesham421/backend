package com.example.security.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.erp.common.web.util.PageableUtils;
import com.example.security.dto.CreateSecRoleBranchRequest;
import com.example.security.dto.SecRoleBranchDto;
import com.example.security.dto.SecRoleBranchSearchContractRequest;
import com.example.security.dto.UpdateSecRoleBranchRequest;
import com.example.security.service.SecRoleBranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for SEC_ROLE_BRANCH (DataScope — API-SEC-036..039).
 *
 * Thin controller — all logic, including {@code @PreAuthorize} permission gates
 * (Phase SEC, Section 8.1 Permissions Matrix — reuses the EXISTING {@code PERM_ROLE_*}
 * permissions per CORE-9, no new SEC_PAGES row/permission set for this sub-tab), lives
 * in {@link SecRoleBranchService} per this codebase's A.5.2 service-contract convention
 * (governance-repo enforce-backend-contract skill) — controllers never carry @PreAuthorize.
 *
 * Update/delete use {roleId}/{branchId} rather than the API register's literal "{id}":
 * SEC_ROLE_BRANCH has no surrogate PK (composite key), so a single {id} path variable would
 * require inventing a non-existent column — flagged in the Phase 3 handoff.
 */
@RestController
@RequestMapping("/api/v1/security/role-branches")
@RequiredArgsConstructor
@Tag(name = "Security - DataScope - Role Branches", description = "إدارة نطاق الفروع للأدوار - Role Branch Scope Management")
public class SecRoleBranchController {

    private final SecRoleBranchService service;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Assign a branch scope to a role", description = "إسناد فرع لدور - RULE-SEC-035, RULE-SEC-036")
    public ResponseEntity<ApiResponse<SecRoleBranchDto>> create(@Valid @RequestBody CreateSecRoleBranchRequest request) {
        return operationCode.craftResponse(service.create(request));
    }

    @GetMapping
    @Operation(
        summary = "List role-branch assignments",
        description = "Get paginated list of role-branch assignments. Allowed sort fields: roleIdFk, branchIdFk, dataAccessLevel, isActiveFl, createdAt."
    )
    public ResponseEntity<ApiResponse<Page<SecRoleBranchDto>>> list(
            @Parameter(description = "Pagination parameters. Sort format: 'fieldName,asc' or 'fieldName,desc'.",
                       schema = @Schema(implementation = Pageable.class))
            @PageableDefault(size = 20, sort = "roleIdFk", direction = Sort.Direction.ASC) Pageable pageable) {
        pageable = PageableUtils.enforceConstraints(pageable);
        return operationCode.craftResponse(service.listRoleBranches(pageable));
    }

    @PostMapping("/search")
    @Operation(summary = "Search role-branch assignments", description = "بحث في نطاقات الفروع للأدوار")
    public ResponseEntity<ApiResponse<Page<SecRoleBranchDto>>> search(@RequestBody SecRoleBranchSearchContractRequest searchRequest) {
        return operationCode.craftResponse(service.search(searchRequest.toCommonSearchRequest()));
    }

    @GetMapping("/{roleId}/{branchId}")
    @Operation(summary = "Get a role-branch assignment", description = "جلب نطاق فرع لدور")
    public ResponseEntity<ApiResponse<SecRoleBranchDto>> getById(@PathVariable Long roleId, @PathVariable Long branchId) {
        return operationCode.craftResponse(service.getById(roleId, branchId));
    }

    @PutMapping("/{roleId}/{branchId}")
    @Operation(summary = "Update a role-branch assignment", description = "تحديث نطاق فرع لدور - RULE-SEC-035")
    public ResponseEntity<ApiResponse<SecRoleBranchDto>> update(
            @PathVariable Long roleId,
            @PathVariable Long branchId,
            @Valid @RequestBody UpdateSecRoleBranchRequest request) {
        return operationCode.craftResponse(service.update(roleId, branchId, request));
    }

    @DeleteMapping("/{roleId}/{branchId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a role-branch assignment", description = "إزالة نطاق فرع من دور")
    public void delete(@PathVariable Long roleId, @PathVariable Long branchId) {
        service.delete(roleId, branchId);
    }
}
