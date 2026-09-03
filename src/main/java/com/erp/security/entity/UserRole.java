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
 * ENTITY-SEC-008 — UserRole (join). Source: db-script-SEC.md DBS-SEC-001, DATA-DOM-RBAC.md.
 * Composite PK (USER_ACCOUNT_FK, ROLE_FK); no surrogate id, no audit columns.
 */
@Entity
@Table(name = "SEC_USER_ROLE",
    indexes = {
        @Index(name = "IDX_SEC_USER_ROLE_ROLE_FK", columnList = "ROLE_FK")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRole {

    @EmbeddedId
    private UserRoleId id;
}
