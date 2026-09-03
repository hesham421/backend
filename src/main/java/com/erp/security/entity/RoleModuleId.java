package com.erp.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Composite key for ENTITY-SEC-011 (RoleModule): (roleFk, moduleFk). */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class RoleModuleId implements Serializable {

    @Column(name = "ROLE_FK", nullable = false)
    private Long roleFk;

    @Column(name = "MODULE_FK", nullable = false)
    private Long moduleFk;
}
