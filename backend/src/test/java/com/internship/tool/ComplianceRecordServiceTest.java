package com.internship.tool;

import com.internship.tool.entity.ComplianceRecord;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.exception.ValidationException;
import com.internship.tool.repository.ComplianceRecordRepository;
import com.internship.tool.service.ComplianceRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplianceRecordServiceTest {

    @Mock
    private ComplianceRecordRepository repository;

    @InjectMocks
    private ComplianceRecordServiceImpl service;

    private ComplianceRecord sampleRecord;

    @BeforeEach
    void setUp() {
        sampleRecord = ComplianceRecord.builder()
                .id(1L)
                .title("Annual Return Filing")
                .companyName("Test Company Ltd")
                .complianceType("Annual Filing")
                .status(ComplianceRecord.ComplianceStatus.PENDING)
                .priority(ComplianceRecord.Priority.HIGH)
                .dueDate(LocalDate.now().plusDays(10))
                .isDeleted(false)
                .build();
    }

    @Test
    void testCreateRecord_Success() {
        when(repository.save(any(ComplianceRecord.class)))
                .thenReturn(sampleRecord);
        ComplianceRecord result = service.createRecord(sampleRecord);
        assertNotNull(result);
        assertEquals("Annual Return Filing", result.getTitle());
        verify(repository, times(1)).save(any(ComplianceRecord.class));
    }

    @Test
    void testCreateRecord_EmptyTitle_ThrowsValidationException() {
        ComplianceRecord invalidRecord = ComplianceRecord.builder()
                .title("")
                .companyName("Test Company")
                .complianceType("Filing")
                .build();
        assertThrows(ValidationException.class,
                () -> service.createRecord(invalidRecord));
        verify(repository, never()).save(any());
    }

    @Test
    void testCreateRecord_EmptyCompanyName_ThrowsValidationException() {
        ComplianceRecord invalidRecord = ComplianceRecord.builder()
                .title("Test Title")
                .companyName("")
                .complianceType("Filing")
                .build();
        assertThrows(ValidationException.class,
                () -> service.createRecord(invalidRecord));
    }

    @Test
    void testGetRecordById_Success() {
        when(repository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(sampleRecord));
        ComplianceRecord result = service.getRecordById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Annual Return Filing", result.getTitle());
    }

    @Test
    void testGetRecordById_NotFound_ThrowsException() {
        when(repository.findByIdAndIsDeletedFalse(99L))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.getRecordById(99L));
    }

    @Test
    void testGetAllRecords_ReturnsPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ComplianceRecord> page =
                new PageImpl<>(List.of(sampleRecord));
        when(repository.findByIsDeletedFalse(pageable)).thenReturn(page);
        Page<ComplianceRecord> result = service.getAllRecords(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testDeleteRecord_SoftDelete_Success() {
        when(repository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(sampleRecord));
        when(repository.save(any(ComplianceRecord.class)))
                .thenReturn(sampleRecord);
        service.deleteRecord(1L);
        assertTrue(sampleRecord.getIsDeleted());
        verify(repository, times(1)).save(sampleRecord);
    }

    @Test
    void testUpdateStatus_Success() {
        when(repository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(sampleRecord));
        when(repository.save(any(ComplianceRecord.class)))
                .thenReturn(sampleRecord);
        ComplianceRecord result = service.updateStatus(
                1L, ComplianceRecord.ComplianceStatus.COMPLETED);
        assertEquals(ComplianceRecord.ComplianceStatus.COMPLETED,
                result.getStatus());
    }

    @Test
    void testGetStats_ReturnsCorrectKeys() {
        when(repository.countByIsDeletedFalse()).thenReturn(10L);
        when(repository.countByStatusAndIsDeletedFalse(
                ComplianceRecord.ComplianceStatus.PENDING)).thenReturn(3L);
        when(repository.countByStatusAndIsDeletedFalse(
                ComplianceRecord.ComplianceStatus.IN_PROGRESS)).thenReturn(2L);
        when(repository.countByStatusAndIsDeletedFalse(
                ComplianceRecord.ComplianceStatus.COMPLETED)).thenReturn(4L);
        when(repository.countByStatusAndIsDeletedFalse(
                ComplianceRecord.ComplianceStatus.OVERDUE)).thenReturn(1L);
        Map<String, Long> stats = service.getStats();
        assertNotNull(stats);
        assertEquals(10L, stats.get("total"));
        assertEquals(3L, stats.get("pending"));
        assertEquals(4L, stats.get("completed"));
        assertEquals(1L, stats.get("overdue"));
    }

    @Test
    void testCreateRecord_DefaultStatus_IsPending() {
        ComplianceRecord recordWithoutStatus = ComplianceRecord.builder()
                .title("Test Filing")
                .companyName("Test Company Ltd")
                .complianceType("Annual Filing")
                .isDeleted(false)
                .build();
        when(repository.save(any(ComplianceRecord.class)))
                .thenReturn(recordWithoutStatus);
        ComplianceRecord result = service.createRecord(recordWithoutStatus);
        assertEquals(ComplianceRecord.ComplianceStatus.PENDING,
                result.getStatus());
    }
}