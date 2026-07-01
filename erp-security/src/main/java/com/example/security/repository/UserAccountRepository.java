package com.example.security.repository;

import com.example.security.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * User Account Repository with optimized lazy loading (Rule 6.4)
 * Supports dynamic search via JpaSpecificationExecutor
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, Long>, JpaSpecificationExecutor<UserAccount> {

    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    // Fetch join to avoid N+1 when loading user with roles and permissions
    @Query("SELECT DISTINCT u FROM UserAccount u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :username")
    Optional<UserAccount> findByUsernameWithRoles(@Param("username") String username);

    // Fetch join to avoid N+1 when loading user by ID with roles and permissions
    @Query("SELECT DISTINCT u FROM UserAccount u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.id = :id")
    Optional<UserAccount> findByIdWithRoles(@Param("id") Long id);

    boolean existsByUsernameIgnoreCase(String username);
}
