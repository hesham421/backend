package com.erp.masterdata.controller;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.masterdata.dto.LookupValueResponse;
import com.erp.masterdata.service.LookupConsumptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Generic read-only lookup-value access for consumption by all ERP modules; responses are
 * cached for performance.
 */
@RestController
@RequestMapping("/api/lookups")
@RequiredArgsConstructor
@Tag(name = "Lookup Consumption", description = "استهلاك القوائم المرجعية - Generic Lookup Access for All Modules")
public class LookupConsumptionController {

    private final LookupConsumptionService lookupConsumptionService;
    private final OperationCode operationCode;

    /**
     * Returns only ACTIVE lookup details, ordered by sortOrder; response is cached.
     */
    @GetMapping("/{lookupCode}")
    @Operation(
        summary = "Get Lookup Values", 
        description = "جلب قيم القائمة المرجعية - للاستخدام في جميع الوحدات"
    )
    public ResponseEntity<ApiResponse<List<LookupValueResponse>>> getLookupValues(
        @PathVariable("lookupCode")
        @Parameter(
            description = "Master lookup code (e.g., ACCOUNT_TYPE, STATUS, COUNTRY)",
            example = "ACCOUNT_TYPE"
        )
        String lookupCode
    ) {
        ServiceResult<List<LookupValueResponse>> result = lookupConsumptionService.fetchLookupValues(lookupCode);
        return operationCode.craftResponse(result);
    }
}
