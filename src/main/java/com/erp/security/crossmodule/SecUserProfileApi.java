package com.erp.security.crossmodule;

import java.util.Optional;

/**
 * Cross-module read surface for erp-security's SEC_USER_PROFILE data (mirrors {@code GET
 * /api/v1/security/user-profiles/{userId}}). Injected directly by other modules in the same
 * JVM — see governance/.github/skills/backend/create-service/SKILL.md's "Cross-Module Calls
 * (XM)" section. This is the ONLY erp-security user-profile surface another module may depend
 * on; never inject {@code SecUserProfileService} or any other internal class directly.
 *
 * <p>Known, pre-existing gap carried over unchanged from the old REST-loopback client: this
 * requires {@code USER_PROFILE_VIEW}, and the calling principal may not hold it (see
 * {@code SecUserProfileApiService}). This migration does not fix or worsen that.
 */
public interface SecUserProfileApi {

    /** Empty if the profile doesn't exist OR the caller lacks {@code USER_PROFILE_VIEW}. */
    Optional<SecUserProfileView> findById(Long userId);
}
