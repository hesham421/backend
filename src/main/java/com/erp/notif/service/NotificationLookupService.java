package com.erp.notif.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.notif.domain.NotificationLogDomain;
import com.erp.notif.dto.LookupOptionResponse;
import com.erp.notif.exception.NotifErrorCodes;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * API-NOTIF-006 — runtime resolution of the NOTIF-local LOVs (LOV-NOTIF-001 NOTIF_CHANNEL,
 * LOV-NOTIF-002 NOTIF_STATUS). These are code lists loaded at runtime (no lookup table, no ENUM per
 * SRS A5), so the options and their bilingual labels (SRS A5) are held in-process. An unknown
 * lookupKey yields ERR-0004 NOT_FOUND.
 *
 * <p>{@code @PreAuthorize("isAuthenticated()")} — deliberate, spec-mandated form (API-NOTIF-006
 * SECURITY = "Security filter"): any authenticated caller may read these platform lookups; they are
 * not gated by SCR-NOTIF-* permissions. Mirrors the MDM lookup-consumption gate.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationLookupService {

    public static final String LOOKUP_NOTIF_CHANNEL = "NOTIF_CHANNEL";
    public static final String LOOKUP_NOTIF_STATUS = "NOTIF_STATUS";

    private static final Map<String, List<LookupOptionResponse>> LOOKUPS = Map.of(
        LOOKUP_NOTIF_CHANNEL, List.of(
            option("EMAIL", "بريد", "Email"),
            option("SMS", "رسالة نصية", "SMS"),
            option("WHATSAPP", "واتساب", "WhatsApp"),
            option("PUSH", "إشعار فوري", "Push"),
            option("INTERNAL", "داخلي", "Internal")),
        // NOTIF_STATUS codes are sourced from NotificationLogDomain (LOV-NOTIF-002 lifecycle
        // owner) so the state machine and this LOV can never drift.
        LOOKUP_NOTIF_STATUS, List.of(
            option(NotificationLogDomain.STATUS_PENDING, "قيد الانتظار", "Pending"),
            option(NotificationLogDomain.STATUS_SENT, "مُرسَل", "Sent"),
            option(NotificationLogDomain.STATUS_FAILED, "فشل", "Failed"),
            option(NotificationLogDomain.STATUS_CHANNEL_DISABLED, "القناة معطّلة", "Channel Disabled")));

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ServiceResult<List<LookupOptionResponse>> get(String lookupKey) {
        log.debug("Resolving NOTIF lookup for key: {}", lookupKey);

        String normalized = lookupKey == null ? null : lookupKey.trim().toUpperCase();
        List<LookupOptionResponse> options = normalized == null ? null : LOOKUPS.get(normalized);
        if (options == null) {
            throw new LocalizedException(
                Status.NOT_FOUND, NotifErrorCodes.NOTIF_LOOKUP_KEY_UNKNOWN, lookupKey);
        }

        return ServiceResult.success(options);
    }

    private static LookupOptionResponse option(String code, String labelAr, String labelEn) {
        return LookupOptionResponse.builder().code(code).labelAr(labelAr).labelEn(labelEn).build();
    }
}
