package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-005 request body: the raw reset token plus the new password. Complexity (RULE-SEC-003)
 * is a business rule decided by UserAccountDomain, not a structural validation, so it is enforced
 * in the service — only presence is checked here.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Password reset submission - إرسال إعادة تعيين كلمة المرور")
public class ResetPasswordRequest {

    @NotBlank(message = "{validation.required}")
    @Schema(description = "Raw reset token - رمز إعادة التعيين الخام", example = "u8Zx1...")
    private String token;

    @NotBlank(message = "{validation.required}")
    @Schema(description = "New raw password - كلمة المرور الجديدة", example = "P@ssw0rd1")
    private String newPassword;
}
