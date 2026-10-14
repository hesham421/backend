package com.erp.common.print;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.CommonErrorCodes;
import com.erp.common.exception.LocalizedException;
import java.util.Locale;

/** The formats a report prints to. */
public enum PrintFormat {
    PDF("application/pdf", "pdf"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");

    private final String contentType;
    private final String extension;

    PrintFormat(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String contentType() {
        return contentType;
    }

    public String extension() {
        return extension;
    }

    /** Case-insensitive name; anything else is 400 {@code PRINT_FORMAT_NOT_SUPPORTED}. */
    public static PrintFormat from(String format) {
        String normalized = format == null ? "" : format.trim().toUpperCase(Locale.ROOT);
        for (PrintFormat candidate : values()) {
            if (candidate.name().equals(normalized)) {
                return candidate;
            }
        }
        throw new LocalizedException(Status.VALIDATION_ERROR, CommonErrorCodes.PRINT_FORMAT_NOT_SUPPORTED, format);
    }
}
