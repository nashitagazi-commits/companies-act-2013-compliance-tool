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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.internship.tool.controller.ComplianceRecordController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ComplianceRecordControllerTest {

    @Mock
    private ComplianceRecordService service;

    @InjectMocks
    private ComplianceRecordController controller;

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

    // Test 1 — getAllRecords returns 200
    @Test
    void testGetAllRecords_Returns200() {
        Page<ComplianceRecord> page = new PageImpl<>(List.of(sampleRecord));
        when(service.getAllRecords(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<ComplianceRecordDTO>> response =
                controller.getAllRecords(0, 10, new String[]{"createdAt,desc"});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    // Test 2 — getRecordById returns 200
    @Test
    void testGetRecordById_Returns200() {
        when(service.getRecordById(1L)).thenReturn(sampleRecord);

        ResponseEntity<ComplianceRecordDTO> response =
                controller.getRecordById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Annual Return Filing", response.getBody().getTitle());
    }

    // Test 3 — getRecordById throws 404
    @Test
    void testGetRecordById_NotFound_Throws404() {
        when(service.getRecordById(99L))
                .thenThrow(new ResourceNotFoundException(
                        "ComplianceRecord", "id", 99L));

        assertThrows(ResourceNotFoundException.class,
                () -> controller.getRecordById(99L));
    }

    // Test 4 — createRecord returns 201
    @Test
    void testCreateRecord_Returns201() {
        ComplianceRecordDTO dto = ComplianceRecordDTO.builder()
                .title("Annual Return Filing")
                .companyName("Test Company Ltd")
                .complianceType("Annual Filing")
                .status(ComplianceRecord.ComplianceStatus.PENDING)
                .priority(ComplianceRecord.Priority.HIGH)
                .build();

        when(service.createRecord(any(ComplianceRecord.class)))
                .thenReturn(sampleRecord);

        ResponseEntity<ComplianceRecordDTO> response =
                controller.createRecord(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // Test 5 — updateRecord returns 200
    @Test
    void testUpdateRecord_Returns200() {
        ComplianceRecordDTO dto = ComplianceRecordDTO.builder()
                .title("Updated Title")
                .companyName("Test Company Ltd")
                .complianceType("Annual Filing")
                .status(ComplianceRecord.ComplianceStatus.IN_PROGRESS)
                .priority(ComplianceRecord.Priority.MEDIUM)
                .build();

        when(service.updateRecord(eq(1L), any(ComplianceRecord.class)))
                .thenReturn(sampleRecord);

        ResponseEntity<ComplianceRecordDTO> response =
                controller.updateRecord(1L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // Test 6 — updateStatus returns 200
    @Test
    void testUpdateStatus_Returns200() {
        when(service.updateStatus(1L,
                ComplianceRecord.ComplianceStatus.COMPLETED))
                .thenReturn(sampleRecord);

        ResponseEntity<ComplianceRecordDTO> response =
                controller.updateStatus(1L,
                        ComplianceRecord.ComplianceStatus.COMPLETED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // Test 7 — deleteRecord returns 200
    @Test
    void testDeleteRecord_Returns200() {
        doNothing().when(service).deleteRecord(1L);

        ResponseEntity<Map<String, String>> response =
                controller.deleteRecord(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().containsKey("message"));
    }

    // Test 8 — searchRecords returns 200
    @Test
    void testSearchRecords_Returns200() {
        Page<ComplianceRecord> page = new PageImpl<>(List.of(sampleRecord));
        when(service.searchRecords(eq("Annual"), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<Page<ComplianceRecordDTO>> response =
                controller.searchRecords("Annual", 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    // Test 9 — getOverdueRecords returns 200
    @Test
    void testGetOverdueRecords_Returns200() {
        when(service.getOverdueRecords()).thenReturn(List.of(sampleRecord));

        ResponseEntity<List<ComplianceRecordDTO>> response =
                controller.getOverdueRecords();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // Test 10 — getRecordsDueSoon returns 200
    @Test
    void testGetRecordsDueSoon_Returns200() {
        when(service.getRecordsDueSoon(7))
                .thenReturn(List.of(sampleRecord));

        ResponseEntity<List<ComplianceRecordDTO>> response =
                controller.getRecordsDueSoon(7);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // Test 11 — getStats returns 200
    @Test
    void testGetStats_Returns200() {
        Map<String, Long> stats = Map.of(
                "total", 10L, "pending", 3L,
                "completed", 4L, "overdue", 1L);
        when(service.getStats()).thenReturn(stats);

        ResponseEntity<Map<String, Long>> response = controller.getStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().get("total"));
    }

    // Test 12 — getByStatus returns 200
    @Test
    void testGetByStatus_Returns200() {
        Page<ComplianceRecord> page = new PageImpl<>(List.of(sampleRecord));
        when(service.getRecordsByStatus(
                eq(ComplianceRecord.ComplianceStatus.PENDING),
                any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<ComplianceRecordDTO>> response =
                controller.getByStatus(
                        ComplianceRecord.ComplianceStatus.PENDING, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // Test 13 — getByPriority returns 200
    @Test
    void testGetByPriority_Returns200() {
        Page<ComplianceRecord> page = new PageImpl<>(List.of(sampleRecord));
        when(service.getRecordsByPriority(
                eq(ComplianceRecord.Priority.HIGH),
                any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<ComplianceRecordDTO>> response =
                controller.getByPriority(
                        ComplianceRecord.Priority.HIGH, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}