package com.erp.notification.crossmodule;

import java.util.List;
import java.util.Map;

/**
 * Cross-module dispatch surface for erp-notification (mirrors {@code POST
 * /api/v1/notifications/send}, API-NOTIF-001). Injected directly by other modules in the same
 * JVM — see governance/.github/skills/backend/create-service/SKILL.md's "Cross-Module Calls
 * (XM)" section. This is the ONLY erp-notification surface another module may depend on; never
 * inject {@code NotificationEventProcessor} or any other internal class directly.
 *
 * <p>Replaces erp-security's old {@code NotificationClient} REST-loopback client (see that
 * class's former javadoc / git history) — this was a tracked, temporary exception until pom
 * consolidation removed the circular-dependency reason it couldn't convert alongside the other
 * 4 cross-module call sites. No principal/authentication is required to call this: it delegates
 * to {@code NotificationEventProcessor.process()}, which is deliberately not
 * {@code @PreAuthorize}-gated for exactly this "trusted in-process caller" reason (see that
 * method's javadoc) — replacing the old REST client's {@code svc-notification} JWT-minting
 * mechanism, which existed only to satisfy an HTTP-layer authentication check that a direct,
 * never-externally-reachable method call doesn't need.
 */
public interface NotificationDispatchApi {

    /**
     * Fire-and-forget — mirrors the old REST client's best-effort semantics (never fails the
     * caller's already-committed flow). Failures are logged by the implementation, not thrown.
     */
    void dispatch(Long recipientId, List<String> channelHint, String templateCode,
                  Map<String, Object> contextData, String priority, String moduleCode);
}
