package com.example.org.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.org.dto.*;
import com.example.org.service.OrgRegionTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/org/region-types")
@RequiredArgsConstructor
@Tag(name = "Region Types", description = "ORG - Region Type Management")
public class OrgRegionTypeController {

    private final OrgRegionTypeService service;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create region type")
    public ResponseEntity<ApiResponse<RegionTypeResponse>> create(@Valid @RequestBody RegionTypeCreateRequest request) {
        return operationCode.craftResponse(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update region type")
    public ResponseEntity<ApiResponse<RegionTypeResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RegionTypeUpdateRequest request) {
        return operationCode.craftResponse(service.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get region type by ID")
    public ResponseEntity<ApiResponse<RegionTypeResponse>> getById(@PathVariable Long id) {
        return operationCode.craftResponse(service.getById(id));
    }

    @PostMapping("/search")
    @Operation(summary = "Search region types")
    public ResponseEntity<ApiResponse<Page<RegionTypeResponse>>> search(@RequestBody RegionTypeSearchRequest request) {
        return operationCode.craftResponse(service.search(request));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate region type")
    public ResponseEntity<ApiResponse<RegionTypeResponse>> activate(@PathVariable Long id) {
        return operationCode.craftResponse(service.activate(id));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate region type")
    public ResponseEntity<ApiResponse<RegionTypeResponse>> deactivate(@PathVariable Long id) {
        return operationCode.craftResponse(service.deactivate(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete region type")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/usage")
    @Operation(summary = "Get region type usage")
    public ResponseEntity<ApiResponse<RegionTypeUsageResponse>> getUsage(@PathVariable Long id) {
        return operationCode.craftResponse(service.getUsage(id));
    }

    @GetMapping("/options")
    @Operation(summary = "List active region type options")
    public ResponseEntity<ApiResponse<List<RegionTypeOptionResponse>>> listOptions() {
        return operationCode.craftResponse(service.listOptions());
    }
}
