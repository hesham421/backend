package com.erp.security.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import com.erp.common.search.SearchRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API-SEC-013 search request (POST /pages/search). moduleFk (QR-SEC-0012 EXACT) is the owning
 * module — a nested association on the Page entity ({@code page.module.id}), not a scalar column —
 * so it is excluded from the generic SpecBuilder filter set and exposed via {@link #getModuleId()}
 * for the service to apply as an explicit Specification join (build-create-service A.5.17). The
 * scalar filters pageCode (LIKE) / isActive (EXACT) flow through the inherited generic filters.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Search request for Page - طلب بحث الشاشات")
public class PageSearchRequest extends BaseSearchContractRequest {

    @Override
    public SearchRequest toCommonSearchRequest() {
        return toCommonSearchRequest(Set.of("moduleFk"));
    }

    /** Extracts the moduleFk EXACT filter value, applied by the service as an explicit join. */
    public Long getModuleId() {
        return extractLongFilter("moduleFk");
    }
}
