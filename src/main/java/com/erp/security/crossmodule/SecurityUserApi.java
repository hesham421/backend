package com.erp.security.crossmodule;

import java.util.Optional;

/**
 * Cross-module read surface for erp-security's user directory (mirrors {@code POST
 * /api/users/search}). Injected directly by other modules in the same JVM — see
 * governance/.github/skills/backend/create-service/SKILL.md's "Cross-Module Calls (XM)"
 * section. This is the ONLY erp-security user-directory surface another module may depend on;
 * never inject {@code UserService} or any other internal class directly.
 *
 * <p>Known, pre-existing gap carried over unchanged from the old REST-loopback client: both
 * methods require {@code USER_VIEW}, and the calling principal may not hold it (see
 * {@code SecurityUserApiService}). This migration does not fix or worsen that.
 */
public interface SecurityUserApi {

    /** Empty if no such username exists OR the caller lacks {@code USER_VIEW}. */
    Optional<SecurityUserView> findByUsername(String username);

    /** Empty if no such user id exists OR the caller lacks {@code USER_VIEW}. */
    Optional<SecurityUserView> findById(Long userId);
}
