package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-006 request body: the raw activation token and an optional new password. When a
 * password is supplied it must meet complexity (RULE-SEC-003, decided in the service). The
 * token drives the PENDING_ACTIVATION -> ACTIVE transition (RULE-SEC-009).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account activation submission - تفعيل الحساب")
public class ActivateAccountRequest {

    @NotBlank(message = "{validation.required}")
    @Schema(description = "Raw activation token - رمز التفعيل الخام", example = "u8Zx1...")
    private String token;

    @Schema(description = "Optional new raw password - كلمة مرور جديدة اختيارية", example = "P@ssw0rd1")
    private String newPassword;
}
