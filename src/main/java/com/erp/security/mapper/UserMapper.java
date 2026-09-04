package com.erp.security.mapper;

import com.erp.security.dto.UserCreateRequest;
import com.erp.security.dto.UserResponse;
import com.erp.security.dto.UserUpdateRequest;
import com.erp.security.entity.UserAccount;
import org.springframework.stereotype.Component;

/** Manual entity/DTO mapper for ENTITY-SEC-001 (UserAccount). Never maps passwordHash. */
@Component
public class UserMapper {

    /**
     * Builds a new UserAccount from a create request. passwordHash and userStatusId are NOT set here
     * — the service assigns an unusable placeholder hash and the PENDING_ACTIVATION status.
     */
    public UserAccount toEntity(UserCreateRequest request) {
        if (request == null) {
            return null;
        }
        return UserAccount.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .phone(request.getPhone())
            .fullName(request.getFullName())
            .preferredLangId(request.getPreferredLangId())
            .isActive(Boolean.TRUE)
            .build();
    }

    /**
     * Mutates in place. Skips username (immutable) and passwordHash (system-managed). isActiveFl,
     * when present, is applied via activate()/deactivate() (never a raw setter); null means "no
     * change". phone is optional — a null (omitted) phone means "no change", consistent with
     * isActiveFl, so a partial update that leaves phone out does not wipe an existing number (send
     * an empty string to clear it explicitly). The RULE-SEC-012 transition guard on userStatusId
     * runs in the service beforehand.
     */
    public void updateEntityFromRequest(UserAccount entity, UserUpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }
        entity.setEmail(request.getEmail());
        if (request.getPhone() != null) {
            entity.setPhone(request.getPhone());
        }
        entity.setFullName(request.getFullName());
        entity.setPreferredLangId(request.getPreferredLangId());
        entity.setUserStatusId(request.getUserStatusId());
        if (request.getIsActiveFl() != null) {
            if (Boolean.TRUE.equals(request.getIsActiveFl())) {
                entity.activate();
            } else {
                entity.deactivate();
            }
        }
    }

    public UserResponse toResponse(UserAccount entity) {
        if (entity == null) {
            return null;
        }
        return UserResponse.builder()
            .id(entity.getId())
            .username(entity.getUsername())
            .email(entity.getEmail())
            .phone(entity.getPhone())
            .fullName(entity.getFullName())
            .preferredLangId(entity.getPreferredLangId())
            .userStatusId(entity.getUserStatusId())
            .failedLoginCount(entity.getFailedLoginCount())
            .lockedUntil(entity.getLockedUntil())
            .isActiveFl(Boolean.TRUE.equals(entity.getIsActive()))
            .createdAt(entity.getCreatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedAt(entity.getUpdatedAt())
            .updatedBy(entity.getUpdatedBy())
            .build();
    }
}
