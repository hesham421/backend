package com.erp.security.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.security.dto.LookupResponse;
import com.erp.security.service.LookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin controller for API-SEC-016 (Lookups / LOV values). Single read-only endpoint resolving a
 * {@code lookupKey} to its LOV entries; all logic lives in {@link LookupService}. Base path taken
 * literally from the SVC-API spec / Plan Index ({@code /api/v1/security/lookups}) rather than the
 * build-create-controller default template — an already-confirmed plan-level path decision.
 *
 * <p>Authenticated (no specific authority) — SecurityConfig's JWT filter gates the path since it
 * is not on the public allow-list, so the service carries no {@code @PreAuthorize} (see its
 * javadoc). No POST /search, activate/deactivate, delete or usage endpoints: this resource is a
 * runtime code registry with no entity lifecycle, so those build-create-controller endpoints do
 * not apply.
 */
@RestController
@RequestMapping("/api/v1/security/lookups")
@RequiredArgsConstructor
@Tag(name = "Security Lookups", description = "Runtime SEC lookup-of-values (LOV) resolution - قوائم القيم المرجعية للأمن")
public class LookupController {

    private final LookupService lookupService;
    private final OperationCode operationCode;

    @GetMapping("/{lookupKey}")
    @Operation(summary = "Resolve lookup values", description = "جلب قيم قائمة مرجعية حسب المفتاح")
    public ResponseEntity<ApiResponse<List<LookupResponse>>> get(@PathVariable String lookupKey) {
        return operationCode.craftResponse(lookupService.get(lookupKey));
    }
}
