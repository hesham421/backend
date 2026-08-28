package com.erp.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Timestamp formatting/parsing; always formats to UTC ISO-8601 for cross-module consistency.
 */
public final class TimestampUtils {

    private TimestampUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Format: 2024-01-15T10:30:45.123Z
     */
    public static String getCurrentTimestamp() {
        return Instant.now().toString();
    }

    public static String format(Instant instant) {
        return instant != null ? instant.toString() : null;
    }

    /**
     * Interprets {@code dateTime} in the system default zone before converting to UTC.
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault())
                       .withZoneSameInstant(ZoneId.of("UTC"))
                       .format(DateTimeFormatter.ISO_INSTANT);
    }

    public static String format(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))
                            .format(DateTimeFormatter.ISO_INSTANT);
    }

    public static Instant parse(String timestamp) {
        return timestamp != null ? Instant.parse(timestamp) : null;
    }

    public static Instant now() {
        return Instant.now();
    }
}
