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
 * API-SEC-005 search body. A {@code fullName} filter is lifted out of the generic set because it
 * matches fullNameAr OR fullNameEn and {@code SearchOperator} has no OR; every other filter reaches
 * SpecBuilder untouched. No {@code @AllArgsConstructor} — the class adds no field of its own, so it
 * would collide with the no-arg constructor.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Search request for User - طلب بحث المستخدمين")
public class UserSearchRequest extends BaseSearchContractRequest {

    private static final String FULL_NAME = "fullName";

    @Override
    public SearchRequest toCommonSearchRequest() {
        return toCommonSearchRequest(Set.of(FULL_NAME));
    }

    public String getFullName() {
        return extractStringFilter(FULL_NAME);
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
