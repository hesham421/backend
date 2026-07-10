package com.example.security.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite PK for SEC_ROLE_BRANCH (ROLE_ID_FK, BRANCH_ID_FK) — used with
 * {@code @IdClass} on {@link SecRoleBranch}. RULE-SEC-036 (no duplicate role+branch)
 * is enforced by this composite PK at the DB level.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SecRoleBranchId implements Serializable {

    private Long roleIdFk;
    private Long branchIdFk;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SecRoleBranchId that)) {
            return false;
        }
        return Objects.equals(roleIdFk, that.roleIdFk) && Objects.equals(branchIdFk, that.branchIdFk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleIdFk, branchIdFk);
    }
}
