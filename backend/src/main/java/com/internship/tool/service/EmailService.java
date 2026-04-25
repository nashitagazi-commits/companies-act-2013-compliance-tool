package com.internship.tool.service;

import com.internship.tool.entity.ComplianceRecord;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Send deadline alert email
    @Async
    public void sendDeadlineAlert(String toEmail,
                                   List<ComplianceRecord> records,
                                   int days) {
        try {
            log.info("Sending deadline alert to: {}", toEmail);

            Context context = new Context();
            context.setVariable("records", records);
            context.setVariable("days", days);

            String htmlContent = templateEngine
                    .process("deadline-alert", context);

            sendHtmlEmail(toEmail,
                    "⚠️ Compliance Deadline Alert — Action Required",
                    htmlContent);

            log.info("Deadline alert sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send deadline alert to {}: {}",
                    toEmail, e.getMessage());
        }
    }

    // Send daily reminder email
    @Async
    public void sendDailyReminder(String toEmail,
                                   Map<String, Long> stats) {
        try {
            log.info("Sending daily reminder to: {}", toEmail);

            Context context = new Context();
            context.setVariable("stats", stats);
            context.setVariable("today", LocalDate.now().toString());

            String htmlContent = templateEngine
                    .process("daily-reminder", context);

            sendHtmlEmail(toEmail,
                    "📋 Daily Compliance Summary — " + LocalDate.now(),
                    htmlContent);

            log.info("Daily reminder sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send daily reminder to {}: {}",
                    toEmail, e.getMessage());
        }
    }

    // Core email sender
    private void sendHtmlEmail(String to,
                                String subject,
                                String htmlContent)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}