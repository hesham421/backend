package com.example.erp.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for API-NOTIF-006/007/008/010 — ENTITY-NOTIF-002 (NotificationTemplate), all
 * fields + audit.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notification template - قالب الإشعار")
public class NotificationTemplateResponse {

    @Schema(description = "Template ID")
    private Long id;

    @Schema(description = "Unique, immutable natural code")
    private String templateCode;

    @Schema(description = "Template display name (Arabic)")
    private String templateNameAr;

    @Schema(description = "Template display name (English)")
    private String templateNameEn;

    @Schema(description = "Target channel (LOV-NOTIF-001)")
    private String channelTypeId;

    @Schema(description = "Owning module code")
    private String moduleCode;

    @Schema(description = "Template body, Arabic")
    private String templateBodyAr;

    @Schema(description = "Template body, English")
    private String templateBodyEn;

    @Schema(description = "DEFERRED FK to FileDocument (File Service) — XM-NOTIF-001, unused Phase 1")
    private Long fileFk;

    @Schema(description = "Active status")
    private Boolean isActiveFl;

    @Schema(description = "Created timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @Schema(description = "Created by")
    private String createdBy;

    @Schema(description = "Updated timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;

    @Schema(description = "Updated by")
    private String updatedBy;
}
