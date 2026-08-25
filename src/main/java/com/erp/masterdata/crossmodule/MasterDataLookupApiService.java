package com.erp.masterdata.crossmodule;

import com.erp.common.domain.status.ServiceResult;
import com.erp.masterdata.dto.LookupValueResponse;
import com.erp.masterdata.service.LookupConsumptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Kept separate from {@link LookupConsumptionService} so the cross-module contract surface
 * stays intentionally narrow and doesn't grow un-reviewed as that internal service evolves.
 */
@Service
@RequiredArgsConstructor
class MasterDataLookupApiService implements MasterDataLookupApi {

    private final LookupConsumptionService lookupConsumptionService;

    /**
     * Pure read, no write on this side — {@code REQUIRED} propagation (the default) is safe
     * and simpler than {@code REQUIRES_NEW} for callers that consult this before writing
     * anything themselves.
     */
    @Override
    @Transactional(readOnly = true)
    public List<LookupValueView> getActiveValues(String lookupCode) {
        ServiceResult<List<LookupValueResponse>> result = lookupConsumptionService.fetchLookupValues(lookupCode);
        List<LookupValueResponse> values = result.getData();
        if (values == null) {
            return List.of();
        }
        return values.stream().map(v -> new LookupValueView(v.getCode())).toList();
    }
}
