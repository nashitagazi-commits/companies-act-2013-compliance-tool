package com.internship.tool.controller;

import com.internship.tool.entity.ComplianceRecord;
import com.internship.tool.entity.ComplianceRecordDTO;
import com.internship.tool.service.ComplianceRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Compliance Records", description = "Companies Act 2013 Compliance Management APIs")
public class ComplianceRecordController {

    private final ComplianceRecordService service;

    // GET /api/compliance/all — any authenticated user
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get all compliance records with pagination")
    public ResponseEntity<Page<ComplianceRecordDTO>> getAllRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ComplianceRecordDTO> records = service.getAllRecords(pageable)
                .map(ComplianceRecordDTO::fromEntity);
        return ResponseEntity.ok(records);
    }

    // GET /api/compliance/{id} — any authenticated user
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get compliance record by ID")
    public ResponseEntity<ComplianceRecordDTO> getRecordById(
            @PathVariable Long id) {
        ComplianceRecord record = service.getRecordById(id);
        return ResponseEntity.ok(ComplianceRecordDTO.fromEntity(record));
    }

    // POST /api/compliance/create — ADMIN and MANAGER only
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new compliance record")
    public ResponseEntity<ComplianceRecordDTO> createRecord(
            @Valid @RequestBody ComplianceRecordDTO dto) {
        ComplianceRecord created = service.createRecord(dto.toEntity());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ComplianceRecordDTO.fromEntity(created));
    }

    // PUT /api/compliance/{id} — ADMIN and MANAGER only
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update compliance record")
    public ResponseEntity<ComplianceRecordDTO> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody ComplianceRecordDTO dto) {
        ComplianceRecord updated = service.updateRecord(id, dto.toEntity());
        return ResponseEntity.ok(ComplianceRecordDTO.fromEntity(updated));
    }

    // PATCH /api/compliance/{id}/status — ADMIN, MANAGER, USER
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Update compliance record status")
    public ResponseEntity<ComplianceRecordDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam ComplianceRecord.ComplianceStatus status) {
        ComplianceRecord updated = service.updateStatus(id, status);
        return ResponseEntity.ok(ComplianceRecordDTO.fromEntity(updated));
    }

    // DELETE /api/compliance/{id} — ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete compliance record")
    public ResponseEntity<Map<String, String>> deleteRecord(
            @PathVariable Long id) {
        service.deleteRecord(id);
        return ResponseEntity.ok(Map.of(
                "message", "Record deleted successfully",
                "id", id.toString()));
    }

    // GET /api/compliance/search — any authenticated user
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Search compliance records")
    public ResponseEntity<Page<ComplianceRecordDTO>> searchRecords(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ComplianceRecordDTO> records = service.searchRecords(q, pageable)
                .map(ComplianceRecordDTO::fromEntity);
        return ResponseEntity.ok(records);
    }

    // GET /api/compliance/filter/status — any authenticated user
    @GetMapping("/filter/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Filter records by status")
    public ResponseEntity<Page<ComplianceRecordDTO>> getByStatus(
            @RequestParam ComplianceRecord.ComplianceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ComplianceRecordDTO> records = service
                .getRecordsByStatus(status, pageable)
                .map(ComplianceRecordDTO::fromEntity);
        return ResponseEntity.ok(records);
    }

    // GET /api/compliance/filter/priority — any authenticated user
    @GetMapping("/filter/priority")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Filter records by priority")
    public ResponseEntity<Page<ComplianceRecordDTO>> getByPriority(
            @RequestParam ComplianceRecord.Priority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ComplianceRecordDTO> records = service
                .getRecordsByPriority(priority, pageable)
                .map(ComplianceRecordDTO::fromEntity);
        return ResponseEntity.ok(records);
    }

    // GET /api/compliance/overdue — any authenticated user
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get all overdue compliance records")
    public ResponseEntity<List<ComplianceRecordDTO>> getOverdueRecords() {
        List<ComplianceRecordDTO> records = service.getOverdueRecords()
                .stream()
                .map(ComplianceRecordDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(records);
    }

    // GET /api/compliance/due-soon — any authenticated user
    @GetMapping("/due-soon")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get records due soon")
    public ResponseEntity<List<ComplianceRecordDTO>> getRecordsDueSoon(
            @RequestParam(defaultValue = "7") int days) {
        List<ComplianceRecordDTO> records = service.getRecordsDueSoon(days)
                .stream()
                .map(ComplianceRecordDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(records);
    }

    // GET /api/compliance/stats — ADMIN and MANAGER only
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get compliance statistics")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(service.getStats());
    }
}