package com.example.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "{validation.required}")
        @Size(min = 3, max = 80, message = "{validation.size}")
        String username,

        @NotBlank(message = "{validation.required}")
        @Size(min = 6, max = 120, message = "{validation.size}")
        String password,

        // Optional — the real Add User drawer always sends this (its form state
        // defaults to true). Null (any other caller that omits it) preserves the
        // pre-existing always-enabled behavior.
        Boolean enabled
) {}
