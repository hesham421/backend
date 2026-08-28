package com.erp.notification.crossmodule;

import java.util.List;
import java.util.Map;

/**
 * Cross-module dispatch surface for erp-notification (mirrors {@code POST
 * /api/v1/notifications/send}) — the ONLY surface another module may depend on; never inject
 * {@code NotificationEventProcessor} directly. No HTTP/JWT principal is required.
 */
public interface NotificationDispatchApi {

    /**
     * Fire-and-forget — mirrors the old REST client's best-effort semantics (never fails the
     * caller's already-committed flow). Failures are logged by the implementation, not thrown.
     */
    void dispatch(Long recipientId, List<String> channelHint, String templateCode,
                  Map<String, Object> contextData, String priority, String moduleCode);
}
