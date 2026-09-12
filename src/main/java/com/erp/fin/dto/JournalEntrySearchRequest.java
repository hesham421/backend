package com.erp.fin.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import com.erp.common.search.SearchRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API-FIN-018 — search body for ENT-FIN-004 (JournalEntry), QR-FIN-023. Filters named by the plan:
 * {@code docNo} (LIKE), {@code docDate} (DATE_RANGE — two filters using the shared
 * {@code GREATER_THAN_OR_EQUAL} / {@code LESS_THAN_OR_EQUAL} operators), {@code periodId} (EXACT),
 * {@code statusCode} (EXACT) and {@code journalTypeCode} (EXACT).
 *
 * <p>{@code periodId} (DBF-FIN-038) is a foreign key mapped as the association {@code period}, a
 * nested {@code period.fiscalPeriodPk} path, so it is excluded here and ANDed in by the service as
 * an explicit {@code Specification} join — same mechanism as
 * {@link AllocationRuleSearchRequest#getSourceAccountId()}.
 *
 * <p><b>A.3.1 deviation, forced by the language.</b> The skill's DTO annotation set is
 * {@code @Data @Builder @NoArgsConstructor @AllArgsConstructor}; this class declares no instance
 * field of its own (every filter travels in the inherited {@code filters} list), so
 * {@code @AllArgsConstructor} would generate a second no-argument constructor and the class would
 * not compile. {@code @NoArgsConstructor} is the one Jackson needs to deserialize the body, so
 * {@code @AllArgsConstructor} is the one omitted; {@code @SuperBuilder} (not {@code @Builder})
 * is required in its place because the superclass uses it.
 */
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Search request for journal entries - طلب بحث في قيود اليومية")
public class JournalEntrySearchRequest extends BaseSearchContractRequest {

    /** The filter field carrying the target fiscal period id (DBF-FIN-038). */
    public static final String PERIOD_ID_FILTER = "periodId";

    @Override
    public SearchRequest toCommonSearchRequest() {
        return toCommonSearchRequest(Set.of(PERIOD_ID_FILTER));
    }

    /** The nested-path filter, or {@code null} when the caller sent none (it is optional here). */
    @Schema(hidden = true)
    public Long getPeriodId() {
        return extractLongFilter(PERIOD_ID_FILTER);
    }
}
