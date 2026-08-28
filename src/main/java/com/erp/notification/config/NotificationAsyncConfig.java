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
 * Dedicated bounded executor so RULE-NOTIF-004's retry backoff never occupies request threads.
 * Also applies {@link SecurityContextTaskDecorator} — without it, EmailChannelSender's
 * SecurityUserApi calls run with an empty SecurityContext and are denied.
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
     * Captures the submitting thread's {@link SecurityContext} at submission time (not lazily on the
     * worker thread) since dispatchAsync() is invoked synchronously while the context is still live.
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
