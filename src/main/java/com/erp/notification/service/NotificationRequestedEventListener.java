package com.erp.notification.service;

import com.erp.common.security.InternalCaller;
import com.erp.notification.event.NotificationRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Calls {@link NotificationEventProcessor#process} (not the public send()/schedule()) since
 * this ingress has no HTTP/JWT principal — gated on {@link InternalCaller#AUTHORITY} instead,
 * supplied via {@link InternalCaller#run}.
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
