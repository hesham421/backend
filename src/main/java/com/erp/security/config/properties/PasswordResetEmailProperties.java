package com.erp.security.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Password-reset email config, Security-owned. Notification never sees these values directly —
 * only the rendered contextData from {@link com.erp.security.service.PasswordResetEmailContextBuilder}.
 */
@Validated
@ConfigurationProperties(prefix = "erp.security.password-reset")
public record PasswordResetEmailProperties(

        /**
         * Application name shown in the reset email. Default: "ERP System".
         */
        String applicationName,

        /**
         * Support contact email shown in the reset email. Optional — rendered as an empty
         * string when unset.
         */
        String supportEmail,

        /**
         * Base URL the reset link is built from (the token is appended as a query param).
         * Default matches the frontend's dev origin (erp.security.cors.allowed-origins).
         */
        String resetBaseUrl,

        /**
         * Timezone used to format the reset-expiry date/time shown to the user — never the
         * JVM default zone implicitly.
         */
        String defaultTimezone
) {
    public PasswordResetEmailProperties {
        if (applicationName == null || applicationName.isBlank()) {
            applicationName = "ERP System";
        }
        if (resetBaseUrl == null || resetBaseUrl.isBlank()) {
            resetBaseUrl = "http://localhost:4200/reset-password";
        }
        if (defaultTimezone == null || defaultTimezone.isBlank()) {
            defaultTimezone = "Asia/Muscat";
        }
    }
}
