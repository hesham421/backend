package com.example.security.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for account activation (API-SEC-041, SCR-SEC-008).
 */
public record ActivateAccountRequest(
        @NotBlank(message = "{validation.required}")
        String token
) {}
