package com.example.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for Forgot Password (API-SEC-042, SCR-SEC-009).
 */
public record ForgotPasswordRequest(
        @NotBlank(message = "{validation.required}")
        @Email(message = "{validation.email}")
        String email
) {}
