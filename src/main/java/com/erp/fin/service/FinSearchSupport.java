package com.erp.fin.service;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.CommonErrorCodes;
import com.erp.common.exception.LocalizedException;
import com.erp.common.search.FieldValueConverter;
import com.erp.fin.exception.FinErrorCodes;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;

/**
 * The two things FIN's seven searches (SVC-API-SEARCH) need that the shared search layer does not
 * give them. Both are deliberately thin helpers over the shared plumbing, never a replacement for
 * it: {@code SpecBuilder} and {@code PageableBuilder} still build every specification and every
 * pageable.
 *
 * <p>Same shape, and the same reasoning, as SEC's delivered {@code SecSearchSupport} — copied as a
 * FIN-local class rather than shared, because promoting it to {@code com.erp.common} would mean
 * modifying shared infrastructure and the error code it raises is module-specific by design
 * ({@code FIN-400-INVALID-SORT}, not SEC's).
 */
final class FinSearchSupport {

    private FinSearchSupport() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    /**
     * {@code PageableBuilder.from(...)} silently drops a sort field that is not on the whitelist
     * and returns an unsorted page, so the caller would receive a differently ordered result than
     * it asked for with nothing saying so. The Error Catalog registers a code for exactly this
     * ({@code FIN-400-INVALID-SORT}, "every search API"), so the whitelist check is made before
     * the builder is reached.
     *
     * <p>A null or blank sort field is not an error — it means "no sort", the contract's default.
     */
    static void assertSortAllowed(String sortField, Set<String> allowedSortFields) {
        if (sortField == null || sortField.isBlank()) {
            return;
        }
        if (!allowedSortFields.contains(sortField)) {
            throw new LocalizedException(
                Status.VALIDATION_ERROR, FinErrorCodes.FIN_400_INVALID_SORT, sortField);
        }
    }

    /**
     * API-FIN-018's {@code docDate} filter is a DATE_RANGE over a {@code DATE} column
     * (DBF-FIN-036), but a JSON body carries the bound as a string; without this the criteria build
     * would compare a String to a {@link LocalDate} path. A malformed bound is client input, so it
     * surfaces as a 400 rather than an unhandled 500 escaping the specification build.
     */
    static FieldValueConverter localDateFieldConverter(Set<String> dateFields) {
        return (field, rawValue) -> {
            if (rawValue == null || rawValue instanceof LocalDate || !dateFields.contains(field)) {
                return rawValue;
            }
            try {
                return LocalDate.parse(String.valueOf(rawValue).trim());
            } catch (DateTimeParseException e) {
                throw new LocalizedException(
                    Status.VALIDATION_ERROR, CommonErrorCodes.VALIDATION_ERROR);
            }
        };
    }
}
