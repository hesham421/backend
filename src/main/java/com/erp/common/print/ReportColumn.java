package com.erp.common.print;

/**
 * One printed column: {@link #key()} is the key of its value in every row map; the header is
 * {@link #labelAr()} or {@link #labelEn()} by the request's {@code Accept-Language}.
 */
public record ReportColumn(String key, ColumnType type, String labelAr, String labelEn) {

    public static ReportColumn text(String key, String labelAr, String labelEn) {
        return new ReportColumn(key, ColumnType.STRING, labelAr, labelEn);
    }

    public static ReportColumn decimal(String key, String labelAr, String labelEn) {
        return new ReportColumn(key, ColumnType.DECIMAL, labelAr, labelEn);
    }

    public static ReportColumn date(String key, String labelAr, String labelEn) {
        return new ReportColumn(key, ColumnType.DATE, labelAr, labelEn);
    }
}
