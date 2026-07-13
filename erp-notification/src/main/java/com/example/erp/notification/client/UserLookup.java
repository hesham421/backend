package com.example.erp.notification.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Minimal local view of erp-security's {@code UserDto} (POST /api/users/search). Only the
 * fields needed to resolve a username to its numeric {@code USERS_PK} are declared.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserLookup(Long id, String username) {
}
