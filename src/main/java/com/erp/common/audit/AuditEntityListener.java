package com.erp.common.audit;

import com.erp.common.domain.AuditableEntity;
import com.erp.common.util.SecurityContextHelper;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

/**
 * Populates audit fields on {@link AuditableEntity} instances; replaces Spring Data's
 * {@code AuditingEntityListener} to avoid that dependency.
 */
public class AuditEntityListener {

    @PrePersist
    public void prePersist(AuditableEntity entity) {
        Instant now = Instant.now();
        String user = SecurityContextHelper.getUsernameOrSystem();

        entity.setCreatedAt(now);
        entity.setCreatedBy(user);
    }

    @PreUpdate
    public void preUpdate(AuditableEntity entity) {
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(SecurityContextHelper.getUsernameOrSystem());
    }
}
