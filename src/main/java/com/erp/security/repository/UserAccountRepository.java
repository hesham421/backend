package com.erp.security.repository;

import com.erp.security.entity.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-SEC-001 (UserAccount). Built general for reuse by the USERS sub, not
 * overfitted to auth. UserAccount does not model its roles as a JPA collection, so login
 * authorities are resolved by a dedicated projection query across the RBAC join tables
 * (SEC_USER_ROLE -> SEC_ROLE_PERMISSION -> SEC_PERMISSION) rather than a JOIN FETCH (DRV-003).
 */
@Repository
public interface UserAccountRepository
    extends JpaRepository<UserAccount, Long>,
            JpaSpecificationExecutor<UserAccount> {

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /** RULE-SEC-001 update-uniqueness — email taken by a DIFFERENT account (API-SEC-009). */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * QR-SEC-0002 (DRV-003) — the distinct active permission codes granted to a user through its
     * roles, used as the JWT authorities. SEC_USER_ROLE -> SEC_ROLE_PERMISSION -> SEC_PERMISSION.
     */
    @Query("""
        SELECT DISTINCT p.permissionCode
        FROM UserRole ur, RolePermission rp, Permission p
        WHERE ur.id.userAccountFk = :userId
          AND rp.id.roleFk = ur.id.roleFk
          AND p.id = rp.id.permissionFk
          AND p.isActive = true
        """)
    List<String> findGrantedPermissionCodes(@Param("userId") Long userId);
}
