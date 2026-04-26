package com.internship.tool.service;

import com.internship.tool.config.RedisConfig;
import com.internship.tool.entity.ComplianceRecord;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.exception.ValidationException;
import com.internship.tool.repository.ComplianceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ComplianceRecordServiceImpl implements ComplianceRecordService {

    private final ComplianceRecordRepository repository;

    @Override
    @Caching(evict = {
        @CacheEvict(value = RedisConfig.COMPLIANCE_RECORDS_CACHE, allEntries = true),
        @CacheEvict(value = RedisConfig.COMPLIANCE_STATS_CACHE, allEntries = true)
    })
    public ComplianceRecord createRecord(ComplianceRecord record) {
        log.info("Creating compliance record: {}", record.getTitle());
        validateRecord(record);
        record.setIsDeleted(false);
        if (record.getStatus() == null) {
            record.setStatus(ComplianceRecord.ComplianceStatus.PENDING);
        }
        if (record.getPriority() == null) {
            record.setPriority(ComplianceRecord.Priority.MEDIUM);
        }
        return repository.save(record);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.COMPLIANCE_RECORD_CACHE, key = "#id")
    public ComplianceRecord getRecordById(Long id) {
        log.info("Fetching compliance record with id: {}", id);
        return repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ComplianceRecord", "id", id));
    }

   @Override
@Transactional(readOnly = true)
public Page<ComplianceRecord> getAllRecords(Pageable pageable) {
        log.info("Fetching all compliance records");
        return repository.findByIsDeletedFalse(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplianceRecord> searchRecords(String query, Pageable pageable) {
        log.info("Searching compliance records with query: {}", query);
        if (query == null || query.trim().isEmpty()) {
            return repository.findByIsDeletedFalse(pageable);
        }
        return repository.searchByTitleOrCompany(query.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplianceRecord> getRecordsByStatus(
            ComplianceRecord.ComplianceStatus status, Pageable pageable) {
        log.info("Fetching records by status: {}", status);
        return repository.findByStatusAndIsDeletedFalse(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplianceRecord> getRecordsByPriority(
            ComplianceRecord.Priority priority, Pageable pageable) {
        log.info("Fetching records by priority: {}", priority);
        return repository.findByPriorityAndIsDeletedFalse(priority, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplianceRecord> getOverdueRecords() {
        log.info("Fetching overdue records");
        return repository.findOverdueRecords(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplianceRecord> getRecordsDueSoon(int days) {
        log.info("Fetching records due in {} days", days);
        return repository.findRecordsDueSoon(
                LocalDate.now(),
                LocalDate.now().plusDays(days));
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = RedisConfig.COMPLIANCE_RECORD_CACHE, key = "#id"),
        @CacheEvict(value = RedisConfig.COMPLIANCE_RECORDS_CACHE, allEntries = true),
        @CacheEvict(value = RedisConfig.COMPLIANCE_STATS_CACHE, allEntries = true)
    })
    public ComplianceRecord updateRecord(Long id, ComplianceRecord updatedRecord) {
        log.info("Updating compliance record with id: {}", id);
        ComplianceRecord existing = getRecordById(id);
        validateRecord(updatedRecord);
        existing.setTitle(updatedRecord.getTitle());
        existing.setDescription(updatedRecord.getDescription());
        existing.setSectionNumber(updatedRecord.getSectionNumber());
        existing.setComplianceType(updatedRecord.getComplianceType());
        existing.setStatus(updatedRecord.getStatus());
        existing.setPriority(updatedRecord.getPriority());
        existing.setDueDate(updatedRecord.getDueDate());
        existing.setAssignedTo(updatedRecord.getAssignedTo());
        existing.setCompanyName(updatedRecord.getCompanyName());
        existing.setFilingFrequency(updatedRecord.getFilingFrequency());
        existing.setPenaltyAmount(updatedRecord.getPenaltyAmount());
        existing.setRemarks(updatedRecord.getRemarks());
        return repository.save(existing);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = RedisConfig.COMPLIANCE_RECORD_CACHE, key = "#id"),
        @CacheEvict(value = RedisConfig.COMPLIANCE_RECORDS_CACHE, allEntries = true),
        @CacheEvict(value = RedisConfig.COMPLIANCE_STATS_CACHE, allEntries = true)
    })
    public ComplianceRecord updateStatus(Long id,
            ComplianceRecord.ComplianceStatus status) {
        log.info("Updating status of record {} to {}", id, status);
        ComplianceRecord existing = getRecordById(id);
        existing.setStatus(status);
        return repository.save(existing);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = RedisConfig.COMPLIANCE_RECORD_CACHE, key = "#id"),
        @CacheEvict(value = RedisConfig.COMPLIANCE_RECORDS_CACHE, allEntries = true),
        @CacheEvict(value = RedisConfig.COMPLIANCE_STATS_CACHE, allEntries = true)
    })
    public void deleteRecord(Long id) {
        log.info("Soft deleting compliance record with id: {}", id);
        ComplianceRecord existing = getRecordById(id);
        existing.setIsDeleted(true);
        repository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.COMPLIANCE_STATS_CACHE, key = "'stats'")
    public Map<String, Long> getStats() {
        log.info("Fetching compliance stats");
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
        return stats;
    }

    @Override
    @CacheEvict(value = RedisConfig.COMPLIANCE_RECORD_CACHE, key = "#id")
    public ComplianceRecord attachAiDescription(Long id, String aiDescription) {
        log.info("Attaching AI description to record {}", id);
        ComplianceRecord existing = getRecordById(id);
        existing.setAiDescription(aiDescription);
        return repository.save(existing);
    }

    @Override
    @CacheEvict(value = RedisConfig.COMPLIANCE_RECORD_CACHE, key = "#id")
    public ComplianceRecord attachAiRecommendations(Long id,
            String aiRecommendations) {
        log.info("Attaching AI recommendations to record {}", id);
        ComplianceRecord existing = getRecordById(id);
        existing.setAiRecommendations(aiRecommendations);
        return repository.save(existing);
    }

    // --- Private validation ---
    private void validateRecord(ComplianceRecord record) {
        Map<String, String> errors = new HashMap<>();
        if (record.getTitle() == null || record.getTitle().trim().isEmpty()) {
            errors.put("title", "Title is required");
        }
        if (record.getCompanyName() == null ||
                record.getCompanyName().trim().isEmpty()) {
            errors.put("companyName", "Company name is required");
        }
        if (record.getComplianceType() == null ||
                record.getComplianceType().trim().isEmpty()) {
            errors.put("complianceType", "Compliance type is required");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }
}