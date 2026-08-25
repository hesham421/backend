package com.erp.notification.service;

import com.erp.common.security.InternalCaller;
import com.erp.notification.event.NotificationRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * CORE.md ingress path 2 (Spring Events, same-process/same-transaction). Deliberately calls
 * {@link NotificationEventProcessor#process} — not the public {@code send()}/{@code schedule()}
 * — because this ingress has no HTTP request/JWT principal to satisfy
 * {@code @PreAuthorize("isAuthenticated()")}; that gate only applies to the REST ingress
 * (API-NOTIF-001/002). {@code process()} is instead gated on {@link InternalCaller#AUTHORITY},
 * which this listener supplies via {@link InternalCaller#run} for the duration of the call. Both
 * ingress paths still converge on the identical validate/fan-out/persist logic, per CORE's
 * "described once, referenced by both" requirement.
 */
@Component
@RequiredArgsConstructor
public class NotificationRequestedEventListener {

    private final NotificationEventProcessor eventProcessor;

    @EventListener
    public void onNotificationRequested(NotificationRequestedEvent event) {
        InternalCaller.run(() -> eventProcessor.process(event.request()));
    }
}
