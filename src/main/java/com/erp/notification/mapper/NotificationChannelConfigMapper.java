package com.erp.notification.mapper;

import com.erp.notification.dto.NotificationChannelConfigResponse;
import com.erp.notification.dto.NotificationChannelConfigUpdateRequest;
import com.erp.notification.entity.NotificationChannelConfig;
import org.springframework.stereotype.Component;

/** No {@code toEntity()} — NotificationChannelConfig has no Create (CORE.md, seed-only). */
@Component
public class NotificationChannelConfigMapper {

    /** channelTypeId is never updated here — one fixed row per channel, no rename support. */
    public void updateEntityFromRequest(NotificationChannelConfig entity, NotificationChannelConfigUpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }
        entity.setIsEnabledFl(request.getIsEnabledFl());
        entity.setConfigJson(request.getConfigJson());
    }

    public NotificationChannelConfigResponse toResponse(NotificationChannelConfig entity) {
        if (entity == null) {
            return null;
        }
        return NotificationChannelConfigResponse.builder()
                .id(entity.getId())
                .channelTypeId(entity.getChannelTypeId())
                .isEnabledFl(Boolean.TRUE.equals(entity.getIsEnabledFl()))
                .configJson(entity.getConfigJson())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
