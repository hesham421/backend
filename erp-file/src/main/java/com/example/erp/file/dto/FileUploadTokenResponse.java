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
@Schema(description = "Issued upload token - رمز الرفع الصادر")
public class FileUploadTokenResponse {

    @Schema(description = "Encrypted, single-use upload token - رمز الرفع المشفر أحادي الاستخدام")
    private String encryptedToken;

    @Schema(description = "Token expiry timestamp - وقت انتهاء صلاحية الرمز")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime expiresAt;
}
