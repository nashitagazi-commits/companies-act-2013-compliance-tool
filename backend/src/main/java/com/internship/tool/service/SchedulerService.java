package com.internship.tool.service;

import com.internship.tool.entity.ComplianceRecord;
import com.internship.tool.repository.ComplianceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final ComplianceRecordRepository repository;
    private final EmailService emailService;

    @Value("${spring.mail.username}")
    private String adminEmail;

    // Daily reminder — runs every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReminder() {
        log.info("Running daily reminder scheduler at: {}",
                LocalDateTime.now());
        try {
            // Build stats
            Map<String, Long> stats = new HashMap<>();
            stats.put("total", repository.countByIsDeletedFalse());
            stats.put("pending", repository.countByStatusAndIsDeletedFalse(
                    ComplianceRecord.ComplianceStatus.PENDING));
            stats.put("inProgress", repository.countByStatusAndIsDeletedFalse(
                    ComplianceRecord.ComplianceStatus.IN_PROGRESS));
            stats.put("completed", repository.countByStatusAndIsDeletedFalse(
                    ComplianceRecord.ComplianceStatus.COMPLETED));
            stats.put("overdue", repository.countByStatusAndIsDeletedFalse(
                    ComplianceRecord.ComplianceStatus.OVERDUE));

            // Send email
            emailService.sendDailyReminder(adminEmail, stats);
            log.info("Daily reminder sent successfully");

        } catch (Exception e) {
            log.error("Daily reminder scheduler failed: {}",
                    e.getMessage());
        }
    }

    // Deadline alert — runs every day at 9:00 AM
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDeadlineAlerts() {
        log.info("Running deadline alert scheduler at: {}",
                LocalDateTime.now());
        try {
            // Find records due in 7 days
            List<ComplianceRecord> recordsDueSoon =
                    repository.findRecordsDueSoon(
                            LocalDate.now(),
                            LocalDate.now().plusDays(7));

            if (!recordsDueSoon.isEmpty()) {
                emailService.sendDeadlineAlert(
                        adminEmail, recordsDueSoon, 7);
                log.info("Deadline alert sent for {} records",
                        recordsDueSoon.size());
            } else {
                log.info("No records due in 7 days — skipping alert");
            }

        } catch (Exception e) {
            log.error("Deadline alert scheduler failed: {}",
                    e.getMessage());
        }
    }

    // Overdue checker — runs every day at 7:00 AM
    @Scheduled(cron = "0 0 7 * * *")
    public void checkAndMarkOverdueRecords() {
        log.info("Running overdue checker at: {}", LocalDateTime.now());
        try {
            List<ComplianceRecord> overdueRecords =
                    repository.findOverdueRecords(LocalDate.now());

            // Mark them as OVERDUE
            overdueRecords.forEach(record -> {
                if (record.getStatus() !=
                        ComplianceRecord.ComplianceStatus.OVERDUE) {
                    record.setStatus(
                            ComplianceRecord.ComplianceStatus.OVERDUE);
                    repository.save(record);
                }
            });

            log.info("Marked {} records as OVERDUE",
                    overdueRecords.size());

        } catch (Exception e) {
            log.error("Overdue checker failed: {}", e.getMessage());
        }
    }
}