package com.example.erp.file.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Issued download/delete access token - رمز الوصول الصادر للتنزيل أو الحذف")
public class FileAccessTokenResponse {

    @Schema(description = "Encrypted, single-use access token - رمز الوصول المشفر أحادي الاستخدام")
    private String encryptedToken;

    @Schema(description = "Token expiry timestamp - وقت انتهاء صلاحية الرمز")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime expiresAt;
}
