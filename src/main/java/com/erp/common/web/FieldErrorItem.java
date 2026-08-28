package com.erp.common.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single field validation error, used in {@link ApiError} for field-level feedback.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FieldErrorItem {
    
    /**
     * Name of the field that failed validation
     */
    private String field;
    
    /**
     * Validation error message
     */
    private String message;
}
