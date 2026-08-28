package com.erp.common.search;

/**
 * Converts a raw search filter value to the appropriate type for a given field/operator
 * (e.g. parsing date strings, transforming enum values).
 */
public interface FieldValueConverter {

    /**
     * @throws SearchException if the value cannot be converted
     */
    Object convert(String field, Object rawValue, Op op);
}
