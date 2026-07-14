package com.example.erp.notification.dto;

import com.erp.common.search.Op;
import com.erp.common.search.SearchFilter;
import com.erp.common.search.SearchRequest;
import com.example.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for API-NOTIF-003 (Notification History). {@code notificationTypeId}/{@code
 * notificationStatusId} are first-class optional fields (per SVCAPI.md), folded into the
 * inherited filter list by {@link #toCommonSearchRequest()} — the "child search request"
 * pattern (create-dto skill). {@code recipientId} is deliberately NOT handled here: resolving
 * "defaults to caller's own id, unless caller may query others" is server-side authorization,
 * not a DTO concern — see {@code NotificationLogQueryService}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Schema(description = "Notification history search request - طلب بحث سجل الإشعارات")
public class NotificationHistorySearchRequest extends BaseSearchContractRequest {

    @Schema(description = "Recipient user ID — optional, defaults to the caller - معرف المستقبِل (اختياري)")
    private Long recipientId;

    @Schema(description = "Filter by channel (LOV-NOTIF-001) - تصفية حسب القناة")
    private String notificationTypeId;

    @Schema(description = "Filter by delivery status (LOV-NOTIF-002) - تصفية حسب الحالة")
    private String notificationStatusId;

    @Override
    public SearchRequest toCommonSearchRequest() {
        SearchRequest request = super.toCommonSearchRequest();
        List<SearchFilter> filters = new ArrayList<>(request.getFilters());
        if (notificationTypeId != null) {
            filters.add(new SearchFilter("notificationTypeId", Op.EQ, notificationTypeId));
        }
        if (notificationStatusId != null) {
            filters.add(new SearchFilter("notificationStatusId", Op.EQ, notificationStatusId));
        }
        request.setFilters(filters);
        return request;
    }
}
