package com.erp.notification.service;

import com.erp.notification.channel.ChannelSender;
import com.erp.notification.entity.NotificationLog;
import com.erp.notification.repository.NotificationLogRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Post-implementation-audit remediation, Item 2 — plain Mockito unit test (no Spring context,
 * matching this repo's existing zero-integration-test baseline; see
 * {@code CrossModuleBoundaryArchTest} for the only other test class) proving
 * {@link FailedNotificationSweepScheduler}'s retry/terminal-state logic actually behaves as
 * documented, not just that it compiles.
 */
class FailedNotificationSweepSchedulerTest {

    private final NotificationLogRepository logRepository = mock(NotificationLogRepository.class);
    private final ChannelSender channelSender = mock(ChannelSender.class);
    private final FailedNotificationSweepScheduler scheduler =
            new FailedNotificationSweepScheduler(logRepository, channelSender);

    @Test
    void noCandidates_doesNothing() {
        when(logRepository.findByNotificationStatusIdAndSweepRetryCountLessThan(
                eq(NotificationLog.STATUS_FAILED), anyShort())).thenReturn(List.of());

        scheduler.sweep();

        verify(channelSender, times(0)).send(any());
    }

    @Test
    void successfulRetry_marksSentAndSaves() {
        NotificationLog failedRow = NotificationLog.builder()
                .id(1L)
                .notificationStatusId(NotificationLog.STATUS_FAILED)
                .sweepRetryCount((short) 1)
                .build();
        when(logRepository.findByNotificationStatusIdAndSweepRetryCountLessThan(
                eq(NotificationLog.STATUS_FAILED), eq(NotificationLog.MAX_SWEEP_RETRY_COUNT)))
                .thenReturn(List.of(failedRow));
        when(channelSender.send(failedRow)).thenReturn(true);

        scheduler.sweep();

        assertThat(failedRow.getNotificationStatusId()).isEqualTo(NotificationLog.STATUS_SENT);
        assertThat(failedRow.getSentAt()).isNotNull();
        assertThat(failedRow.getSweepRetryCount()).isEqualTo((short) 1);
        verify(logRepository).save(failedRow);
    }

    @Test
    void failedRetry_incrementsSweepRetryCountAndStaysFailed() {
        NotificationLog failedRow = NotificationLog.builder()
                .id(2L)
                .notificationStatusId(NotificationLog.STATUS_FAILED)
                .sweepRetryCount((short) 0)
                .build();
        when(logRepository.findByNotificationStatusIdAndSweepRetryCountLessThan(
                eq(NotificationLog.STATUS_FAILED), eq(NotificationLog.MAX_SWEEP_RETRY_COUNT)))
                .thenReturn(List.of(failedRow));
        when(channelSender.send(failedRow)).thenReturn(false);

        scheduler.sweep();

        assertThat(failedRow.getNotificationStatusId()).isEqualTo(NotificationLog.STATUS_FAILED);
        assertThat(failedRow.getSweepRetryCount()).isEqualTo((short) 1);
        verify(logRepository).save(failedRow);
    }

    @Test
    void sweep_queriesTheRepositoryWithTheDocumentedCeiling() {
        // The ceiling itself is enforced by the repository query (findBy...LessThan), not this
        // class — this test pins the exact status/ceiling arguments the scheduler passes to it.
        when(logRepository.findByNotificationStatusIdAndSweepRetryCountLessThan(
                eq(NotificationLog.STATUS_FAILED), eq(NotificationLog.MAX_SWEEP_RETRY_COUNT)))
                .thenReturn(List.of());

        scheduler.sweep();

        verify(logRepository).findByNotificationStatusIdAndSweepRetryCountLessThan(
                NotificationLog.STATUS_FAILED, NotificationLog.MAX_SWEEP_RETRY_COUNT);
        assertThat(NotificationLog.MAX_SWEEP_RETRY_COUNT).isEqualTo((short) 3);
    }
}
