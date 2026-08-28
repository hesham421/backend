package com.erp.notification.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.security.InternalCaller;
import com.erp.security.crossmodule.SecUserProfileApi;
import com.erp.notification.dto.NotificationScheduleRequest;
import com.erp.notification.dto.NotificationSendConfirmation;
import com.erp.notification.dto.NotificationSendRequest;
import com.erp.notification.entity.NotificationChannelConfig;
import com.erp.notification.entity.NotificationLog;
import com.erp.notification.entity.NotificationTemplate;
import com.erp.notification.event.NotificationLogPersistedEvent;
import com.erp.notification.exception.NotificationErrorCodes;
import com.erp.notification.repository.NotificationChannelConfigRepository;
import com.erp.notification.repository.NotificationLogRepository;
import com.erp.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shared orchestration invoked by both the REST ingress (send/schedule) and the Spring-Event
 * ingress, so validation/fan-out/persist logic is declared once, not duplicated per path.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventProcessor {

    /**
     * DRV-NOTIF-002 fallback template code — see {@link #resolveTemplate}. Not seeded by any
     * governed script; used only as a first fallback probe before the fully in-memory default.
     */
    private static final String DEFAULT_TEMPLATE_CODE = "SYSTEM_DEFAULT";

    /**
     * body_preview is TEXT now (was VARCHAR(1000) — see V13 migration), but it's still the
     * literal content EmailChannelSender emails, not an unbounded log field — this is a
     * defensive ceiling against a pathological template, not a real-world limit for an email.
     */
    private static final int MAX_BODY_LENGTH = 20_000;

    /** RULE-NOTIF-006 — falls back to this when {@link SecUserProfileApi} can't resolve a
     * language (profile missing, no preferredLang set, or the known USER_PROFILE_VIEW gap —
     * see {@link SecUserProfileApi}'s javadoc). This class, not erp-security, owns this
     * fallback decision — {@link SecUserProfileApi} only supplies the raw profile data. */
    private static final String DEFAULT_LANGUAGE = "EN";

    private final NotificationLogRepository logRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationChannelConfigRepository channelConfigRepository;
    private final SecUserProfileApi secUserProfileApi;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ServiceResult<NotificationSendConfirmation> send(NotificationSendRequest request) {
        log.info("Processing notification send — recipient={}, templateCode={}, moduleCode={}",
                request.getRecipientId(), request.getTemplateCode(), request.getModuleCode());
        List<Long> logIds = process(request);
        return ServiceResult.success(toConfirmation(logIds), Status.CREATED);
    }

    /**
     * No durable column exists for {@code scheduledAt}, so this processes identically to
     * {@link #send} (immediate dispatch) rather than silently building an in-memory-only timer that
     * would lose scheduled notifications on restart.
     */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ServiceResult<NotificationSendConfirmation> schedule(NotificationScheduleRequest request) {
        log.info("Processing notification schedule (DRV-NOTIF-004 — dispatching immediately, see class javadoc) "
                        + "— recipient={}, templateCode={}, scheduledAt={}",
                request.getRecipientId(), request.getTemplateCode(), request.getScheduledAt());
        List<Long> logIds = process(request);
        return ServiceResult.success(toConfirmation(logIds), Status.CREATED);
    }

    /**
     * Gated on {@link InternalCaller#AUTHORITY}, not {@code isAuthenticated()}, since Spring Event/cross-module callers
     * have no HTTP principal — no external request can satisfy this under any authentication. The literal below MUST
     * equal {@link InternalCaller#AUTHORITY} as a plain string, not a SpEL type reference.
     */
    @PreAuthorize("hasAuthority('INTERNAL_TRUSTED_CALLER')")
    public List<Long> process(NotificationSendRequest request) {
        validateCompleteness(request);

        List<String> channels = resolveChannels(request.getChannelHint());
        String language = resolvePreferredLanguage(request.getRecipientId());
        NotificationTemplate template = resolveTemplate(request.getTemplateCode());

        List<Long> logIds = new ArrayList<>();
        for (String channel : channels) {
            NotificationLog logEntry = persistForChannel(request, channel, template, language);
            logIds.add(logEntry.getId());
            if (NotificationLog.STATUS_PENDING.equals(logEntry.getNotificationStatusId())) {
                eventPublisher.publishEvent(new NotificationLogPersistedEvent(logEntry.getId()));
            }
        }
        return logIds;
    }

    private NotificationSendConfirmation toConfirmation(List<Long> logIds) {
        return NotificationSendConfirmation.builder().logEntryIds(logIds.toArray(new Long[0])).build();
    }

    /** RULE-NOTIF-001 — re-checked here (not just via DTO @Valid) so every ingress is covered. */
    private void validateCompleteness(NotificationSendRequest request) {
        boolean incomplete = request.getRecipientId() == null
                || request.getChannelHint() == null || request.getChannelHint().isEmpty()
                || request.getTemplateCode() == null || request.getTemplateCode().isBlank()
                || request.getContextData() == null
                || request.getPriority() == null || request.getPriority().isBlank();
        if (incomplete) {
            throw new LocalizedException(Status.BAD_REQUEST, NotificationErrorCodes.NOTIF_EVENT_INCOMPLETE);
        }
    }

    /**
     * Moved here from the old {@code SecUserProfileClient}'s DEFAULT_LANGUAGE-fallback logic since
     * this is this Service's own decision — {@code SecUserProfileApi} only supplies raw profile
     * data, never fails the send.
     */
    private String resolvePreferredLanguage(Long recipientId) {
        return secUserProfileApi.findById(recipientId)
                .map(view -> view.preferredLang())
                .filter(lang -> lang != null && !lang.isBlank())
                .orElse(DEFAULT_LANGUAGE);
    }

    /** RULE-NOTIF-002 — expands the "ALL" sentinel; otherwise the caller's explicit list is used. */
    private List<String> resolveChannels(List<String> channelHint) {
        if (channelHint.size() == 1 && "ALL".equalsIgnoreCase(channelHint.get(0))) {
            return channelConfigRepository.findAllByOrderByChannelTypeIdAsc().stream()
                    .map(NotificationChannelConfig::getChannelTypeId)
                    .toList();
        }
        // NOTIF_CHANNEL_CONFIG.channel_type_id is stored uppercase (EMAIL, SMS, ...);
        // normalize here so a lowercase/mixed-case request value doesn't silently miss the
        // config lookup in persistForChannel() and get treated as disabled/unconfigured.
        return channelHint.stream().map(c -> c == null ? null : c.trim().toUpperCase()).toList();
    }

    /**
     * QR-NOTIF-002 + DRV-NOTIF-002 fallback: active-template lookup, then a probe of
     * {@link #DEFAULT_TEMPLATE_CODE}, then a fully in-memory generic template — never fails the
     * send for a missing template (RULE-NOTIF-006).
     */
    private NotificationTemplate resolveTemplate(String templateCode) {
        return templateRepository.findByTemplateCodeAndIsActiveFlTrue(templateCode)
                .or(() -> templateRepository.findByTemplateCodeAndIsActiveFlTrue(DEFAULT_TEMPLATE_CODE))
                .orElseGet(this::transientFallbackTemplate);
    }

    private NotificationTemplate transientFallbackTemplate() {
        log.warn("No active NotificationTemplate found (including fallback '{}') — using generic "
                + "in-memory fallback (DRV-NOTIF-002); not persisted.", DEFAULT_TEMPLATE_CODE);
        return NotificationTemplate.builder()
                .templateCode(DEFAULT_TEMPLATE_CODE)
                .templateNameAr("إشعار")
                .templateNameEn("Notification")
                .channelTypeId("INTERNAL")
                .moduleCode("NOTIFICATION")
                .templateBodyAr("لديك إشعار جديد")
                .templateBodyEn("You have a new notification")
                .build();
    }

    /** QR-NOTIF-003 — one row per fan-out channel; RULE-NOTIF-005 for the disabled-channel case. */
    private NotificationLog persistForChannel(NotificationSendRequest request, String channelTypeId,
                                               NotificationTemplate template, String language) {
        NotificationChannelConfig channelConfig = channelConfigRepository.findByChannelTypeId(channelTypeId).orElse(null);
        boolean enabled = channelConfig != null && Boolean.TRUE.equals(channelConfig.getIsEnabledFl());

        String subject = "AR".equalsIgnoreCase(language) ? template.getTemplateNameAr() : template.getTemplateNameEn();
        String renderedBody = renderBody(template, language, request.getContextData());

        NotificationLog logEntry = NotificationLog.builder()
                .recipientId(request.getRecipientId())
                .notificationTypeId(channelTypeId)
                .templateCode(request.getTemplateCode())
                .subject(truncate(subject, 500))
                .bodyPreview(truncate(renderedBody, MAX_BODY_LENGTH))
                .moduleCode(request.getModuleCode())
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .build();

        if (!enabled) {
            logEntry.markChannelDisabled();
            log.debug("Channel {} disabled or unconfigured — logging CHANNEL_DISABLED for recipient {}",
                    channelTypeId, request.getRecipientId());
        }

        return logRepository.save(logEntry);
    }

    /** RULE-NOTIF-006 fallback body, with {{placeholder}} substitution from contextData. */
    private String renderBody(NotificationTemplate template, String language, Map<String, Object> contextData) {
        String body = template.resolveBody(language);
        if (body == null || contextData == null || contextData.isEmpty()) {
            return body;
        }
        String rendered = body;
        for (Map.Entry<String, Object> entry : contextData.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return rendered;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
