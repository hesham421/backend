package com.erp.security.crossmodule;

/**
 * Narrow read model returned by {@link SecUserProfileApi} — deliberately not
 * {@code SecUserProfileDto} (the internal DTO), so a consuming module never gains a
 * compile-time dependency on erp-security's internal shape, only on this contract.
 */
public record SecUserProfileView(Long userId, String preferredLang) {
}
