package com.erp.sec.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import com.erp.common.search.SearchFilter;
import com.erp.common.search.SearchRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API-SEC-021 search body. The paged root is the module, so a {@code pageCode} filter — a column of
 * the child screen — is lifted out of the generic set and applied by the service as an EXISTS
 * subquery. No {@code @AllArgsConstructor} — the class adds no field of its own, so it would collide
 * with the no-arg constructor.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Search request for the module/screen/action registry - طلب بحث سجل الوحدات")
public class RegistrySearchRequest extends BaseSearchContractRequest {

    private static final String PAGE_CODE = "pageCode";

    @Override
    public SearchRequest toCommonSearchRequest() {
        return toCommonSearchRequest(Set.of(PAGE_CODE));
    }

    public String getPageCode() {
        return extractStringFilter(PAGE_CODE);
    }

    private String extractStringFilter(String field) {
        List<SearchFilter> current = getFilters();
        if (current == null) {
            return null;
        }
        return current.stream()
            .filter(f -> f != null && field.equals(f.getField()) && f.getValue() != null)
            .map(f -> String.valueOf(f.getValue()))
            .findFirst()
            .orElse(null);
    }
}
