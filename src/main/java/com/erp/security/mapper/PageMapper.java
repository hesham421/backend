package com.erp.security.mapper;

import com.erp.security.dto.PageCreateRequest;
import com.erp.security.dto.PageResponse;
import com.erp.security.dto.PageUpdateRequest;
import com.erp.security.entity.Page;
import org.springframework.stereotype.Component;

/**
 * Manual entity/DTO mapper for ENTITY-SEC-004 (Page). Scalar-only: the module and parentPage
 * associations are resolved and set by PageService (mappers stay query-free — build-create-mapper
 * SH.3), so toEntity/updateEntityFromRequest never touch the FK relationships. toResponse reads the
 * FK ids off the (lazy) associations.
 */
@Component
public class PageMapper {

    public Page toEntity(PageCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Page.builder()
            .pageCode(request.getPageCode())   // NOT .toUpperCase() — Page.onCreate() owns it
            .nameAr(request.getNameAr())
            .nameEn(request.getNameEn())
            .isActive(request.getIsActiveFl() != null ? request.getIsActiveFl() : Boolean.TRUE)
            .build();
    }

    /**
     * Mutates scalar fields in place. Skips pageCode — immutable (RULE-SEC-010, absent from the
     * request). The module/parentPage associations are re-assigned by the service. isActiveFl, when
     * present, is applied via activate()/deactivate(); null means "no change".
     */
    public void updateEntityFromRequest(Page entity, PageUpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }
        entity.setNameAr(request.getNameAr());
        entity.setNameEn(request.getNameEn());
        if (request.getIsActiveFl() != null) {
            if (Boolean.TRUE.equals(request.getIsActiveFl())) {
                entity.activate();
            } else {
                entity.deactivate();
            }
        }
    }

    public PageResponse toResponse(Page entity) {
        if (entity == null) {
            return null;
        }
        return PageResponse.builder()
            .id(entity.getId())
            .pageCode(entity.getPageCode())
            .nameAr(entity.getNameAr())
            .nameEn(entity.getNameEn())
            .moduleFk(entity.getModule() != null ? entity.getModule().getId() : null)
            .parentPageFk(entity.getParentPage() != null ? entity.getParentPage().getId() : null)
            .isActiveFl(Boolean.TRUE.equals(entity.getIsActive()))
            .createdAt(entity.getCreatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedAt(entity.getUpdatedAt())
            .updatedBy(entity.getUpdatedBy())
            .build();
    }
}
