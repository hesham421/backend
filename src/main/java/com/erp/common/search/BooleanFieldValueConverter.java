package com.erp.common.search;

import com.erp.common.converter.BooleanNumberConverter;

import java.util.Set;

/**
 * Converts Boolean (and boolean-like String/Number) values to Integer (0/1) for search fields
 * mapped to NUMBER(1) columns; null passes through unchanged to allow "match all" queries.
 */
public class BooleanFieldValueConverter implements FieldValueConverter {

    private final Set<String> booleanFields;
    private final FieldValueConverter delegate;

    public BooleanFieldValueConverter(Set<String> booleanFields) {
        this(booleanFields, DefaultFieldValueConverter.INSTANCE);
    }

    public BooleanFieldValueConverter(Set<String> booleanFields, FieldValueConverter delegate) {
        this.booleanFields = booleanFields != null ? booleanFields : Set.of();
        this.delegate = delegate != null ? delegate : DefaultFieldValueConverter.INSTANCE;
    }

    @Override
    public Object convert(String field, Object rawValue, Op op) {
        // Check if this field should be converted as a boolean
        if (booleanFields.contains(field)) {
            return convertBooleanField(rawValue);
        }
        
        // Delegate to the wrapped converter for other fields
        return delegate.convert(field, rawValue, op);
    }

    private Integer convertBooleanField(Object rawValue) {
        if (rawValue == null) {
            return null;
        }

        // Already a Boolean - convert directly
        if (rawValue instanceof Boolean) {
            return BooleanNumberConverter.toDbValue((Boolean) rawValue);
        }

        // String value - parse as boolean first
        if (rawValue instanceof String) {
            String strValue = ((String) rawValue).trim();
            if (strValue.isEmpty()) {
                return null;
            }
            
            // Handle common boolean string representations
            if ("true".equalsIgnoreCase(strValue) || "1".equals(strValue) || "yes".equalsIgnoreCase(strValue)) {
                return BooleanNumberConverter.DB_TRUE;
            }
            if ("false".equalsIgnoreCase(strValue) || "0".equals(strValue) || "no".equalsIgnoreCase(strValue)) {
                return BooleanNumberConverter.DB_FALSE;
            }
            
            throw new SearchException(
                "Invalid boolean value: '" + strValue + "'. Expected: true, false, 1, 0, yes, or no."
            );
        }

        // Integer value - validate and pass through
        if (rawValue instanceof Integer) {
            Integer intValue = (Integer) rawValue;
            if (BooleanNumberConverter.DB_TRUE.equals(intValue) || 
                BooleanNumberConverter.DB_FALSE.equals(intValue)) {
                return intValue;
            }
            throw new SearchException(
                "Invalid boolean integer value: " + intValue + ". Expected: 0 or 1."
            );
        }

        // Number value (Long, etc.) - convert to Integer
        if (rawValue instanceof Number) {
            int intValue = ((Number) rawValue).intValue();
            if (intValue == 1 || intValue == 0) {
                return intValue;
            }
            throw new SearchException(
                "Invalid boolean numeric value: " + rawValue + ". Expected: 0 or 1."
            );
        }

        throw new SearchException(
            "Cannot convert value of type " + rawValue.getClass().getSimpleName() + " to boolean."
        );
    }

    /**
     * Factory method to create a converter for common active/enabled fields.
     * 
     * @return converter configured for standard boolean field names
     */
    public static BooleanFieldValueConverter forActiveFields() {
        return new BooleanFieldValueConverter(Set.of(
            "isActive",
            "active", 
            "enabled",
            "visible",
            "deleted",
            "locked",
            "published",
            "verified"
        ));
    }

    public static BooleanFieldValueConverter forActiveFields(FieldValueConverter delegate) {
        return new BooleanFieldValueConverter(Set.of(
            "isActive",
            "active",
            "enabled",
            "visible",
            "deleted",
            "locked",
            "published",
            "verified"
        ), delegate);
    }
}
