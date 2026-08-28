package com.erp.notification.dto;

import com.erp.common.search.Op;
import com.erp.common.search.SearchFilter;
import com.erp.common.search.SearchRequest;
import com.erp.common.dto.BaseSearchContractRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code recipientId} is deliberately not a field here — resolving it (default to caller, unless
 * authorized to query others) is server-side authorization, handled in
 * {@code NotificationLogQueryService}, not a DTO concern.
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
