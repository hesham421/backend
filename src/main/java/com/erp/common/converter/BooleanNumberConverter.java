package com.erp.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Maps Oracle NUMBER(1) boolean flags (0/1) to Boolean; any other value throws (fail-fast).
 * Explicit (autoApply = false) — see {@link BooleanCharYNConverter} for Y/N columns.
 */
@Converter(autoApply = false)
public class BooleanNumberConverter implements AttributeConverter<Boolean, Integer> {

    /** Database value representing TRUE */
    public static final Integer DB_TRUE = 1;
    
    /** Database value representing FALSE */
    public static final Integer DB_FALSE = 0;

    @Override
    public Integer convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute ? DB_TRUE : DB_FALSE;
    }

    /**
     * @throws IllegalArgumentException if dbData is not 0, 1, or null (fail-fast for data integrity)
     */
    @Override
    public Boolean convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        if (DB_TRUE.equals(dbData)) {
            return Boolean.TRUE;
        }
        if (DB_FALSE.equals(dbData)) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException(
            "Invalid NUMBER(1) boolean value: " + dbData + 
            ". Expected 0 (false), 1 (true), or null."
        );
    }

    /**
     * For building native queries or JDBC operations outside the JPA context.
     */
    public static Integer toDbValue(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? DB_TRUE : DB_FALSE;
    }

    /**
     * For processing native query results outside the JPA context.
     *
     * @throws IllegalArgumentException if dbValue is not 0, 1, or null
     */
    public static Boolean fromDbValue(Integer dbValue) {
        if (dbValue == null) {
            return null;
        }
        if (DB_TRUE.equals(dbValue)) {
            return Boolean.TRUE;
        }
        if (DB_FALSE.equals(dbValue)) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException(
            "Invalid NUMBER(1) boolean value: " + dbValue
        );
    }
}
