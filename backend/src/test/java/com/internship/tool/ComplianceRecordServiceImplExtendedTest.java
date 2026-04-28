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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplianceRecordServiceImplExtendedTest {

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

    // Test 1 — updateRecord updates all fields
    @Test
    void testUpdateRecord_UpdatesAllFields() {
        ComplianceRecord updated = ComplianceRecord.builder()
                .title("Updated Title")
                .companyName("Updated Company")
                .complianceType("Updated Type")
                .status(ComplianceRecord.ComplianceStatus.IN_PROGRESS)
                .priority(ComplianceRecord.Priority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(20))
                .assignedTo("user@test.com")
                .filingFrequency("Quarterly")
                .penaltyAmount(10000.0)
                .remarks("Updated remark")
                .isDeleted(false)
                .build();

        when(repository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(sampleRecord));
        when(repository.save(any(ComplianceRecord.class)))
                .thenReturn(sampleRecord);

        ComplianceRecord result = service.updateRecord(1L, updated);

        assertNotNull(result);
        verify(repository, times(1)).save(any());
    }

    // Test 2 — updateRecord throws when not found
    @Test
    void testUpdateRecord_NotFound_ThrowsException() {
        ComplianceRecord updated = ComplianceRecord.builder()
                .title("Title")
                .companyName("Company")
                .complianceType("Type")
                .build();

        when(repository.findByIdAndIsDeletedFalse(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateRecord(99L, updated));
    }

    // Test 3 — updateRecord throws when validation fails
    @Test
    void testUpdateRecord_ValidationFails_ThrowsException() {
        ComplianceRecord invalid = ComplianceRecord.builder()
                .title("")
                .companyName("Company")
                .complianceType("Type")
                .build();

        when(repository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(sampleRecord));

        assertThrows(ValidationException.class,
                () -> service.updateRecord(1L, invalid));
    }

    // Test 4 — getRecordsByStatus returns page
    @Test
    void testGetRecordsByStatus_ReturnsPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ComplianceRecord> page = new PageImpl<>(List.of(sampleRecord));
        when(repository.findByStatusAndIsDeletedFalse(
                ComplianceRecord.ComplianceStatus.PENDING, pageable))
                .thenReturn(page);

        Page<ComplianceRecord> result = service.getRecordsByStatus(
                ComplianceRecord.ComplianceStatus.PENDING, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // Test 5 — getRecordsByPriority returns page
    @Test
    void testGetRecordsByPriority_ReturnsPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ComplianceRecord> page = new PageImpl<>(List.of(sampleRecord));
        when(repository.findByPriorityAndIsDeletedFalse(
                ComplianceRecord.Priority.HIGH, pageable))
                .thenReturn(page);

        Page<ComplianceRecord> result = service.getRecordsByPriority(
                ComplianceRecord.Priority.HIGH, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // Test 6 — searchRecords with keyword returns results
    @Test
    void testSearchRecords_WithKeyword_ReturnsResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ComplianceRecord> page = new PageImpl<>(List.of(sampleRecord));
        when(repository.searchByTitleOrCompany(eq("Annual"), eq(pageable)))
                .thenReturn(page);

        Page<ComplianceRecord> result = service.searchRecords("Annual", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // Test 7 — searchRecords with whitespace-only returns all
    @Test
    void testSearchRecords_WhitespaceQuery_ReturnsAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ComplianceRecord> page = new PageImpl<>(List.of(sampleRecord));
        when(repository.findByIsDeletedFalse(pageable)).thenReturn(page);

        Page<ComplianceRecord> result = service.searchRecords("   ", pageable);

        assertNotNull(result);
        verify(repository, never()).searchByTitleOrCompany(any(), any());
    }

    // Test 8 — getRecordsDueSoon with different days
    @Test
    void testGetRecordsDueSoon_30Days_ReturnsResults() {
        when(repository.findRecordsDueSoon(
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(sampleRecord));

        List<ComplianceRecord> result = service.getRecordsDueSoon(30);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // Test 9 — attachAiDescription throws when not found
    @Test
    void testAttachAiDescription_NotFound_ThrowsException() {
        when(repository.findByIdAndIsDeletedFalse(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.attachAiDescription(99L, "desc"));
    }

    // Test 10 — attachAiRecommendations throws when not found
    @Test
    void testAttachAiRecommendations_NotFound_ThrowsException() {
        when(repository.findByIdAndIsDeletedFalse(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.attachAiRecommendations(99L, "recs"));
    }

    // Test 11 — createRecord sets isDeleted false
    @Test
    void testCreateRecord_SetsIsDeletedFalse() {
        when(repository.save(any(ComplianceRecord.class)))
                .thenReturn(sampleRecord);

        ComplianceRecord result = service.createRecord(sampleRecord);

        assertFalse(result.getIsDeleted());
    }

    // Test 12 — getOverdueRecords returns empty list when none
    @Test
    void testGetOverdueRecords_ReturnsEmptyList_WhenNone() {
        when(repository.findOverdueRecords(any(LocalDate.class)))
                .thenReturn(List.of());

        List<ComplianceRecord> result = service.getOverdueRecords();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}