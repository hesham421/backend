package com.example.security.repository;

import com.example.security.entity.SecUserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for SEC_USER_PROFILE (ENTITY-SEC-009).
 * PK is USER_ID_FK itself (shared 1:1 PK with USERS) — no separate surrogate ID.
 */
@Repository
public interface SecUserProfileRepository
        extends JpaRepository<SecUserProfile, Long>, JpaSpecificationExecutor<SecUserProfile> {
}
