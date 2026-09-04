package com.erp.common.dto;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.CommonErrorCodes;
import com.erp.common.exception.LocalizedException;
import com.erp.common.search.SearchFilter;
import com.erp.common.search.SearchRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Sort;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Base search/filter contract - العقد الأساسي للبحث والتصفية")
public class BaseSearchContractRequest {

    @Builder.Default
    @Schema(description = "Filter criteria - معايير التصفية")
    private List<SearchFilter> filters = List.of();

    @Schema(description = "Sort field - حقل الترتيب")
    private String sortField;

    @Builder.Default
    @Schema(description = "Sort direction - اتجاه الترتيب")
    private Sort.Direction sortDirection = Sort.Direction.ASC;

    @Builder.Default
    @Schema(description = "Page number, zero-based - رقم الصفحة", example = "0")
    private int page = 0;

    @Builder.Default
    @Schema(description = "Page size - حجم الصفحة", example = "20")
    private int size = 20;

    public SearchRequest toCommonSearchRequest() {
        return toCommonSearchRequest(Set.of());
    }

    public SearchRequest toCommonSearchRequest(Set<String> excludeFields) {
        List<SearchFilter> effectiveFilters = filters == null ? List.of() : filters.stream()
            .filter(f -> f != null && f.getField() != null && !excludeFields.contains(f.getField()))
            .toList();
        return SearchRequest.builder()
            .filters(effectiveFilters)
            .sortField(sortField)
            .sortDirection(sortDirection != null ? sortDirection : Sort.Direction.ASC)
            .page(page)
            .size(size)
            .build();
    }

    protected Long extractLongFilter(String field) {
        if (filters == null) {
            return null;
        }
        return filters.stream()
            .filter(f -> f != null && field.equals(f.getField()))
            .map(SearchFilter::getValue)
            // Only scalar values yield a single Long — an IN-list (Iterable/array) value is not a
            // scalar id filter, so skip it rather than blow up parsing "[1, 2]".
            .filter(v -> v != null && !(v instanceof Iterable<?>) && !v.getClass().isArray())
            .map(BaseSearchContractRequest::toLong)
            .filter(v -> v != null)
            .findFirst()
            .orElse(null);
    }

    /**
     * Parses a scalar filter value to a Long. A malformed (non-numeric) value is a client input
     * error, so it surfaces as a 400 VALIDATION_ERROR rather than an unhandled 500 from a raw
     * NumberFormatException escaping through the specification build.
     */
    private static Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new LocalizedException(Status.VALIDATION_ERROR, CommonErrorCodes.VALIDATION_ERROR);
        }
    }
}
