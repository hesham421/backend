package com.example.security.repository;

import com.example.security.entity.SecRoleBranch;
import com.example.security.entity.SecRoleBranchId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Repository for SEC_ROLE_BRANCH (ENTITY-SEC-010). Composite PK via {@link SecRoleBranchId}.
 */
@Repository
public interface SecRoleBranchRepository
        extends JpaRepository<SecRoleBranch, SecRoleBranchId>, JpaSpecificationExecutor<SecRoleBranch> {

    // RULE-SEC-036 — no duplicate (ROLE_ID_FK, BRANCH_ID_FK); DB composite PK already
    // enforces this, this pre-check exists to surface a clean LocalizedException instead
    // of a raw constraint-violation stack trace (per execution-plan-SEC-gaps.md Section 3).
    boolean existsByRoleIdFkAndBranchIdFk(Long roleIdFk, Long branchIdFk);

    // RULE-SEC-037 — source of the JWT allowedBranches[] claim: active branch scope
    // for the user's active roles, computed by AuthService at login/refresh time.
    List<SecRoleBranch> findByRoleIdFkInAndIsActiveFlTrue(Collection<Long> roleIdFks);
}
