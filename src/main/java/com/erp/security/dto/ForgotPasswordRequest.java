package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-004 request body: the account email. The response is always neutral (202) to prevent
 * account enumeration, regardless of whether the email matches an active account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Password reset request - طلب إعادة تعيين كلمة المرور")
public class ForgotPasswordRequest {

    @NotBlank(message = "{validation.required}")
    @Email(message = "{validation.invalid}")
    @Schema(description = "Account email - بريد الحساب", example = "user@example.com")
    private String email;
}
