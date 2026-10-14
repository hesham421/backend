package com.erp.common.print;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.CommonErrorCodes;
import com.erp.common.exception.LocalizedException;
import com.erp.common.search.SearchFilter;
import com.erp.common.search.SearchOperator;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntFunction;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

/**
 * The criteria a screen sent with an export: scalar report parameters, or a list screen's own search
 * body ({@code filters}, {@code sortField}, {@code sortDirection}). A malformed value is 400
 * {@code REPORT_PARAM_INVALID}; {@link #allPages} enforces the row cap.
 */
public final class ReportParams {

    /** Rows fetched per search call: the search endpoints' own maximum page size. */
    public static final int PAGE_SIZE = 200;

    private final Map<String, Object> values;
    private final int maxRows;

    public ReportParams(Map<String, Object> values, int maxRows) {
        this.values = values == null ? Map.of() : values;
        this.maxRows = maxRows;
    }

    /** Whether the report prints in Arabic ({@code Accept-Language}). */
    public boolean arabic() {
        return "ar".equals(LocaleContextHolder.getLocale().getLanguage());
    }

    /** {@code ar} or {@code en} by the print language, falling back to the other when blank. */
    public String localized(String ar, String en) {
        String preferred = arabic() ? ar : en;
        return preferred == null || preferred.isBlank() ? (arabic() ? en : ar) : preferred;
    }

    /** The trimmed text, or {@code null} when absent or blank. */
    public String text(String name) {
        Object value = values.get(name);
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    public Long longValue(String name) {
        Object value = values.get(name);
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = text(name);
        try {
            return text == null ? null : Long.valueOf(text);
        } catch (NumberFormatException e) {
            throw invalid(name);
        }
    }

    public Long requiredLong(String name) {
        Long value = longValue(name);
        if (value == null) {
            throw invalid(name);
        }
        return value;
    }

    /** An ISO {@code yyyy-MM-dd} date. */
    public LocalDate date(String name) {
        String text = text(name);
        try {
            return text == null ? null : LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw invalid(name);
        }
    }

    /** The search body's {@code filters}, as the screen sent them to its {@code /search} endpoint. */
    public List<SearchFilter> filters() {
        Object raw = values.get("filters");
        if (raw == null) {
            return List.of();
        }
        if (!(raw instanceof Collection<?> items)) {
            throw invalid("filters");
        }
        List<SearchFilter> filters = new ArrayList<>(items.size());
        for (Object item : items) {
            if (!(item instanceof Map<?, ?> filter) || !(filter.get("field") instanceof String field)) {
                throw invalid("filters");
            }
            filters.add(SearchFilter.builder().field(field).operator(operator(filter.get("operator")))
                .value(filter.get("value")).build());
        }
        return filters;
    }

    public String sortField() {
        return text("sortField");
    }

    public Sort.Direction sortDirection() {
        String direction = text("sortDirection");
        try {
            return direction == null ? Sort.Direction.ASC : Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            throw invalid("sortDirection");
        }
    }

    /**
     * Every row of a paged search, page by page ({@link #PAGE_SIZE} each); more than the configured
     * cap is 422 {@code REPORT_EXPORT_TOO_LARGE}, so narrow the filter.
     */
    public <T> List<T> allPages(IntFunction<Page<T>> search) {
        Page<T> first = search.apply(0);
        if (first.getTotalElements() > maxRows) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, CommonErrorCodes.REPORT_EXPORT_TOO_LARGE, maxRows);
        }
        List<T> rows = new ArrayList<>(first.getContent());
        for (int page = 1; page < first.getTotalPages(); page++) {
            rows.addAll(search.apply(page).getContent());
        }
        return rows;
    }

    private SearchOperator operator(Object raw) {
        if (raw == null) {
            return null;
        }
        try {
            return SearchOperator.valueOf(raw.toString().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw invalid("filters");
        }
    }

    private static LocalizedException invalid(String name) {
        return new LocalizedException(Status.VALIDATION_ERROR, CommonErrorCodes.REPORT_PARAM_INVALID, name);
    }
}
