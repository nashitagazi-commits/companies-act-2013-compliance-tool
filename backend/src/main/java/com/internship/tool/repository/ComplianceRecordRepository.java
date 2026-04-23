package com.internship.tool.repository;

import com.internship.tool.entity.ComplianceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, Long> {

    // Find all non-deleted records with pagination
    Page<ComplianceRecord> findByIsDeletedFalse(Pageable pageable);

    // Find by ID only if not deleted
    Optional<ComplianceRecord> findByIdAndIsDeletedFalse(Long id);

    // Search by title or company name
    @Query("SELECT c FROM ComplianceRecord c WHERE c.isDeleted = false AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<ComplianceRecord> searchByTitleOrCompany(@Param("query") String query, Pageable pageable);

    // Find by status
    Page<ComplianceRecord> findByStatusAndIsDeletedFalse(
        ComplianceRecord.ComplianceStatus status, Pageable pageable);

    // Find by priority
    Page<ComplianceRecord> findByPriorityAndIsDeletedFalse(
        ComplianceRecord.Priority priority, Pageable pageable);

    // Find overdue records
    @Query("SELECT c FROM ComplianceRecord c WHERE c.isDeleted = false AND " +
           "c.dueDate < :today AND c.status != 'COMPLETED'")
    List<ComplianceRecord> findOverdueRecords(@Param("today") LocalDate today);

    // Find records due soon (within X days)
    @Query("SELECT c FROM ComplianceRecord c WHERE c.isDeleted = false AND " +
           "c.dueDate BETWEEN :today AND :futureDate AND c.status != 'COMPLETED'")
    List<ComplianceRecord> findRecordsDueSoon(
        @Param("today") LocalDate today,
        @Param("futureDate") LocalDate futureDate);

    // Count by status
    Long countByStatusAndIsDeletedFalse(ComplianceRecord.ComplianceStatus status);

    // Count all non-deleted
    Long countByIsDeletedFalse();

    // Find by company name
    Page<ComplianceRecord> findByCompanyNameContainingIgnoreCaseAndIsDeletedFalse(
        String companyName, Pageable pageable);

    // Find by assigned to
    List<ComplianceRecord> findByAssignedToAndIsDeletedFalse(String assignedTo);
}