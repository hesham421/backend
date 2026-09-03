package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-002 request body: the raw refresh token to rotate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refresh token rotation request - طلب تجديد الرمز")
public class RefreshTokenRequest {

    @NotBlank(message = "{validation.required}")
    @Schema(description = "Raw refresh token - رمز التجديد الخام", example = "u8Zx1...")
    private String refreshToken;
}
