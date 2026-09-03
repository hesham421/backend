package com.erp.common.domain;

import com.erp.common.audit.AuditEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
@EntityListeners(AuditEntityListener.class)
public abstract class AuditableEntity {

    @Column(name = "CREATED_BY", updatable = false, length = 255)
    private String createdBy;

    @Column(name = "CREATED_AT", updatable = false)
    private Instant createdAt;

    @Column(name = "UPDATED_BY", length = 255)
    private String updatedBy;

    @Column(name = "UPDATED_AT")
    private Instant updatedAt;
}
