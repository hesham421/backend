package com.example.erp.org.controller;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.erp.org.dto.LegalEntityCreateRequest;
import com.example.erp.org.dto.LegalEntityResponse;
import com.example.erp.org.dto.LegalEntitySearchRequest;
import com.example.erp.org.dto.LegalEntityUpdateRequest;
import com.example.erp.org.service.LegalEntityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/org/legal-entities")
@RequiredArgsConstructor
@Tag(name = "Legal Entity Management", description = "إدارة الكيانات القانونية - Legal Entity Management API")
public class LegalEntityController {

    private final LegalEntityService legalEntityService;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create Legal Entity", description = "إنشاء كيان قانوني جديد")
    public ResponseEntity<ApiResponse<LegalEntityResponse>> create(
            @Valid @RequestBody LegalEntityCreateRequest request) {
        ServiceResult<LegalEntityResponse> result = legalEntityService.create(request);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Legal Entity", description = "تحديث كيان قانوني")
    public ResponseEntity<ApiResponse<LegalEntityResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody LegalEntityUpdateRequest request) {
        ServiceResult<LegalEntityResponse> result = legalEntityService.update(id, request);
        return operationCode.craftResponse(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Legal Entity by ID", description = "جلب كيان قانوني بالمعرف")
    public ResponseEntity<ApiResponse<LegalEntityResponse>> getById(@PathVariable Long id) {
        ServiceResult<LegalEntityResponse> result = legalEntityService.getById(id);
        return operationCode.craftResponse(result);
    }

    @PostMapping("/search")
    @Operation(summary = "Search Legal Entities", description = "بحث في الكيانات القانونية")
    public ResponseEntity<ApiResponse<Page<LegalEntityResponse>>> search(
            @Valid @RequestBody LegalEntitySearchRequest searchRequest) {
        ServiceResult<Page<LegalEntityResponse>> result = legalEntityService.search(searchRequest);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate Legal Entity", description = "تفعيل كيان قانوني")
    public ResponseEntity<ApiResponse<LegalEntityResponse>> activate(@PathVariable Long id) {
        ServiceResult<LegalEntityResponse> result = legalEntityService.activate(id);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate Legal Entity", description = "إلغاء تفعيل كيان قانوني")
    public ResponseEntity<ApiResponse<LegalEntityResponse>> deactivate(@PathVariable Long id) {
        ServiceResult<LegalEntityResponse> result = legalEntityService.deactivate(id);
        return operationCode.craftResponse(result);
    }
}
