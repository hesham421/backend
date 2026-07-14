package com.example.erp.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for API-NOTIF-003 (History) — ENTITY-NOTIF-001 (NotificationLog), all fields
 * + audit.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notification log entry - سجل الإشعار")
public class NotificationLogResponse {

    @Schema(description = "Log entry ID")
    private Long id;

    @Schema(description = "Recipient user ID (Security USERS_PK)")
    private Long recipientId;

    @Schema(description = "Channel used for this row (LOV-NOTIF-001)")
    private String notificationTypeId;

    @Schema(description = "Template natural code")
    private String templateCode;

    @Schema(description = "Subject (primarily Email channel)")
    private String subject;

    @Schema(description = "Short preview of the sent content")
    private String bodyPreview;

    @Schema(description = "Delivery status (LOV-NOTIF-002)")
    private String notificationStatusId;

    @Schema(description = "Delivery retry attempts (ceiling 5)")
    private Short retryCount;

    @Schema(description = "Actual send timestamp — null until sent")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant sentAt;

    @Schema(description = "Publishing module code")
    private String moduleCode;

    @Schema(description = "Polymorphic reference to the related business record")
    private Long referenceId;

    @Schema(description = "Related entity type name")
    private String referenceType;

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
