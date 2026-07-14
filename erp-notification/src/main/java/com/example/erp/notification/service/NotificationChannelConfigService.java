package com.example.erp.notification.service;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.notification.dto.NotificationChannelConfigResponse;
import com.example.erp.notification.dto.NotificationChannelConfigUpdateRequest;
import com.example.erp.notification.entity.NotificationChannelConfig;
import com.example.erp.notification.exception.NotificationErrorCodes;
import com.example.erp.notification.mapper.NotificationChannelConfigMapper;
import com.example.erp.notification.repository.NotificationChannelConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * API-NOTIF-011 (List, fixed 5 rows, no pagination per SVCAPI.md) + API-NOTIF-012 (Update).
 * No create/delete/search — CORE.md: "no create/delete — only isEnabledFl toggle."
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationChannelConfigService {

    private final NotificationChannelConfigRepository channelConfigRepository;
    private final NotificationChannelConfigMapper channelConfigMapper;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).NOTIFICATION_CHANNEL_CONFIG_VIEW)")
    public ServiceResult<List<NotificationChannelConfigResponse>> list() {
        log.debug("Listing notification channel configs");
        List<NotificationChannelConfigResponse> responses = channelConfigRepository.findAllByOrderByChannelTypeIdAsc()
                .stream()
                .map(channelConfigMapper::toResponse)
                .toList();
        return ServiceResult.success(responses);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).NOTIFICATION_CHANNEL_CONFIG_UPDATE)")
    public ServiceResult<NotificationChannelConfigResponse> update(Long id, NotificationChannelConfigUpdateRequest request) {
        log.info("Updating NotificationChannelConfig ID: {}", id);

        NotificationChannelConfig entity = channelConfigRepository.findById(id)
                .orElseThrow(() -> new LocalizedException(
                        Status.NOT_FOUND, NotificationErrorCodes.NOTIF_CHANNEL_CONFIG_NOT_FOUND, id));

        // RULE-NOTIF-005 fires downstream (NotificationEventProcessor) the next time this
        // channel is used — no rejection rule on the toggle itself, per SVCAPI.md.
        channelConfigMapper.updateEntityFromRequest(entity, request);
        NotificationChannelConfig saved = channelConfigRepository.save(entity);

        log.info("Updated NotificationChannelConfig ID: {}", saved.getId());
        return ServiceResult.success(channelConfigMapper.toResponse(saved), Status.UPDATED);
    }
}
