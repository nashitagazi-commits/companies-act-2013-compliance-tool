package com.internship.tool.repository;

import com.internship.tool.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find logs by entity name and id
    List<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId);

    // Find logs by who performed the action
    Page<AuditLog> findByPerformedBy(String performedBy, Pageable pageable);

    // Find logs by action type
    List<AuditLog> findByAction(String action);

    // Find logs between two dates
    List<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Find recent logs
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}