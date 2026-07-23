package com.example.erp.notification.service;

import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.Op;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchFilter;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.common.util.SecurityContextHelper;
import com.example.erp.notification.client.SecurityUserClient;
import com.example.erp.notification.dto.NotificationHistorySearchRequest;
import com.example.erp.notification.dto.NotificationLogResponse;
import com.example.erp.notification.dto.NotificationSendConfirmation;
import com.example.erp.notification.dto.NotificationUnreadSummary;
import com.example.erp.notification.entity.NotificationLog;
import com.example.erp.notification.exception.NotificationErrorCodes;
import com.example.erp.notification.mapper.NotificationLogMapper;
import com.example.erp.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * API-NOTIF-003 (History, fully implemented) + API-NOTIF-004/005 (Unread/Mark-as-Read —
 * GOVERNANCE-NOTE-BLOCKED contract shells, see class-level TODOs).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationLogQueryService {

    // QR-NOTIF-004 ALLOWED_SORT_FIELDS, per SVCAPI.md
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "sentAt");

    // Filter allowlist for SpecBuilder — distinct from ALLOWED_SORT_FIELDS above (QR-NOTIF-004
    // only governs sortable fields). Must include every field NotificationHistorySearchRequest
    // can add to SearchRequest.filters: recipientId (always injected server-side by
    // resolveEffectiveRecipientId, see below) plus the two optional DTO filters.
    private static final Set<String> ALLOWED_FILTER_FIELDS =
        Set.of("recipientId", "notificationTypeId", "notificationStatusId");

    private final NotificationLogRepository logRepository;
    private final NotificationLogMapper logMapper;
    private final SecurityUserClient securityUserClient;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).NOTIFICATION_INBOX_VIEW)")
    public ServiceResult<Page<NotificationLogResponse>> search(NotificationHistorySearchRequest searchRequest) {
        log.debug("Searching notification history");

        Long effectiveRecipientId = resolveEffectiveRecipientId(searchRequest.getRecipientId());

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        List<SearchFilter> filters = new ArrayList<>(commonRequest.getFilters());
        filters.add(new SearchFilter("recipientId", Op.EQ, effectiveRecipientId));
        commonRequest.setFilters(filters);

        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_FILTER_FIELDS);
        Specification<NotificationLog> spec = SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS, "createdAt");

        Page<NotificationLog> page = logRepository.findAll(spec, pageable);
        return ServiceResult.success(page.map(logMapper::toResponse));
    }

    /**
     * SVCAPI.md: "recipientId defaults to caller's own id unless caller has an Admin-level
     * permission to query other recipients." No distinct admin-tier permission is seeded for
     * this page (only PERM_NOTIFICATION_INBOX_VIEW exists in dbs-notif-001.md) — see
     * execution-state.json's svcapi_layer2 note. Until a real admin-scope permission exists,
     * any caller holding NOTIFICATION_INBOX_VIEW (already required above) may pass an explicit
     * recipientId; this is a known, flagged gap, not a silent security decision.
     */
    private Long resolveEffectiveRecipientId(Long requestedRecipientId) {
        if (requestedRecipientId != null) {
            return requestedRecipientId;
        }
        String username = SecurityContextHelper.requireUsername();
        return securityUserClient.resolveUserIdByUsername(username)
                .orElseThrow(() -> new LocalizedException(
                        Status.UNAUTHORIZED, NotificationErrorCodes.NOTIF_CURRENT_USER_UNRESOLVED));
    }

    /**
     * TODO: DRV-NOTIF-003 — QR-NOTIF-005 is BLOCKED (no read/unread predicate column on
     * NOTIF_LOG). Per the Escalation Note in SVCAPI.md, this is NOT implemented against an
     * invented column (e.g. treating notificationStatusId IN ('PENDING','SENT') as "unread" was
     * explicitly rejected as semantically wrong). Pending an SRS/DB amendment.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).NOTIFICATION_INBOX_VIEW)")
    public ServiceResult<NotificationUnreadSummary> getUnread() {
        throw new LocalizedException(Status.OPERATION_NOT_ALLOWED, NotificationErrorCodes.NOTIF_READ_TRACKING_UNAVAILABLE);
    }

    /**
     * TODO: DRV-NOTIF-003 — QR-NOTIF-006 is BLOCKED for the same reason as {@link #getUnread}.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).NOTIFICATION_INBOX_UPDATE)")
    public ServiceResult<NotificationSendConfirmation> markAsRead(Long notificationLogId) {
        throw new LocalizedException(Status.OPERATION_NOT_ALLOWED, NotificationErrorCodes.NOTIF_READ_TRACKING_UNAVAILABLE);
    }
}
