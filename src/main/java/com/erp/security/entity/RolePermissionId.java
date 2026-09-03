package com.erp.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Composite key for ENTITY-SEC-009 (RolePermission): (roleFk, permissionFk). */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class RolePermissionId implements Serializable {

    @Column(name = "ROLE_FK", nullable = false)
    private Long roleFk;

    @Column(name = "PERMISSION_FK", nullable = false)
    private Long permissionFk;
}
