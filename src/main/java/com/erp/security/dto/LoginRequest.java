package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * API-SEC-001 request body. Credentials only — no id, no server-managed fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login credentials - بيانات تسجيل الدخول")
public class LoginRequest {

    @NotBlank(message = "{validation.required}")
    @Schema(description = "Username - اسم المستخدم", example = "admin")
    private String username;

    @NotBlank(message = "{validation.required}")
    @Schema(description = "Raw password - كلمة المرور", example = "P@ssw0rd1")
    @ToString.Exclude // never let the plaintext password reach a log via the generated toString()
    private String password;
}
