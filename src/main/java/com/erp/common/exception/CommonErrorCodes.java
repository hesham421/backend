package com.erp.common.exception;

/**
 * Centralized error codes for the common-utils module; each must have corresponding entries
 * in the English and Arabic message bundles.
 */
public final class CommonErrorCodes {

    private CommonErrorCodes() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String PAGEABLE_NULL = "PAGEABLE_NULL";
    public static final String PAGEABLE_INVALID_MAX_SIZE = "PAGEABLE_INVALID_MAX_SIZE";
    public static final String INVALID_SORT_FIELD = "INVALID_SORT_FIELD";

    public static final String SEARCH_VALUE_REQUIRED = "SEARCH_VALUE_REQUIRED";
    public static final String SEARCH_IN_EMPTY_VALUES = "SEARCH_IN_EMPTY_VALUES";
    public static final String SEARCH_BETWEEN_INVALID = "SEARCH_BETWEEN_INVALID";
    public static final String SEARCH_BETWEEN_TWO_VALUES = "SEARCH_BETWEEN_TWO_VALUES";
    public static final String SEARCH_FIELD_NOT_ALLOWED = "SEARCH_FIELD_NOT_ALLOWED";
    public static final String SEARCH_INVALID_OPERATOR = "SEARCH_INVALID_OPERATOR";

    public static final String ACCESS_DENIED = "ACCESS_DENIED";

    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
}
