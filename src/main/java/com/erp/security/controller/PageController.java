package com.erp.security.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.security.dto.PageCreateRequest;
import com.erp.security.dto.PageResponse;
import com.erp.security.dto.PageSearchRequest;
import com.erp.security.dto.PageUpdateRequest;
import com.erp.security.service.PageService;
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
 * Thin controller for API-SEC-013 (Page/screen registry CRUD, CORE-9 owner, SCR-SEC-003). Base path
 * from the SVC-API spec ({@code /api/v1/security/pages}). DELETE maps onto the service's soft
 * deactivate (204). Permission auto-generation (RULE-SEC-011) happens inside the service on create.
 */
@RestController
@RequestMapping("/api/v1/security/pages")
@RequiredArgsConstructor
@Tag(name = "Page Registry", description = "Security screen/page registry (CORE-9) - سجل شاشات الأمان")
public class PageController {

    private final PageService pageService;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create page", description = "إنشاء شاشة جديدة")
    public ResponseEntity<ApiResponse<PageResponse>> create(
            @Valid @RequestBody PageCreateRequest request) {
        return operationCode.craftResponse(pageService.create(request));
    }

    @PostMapping("/search")
    @Operation(summary = "Search pages", description = "البحث في الشاشات")
    public ResponseEntity<ApiResponse<Page<PageResponse>>> search(
            @Valid @RequestBody PageSearchRequest searchRequest) {
        return operationCode.craftResponse(pageService.search(searchRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update page", description = "تحديث شاشة حسب المعرف")
    public ResponseEntity<ApiResponse<PageResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PageUpdateRequest request) {
        return operationCode.craftResponse(pageService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get page by ID", description = "جلب شاشة حسب المعرف")
    public ResponseEntity<ApiResponse<PageResponse>> getById(@PathVariable Long id) {
        return operationCode.craftResponse(pageService.getById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate page", description = "إلغاء تفعيل شاشة حسب المعرف")
    public void deactivate(@PathVariable Long id) {
        pageService.deactivate(id);
    }
}
