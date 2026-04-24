package com.internship.tool.service;

import com.internship.tool.entity.ComplianceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ComplianceRecordService {

    // Create
    ComplianceRecord createRecord(ComplianceRecord record);

    // Read
    ComplianceRecord getRecordById(Long id);
    Page<ComplianceRecord> getAllRecords(Pageable pageable);
    Page<ComplianceRecord> searchRecords(String query, Pageable pageable);
    Page<ComplianceRecord> getRecordsByStatus(ComplianceRecord.ComplianceStatus status, Pageable pageable);
    Page<ComplianceRecord> getRecordsByPriority(ComplianceRecord.Priority priority, Pageable pageable);
    List<ComplianceRecord> getOverdueRecords();
    List<ComplianceRecord> getRecordsDueSoon(int days);

    // Update
    ComplianceRecord updateRecord(Long id, ComplianceRecord record);
    ComplianceRecord updateStatus(Long id, ComplianceRecord.ComplianceStatus status);

    // Delete (soft)
    void deleteRecord(Long id);

    // Stats
    Map<String, Long> getStats();

    // AI
    ComplianceRecord attachAiDescription(Long id, String aiDescription);
    ComplianceRecord attachAiRecommendations(Long id, String aiRecommendations);
}