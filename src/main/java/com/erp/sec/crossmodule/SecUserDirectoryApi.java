package com.erp.sec.crossmodule;

import java.util.List;
import java.util.Optional;

/**
 * SEC's only inbound cross-module surface (REQ-SEC-034, REQ-SEC-035): direct Spring interface
 * injection, never loopback HTTP. Both methods are reads and return narrow read-models only —
 * never a JPA entity, an internal DTO, a credential, a session or a grant row.
 */
public interface SecUserDirectoryApi {

    /**
     * REQ-SEC-034 — a user's contact details, for a consumer that must reach that user out of
     * band. Anticipated by NOTIF's {@code RecipientStatusReader} / its
     * {@code DefaultRecipientStatusReader} stub (XM-NOTIF-001). Empty when the id is unknown.
     */
    Optional<UserContact> findContact(Long userPk);

    /**
     * REQ-SEC-035 / QR-SEC-039 — the user ids currently holding {@code permissionCode} through an
     * active role. Anticipated by a consumer module's separation-of-duties check over its own
     * permission codes (FIN RULE-FIN-015).
     */
    List<Long> findUserIdsHoldingPermission(String permissionCode);
}
