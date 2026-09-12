package com.erp.fin.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.fin.dto.DimensionCreateRequest;
import com.erp.fin.dto.DimensionResponse;
import com.erp.fin.dto.DimensionSearchRequest;
import com.erp.fin.dto.DimensionValueCreateRequest;
import com.erp.fin.dto.DimensionValueResponse;
import com.erp.fin.dto.DimensionValueSearchRequest;
import com.erp.fin.service.DimensionService;
import com.erp.fin.service.DimensionValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin controller for API-FIN-006 and API-FIN-007 (SVC-API-CRUD.md).
 *
 * <p>A.6.9 — the child endpoint lives here, on the parent's controller: creating a dimension value
 * is {@code createDimensionValue} on this class, delegating to {@link DimensionValueService}.
 * There is deliberately no {@code DimensionValueController}; the skill folds controllers, not
 * services.
 *
 * <p>Dimension and dimension-value search (API-FIN-005, API-FIN-008) were added here by the
 * SVC-API-SEARCH sub, both POST {@code /search} with a body (A.6.6). API-FIN-008 is the CHILD
 * search and sits on this parent controller too (A.6.9); its parent dimension id travels inside
 * the request body's filters, never as a path variable — see
 * {@code DimensionValueSearchRequest}.
 */
@RestController
@RequestMapping("/api/v1/fin/dimensions")
@RequiredArgsConstructor
@Tag(name = "FIN Dimension Management", description = "Analysis dimensions - الأبعاد التحليلية")
public class DimensionController {

    private final DimensionService service;
    private final DimensionValueService dimensionValueService;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create dimension", description = "إنشاء بُعد تحليلي")
    public ResponseEntity<ApiResponse<DimensionResponse>> create(
            @Valid @RequestBody DimensionCreateRequest request) {
        return operationCode.craftResponse(service.create(request));
    }

    @PostMapping("/{id}/values")
    @Operation(summary = "Create dimension value", description = "إنشاء قيمة ضمن بُعد تحليلي")
    public ResponseEntity<ApiResponse<DimensionValueResponse>> createDimensionValue(
            @PathVariable("id") Long dimensionId,
            @Valid @RequestBody DimensionValueCreateRequest request) {
        return operationCode.craftResponse(dimensionValueService.create(dimensionId, request));
    }

    @PostMapping("/search")
    @Operation(summary = "Search dimensions", description = "بحث في الأبعاد التحليلية")
    public ResponseEntity<ApiResponse<Page<DimensionResponse>>> search(
            @Valid @RequestBody DimensionSearchRequest searchRequest) {
        return operationCode.craftResponse(service.search(searchRequest));
    }

    @PostMapping("/values/search")
    @Operation(summary = "Search dimension values",
        description = "بحث في قيم البُعد التحليلي — معرّف البُعد يُرسَل ضمن مرشِّحات الطلب")
    public ResponseEntity<ApiResponse<Page<DimensionValueResponse>>> searchDimensionValues(
            @Valid @RequestBody DimensionValueSearchRequest searchRequest) {
        return operationCode.craftResponse(dimensionValueService.search(searchRequest));
    }
}
