package com.erp.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Composite key for ENTITY-SEC-008 (UserRole): (userAccountFk, roleFk). */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class UserRoleId implements Serializable {

    @Column(name = "USER_ACCOUNT_FK", nullable = false)
    private Long userAccountFk;

    @Column(name = "ROLE_FK", nullable = false)
    private Long roleFk;
}
