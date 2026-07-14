package com.example.erp.notification.service;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.notification.client.SecUserProfileClient;
import com.example.erp.notification.dto.NotificationScheduleRequest;
import com.example.erp.notification.dto.NotificationSendConfirmation;
import com.example.erp.notification.dto.NotificationSendRequest;
import com.example.erp.notification.entity.NotificationChannelConfig;
import com.example.erp.notification.entity.NotificationLog;
import com.example.erp.notification.entity.NotificationTemplate;
import com.example.erp.notification.event.NotificationLogPersistedEvent;
import com.example.erp.notification.exception.NotificationErrorCodes;
import com.example.erp.notification.repository.NotificationChannelConfigRepository;
import com.example.erp.notification.repository.NotificationLogRepository;
import com.example.erp.notification.repository.NotificationTemplateRepository;
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
 * Shared internal orchestration for the INTERNAL EVENT PROCESSING block in SVCAPI.md — invoked
 * by both the synchronous REST ingress (API-NOTIF-001/002, via {@link #send}/{@link #schedule})
 * and the same-process Spring Event ingress (via {@code NotificationRequestedEventListener}),
 * per CORE.md's "both ingress forms invoke the identical validation/fan-out/persist sequence
 * declared once ... not duplicated per ingress path."
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

    private final NotificationLogRepository logRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationChannelConfigRepository channelConfigRepository;
    private final SecUserProfileClient languageClient;
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
     * DRV-NOTIF-004 (see {@link NotificationScheduleRequest} javadoc) — no durable column exists
     * for {@code scheduledAt}, so this processes identically to {@link #send} (immediate
     * dispatch) rather than silently building an in-memory-only timer that would lose scheduled
     * notifications on restart.
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
     * Package-private on purpose — the Spring Event ingress
     * ({@code NotificationRequestedEventListener}) calls this directly, bypassing the
     * {@code @PreAuthorize("isAuthenticated()")} REST gate on {@link #send}/{@link #schedule},
     * since that ingress has no HTTP/JWT principal (see the listener's own javadoc).
     */
    List<Long> process(NotificationSendRequest request) {
        validateCompleteness(request);

        List<String> channels = resolveChannels(request.getChannelHint());
        String language = languageClient.resolvePreferredLanguage(request.getRecipientId());
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

    /** RULE-NOTIF-002 — expands the "ALL" sentinel; otherwise the caller's explicit list is used. */
    private List<String> resolveChannels(List<String> channelHint) {
        if (channelHint.size() == 1 && "ALL".equalsIgnoreCase(channelHint.get(0))) {
            return channelConfigRepository.findAllByOrderByChannelTypeIdAsc().stream()
                    .map(NotificationChannelConfig::getChannelTypeId)
                    .toList();
        }
        return channelHint;
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
                .bodyPreview(truncate(renderedBody, 1000))
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
