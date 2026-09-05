package com.erp.notif.crossmodule;

import org.springframework.stereotype.Component;

/**
 * Deferred stub for {@link RecipientStatusReader}: assumes every recipient is active.
 *
 * <p>TODO: XM-NOTIF-001 DEFERRED — replace with SEC crossmodule UserAccount status reader when SEC
 * exposes one. SEC currently has no crossmodule surface, and this module must not inject SEC
 * internals, so recipient-active (RULE-NOTIF-007) short-circuits to true for now.
 */
@Component
public class DefaultRecipientStatusReader implements RecipientStatusReader {

    @Override
    public boolean isRecipientActive(Long recipientId) {
        return true;
    }
}
