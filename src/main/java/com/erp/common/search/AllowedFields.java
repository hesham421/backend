package com.erp.common.search;

/**
 * Validates which fields are permitted for dynamic search/filtering on an entity, preventing
 * SQL injection and access to non-searchable fields.
 */
public interface AllowedFields {

    /**
     * {@code field} may use dot notation for nested properties.
     */
    boolean isAllowed(String field);

    /**
     * @throws SearchException if the field is not allowed
     */
    default void validateField(String field) {
        if (!isAllowed(field)) {
            throw new SearchException("Field '" + field + "' is not allowed for searching");
        }
    }
}
