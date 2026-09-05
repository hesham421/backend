package com.erp.mdm.service;

import com.erp.common.search.SearchFilter;
import com.erp.common.search.SearchOperator;
import java.util.ArrayList;
import java.util.List;

/**
 * Module-internal helpers shared by the two MDM lookup services, so the DRV-011 default-active
 * policy and the canonical code normalization live in exactly one place. Package-private — never
 * leaves {@code com.erp.mdm.service}.
 */
final class MdmSearchSupport {

    private MdmSearchSupport() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    /**
     * Canonical uppercase form the entities' {@code @PrePersist}/{@code @PreUpdate} always apply
     * before persisting, so uniqueness/consumption probes match the stored value.
     */
    static String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    /**
     * DRV-011 — default to active-only when the caller supplies no {@code isActive} filter. Returns
     * a new list (the caller's own filter list is never mutated) with the default appended when
     * absent; an existing {@code isActive} filter (any operator/value) suppresses the default.
     */
    static List<SearchFilter> withDefaultActiveFilter(List<SearchFilter> requested) {
        List<SearchFilter> filters = new ArrayList<>(requested == null ? List.of() : requested);
        boolean hasActiveFilter = filters.stream()
            .anyMatch(f -> f != null && "isActive".equals(f.getField()));
        if (!hasActiveFilter) {
            filters.add(SearchFilter.builder()
                .field("isActive")
                .operator(SearchOperator.EQUALS)
                .value(Boolean.TRUE)
                .build());
        }
        return filters;
    }
}
