package com.vortexadmin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service responsible for sending plain-text emails via JavaMail.  When no SMTP server is
 * configured (e.g., in local development), delivery is skipped and the message content is
 * written to the log instead.
 */
@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${vortex.app.mailFrom}")
    private String mailFrom;

    /**
     * Constructs the {@code MailService} with an {@link ObjectProvider} for the mail sender
     * so that the bean remains optional when SMTP is not configured.
     *
     * @param mailSenderProvider a provider that resolves the {@link JavaMailSender} bean lazily,
     *                           returning {@code null} if no mail sender is configured
     */
    public MailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    /**
     * Returns whether a valid SMTP configuration (host and sender bean) is present.
     * Used as a guard before attempting delivery to avoid errors in environments without SMTP.
     *
     * @return {@code true} if a non-blank mail host and an available {@link JavaMailSender} bean
     *         are both present; {@code false} otherwise
     */
    public boolean isConfigured() {
        return mailHost != null && !mailHost.isBlank() && mailSenderProvider.getIfAvailable() != null;
    }

    /**
     * Sends a plain-text email asynchronously to the specified recipient.  If SMTP is not
     * configured, the message is logged as a warning and no exception is thrown, so the calling
     * thread is not disrupted.
     *
     * @param to      the recipient email address
     * @param subject the email subject line
     * @param body    the plain-text email body
     */
    @Async
    public void send(String to, String subject, String body) {
        if (!isConfigured()) {
            // No SMTP configured (e.g. local dev) — log instead of failing the request
            logger.warn("Mail not configured. Skipping email to {} — subject: {}\n{}", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSenderProvider.getObject().send(message);
            logger.info("Email sent to {} — subject: {}", to, subject);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
