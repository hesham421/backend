package com.erp.common.search;

/**
 * No-op {@link FieldValueConverter} used when no custom conversion logic is needed.
 */
public class DefaultFieldValueConverter implements FieldValueConverter {

    /**
     * Singleton instance.
     */
    public static final DefaultFieldValueConverter INSTANCE = new DefaultFieldValueConverter();

    private DefaultFieldValueConverter() {
    }

    @Override
    public Object convert(String field, Object rawValue, Op op) {
        return rawValue;
    }
}
