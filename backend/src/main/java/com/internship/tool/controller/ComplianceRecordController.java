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

    // GET /api/compliance/all?page=0&size=10&sort=createdAt,desc
    @GetMapping("/all")
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

    // GET /api/compliance/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Get compliance record by ID")
    public ResponseEntity<ComplianceRecordDTO> getRecordById(@PathVariable Long id) {
        ComplianceRecord record = service.getRecordById(id);
        return ResponseEntity.ok(ComplianceRecordDTO.fromEntity(record));
    }

    // POST /api/compliance/create
    @PostMapping("/create")
    @Operation(summary = "Create a new compliance record")
    public ResponseEntity<ComplianceRecordDTO> createRecord(
            @Valid @RequestBody ComplianceRecordDTO dto) {
        ComplianceRecord created = service.createRecord(dto.toEntity());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ComplianceRecordDTO.fromEntity(created));
    }

    // PUT /api/compliance/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Update compliance record")
    public ResponseEntity<ComplianceRecordDTO> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody ComplianceRecordDTO dto) {
        ComplianceRecord updated = service.updateRecord(id, dto.toEntity());
        return ResponseEntity.ok(ComplianceRecordDTO.fromEntity(updated));
    }

    // PATCH /api/compliance/{id}/status
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update compliance record status")
    public ResponseEntity<ComplianceRecordDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam ComplianceRecord.ComplianceStatus status) {
        ComplianceRecord updated = service.updateStatus(id, status);
        return ResponseEntity.ok(ComplianceRecordDTO.fromEntity(updated));
    }

    // DELETE /api/compliance/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete compliance record")
    public ResponseEntity<Map<String, String>> deleteRecord(@PathVariable Long id) {
        service.deleteRecord(id);
        return ResponseEntity.ok(Map.of(
                "message", "Record deleted successfully",
                "id", id.toString()));
    }

    // GET /api/compliance/search?q=keyword
    @GetMapping("/search")
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

    // GET /api/compliance/filter/status?status=PENDING
    @GetMapping("/filter/status")
    @Operation(summary = "Filter records by status")
    public ResponseEntity<Page<ComplianceRecordDTO>> getByStatus(
            @RequestParam ComplianceRecord.ComplianceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ComplianceRecordDTO> records = service.getRecordsByStatus(status, pageable)
                .map(ComplianceRecordDTO::fromEntity);
        return ResponseEntity.ok(records);
    }

    // GET /api/compliance/filter/priority?priority=HIGH
    @GetMapping("/filter/priority")
    @Operation(summary = "Filter records by priority")
    public ResponseEntity<Page<ComplianceRecordDTO>> getByPriority(
            @RequestParam ComplianceRecord.Priority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ComplianceRecordDTO> records = service.getRecordsByPriority(priority, pageable)
                .map(ComplianceRecordDTO::fromEntity);
        return ResponseEntity.ok(records);
    }

    // GET /api/compliance/overdue
    @GetMapping("/overdue")
    @Operation(summary = "Get all overdue compliance records")
    public ResponseEntity<List<ComplianceRecordDTO>> getOverdueRecords() {
        List<ComplianceRecordDTO> records = service.getOverdueRecords()
                .stream()
                .map(ComplianceRecordDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(records);
    }

    // GET /api/compliance/due-soon?days=7
    @GetMapping("/due-soon")
    @Operation(summary = "Get records due soon")
    public ResponseEntity<List<ComplianceRecordDTO>> getRecordsDueSoon(
            @RequestParam(defaultValue = "7") int days) {
        List<ComplianceRecordDTO> records = service.getRecordsDueSoon(days)
                .stream()
                .map(ComplianceRecordDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(records);
    }

    // GET /api/compliance/stats
    @GetMapping("/stats")
    @Operation(summary = "Get compliance statistics")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(service.getStats());
    }
}