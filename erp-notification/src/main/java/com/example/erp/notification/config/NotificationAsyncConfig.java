package com.example.erp.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * CORE.md — "Post-persist async dispatch (channel/ package, per CORE)". Dedicated bounded
 * executor (not the default {@code SimpleAsyncTaskExecutor}) so RULE-NOTIF-004's retry backoff
 * sleeps (up to ~17s total across 5 attempts) never occupy request threads and are bounded in
 * number, even though the channel send itself is currently a stub (see StubChannelSender).
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
        executor.initialize();
        return executor;
    }
}
