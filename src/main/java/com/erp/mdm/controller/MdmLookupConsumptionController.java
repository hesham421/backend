package com.erp.mdm.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.mdm.dto.LookupValueLite;
import com.erp.mdm.service.MdmLookupValueService;
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
 * Thin controller for API-MDM-011 — the platform-wide lookup provider endpoint. Any authenticated
 * caller may read the ACTIVE values under an ACTIVE type by the type's natural key; it is NOT gated
 * by SCR-MDM-001 permissions (DRV-006, enforced by the service's {@code isAuthenticated()} gate).
 * The endpoint is always active-only, so the spec's optional {@code active=true} query param is
 * omitted (it has no alternative behavior to select). Pure delegation — zero business logic.
 */
@RestController
@RequestMapping("/api/v1/mdm/lookups")
@RequiredArgsConstructor
@Tag(name = "MDM Lookup Consumption", description = "Platform-wide reference-data provider - مزود البيانات المرجعية للمنصة")
public class MdmLookupConsumptionController {

    private final MdmLookupValueService service;
    private final OperationCode operationCode;

    @GetMapping("/{typeCode}")
    @Operation(summary = "Get active lookup values by type code",
        description = "إرجاع القيم المرجعية النشطة ضمن نوع نشط حسب رمز النوع")
    public ResponseEntity<ApiResponse<List<LookupValueLite>>> getActiveValues(@PathVariable String typeCode) {
        return operationCode.craftResponse(service.findActiveByTypeCode(typeCode));
    }
}
