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
     * Post-implementation-audit remediation, skill-alignment item — this performs a write
     * (persists {@code NotificationLog} rows), so per {@code create-service/SKILL.md}'s
     * "read → REQUIRED, write → REQUIRES_NEW" rule it takes {@code REQUIRES_NEW} explicitly,
     * not the default. The only caller today ({@code AuthEventListener}) invokes this from an
     * {@code AFTER_COMMIT} transactional-event listener — there is no open transaction on the
     * calling thread at that point, so this was previously left at default {@code REQUIRED},
     * reasoning it was behaviorally identical to {@code REQUIRES_NEW} in that one case. That
     * reasoning was correct for today's only caller but silently depended on it never changing:
     * a future caller invoking this from within an active transaction would have fused this
     * write to that unrelated transaction's outcome with no compiler or test warning. Declaring
     * {@code REQUIRES_NEW} explicitly costs nothing today (identical behavior, since there is no
     * transaction to suspend) and removes that latent hazard outright.
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
