package com.erp.security.crossmodule;

/**
 * Narrow read model returned by {@link SecurityUserApi} — deliberately not {@code UserDto}
 * (the internal DTO), so a consuming module never gains a compile-time dependency on
 * erp-security's internal shape, only on this contract.
 */
public record SecurityUserView(Long id, String username, String email) {
}
