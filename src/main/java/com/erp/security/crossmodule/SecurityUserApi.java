package com.erp.security.crossmodule;

import java.util.Optional;

/**
 * The ONLY cross-module surface other modules may depend on for the erp-security user directory
 * — never inject {@code UserService} directly. Pre-existing gap: both methods require
 * {@code USER_VIEW}, which the calling principal may not hold.
 */
public interface SecurityUserApi {

    /** Empty if no such username exists OR the caller lacks {@code USER_VIEW}. */
    Optional<SecurityUserView> findByUsername(String username);

    /** Empty if no such user id exists OR the caller lacks {@code USER_VIEW}. */
    Optional<SecurityUserView> findById(Long userId);
}
