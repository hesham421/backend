package com.example.erp.notification.channel;

import com.example.erp.notification.client.SecurityUserClient;
import com.example.erp.notification.entity.NotificationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * EMAIL-channel adapter (AQ-010/AQ-011 RESOLVED — Email only via Gmail SMTP, free tier).
 * Replaces {@link StubChannelSender} as the registered {@link ChannelSender} bean now that
 * EMAIL is the only enabled channel ({@code NOTIF_CHANNEL_CONFIG.is_enabled_fl}); SMS/WhatsApp/
 * Push rows never reach {@link #send} at all — {@code NotificationEventProcessor} marks them
 * {@code CHANNEL_DISABLED} at persist time before dispatch.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailChannelSender implements ChannelSender {

    private static final String CHANNEL_EMAIL = "EMAIL";

    private final JavaMailSender mailSender;
    private final SecurityUserClient securityUserClient;

    @Override
    public boolean send(NotificationLog logEntry) {
        if (!CHANNEL_EMAIL.equals(logEntry.getNotificationTypeId())) {
            log.warn("EmailChannelSender received a non-EMAIL notification (id={}, channel={}) — no adapter for this channel",
                    logEntry.getId(), logEntry.getNotificationTypeId());
            return false;
        }

        Optional<String> recipientEmail = securityUserClient.resolveEmailByUserId(logEntry.getRecipientId());
        if (recipientEmail.isEmpty()) {
            log.warn("No email on file for recipientId={} (NOTIF_LOG id={}) — cannot dispatch",
                    logEntry.getRecipientId(), logEntry.getId());
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail.get());
            message.setSubject(logEntry.getSubject());
            message.setText(logEntry.getBodyPreview());
            mailSender.send(message);
            return true;
        } catch (MailException ex) {
            log.warn("Email send failed for NOTIF_LOG id={}, recipientId={}: {}",
                    logEntry.getId(), logEntry.getRecipientId(), ex.getMessage());
            return false;
        }
    }
}
