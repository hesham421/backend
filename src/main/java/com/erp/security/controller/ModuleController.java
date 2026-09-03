package com.erp.security.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.security.dto.ModuleCreateRequest;
import com.erp.security.dto.ModuleResponse;
import com.erp.security.dto.ModuleSearchRequest;
import com.erp.security.dto.ModuleUpdateRequest;
import com.erp.security.service.ModuleService;
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
 * Thin controller for API-SEC-020 (Module Registry CRUD, SCR-SEC-004). Base path taken literally
 * from the SVC-API spec / Plan Index ({@code /api/v1/security/modules}). DELETE maps onto the
 * service's soft deactivate (204). No activate/usage endpoints — the module registry has no
 * reactivation API nor referencing-entity usage view in this spec.
 */
@RestController
@RequestMapping("/api/v1/security/modules")
@RequiredArgsConstructor
@Tag(name = "Module Registry", description = "Security module registry (Tier-1 grantable units) - سجل موديولات الأمان")
public class ModuleController {

    private final ModuleService moduleService;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create module", description = "إنشاء موديل جديد")
    public ResponseEntity<ApiResponse<ModuleResponse>> create(
            @Valid @RequestBody ModuleCreateRequest request) {
        return operationCode.craftResponse(moduleService.create(request));
    }

    @PostMapping("/search")
    @Operation(summary = "Search modules", description = "البحث في الموديولات")
    public ResponseEntity<ApiResponse<Page<ModuleResponse>>> search(
            @Valid @RequestBody ModuleSearchRequest searchRequest) {
        return operationCode.craftResponse(moduleService.search(searchRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update module", description = "تحديث موديل حسب المعرف")
    public ResponseEntity<ApiResponse<ModuleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ModuleUpdateRequest request) {
        return operationCode.craftResponse(moduleService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get module by ID", description = "جلب موديل حسب المعرف")
    public ResponseEntity<ApiResponse<ModuleResponse>> getById(@PathVariable Long id) {
        return operationCode.craftResponse(moduleService.getById(id));
    }

    /**
     * API-SEC-020 DELETE — soft deactivate (isActiveFl = false). Mirrors build-create-controller's
     * delete() shape (204, void, no wrapped response) while calling the service's deactivate().
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate module", description = "إلغاء تفعيل موديل حسب المعرف")
    public void deactivate(@PathVariable Long id) {
        moduleService.deactivate(id);
    }
}
