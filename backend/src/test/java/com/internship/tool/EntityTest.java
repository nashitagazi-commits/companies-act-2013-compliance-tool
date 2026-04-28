package com.internship.tool;

import com.internship.tool.entity.AuditLog;
import com.internship.tool.entity.ComplianceRecord;
import com.internship.tool.entity.ComplianceRecordDTO;
import com.internship.tool.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    // --- ComplianceRecord tests ---

    @Test
    void testComplianceRecord_Builder() {
        ComplianceRecord record = ComplianceRecord.builder()
                .title("Annual Return")
                .companyName("Test Co Ltd")
                .complianceType("Annual Filing")
                .status(ComplianceRecord.ComplianceStatus.PENDING)
                .priority(ComplianceRecord.Priority.HIGH)
                .isDeleted(false)
                .build();

        assertEquals("Annual Return", record.getTitle());
        assertEquals("Test Co Ltd", record.getCompanyName());
        assertEquals(ComplianceRecord.ComplianceStatus.PENDING, record.getStatus());
        assertEquals(ComplianceRecord.Priority.HIGH, record.getPriority());
        assertFalse(record.getIsDeleted());
    }

    @Test
    void testComplianceRecord_SettersAndGetters() {
        ComplianceRecord record = new ComplianceRecord();
        record.setId(1L);
        record.setTitle("Board Meeting");
        record.setDescription("Q1 Board Meeting");
        record.setSectionNumber("Section 173");
        record.setComplianceType("Board Meeting");
        record.setStatus(ComplianceRecord.ComplianceStatus.IN_PROGRESS);
        record.setPriority(ComplianceRecord.Priority.MEDIUM);
        record.setDueDate(LocalDate.now().plusDays(10));
        record.setAssignedTo("manager@company.com");
        record.setCompanyName("Infosys Ltd");
        record.setFilingFrequency("Quarterly");
        record.setPenaltyAmount(25000.0);
        record.setRemarks("Test remark");
        record.setAiDescription("AI desc");
        record.setAiRecommendations("AI recs");
        record.setIsDeleted(false);

        assertEquals(1L, record.getId());
        assertEquals("Board Meeting", record.getTitle());
        assertEquals("Section 173", record.getSectionNumber());
        assertEquals(ComplianceRecord.ComplianceStatus.IN_PROGRESS, record.getStatus());
        assertEquals(25000.0, record.getPenaltyAmount());
        assertFalse(record.getIsDeleted());
    }

    @Test
    void testComplianceRecord_DefaultIsDeletedFalse() {
        ComplianceRecord record = ComplianceRecord.builder()
                .title("Test")
                .companyName("Test Co")
                .complianceType("Filing")
                .isDeleted(false)
                .build();
        assertFalse(record.getIsDeleted());
    }

    @Test
    void testComplianceStatus_AllValues() {
        assertEquals(5, ComplianceRecord.ComplianceStatus.values().length);
        assertNotNull(ComplianceRecord.ComplianceStatus.valueOf("PENDING"));
        assertNotNull(ComplianceRecord.ComplianceStatus.valueOf("IN_PROGRESS"));
        assertNotNull(ComplianceRecord.ComplianceStatus.valueOf("COMPLETED"));
        assertNotNull(ComplianceRecord.ComplianceStatus.valueOf("OVERDUE"));
        assertNotNull(ComplianceRecord.ComplianceStatus.valueOf("NOT_APPLICABLE"));
    }

    @Test
    void testPriority_AllValues() {
        assertEquals(4, ComplianceRecord.Priority.values().length);
        assertNotNull(ComplianceRecord.Priority.valueOf("LOW"));
        assertNotNull(ComplianceRecord.Priority.valueOf("MEDIUM"));
        assertNotNull(ComplianceRecord.Priority.valueOf("HIGH"));
        assertNotNull(ComplianceRecord.Priority.valueOf("CRITICAL"));
    }

    // --- ComplianceRecordDTO tests ---

    @Test
    void testDTO_ToEntity() {
        ComplianceRecordDTO dto = ComplianceRecordDTO.builder()
                .title("Annual Return")
                .companyName("Test Co Ltd")
                .complianceType("Annual Filing")
                .status(ComplianceRecord.ComplianceStatus.PENDING)
                .priority(ComplianceRecord.Priority.HIGH)
                .dueDate(LocalDate.now().plusDays(30))
                .assignedTo("user@test.com")
                .filingFrequency("Annual")
                .penaltyAmount(50000.0)
                .remarks("Test")
                .build();

        ComplianceRecord entity = dto.toEntity();

        assertEquals("Annual Return", entity.getTitle());
        assertEquals("Test Co Ltd", entity.getCompanyName());
        assertEquals("Annual Filing", entity.getComplianceType());
        assertEquals(ComplianceRecord.ComplianceStatus.PENDING, entity.getStatus());
        assertEquals(50000.0, entity.getPenaltyAmount());
    }

    @Test
    void testDTO_FromEntity() {
        ComplianceRecord record = ComplianceRecord.builder()
                .id(1L)
                .title("Annual Return")
                .companyName("Test Co Ltd")
                .complianceType("Annual Filing")
                .status(ComplianceRecord.ComplianceStatus.PENDING)
                .priority(ComplianceRecord.Priority.HIGH)
                .dueDate(LocalDate.now().plusDays(30))
                .assignedTo("user@test.com")
                .filingFrequency("Annual")
                .penaltyAmount(50000.0)
                .remarks("Test")
                .aiDescription("AI desc")
                .aiRecommendations("AI recs")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        ComplianceRecordDTO dto = ComplianceRecordDTO.fromEntity(record);

        assertEquals(1L, dto.getId());
        assertEquals("Annual Return", dto.getTitle());
        assertEquals("Test Co Ltd", dto.getCompanyName());
        assertEquals(ComplianceRecord.ComplianceStatus.PENDING, dto.getStatus());
        assertEquals("AI desc", dto.getAiDescription());
    }

    // --- AuditLog tests ---

    @Test
    void testAuditLog_Builder() {
        AuditLog log = AuditLog.builder()
                .entityName("ComplianceRecord")
                .entityId(1L)
                .action("CREATE")
                .performedBy("admin@company.com")
                .oldValue(null)
                .newValue("{\"title\":\"Test\"}")
                .ipAddress("127.0.0.1")
                .build();

        assertEquals("ComplianceRecord", log.getEntityName());
        assertEquals(1L, log.getEntityId());
        assertEquals("CREATE", log.getAction());
        assertEquals("admin@company.com", log.getPerformedBy());
        assertEquals("127.0.0.1", log.getIpAddress());
    }

    @Test
    void testAuditLog_SettersAndGetters() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setEntityName("User");
        log.setEntityId(2L);
        log.setAction("UPDATE");
        log.setPerformedBy("admin@test.com");
        log.setOldValue("old");
        log.setNewValue("new");
        log.setIpAddress("192.168.1.1");

        assertEquals(1L, log.getId());
        assertEquals("User", log.getEntityName());
        assertEquals("UPDATE", log.getAction());
        assertEquals("192.168.1.1", log.getIpAddress());
    }

    // --- User tests ---

    @Test
    void testUser_Builder() {
        User user = User.builder()
                .fullName("John Doe")
                .email("john@test.com")
                .password("encoded_password")
                .role(User.Role.ADMIN)
                .isActive(true)
                .build();

        assertEquals("John Doe", user.getFullName());
        assertEquals("john@test.com", user.getEmail());
        assertEquals(User.Role.ADMIN, user.getRole());
        assertTrue(user.getIsActive());
    }

    @Test
    void testUser_DefaultRole() {
        User user = User.builder()
                .fullName("Jane Doe")
                .email("jane@test.com")
                .password("password")
                .isActive(true)
                .build();

        assertEquals(User.Role.USER, user.getRole());
    }

    @Test
    void testUser_Roles_AllValues() {
        assertEquals(3, User.Role.values().length);
        assertNotNull(User.Role.valueOf("USER"));
        assertNotNull(User.Role.valueOf("ADMIN"));
        assertNotNull(User.Role.valueOf("MANAGER"));
    }

    @Test
    void testUser_SettersAndGetters() {
        User user = new User();
        user.setId(1L);
        user.setFullName("Test User");
        user.setEmail("test@test.com");
        user.setPassword("pass");
        user.setRole(User.Role.MANAGER);
        user.setIsActive(true);

        assertEquals(1L, user.getId());
        assertEquals("Test User", user.getFullName());
        assertEquals(User.Role.MANAGER, user.getRole());
        assertTrue(user.getIsActive());
    }
}