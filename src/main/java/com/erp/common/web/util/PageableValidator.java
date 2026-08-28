package com.erp.common.web.util;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.CommonErrorCodes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates sort fields against a whitelist to prevent SQL injection and sorting on
 * non-indexed columns.
 */
public final class PageableValidator {

    private PageableValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * @throws BusinessException if sort field not in whitelist
     */
    public static Pageable validateSortFields(Pageable pageable, Set<String> allowedFields) {
        return validateSortFields(pageable, allowedFields, Collections.emptyMap());
    }

    /**
     * {@code aliases} maps client-facing sort field names to canonical ones (e.g. accepting
     * {@code sort=name,asc} while mapping it to {@code roleName}).
     *
     * @throws BusinessException if sort field not in whitelist
     */
    public static Pageable validateSortFields(Pageable pageable, Set<String> allowedFields, Map<String, String> aliases) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        Map<String, String> safeAliases = (aliases == null) ? Collections.emptyMap() : aliases;

        // Clean and rebuild sort if needed (handles Swagger UI array format) and apply aliases
        List<Sort.Order> cleanedOrders = pageable.getSort().stream()
                .map(order -> {
                    String requested = cleanSortProperty(order.getProperty());
                    String mapped = safeAliases.getOrDefault(requested, requested);
                    return new Sort.Order(order.getDirection(), mapped);
                })
                .collect(Collectors.toList());

        // Check all requested sort fields against whitelist (after alias mapping)
        List<String> invalidFields = pageable.getSort().stream()
                .map(order -> cleanSortProperty(order.getProperty()))
                .filter(requested -> !allowedFields.contains(safeAliases.getOrDefault(requested, requested)))
                .distinct()
                .collect(Collectors.toList());

        if (!invalidFields.isEmpty()) {
            throw new BusinessException(
                Status.VALIDATION_ERROR,
                CommonErrorCodes.INVALID_SORT_FIELD,
                "Invalid sort fields: " + invalidFields,
                "Allowed fields: " + String.join(", ", allowedFields)
            );
        }

        // Return new pageable with cleaned + mapped sort
        Sort cleanedSort = Sort.by(cleanedOrders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), cleanedSort);
    }
    
    /**
     * Clean sort property name by removing JSON array formatting from Swagger UI.
     * Handles cases like: ["fieldName"] -> fieldName
     */
    private static String cleanSortProperty(String property) {
        if (property == null) {
            return property;
        }
        
        // Remove JSON array brackets and quotes: ["fieldName"] -> fieldName
        String cleaned = property.trim();
        if (cleaned.startsWith("[\"") && cleaned.endsWith("\"]")) {
            cleaned = cleaned.substring(2, cleaned.length() - 2);
        } else if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
            // Remove quotes if present
            if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
                cleaned = cleaned.substring(1, cleaned.length() - 1);
            }
        }
        
        return cleaned.trim();
    }

    public static Pageable validateAndLimit(Pageable pageable, Set<String> allowedFields, int maxPageSize) {
        // Validate sort fields first
        Pageable validated = validateSortFields(pageable, allowedFields);

        // Enforce max page size
        if (validated.getPageSize() > maxPageSize) {
            return PageRequest.of(
                validated.getPageNumber(),
                maxPageSize,
                validated.getSort()
            );
        }

        return validated;
    }

    public static Set<String> allowedFields(String... fields) {
        return Set.of(fields);
    }
}
