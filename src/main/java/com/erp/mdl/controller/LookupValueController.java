package com.erp.mdl.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.mdl.dto.LookupValueCreateRequest;
import com.erp.mdl.dto.LookupValueReorderRequest;
import com.erp.mdl.dto.LookupValueResponse;
import com.erp.mdl.dto.LookupValueUpdateRequest;
import com.erp.mdl.service.LookupValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin controller for API-MDL-006/007/008/009 (SVC-API-CRUD.md) plus API-MDL-005
 * (SVC-API-SEARCH.md). Path asymmetry is deliberate, taken verbatim from the spec's own endpoint
 * lines, not normalized into one shape: create, search and reorder nest under
 * {@code /lookup-types/{id}/...} (the parent-scoped operations), while update and deactivate
 * address the value directly at the flat {@code /lookup-values/{id}}.
 *
 * <p>Deviation: API-MDL-008 (deactivate) returns 200 with the crafted
 * {@code ApiResponse<LookupValueResponse>} body, same reasoning and same named deviation as
 * {@code LookupTypeController.deactivate} — SVC-API-CRUD.md's response line is explicit: "200 ·
 * confirmation {lookupValuePk, isActiveFl: false}".
 *
 * <p>Deviation: {@code search} is {@code GET} with {@code @RequestParam} query params, NOT
 * {@code build-create-controller}'s generic {@code POST /search} + {@code @RequestBody}
 * template — SVC-API-SEARCH.md's API-MDL-005 line is explicit: "GET
 * /api/v1/mdl/lookup-types/{id}/values" with query params. Named, spec-driven deviation, same
 * reasoning as {@code LookupTypeController}'s.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Lookup Value Management", description = "Master data lookup values - قيم بيانات اللوكب")
public class LookupValueController {

    private final LookupValueService service;
    private final OperationCode operationCode;

    @PostMapping("/api/v1/mdl/lookup-types/{id}/values")
    @Operation(summary = "Create lookup value", description = "إنشاء قيمة لوكب جديدة ضمن نوع")
    public ResponseEntity<ApiResponse<LookupValueResponse>> create(
            @PathVariable("id") Long lookupTypeId,
            @Valid @RequestBody LookupValueCreateRequest request) {
        return operationCode.craftResponse(service.create(lookupTypeId, request));
    }

    @PutMapping("/api/v1/mdl/lookup-values/{id}")
    @Operation(summary = "Update lookup value", description = "تعديل قيمة لوكب")
    public ResponseEntity<ApiResponse<LookupValueResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody LookupValueUpdateRequest request) {
        return operationCode.craftResponse(service.update(id, request));
    }

    @DeleteMapping("/api/v1/mdl/lookup-values/{id}")
    @Operation(summary = "Deactivate lookup value", description = "إلغاء تفعيل قيمة لوكب")
    public ResponseEntity<ApiResponse<LookupValueResponse>> deactivate(@PathVariable Long id) {
        return operationCode.craftResponse(service.deactivate(id));
    }

    @PatchMapping("/api/v1/mdl/lookup-types/{id}/values/reorder")
    @Operation(summary = "Reorder lookup values", description = "إعادة ترتيب قيم نوع اللوكب")
    public ResponseEntity<ApiResponse<List<LookupValueResponse>>> reorder(
            @PathVariable("id") Long lookupTypeId,
            @Valid @RequestBody LookupValueReorderRequest request) {
        return operationCode.craftResponse(service.reorder(lookupTypeId, request));
    }

    @GetMapping("/api/v1/mdl/lookup-types/{id}/values")
    @Operation(summary = "Search lookup values of a type", description = "بحث في قيم نوع لوكب")
    public ResponseEntity<ApiResponse<Page<LookupValueResponse>>> search(
            @PathVariable("id") Long lookupTypeId,
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        return operationCode.craftResponse(service.search(lookupTypeId, code, page, size, sort));
    }
}
