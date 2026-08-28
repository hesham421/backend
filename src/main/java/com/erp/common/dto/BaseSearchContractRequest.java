package com.erp.common.dto;

import com.erp.common.search.Op;
import com.erp.common.search.SearchException;
import com.erp.common.search.SearchFilter;
import com.erp.common.search.SearchRequest;
import com.erp.common.exception.CommonErrorCodes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Base class for API-contract search request DTOs; maps the frontend's
 * {@code { filters, sorts, page, size }} shape into the common-utils {@link SearchRequest}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseSearchContractRequest {

    private List<ContractFilter> filters = new ArrayList<>();
    private List<ContractSort> sorts = new ArrayList<>();
    private int page = 0;
    private int size = 20;

    /**
     * Operator aliases accepted from the frontend contract.
     */
    private static final Set<String> EQUALS_ALIASES = Set.of("EQUALS", "EQ");
    private static final Set<String> CONTAINS_ALIASES = Set.of("CONTAINS", "LIKE");
    private static final Set<String> STARTS_WITH_ALIASES = Set.of("STARTS_WITH");

    public SearchRequest toCommonSearchRequest() {
        return toCommonSearchRequest(Set.of());
    }

    /**
     * {@code excludeFields} matching is case-insensitive.
     */
    protected SearchRequest toCommonSearchRequest(Set<String> excludeFields) {
        SearchRequest req = new SearchRequest();
        req.setPage(this.page);
        req.setSize(this.size);

        // Map sorts → sortBy / sortDir (common SearchRequest supports single sort)
        if (sorts != null && !sorts.isEmpty() && sorts.get(0) != null) {
            ContractSort s = sorts.get(0);
            req.setSortBy(s.getField());
            req.setSortDir(s.getDirection());
        }

        // Map filters → SearchFilter with Op
        if (filters != null && !filters.isEmpty()) {
            List<SearchFilter> mapped = new ArrayList<>();
            for (ContractFilter f : filters) {
                if (f == null || f.getField() == null || f.getOperator() == null) {
                    continue;
                }
                if (excludeFields.stream().anyMatch(ex -> ex.equalsIgnoreCase(f.getField()))) {
                    continue;
                }
                Op op = mapOperator(f.getOperator());
                mapped.add(new SearchFilter(f.getField(), op, f.getValue()));
            }
            req.setFilters(mapped);
        }

        return req;
    }

    /**
     * Map frontend operator string to common-utils Op enum.
     */
    protected static Op mapOperator(String operator) {
        String normalized = operator.trim().toUpperCase();
        if (EQUALS_ALIASES.contains(normalized)) return Op.EQ;
        if (CONTAINS_ALIASES.contains(normalized)) return Op.LIKE;
        if (STARTS_WITH_ALIASES.contains(normalized)) return Op.STARTS_WITH;
        throw new SearchException(
            CommonErrorCodes.SEARCH_INVALID_OPERATOR,
            "Invalid operator '" + operator + "'. Allowed: EQUALS, CONTAINS, STARTS_WITH"
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractFilter {
        private String field;
        private String operator;
        private Object value;
    }

    /**
     * Sort DTO matching frontend contract: { field, direction }
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractSort {
        private String field;
        private String direction = "ASC";
    }
}
