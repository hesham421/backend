package com.example.security.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.erp.common.web.util.PageableUtils;
import com.example.security.dto.CreateSecUserProfileRequest;
import com.example.security.dto.SecUserProfileDto;
import com.example.security.dto.SecUserProfileSearchContractRequest;
import com.example.security.dto.UpdateSecUserProfileRequest;
import com.example.security.service.SecUserProfileService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for SEC_USER_PROFILE (DataScope — API-SEC-032..035).
 *
 * Thin controller — all logic, including {@code @PreAuthorize} permission gates
 * (Phase SEC, Section 8.1 Permissions Matrix), lives in {@link SecUserProfileService}
 * per this codebase's A.5.2 service-contract convention (governance-repo
 * enforce-backend-contract skill) — controllers never carry @PreAuthorize.
 */
@RestController
@RequestMapping("/api/v1/security/user-profiles")
@RequiredArgsConstructor
@Tag(name = "Security - DataScope - User Profiles", description = "إدارة ملفات تعريف المستخدمين وإسناد الفروع - User Profile / Branch Assignment Management")
public class SecUserProfileController {

    private final SecUserProfileService service;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create user profile", description = "إنشاء ملف تعريف مستخدم وإسناد فرع - RULE-SEC-034")
    public ResponseEntity<ApiResponse<SecUserProfileDto>> create(@Valid @RequestBody CreateSecUserProfileRequest request) {
        return operationCode.craftResponse(service.create(request));
    }

    @GetMapping
    @Operation(
        summary = "List user profiles",
        description = "Get paginated list of user profiles. Allowed sort fields: userIdFk, branchIdFk, isActiveFl, createdAt."
    )
    public ResponseEntity<ApiResponse<Page<SecUserProfileDto>>> list(
            @Parameter(description = "Pagination parameters. Sort format: 'fieldName,asc' or 'fieldName,desc'.",
                       schema = @Schema(implementation = Pageable.class))
            @PageableDefault(size = 20, sort = "userIdFk", direction = Sort.Direction.ASC) Pageable pageable) {
        pageable = PageableUtils.enforceConstraints(pageable);
        return operationCode.craftResponse(service.listProfiles(pageable));
    }

    @PostMapping("/search")
    @Operation(summary = "Search user profiles", description = "بحث في ملفات تعريف المستخدمين")
    public ResponseEntity<ApiResponse<Page<SecUserProfileDto>>> search(@RequestBody SecUserProfileSearchContractRequest searchRequest) {
        return operationCode.craftResponse(service.search(searchRequest.toCommonSearchRequest()));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user profile by user ID", description = "جلب ملف تعريف مستخدم بالمعرف")
    public ResponseEntity<ApiResponse<SecUserProfileDto>> getById(@PathVariable Long userId) {
        return operationCode.craftResponse(service.getById(userId));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user profile", description = "تحديث ملف تعريف مستخدم - RULE-SEC-034")
    public ResponseEntity<ApiResponse<SecUserProfileDto>> update(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateSecUserProfileRequest request) {
        return operationCode.craftResponse(service.update(userId, request));
    }
}
