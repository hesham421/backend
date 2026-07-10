package com.example.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for Reset Password (API-SEC-043, SCR-SEC-009).
 */
public record ResetPasswordRequest(
        @NotBlank(message = "{validation.required}")
        String token,

        @NotBlank(message = "{validation.required}")
        @Size(min = 6, max = 120, message = "{validation.size}")
        String newPassword
) {}
