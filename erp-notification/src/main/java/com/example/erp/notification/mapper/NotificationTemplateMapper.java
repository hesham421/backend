package com.example.erp.notification.mapper;

import com.example.erp.notification.dto.NotificationTemplateCreateRequest;
import com.example.erp.notification.dto.NotificationTemplateResponse;
import com.example.erp.notification.dto.NotificationTemplateUpdateRequest;
import com.example.erp.notification.entity.NotificationTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationTemplateMapper {

    /** fileFk is never set here — DEFERRED/unused Phase 1 (XM-NOTIF-001). */
    public NotificationTemplate toEntity(NotificationTemplateCreateRequest request) {
        if (request == null) {
            return null;
        }
        return NotificationTemplate.builder()
                .templateCode(request.getTemplateCode())
                .templateNameAr(request.getTemplateNameAr())
                .templateNameEn(request.getTemplateNameEn())
                .channelTypeId(request.getChannelTypeId())
                .moduleCode(request.getModuleCode())
                .templateBodyAr(request.getTemplateBodyAr())
                .templateBodyEn(request.getTemplateBodyEn())
                .build();
    }

    /** templateCode is NEVER updated here — RULE-NOTIF-007 (immutable post-create). */
    public void updateEntityFromRequest(NotificationTemplate entity, NotificationTemplateUpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }
        entity.setTemplateNameAr(request.getTemplateNameAr());
        entity.setTemplateNameEn(request.getTemplateNameEn());
        entity.setChannelTypeId(request.getChannelTypeId());
        entity.setModuleCode(request.getModuleCode());
        entity.setTemplateBodyAr(request.getTemplateBodyAr());
        entity.setTemplateBodyEn(request.getTemplateBodyEn());
    }

    public NotificationTemplateResponse toResponse(NotificationTemplate entity) {
        if (entity == null) {
            return null;
        }
        return NotificationTemplateResponse.builder()
                .id(entity.getId())
                .templateCode(entity.getTemplateCode())
                .templateNameAr(entity.getTemplateNameAr())
                .templateNameEn(entity.getTemplateNameEn())
                .channelTypeId(entity.getChannelTypeId())
                .moduleCode(entity.getModuleCode())
                .templateBodyAr(entity.getTemplateBodyAr())
                .templateBodyEn(entity.getTemplateBodyEn())
                .fileFk(entity.getFileFk())
                .isActiveFl(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
