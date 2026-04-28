package com.internship.tool;

import com.internship.tool.entity.ComplianceRecord;
import com.internship.tool.repository.ComplianceRecordRepository;
import com.internship.tool.service.EmailService;
import com.internship.tool.service.SchedulerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerAndEmailTest {

    @Mock
    private ComplianceRecordRepository repository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SchedulerService schedulerService;

    private ComplianceRecord buildRecord(
            ComplianceRecord.ComplianceStatus status) {
        return ComplianceRecord.builder()
                .id(1L)
                .title("Test Record")
                .companyName("Test Co")
                .complianceType("Filing")
                .status(status)
                .priority(ComplianceRecord.Priority.HIGH)
                .dueDate(LocalDate.now().plusDays(5))
                .isDeleted(false)
                .build();
    }

    @Test
    void testSendDailyReminder_Success() {
        ReflectionTestUtils.setField(
                schedulerService, "adminEmail", "admin@test.com");

        when(repository.countByIsDeletedFalse()).thenReturn(10L);
        when(repository.countByStatusAndIsDeletedFalse(
                ComplianceRecord.ComplianceStatus.PENDING)).thenReturn(3L);
        when(repository.countByStatusAndIsDeletedFalse(
                ComplianceRecord.ComplianceStatus.IN_PROGRESS)).thenReturn(2L);
        when(repository.countByStatusAndIsDeletedFalse(
                ComplianceRecord.ComplianceStatus.COMPLETED)).thenReturn(4L);
        when(repository.countByStatusAndIsDeletedFalse(
                ComplianceRecord.ComplianceStatus.OVERDUE)).thenReturn(1L);
        doNothing().when(emailService)
                .sendDailyReminder(anyString(), anyMap());

        schedulerService.sendDailyReminder();

        verify(emailService, times(1))
                .sendDailyReminder(eq("admin@test.com"), anyMap());
    }

    @Test
    void testSendDeadlineAlerts_WithRecords() {
        ReflectionTestUtils.setField(
                schedulerService, "adminEmail", "admin@test.com");

        ComplianceRecord record = buildRecord(
                ComplianceRecord.ComplianceStatus.PENDING);
        when(repository.findRecordsDueSoon(
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(record));
        doNothing().when(emailService)
                .sendDeadlineAlert(anyString(), anyList(), anyInt());

        schedulerService.sendDeadlineAlerts();

        verify(emailService, times(1))
                .sendDeadlineAlert(eq("admin@test.com"),
                        anyList(), eq(7));
    }

    @Test
    void testSendDeadlineAlerts_NoRecords_SkipsEmail() {
        ReflectionTestUtils.setField(
                schedulerService, "adminEmail", "admin@test.com");

        when(repository.findRecordsDueSoon(
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        schedulerService.sendDeadlineAlerts();

        verify(emailService, never())
                .sendDeadlineAlert(anyString(), anyList(), anyInt());
    }

    @Test
    void testCheckAndMarkOverdueRecords_MarksOverdue() {
        ComplianceRecord record = buildRecord(
                ComplianceRecord.ComplianceStatus.PENDING);
        when(repository.findOverdueRecords(any(LocalDate.class)))
                .thenReturn(List.of(record));
        when(repository.save(any(ComplianceRecord.class)))
                .thenReturn(record);

        schedulerService.checkAndMarkOverdueRecords();

        verify(repository, times(1)).save(any(ComplianceRecord.class));
        assert record.getStatus() ==
                ComplianceRecord.ComplianceStatus.OVERDUE;
    }

    @Test
    void testCheckAndMarkOverdueRecords_AlreadyOverdue_NoSave() {
        ComplianceRecord record = buildRecord(
                ComplianceRecord.ComplianceStatus.OVERDUE);
        when(repository.findOverdueRecords(any(LocalDate.class)))
                .thenReturn(List.of(record));

        schedulerService.checkAndMarkOverdueRecords();

        verify(repository, never()).save(any());
    }

    @Test
    void testCheckAndMarkOverdueRecords_EmptyList() {
        when(repository.findOverdueRecords(any(LocalDate.class)))
                .thenReturn(List.of());

        schedulerService.checkAndMarkOverdueRecords();

        verify(repository, never()).save(any());
    }

    @Test
    void testSendDailyReminder_RepositoryCalledForAllStatuses() {
        ReflectionTestUtils.setField(
                schedulerService, "adminEmail", "admin@test.com");

        when(repository.countByIsDeletedFalse()).thenReturn(5L);
        when(repository.countByStatusAndIsDeletedFalse(any()))
                .thenReturn(1L);
        doNothing().when(emailService)
                .sendDailyReminder(anyString(), anyMap());

        schedulerService.sendDailyReminder();

        verify(repository, times(1)).countByIsDeletedFalse();
        verify(repository, times(4))
                .countByStatusAndIsDeletedFalse(any());
    }

    @Test
    void testSendDeadlineAlerts_MultipleRecords() {
        ReflectionTestUtils.setField(
                schedulerService, "adminEmail", "admin@test.com");

        List<ComplianceRecord> records = List.of(
                buildRecord(ComplianceRecord.ComplianceStatus.PENDING),
                buildRecord(ComplianceRecord.ComplianceStatus.IN_PROGRESS));

        when(repository.findRecordsDueSoon(
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records);
        doNothing().when(emailService)
                .sendDeadlineAlert(anyString(), anyList(), anyInt());

        schedulerService.sendDeadlineAlerts();

        verify(emailService, times(1))
                .sendDeadlineAlert(anyString(), eq(records), eq(7));
    }
}