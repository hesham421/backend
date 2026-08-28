package com.erp.notification.mapper;

import com.erp.notification.dto.NotificationLogResponse;
import com.erp.notification.entity.NotificationLog;
import org.springframework.stereotype.Component;

/**
 * No {@code toEntity()}/{@code updateEntityFromRequest()} — NotificationLog is system-created
 * only, via {@code NotificationEventProcessor}'s own builder usage, never from a public
 * CreateRequest DTO.
 */
@Component
public class NotificationLogMapper {

    public NotificationLogResponse toResponse(NotificationLog entity) {
        if (entity == null) {
            return null;
        }
        return NotificationLogResponse.builder()
                .id(entity.getId())
                .recipientId(entity.getRecipientId())
                .notificationTypeId(entity.getNotificationTypeId())
                .templateCode(entity.getTemplateCode())
                .subject(entity.getSubject())
                .bodyPreview(entity.getBodyPreview())
                .notificationStatusId(entity.getNotificationStatusId())
                .retryCount(entity.getRetryCount())
                .sentAt(entity.getSentAt())
                .moduleCode(entity.getModuleCode())
                .referenceId(entity.getReferenceId())
                .referenceType(entity.getReferenceType())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
