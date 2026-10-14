package com.erp.common.print;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What a {@link ReportProvider} prints: the columns, every row (a map keyed by
 * {@link ReportColumn#key()}) and optional totals keyed by column key.
 */
public record ReportResult(List<ReportColumn> columns, List<Map<String, Object>> rows,
                           Map<String, Object> totals) {

    public ReportResult {
        columns = columns == null ? List.of() : List.copyOf(columns);
        rows = rows == null ? List.of() : rows;
        totals = totals == null ? Map.of() : totals;
    }

    public ReportResult(List<ReportColumn> columns, List<Map<String, Object>> rows) {
        this(columns, rows, Map.of());
    }

    /** A row from alternating keys and values; a value may be {@code null}. */
    public static Map<String, Object> row(Object... keysAndValues) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keysAndValues.length; i += 2) {
            row.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return row;
    }
}
