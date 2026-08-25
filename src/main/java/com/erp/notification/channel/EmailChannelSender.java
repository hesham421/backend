package com.erp.notification.channel;

import com.erp.security.crossmodule.SecurityUserApi;
import com.erp.notification.entity.NotificationChannelConfig;
import com.erp.notification.entity.NotificationLog;
import com.erp.notification.repository.NotificationChannelConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

/**
 * EMAIL-channel adapter (AQ-010/AQ-011 RESOLVED — Email only via Gmail SMTP, free tier).
 * Sends MIME/HTML (not {@link org.springframework.mail.SimpleMailMessage}) so templates can use
 * real markup (dir="rtl"/"ltr" sections, inline CSS) — {@code notif_log.body_preview} is the
 * literal content emailed, widened to TEXT in V13 so it isn't truncated mid-tag.
 *
 * <p>Sender identity (DRV-SEC-NOTIF-005 — Security has no code path that sets a From header;
 * this lives entirely in NOTIF's own {@link NotificationChannelConfig}) is read from the EMAIL
 * row's {@code configJson}: {@code {"mailFrom": "...", "mailFromName": "..."}}. Both keys are
 * optional — unset/unparsable config falls back to {@code spring.mail.username} with no display
 * name, and a bad value never fails the send (same best-effort philosophy as the rest of this
 * class). Note: Gmail's SMTP relay generally only accepts a From *address* that matches the
 * authenticated account (or a verified "Send As" alias) — {@code mailFrom} is therefore only
 * useful once a verified alias exists; {@code mailFromName} (the display name) always works.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailChannelSender implements ChannelSender {

    private static final String CHANNEL_EMAIL = "EMAIL";

    private final JavaMailSender mailSender;
    private final SecurityUserApi securityUserApi;
    private final NotificationChannelConfigRepository channelConfigRepository;
    private final ObjectMapper objectMapper;

    @Value("${spring.mail.username:}")
    private String defaultFromAddress;

    @Override
    public boolean send(NotificationLog logEntry) {
        if (!CHANNEL_EMAIL.equals(logEntry.getNotificationTypeId())) {
            log.warn("EmailChannelSender received a non-EMAIL notification (id={}, channel={}) — no adapter for this channel",
                    logEntry.getId(), logEntry.getNotificationTypeId());
            return false;
        }

        Optional<String> recipientEmail = securityUserApi.findById(logEntry.getRecipientId())
                .map(view -> view.email())
                .filter(email -> !email.isBlank());
        if (recipientEmail.isEmpty()) {
            log.warn("No email on file for recipientId={} (NOTIF_LOG id={}) — cannot dispatch",
                    logEntry.getRecipientId(), logEntry.getId());
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(recipientEmail.get());
            helper.setSubject(logEntry.getSubject());
            helper.setText(logEntry.getBodyPreview(), true);
            applySenderIdentity(helper);
            mailSender.send(message);
            return true;
        } catch (MailException | MessagingException ex) {
            log.warn("Email send failed for NOTIF_LOG id={}, recipientId={}: {}",
                    logEntry.getId(), logEntry.getRecipientId(), ex.getMessage());
            return false;
        }
    }

    private void applySenderIdentity(MimeMessageHelper helper) {
        String fromAddress = defaultFromAddress;
        String fromName = null;
        try {
            NotificationChannelConfig config = channelConfigRepository.findByChannelTypeId(CHANNEL_EMAIL).orElse(null);
            if (config != null && config.getConfigJson() != null && !config.getConfigJson().isBlank()) {
                JsonNode node = objectMapper.readTree(config.getConfigJson());
                if (node.hasNonNull("mailFrom") && !node.get("mailFrom").asText().isBlank()) {
                    fromAddress = node.get("mailFrom").asText();
                }
                if (node.hasNonNull("mailFromName") && !node.get("mailFromName").asText().isBlank()) {
                    fromName = node.get("mailFromName").asText();
                }
            }
        } catch (Exception ex) {
            log.warn("Could not parse EMAIL channel config for sender identity — falling back to {}: {}",
                    defaultFromAddress, ex.getMessage());
        }

        if (fromAddress == null || fromAddress.isBlank()) {
            return;
        }
        try {
            if (fromName != null) {
                helper.setFrom(fromAddress, fromName);
            } else {
                helper.setFrom(fromAddress);
            }
        } catch (UnsupportedEncodingException | MessagingException ex) {
            log.warn("Could not set sender identity ({} / {}) — using mail-session default: {}",
                    fromAddress, fromName, ex.getMessage());
        }
    }
}
