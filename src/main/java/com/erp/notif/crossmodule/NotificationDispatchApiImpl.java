package com.erp.notif.crossmodule;

import com.erp.notif.dto.DispatchRequest;
import com.erp.notif.service.DispatchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The dedicated adapter implementing NOTIF's {@link NotificationDispatchApi} — it maps the narrow
 * cross-module {@link DispatchCommand} read-model onto the internal {@link DispatchRequest} and
 * delegates to {@link DispatchService}. Kept separate from the internal service so the exposed
 * cross-module surface stays narrow (build-create-service "Exposing this module to others").
 * Authorization is enforced on the delegate (@PreAuthorize isAuthenticated(), SEC-BE).
 */
@Component
@RequiredArgsConstructor
public class NotificationDispatchApiImpl implements NotificationDispatchApi {

    private final DispatchService dispatchService;

    @Override
    public List<Long> dispatch(DispatchCommand command) {
        DispatchRequest request = DispatchRequest.builder()
            .recipientId(command.recipientId())
            .templateCode(command.templateCode())
            .channelHint(command.channelHint())
            .moduleCode(command.moduleCode())
            .referenceId(command.referenceId())
            .referenceType(command.referenceType())
            .variables(command.variables())
            .build();
        return dispatchService.dispatch(request).getData().getLogIds();
    }
}
