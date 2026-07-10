package com.example.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for self-registration (API-SEC-040, SCR-SEC-008).
 */
public record SignupRequest(
        @NotBlank(message = "{validation.required}")
        @Size(min = 3, max = 80, message = "{validation.size}")
        String username,

        @NotBlank(message = "{validation.required}")
        @Email(message = "{validation.email}")
        @Size(max = 150, message = "{validation.size}")
        String email,

        @NotBlank(message = "{validation.required}")
        @Size(min = 6, max = 120, message = "{validation.size}")
        String password
) {}
