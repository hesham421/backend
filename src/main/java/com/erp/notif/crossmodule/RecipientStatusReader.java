package com.erp.notif.crossmodule;

/**
 * NOTIF-owned port for the recipient-active check (RULE-NOTIF-007, XM-NOTIF-001). Dispatch resolves
 * a recipient's account status through this narrow interface rather than reaching into SEC directly.
 * The eventual real implementation reads SEC's UserAccount status via SEC's own crossmodule surface;
 * until SEC exposes one, {@link DefaultRecipientStatusReader} stubs it.
 */
public interface RecipientStatusReader {

    /** True when the recipient's account is active and may receive notifications (RULE-NOTIF-007). */
    boolean isRecipientActive(Long recipientId);
}
