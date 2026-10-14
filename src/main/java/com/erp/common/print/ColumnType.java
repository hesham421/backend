package com.erp.common.print;

/**
 * Type of a {@link ReportColumn}: how the printed cell is formatted. Row values are a {@link String},
 * a {@link Number} (INTEGER, DECIMAL), a {@link java.time.LocalDate} (DATE), an
 * {@link java.time.Instant} / {@link java.time.OffsetDateTime} / {@link java.time.LocalDateTime}
 * (DATETIME) or a {@link Boolean}; {@code null} is an empty cell.
 */
public enum ColumnType {
    STRING,
    INTEGER,
    DECIMAL,
    DATE,
    DATETIME,
    BOOLEAN
}
