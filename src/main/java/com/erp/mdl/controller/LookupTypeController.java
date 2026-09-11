package com.erp.mdl.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.mdl.dto.LookupTypeCreateRequest;
import com.erp.mdl.dto.LookupTypeResponse;
import com.erp.mdl.dto.LookupTypeUpdateRequest;
import com.erp.mdl.dto.OwnerGroupResponse;
import com.erp.mdl.service.LookupTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin controller for API-MDL-002/003/004 (SVC-API-CRUD.md) plus API-MDL-001/010
 * (SVC-API-SEARCH.md).
 *
 * <p>Deviation: API-MDL-004 (deactivate) returns 200 with the crafted
 * {@code ApiResponse<LookupTypeResponse>} body, NOT {@code build-create-controller}'s generic
 * delete template ({@code @ResponseStatus(NO_CONTENT)} + {@code void}). SVC-API-CRUD.md's own
 * response line is explicit: "200 · confirmation {lookupTypePk, isActiveFl: false}" — a
 * soft-deactivate with a body confirmation, not a hard delete. Named, spec-driven deviation.
 *
 * <p>Deviation: {@code search}/{@code browseByOwner} are {@code GET} with {@code @RequestParam}
 * query params, NOT {@code build-create-controller}'s generic {@code POST /search} +
 * {@code @RequestBody} template. SVC-API-SEARCH.md's own endpoint lines are explicit — API-MDL-001
 * is "GET /api/v1/mdl/lookup-types" and API-MDL-010 is "GET /api/v1/mdl/lookup-types/by-owner",
 * both with query params, not a search-request body — this module's own binding spec overrides
 * the skill's generic template for these two endpoints. Named, spec-driven deviation.
 */
@RestController
@RequestMapping("/api/v1/mdl/lookup-types")
@RequiredArgsConstructor
@Tag(name = "Lookup Type Management", description = "Master data lookup types - أنواع بيانات اللوكب الرئيسية")
public class LookupTypeController {

    private final LookupTypeService service;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create lookup type", description = "إنشاء نوع لوكب جديد")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> create(
            @Valid @RequestBody LookupTypeCreateRequest request) {
        return operationCode.craftResponse(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update lookup type", description = "تعديل أسماء نوع اللوكب")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody LookupTypeUpdateRequest request) {
        return operationCode.craftResponse(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate lookup type", description = "إلغاء تفعيل نوع اللوكب")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> deactivate(@PathVariable Long id) {
        return operationCode.craftResponse(service.deactivate(id));
    }

    @GetMapping
    @Operation(summary = "Search lookup types", description = "بحث في أنواع اللوكب")
    public ResponseEntity<ApiResponse<Page<LookupTypeResponse>>> search(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String ownerModuleCode,
            @RequestParam(required = false) Boolean isActiveFl,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        return operationCode.craftResponse(service.search(key, ownerModuleCode, isActiveFl, page, size, sort));
    }

    @GetMapping("/by-owner")
    @Operation(summary = "Browse lookup type registry by owner", description = "استعراض سجل أنواع اللوكب حسب المالك")
    public ResponseEntity<ApiResponse<List<OwnerGroupResponse>>> browseByOwner(
            @RequestParam(required = false) String ownerModuleCode,
            @RequestParam(required = false) String key) {
        return operationCode.craftResponse(service.browseByOwner(ownerModuleCode, key));
    }
}
