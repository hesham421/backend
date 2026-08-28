package com.erp.common.search;

import java.util.Collections;
import java.util.Set;

/**
 * {@link AllowedFields} backed by an exact-match (case-sensitive) Set of field names.
 */
public class SetAllowedFields implements AllowedFields {

    private final Set<String> allowedFields;

    public SetAllowedFields(Set<String> allowedFields) {
        this.allowedFields = allowedFields != null
                ? Collections.unmodifiableSet(allowedFields)
                : Collections.emptySet();
    }

    @Override
    public boolean isAllowed(String field) {
        return field != null && allowedFields.contains(field);
    }

    /**
     * Returns an unmodifiable view of the allowed fields.
     *
     * @return the set of allowed fields
     */
    public Set<String> getAllowedFields() {
        return allowedFields;
    }
}
