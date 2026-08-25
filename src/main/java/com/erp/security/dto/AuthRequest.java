package com.erp.security.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "{validation.required}") String username,
        @NotBlank(message = "{validation.required}") String password
) {}
