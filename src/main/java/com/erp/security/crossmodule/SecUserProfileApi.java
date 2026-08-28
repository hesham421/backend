package com.erp.security.crossmodule;

import java.util.Optional;

/**
 * The ONLY cross-module surface other modules may depend on for SEC_USER_PROFILE data — never
 * inject {@code SecUserProfileService} directly. Pre-existing gap: this requires
 * {@code USER_PROFILE_VIEW}, which the calling principal may not hold.
 */
public interface SecUserProfileApi {

    /** Empty if the profile doesn't exist OR the caller lacks {@code USER_PROFILE_VIEW}. */
    Optional<SecUserProfileView> findById(Long userId);
}
