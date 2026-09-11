package com.erp.sec.dto;

import com.erp.common.dto.BaseSearchContractRequest;
import com.erp.common.search.SearchRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API-SEC-025 search body. {@code user} is an association (DBF-SEC-076), so the client filters on
 * the scalar {@code userId}, which is lifted out of the generic set and resolved to a User reference
 * by the service. No {@code @AllArgsConstructor} — the class adds no field of its own, so it would
 * collide with the no-arg constructor.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Search request for ActiveSession - طلب بحث الجلسات النشطة")
public class ActiveSessionSearchRequest extends BaseSearchContractRequest {

    private static final String USER_ID = "userId";

    @Override
    public SearchRequest toCommonSearchRequest() {
        return toCommonSearchRequest(Set.of(USER_ID));
    }

    /**
     * Not a settable request field — {@code filters: [{"field":"userId",...}]} is the client
     * contract (see class doc). Hidden from the OpenAPI schema so bean introspection doesn't
     * publish it as though it were an independent top-level field a client can POST directly.
     */
    @Schema(hidden = true)
    public Long getUserId() {
        return extractLongFilter(USER_ID);
    }
}
