package com.example.erp.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Request DTO for API-NOTIF-002 (Schedule) — extends {@link NotificationSendRequest} + {@code
 * scheduledAt}, per SVCAPI.md.
 *
 * <p><b>DRV-NOTIF-004 (new, this session):</b> DBS-NOTIF-001's NOTIF_LOG field catalog
 * (DBF-0001..0016) has no column to durably persist {@code scheduledAt} — SVCAPI.md's own
 * orchestration note calls the deferred-dispatch mechanism "a scheduled job / delayed queue,
 * P3 implementation detail", but no such column exists to survive a restart. Per Section 2A.3
 * (no column is invented), this layer does NOT fabricate a scheduling column or an in-memory
 * timer that would silently lose scheduled notifications on restart. {@code
 * NotificationEventProcessor.schedule()} therefore processes the request identically to
 * immediate Send for now (dispatch fires right away, not at {@code scheduledAt}) — flagged as
 * GOVERNANCE-NOTE-BLOCKED pending an SRS/DB amendment adding a durable schedule column, same
 * posture as DRV-NOTIF-003 for API-NOTIF-004/005.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Request to schedule a future notification - طلب جدولة إشعار مستقبلي")
public class NotificationScheduleRequest extends NotificationSendRequest {

    @NotNull(message = "{validation.required}")
    @Future(message = "{validation.invalid}")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Requested dispatch time (see DRV-NOTIF-004 — not yet durably honored) - وقت الإرسال المطلوب")
    private LocalDateTime scheduledAt;
}
