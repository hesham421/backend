package com.erp.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.Executor;

/**
 * CORE.md — "Post-persist async dispatch (channel/ package, per CORE)". Dedicated bounded
 * executor (not the default {@code SimpleAsyncTaskExecutor}) so RULE-NOTIF-004's retry backoff
 * sleeps (up to ~17s total across 5 attempts) never occupy request threads and are bounded in
 * number. Originally only mattered for StubChannelSender (no outbound calls); now that
 * EmailChannelSender needs {@code SecurityUserApi} (a direct in-JVM call, gated by
 * {@code @PreAuthorize} — see SecurityUserApi's javadoc) to resolve a recipient's email, the
 * executor also needs {@link SecurityContextTaskDecorator} below — without it,
 * {@code SecurityContextHolder} is empty on this thread and every such call is denied. This is
 * unrelated to — and does not touch — dispatchAsync()'s deliberate lack of @PreAuthorize (see
 * NotificationDispatchService javadoc): that's about authorization on this method itself; this
 * decorator only restores the SecurityContext the submitting thread already had, so downstream
 * {@code @PreAuthorize}-gated calls see the same principal they would on a normal request
 * thread. (Previously this propagated a raw {@code Authorization} header for the old
 * *Client+REST pattern — see {@code DispatchAuthContext}'s removal in the interface-injection
 * migration; the actual SecurityContext is what's needed for a direct method call.)
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
        executor.setTaskDecorator(new SecurityContextTaskDecorator());
        executor.initialize();
        return executor;
    }

    /**
     * Captures the submitting thread's {@link SecurityContext} at submission time — not read
     * lazily on the worker thread, since dispatchAsync() is invoked synchronously from the
     * same-thread {@code ApplicationEventPublisher.publishEvent()} call still inside the
     * original request, so the context is still live at capture time (same timing reasoning
     * this class previously used for the Authorization header).
     */
    private static class SecurityContextTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            SecurityContext capturedContext = SecurityContextHolder.getContext();
            return () -> {
                SecurityContext previous = SecurityContextHolder.getContext();
                try {
                    SecurityContextHolder.setContext(capturedContext);
                    runnable.run();
                } finally {
                    SecurityContextHolder.setContext(previous);
                }
            };
        }
    }
}
