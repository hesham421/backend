package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.fin.dto.FinPrintCriteria;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Shared pieces of the FIN print templates: the criteria line every report states, and the common parameters. */
final class FinPrintTemplates {

    private FinPrintTemplates() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    /** {@code IS_AR}, {@code CRITERIA_TEXT}, the dimension filter and the SQL parameters given. */
    static Map<String, Object> parameters(ReportParams params, List<String> criteria, Object... sqlParameters) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("IS_AR", params.arabic());
        parameters.put("CRITERIA_TEXT", String.join("   |   ", criteria));
        for (int i = 0; i + 1 < sqlParameters.length; i += 2) {
            parameters.put((String) sqlParameters[i], sqlParameters[i + 1]);
        }
        return parameters;
    }

    /** "Branch: Main" when a value is applied, "Branch: all" when only its dimension is, nothing otherwise. */
    static void dimension(List<String> criteria, FinPrintCriteria resolved, ReportParams params) {
        String dimension = params.localized(resolved.dimensionNameAr(), resolved.dimensionNameEn());
        String value = params.localized(resolved.dimensionValueNameAr(), resolved.dimensionValueNameEn());
        if (value != null) {
            criteria.add((dimension == null ? params.localized("البُعد", "Dimension") : dimension) + ": " + value);
        } else if (dimension != null) {
            criteria.add(dimension + ": " + params.localized("الكل", "all"));
        }
    }

    /** A list printout's criteria: the record count and, for a pane, the parent the screen named in {@code subtitle}. */
    static List<String> listCriteria(ReportParams params, int count, String wordAr, String wordEn) {
        List<String> criteria = criteria();
        String parent = params.text("subtitle");
        if (parent != null) {
            criteria.add(parent);
        }
        criteria.add(params.localized("العدد: ", "Count: ") + count + " " + params.localized(wordAr, wordEn));
        return criteria;
    }

    /** The records' ids, in the screen's order, for the template's {@code IDS} parameter. */
    static <T> String ids(List<T> records, Function<T, Long> id) {
        return records.stream().map(record -> String.valueOf(id.apply(record))).collect(Collectors.joining(","));
    }

    static List<String> criteria() {
        return new ArrayList<>();
    }

    static String date(LocalDate date) {
        return date == null ? null : date.toString();
    }

    static java.sql.Date sqlDate(LocalDate date) {
        return date == null ? null : java.sql.Date.valueOf(date);
    }
}
