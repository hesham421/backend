package com.erp.fin.mapper;

import com.erp.fin.dto.EventTypeRuleCreateRequest;
import com.erp.fin.dto.EventTypeRuleResponse;
import com.erp.fin.entity.EventTypeRule;
import org.springframework.stereotype.Component;

/**
 * Manual mapper for ENT-FIN-009 (EventTypeRule) — build-create-mapper, no MapStruct. API-FIN-010
 * is the only write this sub delivers for the entity, so no {@code updateEntityFromRequest}
 * exists yet.
 */
@Component
public class EventTypeRuleMapper {

    public EventTypeRule toEntity(EventTypeRuleCreateRequest request) {
        if (request == null) {
            return null;
        }
        return EventTypeRule.builder()
            .eventTypeCode(request.getEventTypeCode())
            .nameAr(request.getNameAr())
            .nameEn(request.getNameEn())
            .isActiveFl(Boolean.TRUE)
            .build();
    }

    public EventTypeRuleResponse toResponse(EventTypeRule entity) {
        if (entity == null) {
            return null;
        }
        return EventTypeRuleResponse.builder()
            .eventTypeRulePk(entity.getEventTypeRulePk())
            .eventTypeCode(entity.getEventTypeCode())
            .nameAr(entity.getNameAr())
            .nameEn(entity.getNameEn())
            .isActiveFl(Boolean.TRUE.equals(entity.getIsActiveFl()))
            .createdAt(entity.getCreatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedAt(entity.getUpdatedAt())
            .updatedBy(entity.getUpdatedBy())
            .build();
    }
}
