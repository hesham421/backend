package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-003 request body: the raw refresh token to revoke (idempotent).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Logout request - طلب تسجيل الخروج")
public class LogoutRequest {

    @NotBlank(message = "{validation.required}")
    @Schema(description = "Raw refresh token to revoke - رمز التجديد المراد إبطاله", example = "u8Zx1...")
    private String refreshToken;
}
