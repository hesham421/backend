package com.erp.common.dto;

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
            .filter(f -> field.equals(f.getField()))
            .map(SearchFilter::getValue)
            .filter(v -> v != null)
            .map(v -> Long.valueOf(String.valueOf(v)))
            .findFirst()
            .orElse(null);
    }
}
