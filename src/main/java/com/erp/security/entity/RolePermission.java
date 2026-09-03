package com.erp.security.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ENTITY-SEC-009 — RolePermission (join, Tier-2). Source: db-script-SEC.md DBS-SEC-001,
 * DATA-DOM-RBAC.md. Composite PK (ROLE_FK, PERMISSION_FK); no surrogate id, no audit columns.
 * Grant is subject to RULE-SEC-014 (decided by AuthorizationGrantDomainService).
 */
@Entity
@Table(name = "SEC_ROLE_PERMISSION",
    indexes = {
        @Index(name = "IDX_SEC_ROLE_PERM_PERMISSION_FK", columnList = "PERMISSION_FK")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RolePermission {

    @EmbeddedId
    private RolePermissionId id;
}
