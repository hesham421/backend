package com.erp.security.entity;

import com.erp.common.converter.BooleanNumberConverter;
import com.erp.common.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Role branch scope for DataScope. Composite key (roleIdFk, branchIdFk) via {@code @IdClass}
 * using plain scalar FKs, not navigable associations; referential integrity is enforced at
 * the DB level instead.
 */
@Entity
@Table(name = "SEC_ROLE_BRANCH",
    indexes = {
        @Index(name = "IDX_SEC_ROLE_BRANCH_BRANCH", columnList = "BRANCH_ID_FK")
    })
@IdClass(SecRoleBranchId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class SecRoleBranch extends AuditableEntity {

    @Id
    @Column(name = "ROLE_ID_FK")
    private Long roleIdFk;

    @Id
    @Column(name = "BRANCH_ID_FK")
    private Long branchIdFk;

    // LOV-SEC-002 (DATA_ACCESS_LEVEL) — validated against MD_LOOKUP_DETAIL codes at the
    // Service layer (Phase SVC+API); not enforced by a DB check constraint here.
    @Column(name = "DATA_ACCESS_LEVEL", length = 30, nullable = false)
    private String dataAccessLevel;

    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) {
            isActiveFl = Boolean.TRUE;
        }
    }

    public void activate() {
        this.isActiveFl = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActiveFl = Boolean.FALSE;
    }
}
