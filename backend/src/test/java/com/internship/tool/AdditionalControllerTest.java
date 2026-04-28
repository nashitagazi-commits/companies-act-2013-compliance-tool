package com.internship.tool;

import com.internship.tool.entity.ComplianceRecord;
import com.internship.tool.entity.ComplianceRecordDTO;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.service.ComplianceRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.internship.tool.controller.ComplianceRecordController;

@ExtendWith(MockitoExtension.class)
class AdditionalControllerTest {

    @Mock
    private ComplianceRecordService service;

    @InjectMocks
    private ComplianceRecordController controller;

    private ComplianceRecord sampleRecord;

    @BeforeEach
    void setUp() {
        sampleRecord = ComplianceRecord.builder()
                .id(2L)
                .title("Board Meeting Q2")
                .companyName("Infosys Ltd")
                .complianceType("Board Meeting")
                .status(ComplianceRecord.ComplianceStatus.IN_PROGRESS)
                .priority(ComplianceRecord.Priority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(20))
                .isDeleted(false)
                .build();
    }

    // Test 1 — getAllRecords with custom page size
    @Test
    void testGetAllRecords_CustomPageSize() {
        Page<ComplianceRecord> page = new PageImpl<>(List.of(sampleRecord));
        when(service.getAllRecords(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<ComplianceRecordDTO>> response =
                controller.getAllRecords(0, 5, new String[]{"createdAt,desc"});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    // Test 2 — getAllRecords empty page
    @Test
    void testGetAllRecords_EmptyPage() {
        Page<ComplianceRecord> emptyPage = new PageImpl<>(List.of());
        when(service.getAllRecords(any(Pageable.class))).thenReturn(emptyPage);

        ResponseEntity<Page<ComplianceRecordDTO>> response =
                controller.getAllRecords(0, 10, new String[]{"createdAt,desc"});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getTotalElements());
    }

    // Test 3 — searchRecords empty results
    @Test
    void testSearchRecords_EmptyResults() {
        Page<ComplianceRecord> emptyPage = new PageImpl<>(List.of());
        when(service.searchRecords(eq("unknown"), any(Pageable.class)))
                .thenReturn(emptyPage);

        ResponseEntity<Page<ComplianceRecordDTO>> response =
                controller.searchRecords("unknown", 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getTotalElements());
    }

    // Test 4 — getOverdueRecords empty list
    @Test
    void testGetOverdueRecords_EmptyList() {
        when(service.getOverdueRecords()).thenReturn(List.of());

        ResponseEntity<List<ComplianceRecordDTO>> response =
                controller.getOverdueRecords();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // Test 5 — getRecordsDueSoon with 30 days
    @Test
    void testGetRecordsDueSoon_30Days() {
        when(service.getRecordsDueSoon(30))
                .thenReturn(List.of(sampleRecord));

        ResponseEntity<List<ComplianceRecordDTO>> response =
                controller.getRecordsDueSoon(30);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // Test 6 — deleteRecord returns correct message
    @Test
    void testDeleteRecord_ReturnsCorrectMessage() {
        doNothing().when(service).deleteRecord(2L);

        ResponseEntity<Map<String, String>> response =
                controller.deleteRecord(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Record deleted successfully",
                response.getBody().get("message"));
        assertEquals("2", response.getBody().get("id"));
    }

    // Test 7 — updateStatus to OVERDUE
    @Test
    void testUpdateStatus_ToOverdue() {
        when(service.updateStatus(2L,
                ComplianceRecord.ComplianceStatus.OVERDUE))
                .thenReturn(sampleRecord);

        ResponseEntity<ComplianceRecordDTO> response =
                controller.updateStatus(2L,
                        ComplianceRecord.ComplianceStatus.OVERDUE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // Test 8 — getByStatus COMPLETED
    @Test
    void testGetByStatus_Completed() {
        Page<ComplianceRecord> page = new PageImpl<>(List.of(sampleRecord));
        when(service.getRecordsByStatus(
                eq(ComplianceRecord.ComplianceStatus.COMPLETED),
                any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<ComplianceRecordDTO>> response =
                controller.getByStatus(
                        ComplianceRecord.ComplianceStatus.COMPLETED, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    // Test 9 — getByPriority LOW
    @Test
    void testGetByPriority_Low() {
        Page<ComplianceRecord> page = new PageImpl<>(List.of(sampleRecord));
        when(service.getRecordsByPriority(
                eq(ComplianceRecord.Priority.LOW),
                any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<ComplianceRecordDTO>> response =
                controller.getByPriority(
                        ComplianceRecord.Priority.LOW, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // Test 10 — getStats empty map
    @Test
    void testGetStats_EmptyStats() {
        when(service.getStats()).thenReturn(Map.of(
                "total", 0L, "pending", 0L,
                "completed", 0L, "overdue", 0L));

        ResponseEntity<Map<String, Long>> response = controller.getStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0L, response.getBody().get("total"));
    }

    // Test 11 — createRecord with CRITICAL priority
    @Test
    void testCreateRecord_CriticalPriority() {
        ComplianceRecordDTO dto = ComplianceRecordDTO.builder()
                .title("Critical Filing")
                .companyName("Test Co Ltd")
                .complianceType("Annual Filing")
                .status(ComplianceRecord.ComplianceStatus.PENDING)
                .priority(ComplianceRecord.Priority.CRITICAL)
                .build();

        when(service.createRecord(any(ComplianceRecord.class)))
                .thenReturn(sampleRecord);

        ResponseEntity<ComplianceRecordDTO> response =
                controller.createRecord(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    // Test 12 — updateRecord not found throws exception
    @Test
    void testUpdateRecord_NotFound_ThrowsException() {
        ComplianceRecordDTO dto = ComplianceRecordDTO.builder()
                .title("Updated")
                .companyName("Test Co")
                .complianceType("Filing")
                .status(ComplianceRecord.ComplianceStatus.PENDING)
                .priority(ComplianceRecord.Priority.LOW)
                .build();

        when(service.updateRecord(eq(99L), any(ComplianceRecord.class)))
                .thenThrow(new ResourceNotFoundException(
                        "ComplianceRecord", "id", 99L));

        assertThrows(ResourceNotFoundException.class,
                () -> controller.updateRecord(99L, dto));
    }
}