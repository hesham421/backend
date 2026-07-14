package com.example.erp.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for API-NOTIF-001 (Send Immediate) and the base of API-NOTIF-002 (Schedule).
 * Field completeness is RULE-NOTIF-001 (ERR-NOTIF-0001) — re-checked in
 * {@code NotificationEventProcessor} itself (not just here) because the same validation must
 * fire for every ingress path (REST, Spring Event), not only the REST one that {@code @Valid}
 * covers (SVCAPI.md: "Trigger: On publish of any NotificationEvent (any ingress)").
 *
 * <p>{@code moduleCode}/{@code referenceId}/{@code referenceType} are NOT listed in SVCAPI.md's
 * literal Request DTO field set for API-NOTIF-001, but {@code NOTIF_LOG.MODULE_CODE} is NOT
 * NULL (DBF-0010) with no other source for it (no service-to-service credential/principal
 * carries a caller module identity in this codebase) — added here as a documented plan gap,
 * matching this module's own MISSING_IN_DOCS convention. referenceId/referenceType are
 * genuinely OPTIONAL columns (DBF-0011/0012) and are exposed for the same reason.
 *
 * <p>Uses {@code @SuperBuilder} (not the DTO-standard {@code @Builder}) because
 * {@link NotificationScheduleRequest} extends this class — the plan itself mandates that
 * inheritance ("NotificationScheduleRequest extends NotificationSendRequest + {scheduledAt}"),
 * which requires an inheritance-safe builder, same reasoning as entity builders.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to send a notification immediately - طلب إرسال إشعار فوري")
public class NotificationSendRequest {

    @NotNull(message = "{validation.required}")
    @Schema(description = "Recipient user ID (Security USERS_PK) - معرف المستخدم المستقبِل", example = "42")
    private Long recipientId;

    // RULE-NOTIF-002 — single channel, a list, or the sentinel "ALL". Accepts either a bare
    // JSON string or a JSON array on the wire via ACCEPT_SINGLE_VALUE_AS_ARRAY.
    @NotEmpty(message = "{validation.required}")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @Schema(description = "Channel(s): single code, list, or \"ALL\" - القناة أو القنوات المطلوبة",
            example = "[\"EMAIL\", \"PUSH\"]")
    private List<String> channelHint;

    @NotBlank(message = "{validation.required}")
    @Schema(description = "Template natural code (NOTIF_TEMPLATE.TEMPLATE_CODE) - رمز القالب", example = "USER_WELCOME")
    private String templateCode;

    @NotNull(message = "{validation.required}")
    @Schema(description = "Placeholder substitution data for the template body - بيانات القالب")
    private Map<String, Object> contextData;

    @NotBlank(message = "{validation.required}")
    @Pattern(regexp = "HIGH|MEDIUM|LOW", message = "{validation.pattern}")
    @Schema(description = "Priority (HIGH/MEDIUM/LOW) - لا يوجد عمود مخصص لتخزينها حالياً (Phase 1)", example = "MEDIUM")
    private String priority;

    // Plan gap — see class javadoc. Required: NOTIF_LOG.MODULE_CODE is NOT NULL.
    @NotBlank(message = "{validation.required}")
    @Schema(description = "Publishing module code (NOTIF_LOG.MODULE_CODE) - رمز الموديول الناشر", example = "SECURITY")
    private String moduleCode;

    // Plan gap — see class javadoc. Optional, mirrors NOTIF_LOG.REFERENCE_ID (nullable).
    @Schema(description = "Polymorphic reference to the related business record - معرّف مرجعي (اختياري)")
    private Long referenceId;

    // Plan gap — see class javadoc. Optional, mirrors NOTIF_LOG.REFERENCE_TYPE (nullable).
    @Schema(description = "Related entity type name from the publishing module - نوع الكيان المرجعي (اختياري)")
    private String referenceType;
}
