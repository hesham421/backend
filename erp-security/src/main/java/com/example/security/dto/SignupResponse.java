package com.example.security.dto;

/**
 * Response body for self-registration (API-SEC-040). No tokens are issued —
 * RULE-SEC-030 requires the account start disabled, pending activation.
 */
public record SignupResponse(Long userId, String username, boolean enabled) {}
