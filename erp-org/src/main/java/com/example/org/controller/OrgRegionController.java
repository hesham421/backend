package com.example.org.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.org.dto.*;
import com.example.org.service.OrgRegionService;
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
@RequestMapping("/api/org/regions")
@RequiredArgsConstructor
@Tag(name = "Regions", description = "ORG - Region Management")
public class OrgRegionController {

    private final OrgRegionService service;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create region")
    public ResponseEntity<ApiResponse<RegionResponse>> create(@Valid @RequestBody RegionCreateRequest request) {
        return operationCode.craftResponse(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update region")
    public ResponseEntity<ApiResponse<RegionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RegionUpdateRequest request) {
        return operationCode.craftResponse(service.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get region by ID")
    public ResponseEntity<ApiResponse<RegionResponse>> getById(@PathVariable Long id) {
        return operationCode.craftResponse(service.getById(id));
    }

    @PostMapping("/search")
    @Operation(summary = "Search regions")
    public ResponseEntity<ApiResponse<Page<RegionResponse>>> search(@RequestBody RegionSearchRequest request) {
        return operationCode.craftResponse(service.search(request));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate region")
    public ResponseEntity<ApiResponse<RegionResponse>> activate(@PathVariable Long id) {
        return operationCode.craftResponse(service.activate(id));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate region")
    public ResponseEntity<ApiResponse<RegionResponse>> deactivate(@PathVariable Long id) {
        return operationCode.craftResponse(service.deactivate(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete region")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/usage")
    @Operation(summary = "Get region usage")
    public ResponseEntity<ApiResponse<RegionUsageResponse>> getUsage(@PathVariable Long id) {
        return operationCode.craftResponse(service.getUsage(id));
    }

    @GetMapping("/options")
    @Operation(summary = "List active region options")
    public ResponseEntity<ApiResponse<List<RegionOptionResponse>>> listOptions(
            @RequestParam(required = false) Long legalEntityId) {
        return operationCode.craftResponse(service.listOptions(legalEntityId));
    }
}
