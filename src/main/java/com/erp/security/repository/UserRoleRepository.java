package com.erp.security.repository;

import com.erp.security.entity.Role;
import com.erp.security.entity.UserRole;
import com.erp.security.entity.UserRoleId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-SEC-008 (UserRole, role-assignment join). The assignment (API-SEC-012) is
 * served by the inherited composite-key methods ({@code existsById}/{@code save} on
 * {@link UserRoleId}) — QR-SEC-0017 — for an idempotent insert into SEC_USER_ROLE.
 */
@Repository
public interface UserRoleRepository
    extends JpaRepository<UserRole, UserRoleId>,
            JpaSpecificationExecutor<UserRole> {

    /**
     * The roles currently assigned to a user — the read counterpart of the assignment join
     * (API-SEC-012), used to pre-populate the user drawer's roles picker in edit mode. Returns
     * every current assignment (active or not) so the picker reflects true state; ordered by role
     * id for deterministic output. Empty when the user holds no role.
     */
    @Query("""
        SELECT r
        FROM UserRole ur, Role r
        WHERE ur.id.userAccountFk = :userId
          AND r.id = ur.id.roleFk
        ORDER BY r.id
        """)
    List<Role> findRolesByUserId(@Param("userId") Long userId);
}
