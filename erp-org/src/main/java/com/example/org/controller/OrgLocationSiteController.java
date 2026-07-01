package com.example.org.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.org.dto.*;
import com.example.org.service.OrgLocationSiteService;
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
@RequestMapping("/api/org/location-sites")
@RequiredArgsConstructor
@Tag(name = "Location Sites", description = "ORG - Location Site Management")
public class OrgLocationSiteController {

    private final OrgLocationSiteService service;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create location site")
    public ResponseEntity<ApiResponse<LocationSiteResponse>> create(@Valid @RequestBody LocationSiteCreateRequest request) {
        return operationCode.craftResponse(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update location site")
    public ResponseEntity<ApiResponse<LocationSiteResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody LocationSiteUpdateRequest request) {
        return operationCode.craftResponse(service.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get location site by ID")
    public ResponseEntity<ApiResponse<LocationSiteResponse>> getById(@PathVariable Long id) {
        return operationCode.craftResponse(service.getById(id));
    }

    @PostMapping("/search")
    @Operation(summary = "Search location sites")
    public ResponseEntity<ApiResponse<Page<LocationSiteResponse>>> search(@RequestBody LocationSiteSearchRequest request) {
        return operationCode.craftResponse(service.search(request));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate location site")
    public ResponseEntity<ApiResponse<LocationSiteResponse>> activate(@PathVariable Long id) {
        return operationCode.craftResponse(service.activate(id));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate location site")
    public ResponseEntity<ApiResponse<LocationSiteResponse>> deactivate(@PathVariable Long id) {
        return operationCode.craftResponse(service.deactivate(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete location site")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/usage")
    @Operation(summary = "Get location site usage")
    public ResponseEntity<ApiResponse<LocationSiteUsageResponse>> getUsage(@PathVariable Long id) {
        return operationCode.craftResponse(service.getUsage(id));
    }

    @GetMapping("/options")
    @Operation(summary = "List active location site options")
    public ResponseEntity<ApiResponse<List<LocationSiteOptionResponse>>> listOptions(
            @RequestParam(required = false) Long branchId) {
        return operationCode.craftResponse(service.listOptions(branchId));
    }
}
