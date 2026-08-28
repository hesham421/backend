package com.erp.notification.dto;

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
 * Extends {@link NotificationSendRequest} + {@code scheduledAt}. NOTIF_LOG has no column to
 * durably persist it, so {@code schedule()} processes this identically to immediate Send for
 * now (GOVERNANCE-NOTE-BLOCKED) rather than using a timer that loses state on restart.
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
