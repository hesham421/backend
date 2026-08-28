package com.erp.notification.crossmodule;

import com.erp.common.security.InternalCaller;
import com.erp.notification.dto.NotificationSendRequest;
import com.erp.notification.service.NotificationEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Kept separate from {@link NotificationEventProcessor} so the cross-module contract surface
 * stays intentionally narrow and doesn't grow un-reviewed as that internal service evolves.
 */
@Slf4j
@Service
@RequiredArgsConstructor
class NotificationDispatchApiService implements NotificationDispatchApi {

    private final NotificationEventProcessor notificationEventProcessor;

    /**
     * Explicitly {@code REQUIRES_NEW} since this persists NotificationLog rows (create-service/
     * SKILL.md: writes require REQUIRES_NEW). Behaviorally identical to REQUIRED for today's only
     * caller, but avoids silently fusing this write to a future caller's transaction.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatch(Long recipientId, List<String> channelHint, String templateCode,
                          Map<String, Object> contextData, String priority, String moduleCode) {
        NotificationSendRequest request = NotificationSendRequest.builder()
                .recipientId(recipientId)
                .channelHint(channelHint)
                .templateCode(templateCode)
                .contextData(contextData)
                .priority(priority)
                .moduleCode(moduleCode)
                .build();
        try {
            InternalCaller.run(() -> notificationEventProcessor.process(request));
        } catch (RuntimeException ex) {
            // Best-effort — never fails the caller's already-committed flow that triggered
            // this, matching the old NotificationClient's REST-loopback fallback philosophy.
            log.warn("Notification dispatch failed for recipient={} templateCode={}: {}",
                    recipientId, templateCode, ex.getMessage());
        }
    }
}
