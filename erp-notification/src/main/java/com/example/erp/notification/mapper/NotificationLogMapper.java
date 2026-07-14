package com.example.erp.notification.mapper;

import com.example.erp.notification.dto.NotificationLogResponse;
import com.example.erp.notification.entity.NotificationLog;
import org.springframework.stereotype.Component;

/**
 * Entity <-> DTO mapping for ENTITY-NOTIF-001 (NotificationLog). No {@code toEntity()}/{@code
 * updateEntityFromRequest()} — NotificationLog is system-created only, by {@code
 * NotificationEventProcessor}'s own builder usage (Phase SVCAPI Layer 1), never from a public
 * CreateRequest DTO (CORE.md: "Create (system-only, at send time) ... NO manual Update/Delete").
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
