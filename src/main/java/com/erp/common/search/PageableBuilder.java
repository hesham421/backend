package com.erp.common.search;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Builds {@link Pageable} from {@link SearchRequest}, validating sortBy against an allowed-field
 * whitelist (rejecting collection-based paths like "roles.name", which cause Cartesian-product
 * duplication) and defaulting to DESC sort when sortDir is null or invalid.
 */
public class PageableBuilder {

    /**
     * Default page size if not specified.
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * Maximum allowed page size to prevent excessive memory usage.
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Default sort field when none specified or invalid.
     */
    public static final String DEFAULT_SORT_FIELD = "id";

    private PageableBuilder() {
        // Utility class
    }

    /**
     * @throws SearchException if sort field is not allowed or page size exceeds maximum
     */
    public static Pageable from(SearchRequest request, Set<String> allowedSortFields) {
        if (request == null) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, DEFAULT_SORT_FIELD));
        }

        int page = Math.max(0, request.getPage());
        int size = validateAndNormalizeSize(request.getSize());
        Sort sort = buildSort(request.getSortBy(), request.getSortDir(), allowedSortFields);

        return PageRequest.of(page, size, sort);
    }

    /**
     * @throws SearchException if sort field is not allowed or page size exceeds maximum
     */
    public static Pageable from(SearchRequest request, Set<String> allowedSortFields, String defaultSortField) {
        if (request == null) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, defaultSortField));
        }

        int page = Math.max(0, request.getPage());
        int size = validateAndNormalizeSize(request.getSize());
        Sort sort = buildSort(request.getSortBy(), request.getSortDir(), allowedSortFields, defaultSortField);

        return PageRequest.of(page, size, sort);
    }

    /**
     * Validates and normalizes the page size.
     *
     * @throws SearchException if size exceeds MAX_PAGE_SIZE
     */
    private static int validateAndNormalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        if (size > MAX_PAGE_SIZE) {
            throw new SearchException("Page size must not exceed " + MAX_PAGE_SIZE);
        }
        return size;
    }

    /**
     * Builds Sort with validation.
     */
    private static Sort buildSort(String sortBy, String sortDir, Set<String> allowedSortFields) {
        return buildSort(sortBy, sortDir, allowedSortFields, DEFAULT_SORT_FIELD);
    }

    /**
     * Builds Sort with validation and custom default.
     *
     * @throws SearchException if sortBy field is not in allowed list
     */
    private static Sort buildSort(
            String sortBy,
            String sortDir,
            Set<String> allowedSortFields,
            String defaultSortField) {

        // Determine sort field
        String sortField = defaultSortField;
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            if (allowedSortFields != null && allowedSortFields.contains(sortBy)) {
                sortField = sortBy;
            } else if (allowedSortFields != null) {
                throw new SearchException(
                        "Sort field '" + sortBy + "' is not allowed. Allowed fields: " + allowedSortFields);
            } else {
                // No validation if allowedSortFields is null
                sortField = sortBy;
            }
        }

        // Determine sort direction - defaults to DESC
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDir != null) {
            String normalizedDir = sortDir.trim().toUpperCase();
            if ("ASC".equals(normalizedDir)) {
                direction = Sort.Direction.ASC;
            }
            // Any other value (including "DESC") defaults to DESC
        }

        return Sort.by(direction, sortField);
    }

    /**
     * Useful for count queries, which don't need sorting.
     */
    public static Pageable unsorted(SearchRequest request) {
        if (request == null) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE);
        }

        int page = Math.max(0, request.getPage());
        int size = validateAndNormalizeSize(request.getSize());

        return PageRequest.of(page, size);
    }
}
