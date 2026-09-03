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
 * ENTITY-SEC-011 — RoleModule (join, Tier-1 grant). Source: db-script-SEC.md DBS-SEC-001,
 * DATA-DOM-RBAC.md. Composite PK (ROLE_FK, MODULE_FK); no surrogate id, no audit columns.
 * Presence of a (role, module) row = the role is granted the module (RULE-SEC-013).
 */
@Entity
@Table(name = "SEC_ROLE_MODULE",
    indexes = {
        @Index(name = "IDX_SEC_ROLE_MODULE_MODULE_FK", columnList = "MODULE_FK")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoleModule {

    @EmbeddedId
    private RoleModuleId id;
}
