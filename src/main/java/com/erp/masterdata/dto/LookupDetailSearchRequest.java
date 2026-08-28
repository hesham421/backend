package com.erp.masterdata.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import com.erp.common.search.SearchRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * API contract DTO for Lookup Detail search — converts the frontend's
 * {@code {filters:[{field,operator,value}], sorts, page, size}} shape into the backend's
 * {@code SearchFilter}/{@code Op} format.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LookupDetailSearchRequest extends BaseSearchContractRequest {

    /** masterLookupId filter field name */
    private static final String MASTER_LOOKUP_ID_FIELD = "masterLookupId";

    /**
     * Extract masterLookupId from filters (required for parent-child relationship)
     */
    public Long getMasterLookupId() {
        List<ContractFilter> allFilters = getFilters();
        if (allFilters == null) {
            return null;
        }
        return allFilters.stream()
            .filter(f -> MASTER_LOOKUP_ID_FIELD.equalsIgnoreCase(f.getField()))
            .findFirst()
            .map(f -> {
                Object value = f.getValue();
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                } else if (value instanceof String) {
                    try {
                        return Long.parseLong((String) value);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                return null;
            })
            .orElse(null);
    }

    /**
     * Map contract request into common-utils SearchRequest used by the service layer.
     * Excludes masterLookupId from filters (handled separately via getMasterLookupId)
     */
    @Override
    public SearchRequest toCommonSearchRequest() {
        return toCommonSearchRequest(Set.of(MASTER_LOOKUP_ID_FIELD));
    }
}
