package com.example.erp.notification.config;

import com.example.erp.notification.client.DispatchAuthContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.Executor;

/**
 * CORE.md — "Post-persist async dispatch (channel/ package, per CORE)". Dedicated bounded
 * executor (not the default {@code SimpleAsyncTaskExecutor}) so RULE-NOTIF-004's retry backoff
 * sleeps (up to ~17s total across 5 attempts) never occupy request threads and are bounded in
 * number. Originally only mattered for StubChannelSender (no outbound calls); now that
 * EmailChannelSender needs SecurityUserClient's same-JVM self-call to resolve a recipient's
 * email, the executor also needs {@link RequestContextTaskDecorator} below — without it,
 * SecurityUserClient/SecUserProfileClient's forwardedAuthHeaders() finds no Authorization header
 * on this thread and every internal call comes back 401. This is unrelated to — and does not
 * touch — dispatchAsync()'s deliberate lack of @PreAuthorize (see NotificationDispatchService
 * javadoc): that's about SecurityContextHolder/SpEL authorization on this method itself; this
 * decorator only forwards the original Authorization header value so downstream HTTP self-calls
 * can present it, same as they already do on a normal request thread.
 */
@Configuration
@EnableAsync
public class NotificationAsyncConfig {

    public static final String DISPATCH_EXECUTOR = "notificationDispatchExecutor";

    @Bean(name = DISPATCH_EXECUTOR)
    public Executor notificationDispatchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("notif-dispatch-");
        executor.setTaskDecorator(new RequestContextTaskDecorator());
        executor.initialize();
        return executor;
    }

    /**
     * Captures the submitting thread's {@code Authorization} header VALUE (a plain string) — not
     * the live {@code RequestAttributes}/{@code HttpServletRequest}, which the servlet container
     * recycles for reuse once the original response completes, typically already true by the
     * time this executor's worker thread picks up the task; reading a header off a recycled
     * request throws {@code IllegalStateException}. Captured once at submission time, which is
     * fine here: dispatchAsync() is invoked synchronously from the same-thread
     * ApplicationEventPublisher.publishEvent() call still inside the original request, so the
     * header is still present when the executor picks up the task shortly after.
     */
    private static class RequestContextTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            String authorizationHeader = null;
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes servletAttrs) {
                authorizationHeader = servletAttrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            }
            String capturedHeader = authorizationHeader;
            return () -> {
                try {
                    DispatchAuthContext.set(capturedHeader);
                    runnable.run();
                } finally {
                    DispatchAuthContext.clear();
                }
            };
        }
    }
}
