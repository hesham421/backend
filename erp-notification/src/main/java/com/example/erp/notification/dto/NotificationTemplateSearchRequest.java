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
 * Request DTO for API-NOTIF-006 (Template Search). Per SVCAPI.md: templateCode [LIKE],
 * channelTypeId [EXACT], moduleCode [LIKE], isActiveFl [EXACT].
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Schema(description = "Notification template search request - طلب بحث قوالب الإشعار")
public class NotificationTemplateSearchRequest extends BaseSearchContractRequest {

    @Schema(description = "Template code filter (LIKE)")
    private String templateCode;

    @Schema(description = "Channel filter (EXACT, LOV-NOTIF-001)")
    private String channelTypeId;

    @Schema(description = "Module code filter (LIKE)")
    private String moduleCode;

    @Schema(description = "Active-status filter (EXACT)")
    private Boolean isActiveFl;

    @Override
    public SearchRequest toCommonSearchRequest() {
        SearchRequest request = super.toCommonSearchRequest();
        List<SearchFilter> filters = new ArrayList<>(request.getFilters());
        if (templateCode != null) {
            filters.add(new SearchFilter("templateCode", Op.LIKE, templateCode));
        }
        if (channelTypeId != null) {
            filters.add(new SearchFilter("channelTypeId", Op.EQ, channelTypeId));
        }
        if (moduleCode != null) {
            filters.add(new SearchFilter("moduleCode", Op.LIKE, moduleCode));
        }
        if (isActiveFl != null) {
            filters.add(new SearchFilter("isActiveFl", Op.EQ, isActiveFl));
        }
        request.setFilters(filters);
        return request;
    }
}
